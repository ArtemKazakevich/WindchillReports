package ext.by.iba.OneC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


import ext.by.iba.Util.ESKDHelperReport;
import wt.log4j.LogR;
import wt.session.SessionMgr;
import wt.util.WTException;
import wt.util.WTProperties;

public class OneCNomenclature {	
	
	private OneCSaxHandler saxHandler;
	private String ContainerRefPKI = null;
	private String ContainerRefMAT = null;
	private String FolderPKI = null;
	private String FolderMAT = null;
	private String NameView = null;
	private String stateDelete = null;
	private boolean VERBOSE;
	private String AdminUser = null;
	private String Passw = null;   // not used
	private WTProperties wtprops;
	private Map<String, String> mapAttrs = null;	    
	private String nameMapAttrsFile;
    private static String CLASSNAME = "ext.by.iba.OneC.OneCNomenclature";
    final static Logger logger = LogR.getLogger(CLASSNAME);	


	public OneCNomenclature() throws IOException {		
	    try {	    	
	        wtprops = WTProperties.getLocalProperties();
        	if( wtprops.getProperty("by.iba.OneC.GetNomenclature.mapAttrsFile") != null)
        		nameMapAttrsFile = wtprops.getProperty("by.iba.OneC.GetNomenclature.mapAttrsFile");
        	else
    	        nameMapAttrsFile = wtprops.getProperty("by.iba.SPS.env.STARTPOINT_HOME") + "\\Util\\Reports\\MapAttrs.txt";        		

	    } catch (IOException e) {
	        e.printStackTrace();
	    }	    
	    mapAttrs = new HashMap<String, String>();
	    FillMapAttrs( nameMapAttrsFile);
	}

	    public String parseXML(String dataOneC)   {   // пока без схемы или dtd
	        String result = "OK";
	        saxHandler = new OneCSaxHandler();
	        try{
	        XMLReader xmlreader = XMLReaderFactory.createXMLReader(); // SAXParserFactory.newInstance().newSAXParser().getXMLReader();
	        //xmlreader.setFeature("http://xml.org/sax/features/namespaces", false);  // ?
	        xmlreader.setContentHandler( saxHandler);
	        xmlreader.setErrorHandler( saxHandler);
	        xmlreader.parse(new InputSource(new StringReader( dataOneC)));
	        } catch( SAXException e) {
	            result = "XML данные должны быть правильно построенными (well-formed) !";
	            System.out.println("!!!!! SAXException from OneCNomenclature");
	            e.printStackTrace();
	        } catch( IOException e) {
	            result = "IOException ошибка при чтении xml-данных !";
	            System.out.println("!!!!! IOException from OneCNomenclature");
	            e.printStackTrace();
	        }
	        return result;
	    }

