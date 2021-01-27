package ext.by.iba.OneC;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


/**
  *
 * @author Шидловский В.А.
 * @version 01     24.12.2010
 *
 * <ListOfNomenclatures xmlns="http://www.peleg.by/nomenclature" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<Nomenclature>
		<Code>00000146705</Code>
		<Name>Провод МСЭ 16-15 3*0,12</Name>
		<Number>358219762300</Number>
		<Standart1>ТУ16.К76-011-88</Standart1>
		<Quantity>0</Quantity>
		<FullName>ПРОВОД МСЭ 16-15 3*0,12</FullName>
		<Attributes>
			<Attribute>
				<AttributeName>ГОСТ, ОСТ, ТУ, каталог</AttributeName>
				<Value>ТУ16.К76-011-88</Value>
			</Attribute>
			<Attribute>
				<AttributeName>Профиль материала</AttributeName>
				<Value>Пруток</Value>
			</Attribute>
			<Attribute>
				<AttributeName>Плотность</AttributeName>
				<Value>0,254</Value>
			</Attribute>
		</Attributes>
		<ClassificationPath>Материалы и комплектующие/35   ПРОДУКЦИЯ КАБЕЛЬНАЯ/3582   ПРОВОДА МОНТАЖНЫЕ/Пруток</ClassificationPath>
		<Action>редактирование</Action>
	</Nomenclature>
	<Transaction>
		<TransactionNumber>234</TransactionNumber>
		<PublishedBy>АСУ</PublishedBy>
		<PublishedDate>2011-01-10T09:30:03</PublishedDate>
	</Transaction>
</ListOfNomenclatures>
 */
public class OneCSaxHandler extends DefaultHandler {
    private StringBuffer currentText;
    private boolean element, subelement;
    private String key, attrName, attrValue;
    private List<Map<String, String>> listObjects; // хранит все полученные из 1С объекты
    private Map<String, String> object; // хранит полученные из 1С по каждому объекту пары "имя атрибута" - "значение"

    OneCSaxHandler() {
        super();
        element = false;
        subelement = false;
    }

    public void startDocument() {
        listObjects = new ArrayList<Map<String, String>>();
    }

    public void startElement(String uri, String name, String qName, Attributes attributes) {
        if( !qName.equalsIgnoreCase("ListOfNomenclatures")) {
            if( qName.equalsIgnoreCase("Nomenclature")) {
                element = true;
                object = new HashMap<String, String>();
            } else  if( qName.equalsIgnoreCase("AttributeName") || qName.equalsIgnoreCase("Value")) {
                //attribute = true;
                currentText = new StringBuffer();
            } else if( !qName.equalsIgnoreCase("Attributes") & !qName.equalsIgnoreCase("Attribute")
                     & !qName.equalsIgnoreCase("Transaction") & !qName.equalsIgnoreCase("TransactionNumber")
                     & !qName.equalsIgnoreCase("PublishedBy") & !qName.equalsIgnoreCase("PublishedDate")) {
                subelement = true;
                currentText = new StringBuffer();
                key = qName; // .toUpperCase() нельзя т.к. имена параметров регистрозависимые!; все ключи будем хранить и обрабатывать в верхнем регистре !!!
            }
        }
    }

    public void characters(char ch[], int start, int length) {
        currentText.append(ch, start, length);
    }

    public void endElement(String uri, String name, String qName) throws SAXException {
        if( qName.equalsIgnoreCase("AttributeName"))
            attrName = currentText.toString().trim();
        if( qName.equalsIgnoreCase("Value"))
            attrValue = currentText.toString().trim();
        if( qName.equalsIgnoreCase("Attribute"))
            object.put( attrName, attrValue);

        if(subelement) {
            String result = currentText.toString().trim();
            object.put( key, result);
            subelement = false;
        } else
        if(element) {
            listObjects.add( object);
            element = false;
        }
    }

    public List<Map<String, String>> getListObjects() {
        return listObjects;
    }
}