	    /**
	     * Вместо транзакции, которая подразумевает все или ничего, приеняем другую стратегию создания объектов в
	     * Windchill по "заданию" из 1С. Она заключается в следующем:<br>
	     * 1. Создаем все объекты которые можем создать;<br>
	     * 2. Возвращаем в 1С или "ОК" - если все хорошо, или сообщение состоящее из суммы ошибок.
	     * @return
	     */
	    public String processInWindchill() {   // пока без схемы или dtd - чувствительна к регистру тегов !!!
	        List<String> listResult = new ArrayList<String>(); // "Работает Web-сервис Windchill."; в тестовых целях
	        IBAJVLoader windchillLoader = new IBAJVLoader(ContainerRefPKI, ContainerRefMAT, FolderPKI, FolderMAT,
	                NameView, stateDelete, VERBOSE);
	        String nameObj, result, date, action;
	        StringBuffer sb;

	        result = changePrincipal();
	        if( !result.equals( "OK"))
	                return result;

	        for ( Iterator<Map<String, String>> it =  saxHandler.getListObjects().iterator(); it.hasNext();) {
	            Map<String, String> object = it.next(); // объект содержит всю переданную из 1С информацию по библиотечному элементу
	            if( logger.isDebugEnabled()) {
	            	ESKDHelperReport.debugMap(object);
	            }
	            nameObj = object.get("Name");
	            object = CheckMapAttrs( object);
	            if( object == null) {
	                listResult.add(" У объекта "+ nameObj + " отсутствует тэг 'Type' или его значение не из допустимого перечня !!!!");
	                continue;
	            }

	            action = ( object.get( "Action") != null ? (String)object.get( "Action") : ""); // values - создание, удаление, редактирование
	          //  Выключение записи ПКИ в винчил
	            if( action.equalsIgnoreCase("создание")){
	            	//!!!!!!!------ вот тут заменяем создание на изменение объекта
	            	// Так было до изменений передачи с кода опк на oid
	            	//result = windchillLoader.changeObjectInWindchill( object);
	            	//result = windchillLoader.addObjectInWindchill( object);
	            	result = windchillLoader.changeObjectInWindchillID( object);
	                listResult.add( result); // result = addObjectInWindchill( object);
	            }
	            else if( action.equalsIgnoreCase("удаление")) {
	            	//result = windchillLoader.deleteObjectInWindchill( object);
	            	result = windchillLoader.deleteObjectInWindchillID( object);
	                listResult.add( result);
	            }
	            else if( action.equalsIgnoreCase("редактирование")) {
	            	//result = windchillLoader.changeObjectInWindchill( object);
	            	result = windchillLoader.changeObjectInWindchillID( object);
	                listResult.add( result);
	            }
	            else
	                listResult.add( " У объекта "+ object.get("name") + " отсутствует тэг 'Action' или его значение не из допустимого перечня !!!!");
	        	date = String.format("%1$td.%1$tm.%1$ty:%1$tH:%1$tM:%1$tS", new Date());
	        	sb = new StringBuffer();
	        	sb.append(date).append(",").append("OneCNomenclature").append(",").append(action).append(",").
	        		append(nameObj).append(",").append(result);
	        	ESKDHelperReport.writeToFile( wtprops.getProperty("wt.codebase.location") + "\\OneCActivity.txt", sb.toString());
	        }

	        for( ListIterator<String> li = listResult.listIterator(); li.hasNext();) {
	            if( li.next().toString().equals("OK"))
	                li.remove();
	        }
	        if( listResult.size() == 0)
	            result= "OK";
	        else result= listResult.toString();	        
	        
	        return result;
	    }

		/**
	     * Из файла считываются пары "Имя тэга(атрибута) из 1С"-"имя соответствующего атрибута в Windchill".
	     * Причем в полученном от Parser-а object-е:
	     * 0. Проверяем наличие атрибута Type и его допустимых значений;
	     * 1. Имя атрибута из 1С заменяется на имя из Windchill, если такое находится в файле;
	     * 2. Если атрибуту 1С не находится замена в файле, то оствляем как есть;
	     * 2. Если имени из 1С соответствует "-", то в objecte соответствующий Entry удаляется;
	     * 3. Анализируя значение тэга "Type" в object-е, добавляем в object новый Entry с соответствующим значением ATR190
	     * 4. Переводим значения допустимых ЕИ
	     * 5. Дописываем значение для НСИ
	     * @param object
	     * @return object
	     */
	    private Map<String, String> CheckMapAttrs( Map<String, String> object) {
	        String oneCattr, oneCvalue, windattr, windvalue;
	        Map<String, String> temp = new HashMap<String, String>();
	        Map.Entry<String, String> entry;

	        oneCvalue = ( object.get( "Type") != null ? (String)object.get( "Type") : "");
	        if( oneCvalue.equals("материал"))
	            windvalue = "Материалы";
	        else
	        if( oneCvalue.equals("стандартное"))
	            windvalue = "Стандартные изделия";
	        else
	        if( oneCvalue.equals("пки"))
	            windvalue = "Прочие изделия";
	        else
	            return null;

	        Iterator<Map.Entry<String, String>> itm = object.entrySet().iterator();
	        while( itm.hasNext()) {
	            entry = itm.next();
	            //System.out.println( entry.getKey().toString() +"="+ entry.getValue().toString() );
	            oneCattr = entry.getKey().toString();
	            oneCvalue = entry.getValue().toString();
	            windattr = (String)mapAttrs.get( oneCattr);

	            if( windattr != null && windattr.equals("unit")) {
	                if( oneCvalue.equals("ШТ"))
	                    oneCvalue = "ea";
	                else if( oneCvalue.equals("КГ"))
	                    oneCvalue = "kg";
	                else if( oneCvalue.equals("М"))
	                    oneCvalue = "m";
	                else if( oneCvalue.equals("ЛИТР"))
	                    oneCvalue = "l";
	                else if( oneCvalue.equals("КВ.М"))
	                    oneCvalue = "sq_m";
	                else if( oneCvalue.equals("КУБ.М"))
	                    oneCvalue = "cu_m";
	            }

	             if( windattr != null && windattr.equals("НСИ"))
	                oneCvalue = "wt.part.WTPart|" + oneCvalue;

	            if( windattr == null)
	                temp.put( oneCattr, oneCvalue);  // т.е. оставляем как есть
	            else if( !windattr.equals("-"))
	                temp.put( windattr, oneCvalue);  // поменяли название атрибута
	        }

	        temp.put( "ATR_BOM_RS", windvalue);
	        return temp;
	    }

	    // метод мною сильно изменен
	    private String changePrincipal() {
	        String result = "OK";
	        String admin_user;
	        try {
	           if( AdminUser != null)
	               admin_user = AdminUser;
	           else
	               admin_user = wt.admin.AdministrativeDomainHelper.ADMINISTRATOR_NAME;
	           if(VERBOSE)
	               System.out.println((new StringBuilder()).append("Attempting to change principal to <")
	                                       .append( admin_user).append(">").toString());
	           SessionMgr.setPrincipal( admin_user);
	           String s = SessionMgr.getPrincipal().getName();
	           if(VERBOSE)
	               System.out.println((new StringBuilder()).append("Principal was changed to <")
	                                        .append( s).append(">").toString());
	        }
	        catch(WTException e) {
	            e.printStackTrace();
	            result = "Не смог установить пользователя с правами администратора.";
	        }
	        return result;
	    }

	    public void setContainerRefPKI(String containerRefPKI) {
	        ContainerRefPKI = containerRefPKI;
	    }

	    public void setContainerRefMAT(String containerRefMAT) {
	        ContainerRefMAT = containerRefMAT;
	    }

	    public void setFolderPKI(String folderPKI) {
	        FolderPKI = folderPKI;
	    }

	    public void setFolderMAT(String folderMAT) {
	        FolderMAT = folderMAT;
	    }

	    public void setAdminUser(String adminUser) {
	        AdminUser = adminUser;
	    }

		public void setNameView(String nameView) {
			NameView = nameView;
		}

	    public void setStateDelete(String stateDelete) {
	        this.stateDelete = stateDelete;
	    }

	    public void setVerbose(String verbose) {
	        this.VERBOSE = Boolean.valueOf(verbose);
	    }

	    private void FillMapAttrs(String nameFile) throws IOException {
	        BufferedReader in;
	        String str, oneC, wind;
	        File mapFile = new File( nameFile);
	        if (!mapFile.exists())
	             System.out.println("!!!!!! MapAttrs File not found by name: " + nameFile);
	         
	        in = new BufferedReader( new InputStreamReader( new FileInputStream( mapFile)));

	        while((str= in.readLine()) != null) {	        	
	            oneC = str.substring(0, str.indexOf(";")).trim();
	            wind = str.substring( str.lastIndexOf(";")+1);
	            mapAttrs.put( oneC, wind);
	        }
	    }

	    public static void main(String[] args)  throws IOException, SAXException {
	        //String myXML = "<wc:COLLECTION xmlns:wc=\"http://www.ptc.com/infoengine/1.0\"><Nomenclature><Code>12345</Code><Name>Болт</Name></Nomenclature><Nomenclature><Code>00005</Code><Name>Винт</Name></Nomenclature></wc:COLLECTION>";
	        String myXML =
	                "<ListOfNomenclatures xmlns=\"http://www.peleg.by/nomenclature\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
	                      "<Nomenclature><Type>стандартное</Type><Action>создание</Action><Number>D0001</Number><Name>Болт 777</Name><FullName>Big Болт 777</FullName>" +
	                        "<Code>186607</Code><Path>wt.part.WTPart|ПКИ/Болты</Path>" +
	                      "<Attributes><Attribute><AttributeName>ATR710</AttributeName><Value>Болт</Value></Attribute></Attributes></Nomenclature>" +
	                      "<Nomenclature><Type>пки</Type><Action>создание</Action><Number>U0001</Number><Name>Винт 555</Name><FullName>Big Винт 777</FullName>" +
	                        "<Code>7777777</Code><Path>wt.part.WTPart|ПКИ/Болты</Path>" +
	                      "<Attributes><Attribute><AttributeName>ATR710</AttributeName><Value>Винт</Value></Attribute></Attributes></Nomenclature>" +
	                 "</ListOfNomenclatures>";

	        String xmlStr1 =
	         "<ListOfNomenclatures xmlns=\"http://www.peleg.by/nomenclature\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
	          "<Nomenclature><Type>стандартное</Type><Action>создание</Action><Number>D0004</Number><Name>Болт 777</Name><FullName>Big Болт 777</FullName>" +
	              "<Code>186607</Code><Path>wt.part.WTPart|ПКИ/Болты</Path><DefaultUnit>kg</DefaultUnit>" +
	              "<Attributes><Attribute><AttributeName>ATR710</AttributeName><Value>Болт</Value></Attribute></Attributes></Nomenclature>" +
	          "<Nomenclature><Type>пки</Type><Action>редактирование</Action><Number>U0001</Number><Name>Винт 555</Name><FullName>Big Винт 777</FullName>" +
	              "<Code>7777777</Code><Path>wt.part.WTPart|ПКИ/Винты</Path><DefaultUnit>l</DefaultUnit>" +
	              "<Attributes>" +
	              "<Attribute><AttributeName>ATR030</AttributeName><Value>Тестовое примечание</Value></Attribute>" +
	              "<Attribute><AttributeName>ATR340</AttributeName><Value>Добавили к Части новый атрибут</Value></Attribute>" +
	              "</Attributes></Nomenclature>" +
	          "<Transaction><TransactionNumber>4</TransactionNumber><PublishedBy>АСУ</PublishedBy></Transaction>" +
	        "</ListOfNomenclatures>";


	        OneCSaxHandler saxHandler = new OneCSaxHandler();
	        XMLReader xmlreader = XMLReaderFactory.createXMLReader(); // SAXParserFactory.newInstance().newSAXParser().getXMLReader();
	        //xmlreader.setFeature("http://xml.org/sax/features/namespaces", false);  // ?
	        xmlreader.setContentHandler( saxHandler);
	        xmlreader.setErrorHandler( saxHandler);
	        xmlreader.parse(new InputSource(new StringReader( myXML)));

	                OneCNomenclature oneC = new OneCNomenclature();
	                Map map;

	        Map.Entry entry;
	        for ( Iterator it =  saxHandler.getListObjects().iterator(); it.hasNext();) {
	            System.out.println("----- new Object -------------");
	            Iterator itm = ((Map)it.next()).entrySet().iterator();
	            while( itm.hasNext()) {
	                entry = (Map.Entry)itm.next();
	                System.out.println( entry.getKey().toString() +"="+ entry.getValue().toString() );
	            }
	        }
	        for ( Iterator it =  saxHandler.getListObjects().iterator(); it.hasNext();) {
	            System.out.println("----- after Map new Object -------------");
	            map = (Map)it.next();
	            map = oneC.CheckMapAttrs( map);
	            Iterator itm = map.entrySet().iterator();
	            while( itm.hasNext()) {
	                entry = (Map.Entry)itm.next();
	                System.out.println( entry.getKey().toString() +"="+ entry.getValue().toString() );
	            }
	        }

	    }

}
