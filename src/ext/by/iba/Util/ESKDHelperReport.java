package ext.by.iba.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.log4j.Logger;
import wt.change2.AffectedActivityData;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeRecord2;
import wt.change2.ChangeService2;
import wt.change2.Changeable2;
import wt.change2.IncludedInIfc;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.PersistInfo;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.definition.service.IBADefinitionService;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.FloatValueDefaultView;
import wt.iba.value.litevalue.IntegerValueDefaultView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.IBAValueService;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.MaturityService;
import wt.maturity.Promotable;
import wt.maturity.PromotionNotice;
import wt.occurrence.OccurrenceHelper;
import wt.occurrence.OccurrenceService;
import wt.org.OrganizationServicesHelper;
import wt.org.OrganizationServicesManager;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.LineNumber;
import wt.part.PartUsesOccurrence;
import wt.part.Quantity;
import wt.part.QuantityUnit;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.IterationIdentifier;
import wt.vc.VersionIdentifier;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressService;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineService;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfVotingEventAudit;

public class ESKDHelperReport
{
  public static boolean VERBOSE;
  static String delimiterSymbol;
  static WTProperties wtprops;
  static Map<Character, String> charMap = null;
  static Map<String, Double> mCharsSansSerif_9 = new HashMap();
  private static String CLASSNAME = "ext.by.iba.Util.ESKDHelperReport";
  static final Logger logger = LogR.getLogger(CLASSNAME);
  
  static
  {
    try
    {
      wtprops = WTProperties.getLocalProperties();
      charMap = new HashMap();
      charMap.put(Character.valueOf('А'), "A");
      charMap.put(Character.valueOf('Б'), "B");
      charMap.put(Character.valueOf('В'), "V");
      charMap.put(Character.valueOf('Г'), "G");
      charMap.put(Character.valueOf('Д'), "D");
      charMap.put(Character.valueOf('Е'), "E");
      charMap.put(Character.valueOf('Ё'), "E");
      charMap.put(Character.valueOf('Ж'), "Zh");
      charMap.put(Character.valueOf('З'), "Z");
      charMap.put(Character.valueOf('И'), "I");
      charMap.put(Character.valueOf('Й'), "I");
      charMap.put(Character.valueOf('К'), "K");
      charMap.put(Character.valueOf('Л'), "L");
      charMap.put(Character.valueOf('М'), "M");
      charMap.put(Character.valueOf('Н'), "N");
      charMap.put(Character.valueOf('О'), "O");
      charMap.put(Character.valueOf('П'), "P");
      charMap.put(Character.valueOf('Р'), "R");
      charMap.put(Character.valueOf('С'), "S");
      charMap.put(Character.valueOf('Т'), "T");
      charMap.put(Character.valueOf('У'), "U");
      charMap.put(Character.valueOf('Ф'), "F");
      charMap.put(Character.valueOf('Х'), "H");
      charMap.put(Character.valueOf('Ц'), "C");
      charMap.put(Character.valueOf('Ч'), "Ch");
      charMap.put(Character.valueOf('Ш'), "Sh");
      charMap.put(Character.valueOf('Щ'), "Sh");
      charMap.put(Character.valueOf('Ъ'), "_");
      charMap.put(Character.valueOf('Ы'), "Y");
      charMap.put(Character.valueOf('Ь'), "_");
      charMap.put(Character.valueOf('Э'), "E");
      charMap.put(Character.valueOf('Ю'), "U");
      charMap.put(Character.valueOf('Я'), "Ya");
      charMap.put(Character.valueOf('а'), "a");
      charMap.put(Character.valueOf('б'), "b");
      charMap.put(Character.valueOf('в'), "v");
      charMap.put(Character.valueOf('г'), "g");
      charMap.put(Character.valueOf('д'), "d");
      charMap.put(Character.valueOf('е'), "e");
      charMap.put(Character.valueOf('ё'), "e");
      charMap.put(Character.valueOf('ж'), "zh");
      charMap.put(Character.valueOf('з'), "z");
      charMap.put(Character.valueOf('и'), "i");
      charMap.put(Character.valueOf('й'), "i");
      charMap.put(Character.valueOf('к'), "k");
      charMap.put(Character.valueOf('л'), "l");
      charMap.put(Character.valueOf('м'), "m");
      charMap.put(Character.valueOf('н'), "n");
      charMap.put(Character.valueOf('о'), "o");
      charMap.put(Character.valueOf('п'), "p");
      charMap.put(Character.valueOf('р'), "r");
      charMap.put(Character.valueOf('с'), "s");
      charMap.put(Character.valueOf('т'), "t");
      charMap.put(Character.valueOf('у'), "u");
      charMap.put(Character.valueOf('ф'), "f");
      charMap.put(Character.valueOf('х'), "h");
      charMap.put(Character.valueOf('ц'), "c");
      charMap.put(Character.valueOf('ч'), "ch");
      charMap.put(Character.valueOf('ш'), "sh");
      charMap.put(Character.valueOf('щ'), "sh");
      charMap.put(Character.valueOf('ъ'), "_");
      charMap.put(Character.valueOf('ы'), "y");
      charMap.put(Character.valueOf('ь'), "_");
      charMap.put(Character.valueOf('э'), "e");
      charMap.put(Character.valueOf('ю'), "u");
      charMap.put(Character.valueOf('я'), "ya");
      charMap.put(Character.valueOf('.'), "_");
      charMap.put(Character.valueOf(' '), "_");
      

      mCharsSansSerif_9.put("A", Double.valueOf(6.41D));mCharsSansSerif_9.put("B", Double.valueOf(6.41D));mCharsSansSerif_9.put("C", Double.valueOf(7.0D));mCharsSansSerif_9.put("D", Double.valueOf(7.0D));mCharsSansSerif_9.put("E", Double.valueOf(6.41D));mCharsSansSerif_9.put("F", Double.valueOf(5.92D));mCharsSansSerif_9.put("G", Double.valueOf(7.7D));mCharsSansSerif_9.put("H", Double.valueOf(7.0D));mCharsSansSerif_9.put("I", Double.valueOf(2.57D));
      mCharsSansSerif_9.put("J", Double.valueOf(4.53D));mCharsSansSerif_9.put("K", Double.valueOf(6.41D));mCharsSansSerif_9.put("L", Double.valueOf(5.14D));mCharsSansSerif_9.put("M", Double.valueOf(7.7D));mCharsSansSerif_9.put("N", Double.valueOf(7.0D));mCharsSansSerif_9.put("O", Double.valueOf(7.7D));mCharsSansSerif_9.put("P", Double.valueOf(6.41D));mCharsSansSerif_9.put("Q", Double.valueOf(7.7D));mCharsSansSerif_9.put("R", Double.valueOf(7.0D));
      mCharsSansSerif_9.put("S", Double.valueOf(6.41D));mCharsSansSerif_9.put("T", Double.valueOf(5.92D));mCharsSansSerif_9.put("U", Double.valueOf(7.0D));mCharsSansSerif_9.put("V", Double.valueOf(6.41D));mCharsSansSerif_9.put("W", Double.valueOf(8.560000000000001D));mCharsSansSerif_9.put("X", Double.valueOf(6.41D));mCharsSansSerif_9.put("Y", Double.valueOf(6.41D));mCharsSansSerif_9.put("Z", Double.valueOf(5.92D));
      mCharsSansSerif_9.put("a", Double.valueOf(5.14D));mCharsSansSerif_9.put("b", Double.valueOf(5.14D));mCharsSansSerif_9.put("c", Double.valueOf(4.53D));mCharsSansSerif_9.put("d", Double.valueOf(5.14D));mCharsSansSerif_9.put("e", Double.valueOf(5.14D));mCharsSansSerif_9.put("f", Double.valueOf(2.57D));mCharsSansSerif_9.put("g", Double.valueOf(5.14D));mCharsSansSerif_9.put("h", Double.valueOf(5.14D));mCharsSansSerif_9.put("i", Double.valueOf(2.03D));
      mCharsSansSerif_9.put("j", Double.valueOf(2.03D));mCharsSansSerif_9.put("k", Double.valueOf(4.53D));mCharsSansSerif_9.put("l", Double.valueOf(2.03D));mCharsSansSerif_9.put("m", Double.valueOf(7.7D));mCharsSansSerif_9.put("n", Double.valueOf(5.14D));mCharsSansSerif_9.put("o", Double.valueOf(5.14D));mCharsSansSerif_9.put("p", Double.valueOf(5.14D));mCharsSansSerif_9.put("q", Double.valueOf(5.14D));mCharsSansSerif_9.put("r", Double.valueOf(3.08D));
      mCharsSansSerif_9.put("s", Double.valueOf(4.53D));mCharsSansSerif_9.put("t", Double.valueOf(2.57D));mCharsSansSerif_9.put("u", Double.valueOf(5.14D));mCharsSansSerif_9.put("v", Double.valueOf(4.53D));mCharsSansSerif_9.put("w", Double.valueOf(7.0D));mCharsSansSerif_9.put("x", Double.valueOf(4.53D));mCharsSansSerif_9.put("y", Double.valueOf(4.53D));mCharsSansSerif_9.put("z", Double.valueOf(4.53D));
      
      mCharsSansSerif_9.put("А", Double.valueOf(6.41D));mCharsSansSerif_9.put("Б", Double.valueOf(5.92D));mCharsSansSerif_9.put("В", Double.valueOf(6.41D));mCharsSansSerif_9.put("Г", Double.valueOf(5.14D));mCharsSansSerif_9.put("Д", Double.valueOf(6.41D));mCharsSansSerif_9.put("Е", Double.valueOf(6.41D));mCharsSansSerif_9.put("Ё", Double.valueOf(6.41D));mCharsSansSerif_9.put("Ж", Double.valueOf(8.560000000000001D));mCharsSansSerif_9.put("З", Double.valueOf(5.5D));
      mCharsSansSerif_9.put("И", Double.valueOf(7.0D));mCharsSansSerif_9.put("Й", Double.valueOf(7.0D));mCharsSansSerif_9.put("К", Double.valueOf(5.5D));mCharsSansSerif_9.put("Л", Double.valueOf(5.92D));mCharsSansSerif_9.put("М", Double.valueOf(7.7D));mCharsSansSerif_9.put("Н", Double.valueOf(7.0D));mCharsSansSerif_9.put("О", Double.valueOf(7.7D));mCharsSansSerif_9.put("П", Double.valueOf(7.0D));mCharsSansSerif_9.put("Р", Double.valueOf(6.41D));mCharsSansSerif_9.put("С", Double.valueOf(7.0D));
      mCharsSansSerif_9.put("Т", Double.valueOf(5.5D));mCharsSansSerif_9.put("У", Double.valueOf(5.92D));mCharsSansSerif_9.put("Ф", Double.valueOf(7.0D));mCharsSansSerif_9.put("Х", Double.valueOf(6.41D));mCharsSansSerif_9.put("Ц", Double.valueOf(7.0D));mCharsSansSerif_9.put("Ч", Double.valueOf(6.41D));mCharsSansSerif_9.put("Ш", Double.valueOf(8.560000000000001D));mCharsSansSerif_9.put("Щ", Double.valueOf(8.560000000000001D));mCharsSansSerif_9.put("Ъ", Double.valueOf(7.7D));
      mCharsSansSerif_9.put("Ы", Double.valueOf(8.560000000000001D));mCharsSansSerif_9.put("Ь", Double.valueOf(5.92D));mCharsSansSerif_9.put("Э", Double.valueOf(7.0D));mCharsSansSerif_9.put("Ю", Double.valueOf(9.630000000000001D));mCharsSansSerif_9.put("Я", Double.valueOf(7.0D));
      mCharsSansSerif_9.put("а", Double.valueOf(5.14D));mCharsSansSerif_9.put("б", Double.valueOf(5.5D));mCharsSansSerif_9.put("в", Double.valueOf(4.82D));mCharsSansSerif_9.put("г", Double.valueOf(3.35D));mCharsSansSerif_9.put("д", Double.valueOf(5.5D));mCharsSansSerif_9.put("е", Double.valueOf(5.14D));mCharsSansSerif_9.put("ё", Double.valueOf(5.14D));mCharsSansSerif_9.put("ж", Double.valueOf(6.41D));mCharsSansSerif_9.put("з", Double.valueOf(4.28D));
      mCharsSansSerif_9.put("и", Double.valueOf(5.14D));mCharsSansSerif_9.put("й", Double.valueOf(5.14D));mCharsSansSerif_9.put("к", Double.valueOf(4.06D));mCharsSansSerif_9.put("л", Double.valueOf(5.5D));mCharsSansSerif_9.put("м", Double.valueOf(6.41D));mCharsSansSerif_9.put("н", Double.valueOf(5.14D));mCharsSansSerif_9.put("о", Double.valueOf(5.14D));mCharsSansSerif_9.put("п", Double.valueOf(5.14D));mCharsSansSerif_9.put("р", Double.valueOf(5.14D));mCharsSansSerif_9.put("с", Double.valueOf(4.53D));
      mCharsSansSerif_9.put("т", Double.valueOf(4.28D));mCharsSansSerif_9.put("у", Double.valueOf(4.53D));mCharsSansSerif_9.put("ф", Double.valueOf(7.7D));mCharsSansSerif_9.put("х", Double.valueOf(4.53D));mCharsSansSerif_9.put("ц", Double.valueOf(5.5D));mCharsSansSerif_9.put("ч", Double.valueOf(4.82D));mCharsSansSerif_9.put("ш", Double.valueOf(7.7D));mCharsSansSerif_9.put("щ", Double.valueOf(7.7D));mCharsSansSerif_9.put("ъ", Double.valueOf(5.92D));
      mCharsSansSerif_9.put("ы", Double.valueOf(7.0D));mCharsSansSerif_9.put("ь", Double.valueOf(4.82D));mCharsSansSerif_9.put("э", Double.valueOf(4.82D));mCharsSansSerif_9.put("ю", Double.valueOf(6.41D));mCharsSansSerif_9.put("я", Double.valueOf(4.82D));
      
      mCharsSansSerif_9.put("0", Double.valueOf(5.14D));mCharsSansSerif_9.put("1", Double.valueOf(5.14D));mCharsSansSerif_9.put("2", Double.valueOf(5.14D));mCharsSansSerif_9.put("3", Double.valueOf(5.14D));mCharsSansSerif_9.put("4", Double.valueOf(5.14D));mCharsSansSerif_9.put("5", Double.valueOf(5.14D));mCharsSansSerif_9.put("6", Double.valueOf(5.14D));mCharsSansSerif_9.put("7", Double.valueOf(5.14D));mCharsSansSerif_9.put("8", Double.valueOf(5.14D));mCharsSansSerif_9.put("9", Double.valueOf(5.14D));
      
      mCharsSansSerif_9.put(".", Double.valueOf(2.57D));mCharsSansSerif_9.put(",", Double.valueOf(2.57D));mCharsSansSerif_9.put("/", Double.valueOf(2.57D));mCharsSansSerif_9.put("\\", Double.valueOf(2.57D));mCharsSansSerif_9.put("+", Double.valueOf(5.5D));mCharsSansSerif_9.put("-", Double.valueOf(3.08D));mCharsSansSerif_9.put("_", Double.valueOf(5.14D));mCharsSansSerif_9.put(" ", Double.valueOf(5.14D));mCharsSansSerif_9.put("(", Double.valueOf(3.08D));
      mCharsSansSerif_9.put(")", Double.valueOf(3.08D));mCharsSansSerif_9.put("*", Double.valueOf(3.67D));mCharsSansSerif_9.put("?", Double.valueOf(5.14D));mCharsSansSerif_9.put("&", Double.valueOf(6.41D));mCharsSansSerif_9.put(":", Double.valueOf(2.57D));mCharsSansSerif_9.put("%", Double.valueOf(8.560000000000001D));mCharsSansSerif_9.put(";", Double.valueOf(2.57D));mCharsSansSerif_9.put("#", Double.valueOf(5.14D));mCharsSansSerif_9.put("\"", Double.valueOf(3.21D));
      mCharsSansSerif_9.put("@", Double.valueOf(9.630000000000001D));mCharsSansSerif_9.put("!", Double.valueOf(2.57D));mCharsSansSerif_9.put("<", Double.valueOf(5.5D));mCharsSansSerif_9.put(">", Double.valueOf(5.5D));mCharsSansSerif_9.put("$", Double.valueOf(5.14D));mCharsSansSerif_9.put("^", Double.valueOf(4.28D));mCharsSansSerif_9.put("=", Double.valueOf(5.5D));mCharsSansSerif_9.put("№", Double.valueOf(9.630000000000001D));mCharsSansSerif_9.put("±", Double.valueOf(5.92D));
      mCharsSansSerif_9.put("°", Double.valueOf(4.28D));mCharsSansSerif_9.put("[", Double.valueOf(2.49D));mCharsSansSerif_9.put("]", Double.valueOf(2.49D));mCharsSansSerif_9.put("{", Double.valueOf(3.08D));mCharsSansSerif_9.put("}", Double.valueOf(3.08D));mCharsSansSerif_9.put("µ", Double.valueOf(6.41D));mCharsSansSerif_9.put("Ø", Double.valueOf(7.7D));mCharsSansSerif_9.put("ø", Double.valueOf(5.92D));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    delimiterSymbol = wtprops.getProperty("by.iba.Report.delimiterSymbol");
    if (delimiterSymbol == null) {
      delimiterSymbol = "~";
    }
  }
  
  public static String getValueAttr(String nameAttr, Object obj)
    throws WTException
  {
    String valueAttr = "";
    

    String docNumber = "";
    try
    {
      IBAHolder ibaholder = IBAValueHelper.service.refreshAttributeContainerWithoutConstraints((IBAHolder)obj);
      DefaultAttributeContainer defaultattrcontainer = (DefaultAttributeContainer)ibaholder.getAttributeContainer();
      AbstractAttributeDefinizerView abstractattributedefinizerview = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(nameAttr);
      AbstractValueView[] aabstractvalueview = defaultattrcontainer.getAttributeValues((AttributeDefDefaultView)abstractattributedefinizerview);
      if ((abstractattributedefinizerview instanceof StringDefView)) {
        for (int i = 0; i < aabstractvalueview.length;)
        {
          valueAttr = valueAttr + (valueAttr.equals("") ? "" : ", ") + ((StringValueDefaultView)aabstractvalueview[i]).getValue();
          i++;
        }
      } else if ((abstractattributedefinizerview instanceof IntegerDefView)) {
        valueAttr = ((IntegerValueDefaultView)aabstractvalueview[0]).getValueAsString();
      } else if ((abstractattributedefinizerview instanceof FloatDefView)) {
        valueAttr = ((FloatValueDefaultView)aabstractvalueview[0]).getValueAsString();
      }
    }
    catch (Exception e)
    {
      if (VERBOSE) {
        if ((obj instanceof WTDocument)) {
          docNumber = ((WTDocument)obj).getNumber();
        } else if ((obj instanceof EPMDocument)) {
          docNumber = ((EPMDocument)obj).getNumber();
        } else if ((obj instanceof WTPart)) {
          docNumber = ((WTPart)obj).getNumber();
        }
      }
    }
    return valueAttr;
  }
  
  public static String unitLocalizedValue(String unit)
  {
    String localValue = "неизвестная";
    if (unit.endsWith("kg")) {
      localValue = "кг";
    } else if (unit.endsWith("l")) {
      localValue = "л";
    } else if (unit.endsWith("sq_m")) {
      localValue = "кв.м";
    } else if (unit.endsWith("m**2")) {
      localValue = "кв.м";
    } else if (unit.endsWith("cu_m")) {
      localValue = "куб.м";
    } else if (unit.endsWith("m")) {
      localValue = "м";
    } else if (unit.endsWith("ea")) {
      localValue = "";
    }
    return localValue;
  }
  
  public static String getAuthorFullName(String refUser)
  {
    String fullName = null;
    try
    {
      WTUser wtUser = OrganizationServicesHelper.manager.getAuthenticatedUser(refUser);
      fullName = wtUser.getFullName();
      fullName = trimFIO(fullName);
    }
    catch (WTException e)
    {
      e.printStackTrace();
    }
    return fullName;
  }
  
  public static String trimFIO(String fullName)
  {
    String lastName;
    if (fullName.indexOf(" ") > 0) {
      lastName = fullName.substring(0, fullName.indexOf(" "));
    } else {
      lastName = fullName;
    }
    return lastName.replace(",", "");
  }
  
  public static String getNameRS(String s)
  {
    String value = "";
    if ((s.equals("010")) || (s.equals("1010"))) {
      value = "Сборочные единицы";
    } else if ((s.equals("020")) || (s.equals("1020"))) {
      value = "Детали";
    } else if ((s.equals("030")) || (s.equals("1030"))) {
      value = "Стандартные изделия";
    } else if ((s.equals("040")) || (s.equals("1040"))) {
      value = "Прочие изделия";
    } else if ((s.equals("050")) || (s.equals("1050"))) {
      value = "Материалы";
    } else if ((s.equals("060")) || (s.equals("1060"))) {
      value = "Комплекты";
    } else if ((s.equals("000")) || (s.equals("1000"))) {
      value = "Документация";
    } else if (s.equals("RS7")) {
      value = "Наследуемые детали";
    }
    return value;
  }
  
  public static List<String> divisionOfWord(String stroka, int maxLenght)
  {
    String buf = "";
    String probel = "";
    
    List<String> aStrok = new ArrayList();
    if (stroka.trim().replace(delimiterSymbol, "").equals("")) {
      aStrok.add("");
    }
    for (StringTokenizer stRule = new StringTokenizer(stroka.trim(), delimiterSymbol); stRule.hasMoreTokens();)
    {
      buf = "";
      String subStr = "";
      probel = "";
      stroka = stRule.nextToken();
      for (StringTokenizer st = new StringTokenizer(stroka, " "); st.hasMoreTokens();)
      {
        subStr = st.nextToken();
        String temp = buf + probel + subStr;
        int adjustedMaxLenght = adjustPerenos(temp, maxLenght);
        if (temp.length() <= adjustedMaxLenght)
        {
          buf = temp;
          probel = " ";
        }
        else if (subStr.length() <= maxLenght)
        {
          aStrok.add(buf);
          buf = subStr;
        }
        else
        {
          if (buf.length() > 0) {
            aStrok.add(buf);
          }
          adjustedMaxLenght = adjustPerenos(subStr, maxLenght);
          adjustedMaxLenght = adjustedMaxLenght > subStr.length() ? subStr.length() : adjustedMaxLenght;
          
          aStrok.add(subStr.substring(0, adjustedMaxLenght));
          buf = subStr.substring(adjustedMaxLenght);
          if (buf.length() == 0) {
            probel = "";
          }
        }
      }
      if ((!aStrok.isEmpty()) && (buf.length() == 1))
      {
        System.out.println(" 1 character !!!");
        String temp = (String)aStrok.get(aStrok.size() - 1);
        if (temp.length() > 1)
        {
          int idx = temp.lastIndexOf(" ");
          if (idx > 0)
          {
            aStrok.remove(aStrok.size() - 1);
            aStrok.add(temp.substring(0, idx));
            aStrok.add(temp.substring(idx + 1) + probel + buf);
          }
          else
          {
            aStrok.remove(aStrok.size() - 1);
            aStrok.add(temp.substring(0, temp.length() - 2));
            aStrok.add(temp.substring(temp.length() - 2) + buf);
          }
        }
        else if (buf.length() > 0)
        {
          aStrok.add(buf);
        }
      }
      else if (buf.length() > 0)
      {
        aStrok.add(buf);
      }
    }
    return aStrok;
  }
  
  private static int adjustPerenos(String subStr, int maxLenght)
  {
    int newMaxLenght = maxLenght;
    
    String buffer = subStr.replace(",", "");
    buffer = buffer.replace(".", "");
    buffer = buffer.replace(":", "");
    buffer = buffer.replace(";", "");
    buffer = buffer.replace("\"", "");
    buffer = buffer.replace("!", "");
    buffer = buffer.replace(" ", "");
    buffer = buffer.replace("*", "");
    buffer = buffer.replace("(", "");
    buffer = buffer.replace(")", "");
    buffer = buffer.replace("[", "");
    buffer = buffer.replace("]", "");
    buffer = buffer.replace("{", "");
    buffer = buffer.replace("}", "");
    buffer = buffer.replace("-", "");
    buffer = buffer.replace("+", "");
    buffer = buffer.replace("_", "");
    buffer = buffer.replace("/", "");
    buffer = buffer.replace("\\", "");
    buffer = buffer.replace("0", "");
    buffer = buffer.replace("1", "");
    if (buffer.length() < subStr.length())
    {
      int t = (subStr.length() - buffer.length()) / 2;
      newMaxLenght = maxLenght + t;
    }
    if (newMaxLenght - maxLenght > 1) {
      newMaxLenght--;
    }
    return newMaxLenght;
  }
  
  public static List<String> divisionOfWord(String stroka, int maxLenghtInPoints, boolean b)
  {
    String buf = "";
    String probel = "";
    
    List<String> aStrok = new ArrayList();
    if (stroka.trim().replace(delimiterSymbol, "").equals("")) {
      aStrok.add("");
    }
    for (StringTokenizer stRule = new StringTokenizer(stroka.trim(), delimiterSymbol); stRule.hasMoreTokens();)
    {
      buf = "";
      String subStr = "";
      probel = "";
      stroka = stRule.nextToken();
      for (StringTokenizer st = new StringTokenizer(stroka, " "); st.hasMoreTokens();)
      {
        subStr = st.nextToken();
        String temp = buf + probel + subStr;
        probel = " ";
        if (lenghtInPoints(temp) <= maxLenghtInPoints)
        {
          buf = temp;
        }
        else if (lenghtInPoints(subStr) <= maxLenghtInPoints)
        {
          aStrok.add(buf);
          buf = subStr;
        }
        else
        {
          if (buf.length() > 0) {
            aStrok.add(buf);
          }
          buf = calcStroka(subStr, maxLenghtInPoints);
          aStrok.add(buf);
          buf = subStr.substring(buf.length());
        }
      }
      if ((!aStrok.isEmpty()) && (buf.length() == 1))
      {
        if (VERBOSE) {
          System.out.println(" 1 character !!!");
        }
        String temp = (String)aStrok.get(aStrok.size() - 1);
        if (temp.length() > 1)
        {
          int idx = temp.lastIndexOf(" ");
          if (idx > 0)
          {
            aStrok.remove(aStrok.size() - 1);
            aStrok.add(temp.substring(0, idx));
            aStrok.add(temp.substring(idx + 1) + probel + buf);
          }
          else
          {
            aStrok.remove(aStrok.size() - 1);
            aStrok.add(temp.substring(0, temp.length() - 2));
            aStrok.add(temp.substring(temp.length() - 2) + buf);
          }
        }
        else if (buf.length() > 0)
        {
          aStrok.add(buf);
        }
      }
      else if (buf.length() > 0)
      {
        aStrok.add(buf);
      }
    }
    return aStrok;
  }
  
  private static String calcStroka(String str, int maxLenghtInPoints)
  {
    StringBuffer sb = new StringBuffer();
    

    double result = 0.0D;
    for (int i = 0; i < str.length();)
    {
      char ch = str.charAt(i++);
      Double temp = (Double)mCharsSansSerif_9.get(Character.toString(ch));
      result += (temp != null ? temp.doubleValue() : 7.0D);
      if (result > maxLenghtInPoints) {
        break;
      }
      sb.append(ch);
    }
    return sb.toString();
  }
  
  private static double lenghtInPoints(String str)
  {
    double result = 0.0D;
    

    char[] dst = new char[str.length()];
    str.getChars(0, str.length(), dst, 0);
    for (int i = 0; i < dst.length;)
    {
      Double temp = (Double)mCharsSansSerif_9.get(Character.toString(dst[(i++)]));
      result += (temp != null ? temp.doubleValue() : 7.0D);
    }
    return result;
  }
  

  private static void fillHardAttrsPart(Map<String, String> tempEntityData, WTPart nextPart)
    throws WTException
  {
    Locale locale = SessionHelper.manager.getLocale();
    tempEntityData.put("number", nextPart.getNumber());
    tempEntityData.put("name", nextPart.getName());
    tempEntityData.put("versionInfo.identifier.versionId", nextPart.getVersionIdentifier().getValue());
    tempEntityData.put("iterationInfo.identifier.iterationId", nextPart.getIterationIdentifier().getValue());
    tempEntityData.put("state.state", nextPart.getState().getState().getDisplay(locale));
    
    tempEntityData.put("iterationInfo.modifier", trimFIO(nextPart.getModifier().getFullName()));
    tempEntityData.put("iterationInfo.creator", trimFIO(nextPart.getCreator().getFullName()));
    



    tempEntityData.put("thePersistInfo.modifyStamp", new SimpleDateFormat("dd.MM.yy").format(nextPart.getPersistInfo().getModifyStamp()));
    tempEntityData.put("thePersistInfo.createStamp", new SimpleDateFormat("dd.MM.yy").format(nextPart.getPersistInfo().getCreateStamp()));
    tempEntityData.put("view", nextPart.getViewName());
    tempEntityData.put("unitOfPart", unitLocalizedValue(nextPart.getDefaultUnit().getStringValue()));
  }
  

  public static Object[] converterEI(double amount, String unit)
  {
    Object[] conv = null;
    if (unit.equals("кг"))
    {
      if (amount < 0.01D)
      {
        amount *= 1000.0D;
        unit = "г";
      }
    }
    else if (unit.equals("л"))
    {
      if (amount < 0.001D)
      {
        amount *= 1000.0D;
        unit = "мл";
      }
    }
    else if (unit.equals("кв.м"))
    {
      if (amount < 0.001D)
      {
        amount *= 1000000.0D;
        unit = "кв.мм";
      }
    }
    else if (unit.equals("куб.м"))
    {
      if (amount < 1.E-005D)
      {
        amount *= 1000000000.0D;
        unit = "куб.мм";
      }
      else if (amount < 0.01D)
      {
        amount *= 1000000.0D;
        unit = "куб.см";
      }
    }
    else if ((unit.equals("м")) && 
      (amount < 0.01D))
    {
      amount *= 1000.0D;
      unit = "мм";
    }
    conv = new Object[2];
    conv[0] = Double.valueOf(amount);
    conv[1] = unit;
    return conv;
  }
  
  private static String localizationTransformation(String windName, String curValue)
  {
    String reportValue = curValue;
    if (!windName.equals("quantity.amount")) {
      if (curValue.matches("[+-]?[\\d]+[.,][\\d]+[eE]?[-]?[\\d]+")) {
        if (windName.equals("iterationInfo.modifier"))
        {
          String uid = curValue.substring(curValue.indexOf("=") + 1, curValue.indexOf(","));
          

          reportValue = getAuthorFullName(uid);
        }
        else if (windName.equals("thePersistInfo.modifyStamp"))
        {
          reportValue = transformDate(curValue, 2);
        }
      }
    }
    if (curValue.matches("[\\d.-]+ \\d\\d:\\d\\d:\\d\\d [\\w]+")) {
      reportValue = transformDate(curValue, 2);
    } else if (windName.equals("quantity.unit")) {
      reportValue = unitLocalizedValue(curValue);
    }
    return reportValue;
  }
  
  public static String transformDate(String strDate, int v)
  {
    Date shDate = null;
    String shDateStr = "";
    SimpleDateFormat formatD;
    if (v == 1) {
      formatD = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    } else {
      formatD = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss zzzz");
    }
    try
    {
      shDate = formatD.parse(strDate);
      shDateStr = String.format("%1$td.%1$tm.%1$ty", new Object[] { shDate });
    }
    catch (ParseException e)
    {
      System.out.println("!!!!!!!!!!!!! Date format error - " + e);
    }
    return shDateStr;
  }
  
  public static String calculateLittera(String nameLifeState, WTProperties wtprops)
  {
    String litera = "";
    if (wtprops == null) {
      try
      {
        wtprops = WTProperties.getLocalProperties();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    nameLifeState = nameLifeState.substring(nameLifeState.lastIndexOf(".") + 1);
    if (VERBOSE) {
      System.out.println("!!! drawing's life state name is - " + nameLifeState);
    }
    String strProps = wtprops.getProperty("by.iba.CAD.Littera");
    if (VERBOSE) {
      System.out.println("!!! value of by.iba.CAD.Littera is - " + strProps);
    }
    if (strProps == null) {
      strProps = "INWORK=E;RELEASED=E;";
    }
    int start = strProps.indexOf(nameLifeState + "=") + nameLifeState.length() + 1;
    litera = strProps.substring(start, start + 1);
    if (VERBOSE) {
      System.out.println("!!! calculated littera is - " + litera);
    }
    return litera;
  }

  @Deprecated
  public static WTPart findPartByNumber(String number)
  {
    try
    {
      LatestConfigSpec configSpec = new LatestConfigSpec();
      WTPart part = null;
      
      QuerySpec qs = new QuerySpec(WTPart.class);
      int idx = qs.addClassList(WTPart.class, true);
      qs.appendWhere(new SearchCondition(WTPart.class, 
        "master>number", 
        "=", 
        number.toUpperCase().trim(), 
        false), new int[] { idx });
      
      configSpec.appendSearchCriteria(qs);
      QueryResult qr = PersistenceHelper.manager.find(qs);
      if (VERBOSE) {
        System.out.println("WTParts found by Number: " + qr.size());
      }
      qr = configSpec.process(qr);
      if (VERBOSE) {
        System.out.println("filtered by ConfigSpec found " + qr.size() + " matching parts.");
      }
      while (qr.hasMoreElements())
      {
        part = (WTPart)qr.nextElement();
        if (part.isLatestIteration()) {
          return part;
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
  }

  @Deprecated
  public static WTPart findPartByName(String name)
  {
    try
    {
      LatestConfigSpec configSpec = new LatestConfigSpec();
      WTPart part = null;
      
      QuerySpec qs = new QuerySpec(WTPart.class);
      int idx = qs.addClassList(WTPart.class, true);
      qs.appendWhere(new SearchCondition(WTPart.class, 
        "master>name", 
        "=", 
        name.trim(), 
        false), new int[] { idx });
      
      configSpec.appendSearchCriteria(qs);
      QueryResult qr = PersistenceHelper.manager.find(qs);
      if (VERBOSE) {
        System.out.println("WTParts found by Name: " + qr.size());
      }
      qr = configSpec.process(qr);
      if (VERBOSE) {
        System.out.println("filtered by ConfigSpec found " + qr.size() + " matching parts.");
      }
      while (qr.hasMoreElements())
      {
        part = (WTPart)qr.nextElement();
        if (part.isLatestIteration()) {
          return part;
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return null;
  }
  
  public static boolean checkPermissionRePOZ(WTPart primaryPart)
    throws WTException
  {
    boolean res = true;
    if (primaryPart.getLifeCycleState().equals(State.toState("INWORK"))) {
      res = false;
    }
    return res;
  }
  
  public static String getCheckOutObject(WTPart primaryPart)
    throws WTException, WTPropertyVetoException
  {
    String coPart_oid = null;
    WTPart coPart = null;
    ReferenceFactory refFact = new ReferenceFactory();
    if (!WorkInProgressHelper.isCheckedOut(primaryPart))
    {
      if (WorkInProgressHelper.service.isCheckoutAllowed(primaryPart))
      {
        CheckoutLink cl = WorkInProgressHelper.service.checkout(primaryPart, WorkInProgressHelper.service.getCheckoutFolder(), "checked out by Specif ESKD");
        coPart = (WTPart)cl.getWorkingCopy();
        coPart_oid = refFact.getReferenceString(coPart);
      }
      else
      {
        throw new WTException("com.ptc.windchill.enterprise.object.multiObjectResource", "1", null);
      }
    }
    else
    {
      if (!WorkInProgressHelper.isCheckedOut(primaryPart, SessionHelper.getPrincipal())) {
        throw new WTException("Часть уже взята на изменение другим пользователем !");
      }
      if (!WorkInProgressHelper.isWorkingCopy(primaryPart))
      {
        coPart = (WTPart)WorkInProgressHelper.service.workingCopyOf(primaryPart);
        coPart_oid = refFact.getReferenceString(coPart);
      }
      else
      {
        coPart_oid = refFact.getReferenceString(primaryPart);
      }
    }
    if (VERBOSE) {
      System.out.println("!!!!!! c/o oid - " + coPart_oid);
    }
    return coPart_oid;
  }
  
  public static String padl(String initStr, String simbol, int needsize)
  {
    int delta = needsize - initStr.length();
    StringBuffer buff = new StringBuffer();
    for (int i = 0; i < delta; i++) {
      buff = buff.append(simbol);
    }
    buff = buff.append(initStr);
    return buff.toString();
  }
  
  public static String getECNForWTPart(WTPart shtampPart)
    throws WTException
  {
    String ecnNums = "";
    


    QueryResult qr = null;
    QueryResult qr1 = null;
    Vector allca = new Vector();
    WTChangeActivity2 ca = null;
    
















    qr1 = PersistenceHelper.manager.navigate(shtampPart, "theChangeActivity2", 
      ChangeRecord2.class, true);
    if ((qr1 != null) && (qr1.size() > 0)) {
      while (qr1.hasMoreElements())
      {
        ca = (WTChangeActivity2)qr1.nextElement();
        allca.add(ca);
      }
    }
    int totalca = allca.size();
    if (totalca > 0) {
      for (int i = 0; i < totalca; i++)
      {
        ca = (WTChangeActivity2)allca.get(i);
        QueryResult ecnresult = null;
        ecnresult = PersistenceHelper.manager.navigate(ca, "theChangeOrderIfc", IncludedInIfc.class, true);
        if ((ecnresult != null) && (ecnresult.size() > 0)) {
          while (ecnresult.hasMoreElements())
          {
            WTChangeOrder2 ecn = (WTChangeOrder2)ecnresult.nextElement();
            if (ecnNums == null) {
              ecnNums = ecn.getNumber();
            } else {
              ecnNums = ecnNums + " " + ecn.getNumber();
            }
          }
        }
      }
    }
    return ecnNums.trim();
  }
  
  public static void debugMap(Map map)
  {
    System.out.println("+++++++++++++++");
    Entry mapEntry;
    for (Iterator itm = map.entrySet().iterator(); itm.hasNext(); System.out.println((String)mapEntry.getKey() + "--" + (String)mapEntry.getValue())) {
      mapEntry = (Entry)itm.next();
    }
    System.out.println("+++++++++++++++");
  }
  
  public static String transformRS(String rs)
    throws StringIndexOutOfBoundsException
  {
    if ((rs.equalsIgnoreCase("Сборочные единицы")) || (rs.startsWith("RS1"))) {
      rs = "010";
    } else if ((rs.equalsIgnoreCase("Детали")) || (rs.startsWith("RS2"))) {
      rs = "020";
    } else if ((rs.equalsIgnoreCase("Стандартные изделия")) || (rs.startsWith("RS3"))) {
      rs = "030";
    } else if ((rs.equalsIgnoreCase("Прочие изделия")) || (rs.startsWith("RS4"))) {
      rs = "040";
    } else if ((rs.equalsIgnoreCase("Материалы")) || (rs.startsWith("RS5"))) {
      rs = "050";
    } else if ((rs.equalsIgnoreCase("Комплекты")) || (rs.startsWith("RS6"))) {
      rs = "060";
    } else if ((rs.equalsIgnoreCase("Документация")) || (rs.startsWith("RS0"))) {
      rs = "000";
    } else if (rs.startsWith("RS7")) {
      rs = "RS7";
    } else {
      rs = rs.substring(0, 3);
    }
    return rs;
  }
  
  public static String transformKURS(String rs)
  {
    if (rs.equalsIgnoreCase("Запасные части")) {
      rs = "010";
    } else if (rs.equalsIgnoreCase("Принадлежности")) {
      rs = "020";
    } else if (rs.equalsIgnoreCase("Инструмент")) {
      rs = "030";
    } else if (rs.equalsIgnoreCase("Сменные части")) {
      rs = "040";
    } else if (rs.equalsIgnoreCase("Монтажные части")) {
      rs = "050";
    } else if (rs.equalsIgnoreCase("Техдокументация")) {
      rs = "060";
    } else if (rs.equalsIgnoreCase("Демонтированные части")) {
      rs = "000";
    } else {
      rs = "000";
    }
    return rs;
  }
  

  private static WTChangeActivity2 checkForLitera(WTObject objForShtamp, WTChangeActivity2 caCurrent)
    throws ChangeException2, WTException
  {
    WTChangeActivity2 ca = caCurrent;
    
    Changeable2 obj = null;
    String number = "";String affNumber = null;
    if ((objForShtamp instanceof WTPart)) {
      number = ((WTPart)objForShtamp).getNumber();
    } else if ((objForShtamp instanceof EPMDocument)) {
      number = ((EPMDocument)objForShtamp).getNumber();
    } else {
      return ca;
    }
    String litera = getValueAttr("ATR_LIFECYCLE_STATE_ESKD", objForShtamp);
    if ((litera.isEmpty()) || (litera.equals("О"))) {
      return ca;
    }
    QueryResult qr = ChangeHelper2.service.getChangeablesBefore(caCurrent, false);
    if ((qr != null) && (qr.size() > 0)) {
      while (qr.hasMoreElements())
      {
        AffectedActivityData affObj = (AffectedActivityData)qr.nextElement();
        
        obj = affObj.getChangeable2();
        if ((obj instanceof WTPart))
        {
          affNumber = ((WTPart)obj).getNumber();
          if (affNumber.equals(number)) {
            break;
          }
        }
        else if ((obj instanceof EPMDocument))
        {
          affNumber = ((EPMDocument)obj).getNumber();
          if (affNumber.equals(number)) {
            break;
          }
        }
        affNumber = null;
      }
    }
    if (affNumber == null) {
      return ca;
    }
    String affLitera = getValueAttr("ATR_LIFECYCLE_STATE_ESKD", obj);
    if (affLitera.equals(litera)) {
      return ca;
    }
    System.out.println("CN - Litera is changed");
    

    QueryResult qrCA = ChangeHelper2.service.getChangingChangeActivities(obj);
    while (qrCA.hasMoreElements())
    {
      WTChangeActivity2 element = (WTChangeActivity2)qrCA.nextElement();
      ca = element;
    }
    return ca;
  }
  
  public static boolean ourObjectIsTarget(PromotionNotice promNt, Promotable objPromotable)
    throws MaturityException, WTException
  {
    boolean res = false;
    
    QueryResult qr = MaturityHelper.getService().getPromotionTargets(promNt);
    while (qr.hasMoreElements())
    {
      Promotable obj = (Promotable)qr.nextElement();
      if (obj.hashCode() == objPromotable.hashCode())
      {
        res = true;
        break;
      }
    }
    return res;
  }
  

  private static Map<String, String[]> fillRolesMap(Map<String, List<String>> mapRuleRoles, WTObject objForShtamp, String signature)
    throws WTException
  {
    Map<String, String[]> mapEmptyAuditData = new HashMap();
    
    String[] auditData = null;
    for (Iterator<Entry<String, List<String>>> itRoles = mapRuleRoles.entrySet().iterator(); itRoles.hasNext();)
    {
      Entry<String, List<String>> mapEntry = (Entry)itRoles.next();
      String iReportName = (String)mapEntry.getKey();
      
      auditData = new String[4];
      auditData[0] = getValueAttr(iReportName.replace("_WM", "") + "_BY", objForShtamp);
      auditData[1] = getValueAttr(iReportName.replace("_WM", "") + "_ON", objForShtamp);
      auditData[2] = "";
      if (!auditData[0].equals("")) {
        auditData[3] = (signature == null ? auditData[0] : signature);
      } else {
        auditData[3] = "";
      }
      mapEmptyAuditData.put(iReportName, auditData);
    }
    return mapEmptyAuditData;
  }
  

  public static String transliterate(String string)
  {
    StringBuilder transliteratedString = new StringBuilder();
    for (int i = 0; i < string.length(); i++)
    {
      Character ch = Character.valueOf(string.charAt(i));
      String charFromMap = (String)charMap.get(ch);
      if (charFromMap == null) {
        transliteratedString.append(ch);
      } else {
        transliteratedString.append(charFromMap);
      }
    }
    return transliteratedString.toString();
  }
  
  public static void writeToFile(String fileName, String text)
  {
    if (VERBOSE) {
      System.out.println("Запись в файл - " + fileName);
    }
    File file = new File(fileName);
    try
    {
      if (!file.exists()) {
        file.createNewFile();
      }
      FileWriter out = new FileWriter(file.getAbsoluteFile(), true);
      try
      {
        out.write(text + "\n");
      }
      finally
      {
        out.close();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static String calculatePrimRefDes(WTPartUsageLink uLink)
    throws WTException
  {
    String primRefDes = "";String curRefDes = "";
    String strCur = "";String strBuff = "";
    

    SortedMap<String, String> mapRefDes = new TreeMap();
    
    QueryResult occQr = OccurrenceHelper.service.getUsesOccurrences(uLink);
    while (occQr.hasMoreElements())
    {
      PartUsesOccurrence po = (PartUsesOccurrence)occQr.nextElement();
      curRefDes = po.getName();
      if (curRefDes != null)
      {
        StringBuffer strForSort = new StringBuffer();
        String[] mas = curRefDes.split("\\d");
        if (mas.length > 0)
        {
          String symbolRefDes = curRefDes.split("\\d")[0];
          strForSort.append(symbolRefDes);
          String digitRefDes = curRefDes.replace(symbolRefDes, "");
          mas = digitRefDes.split("\\D");
          for (int i = 0; i < mas.length;)
          {
            strForSort.append(padl(mas[i], "0", 5));
            i++;
          }
        }
        else
        {
          strForSort.append(curRefDes);
        }
        mapRefDes.put(strForSort.toString(), curRefDes);
      }
    }
    Set s = mapRefDes.entrySet();
    Iterator<Entry<String, String>> i = s.iterator();
    while (i.hasNext())
    {
      Entry<String, String> m = (Entry)i.next();
      
      curRefDes = (String)m.getValue();
      if (primRefDes.isEmpty())
      {
        primRefDes = curRefDes;
      }
      else
      {
        int intCur = getLastNumberFromRefDes(curRefDes);
        strCur = curRefDes.substring(0, curRefDes.lastIndexOf(intCur));
        

        int intBuff = getLastNumberFromRefDes(primRefDes);
        

        int intKoska = primRefDes.lastIndexOf(",");
        int intDefis = primRefDes.lastIndexOf("-");
        if (((intKoska == -1 ? 1 : 0) & (intDefis == -1 ? 1 : 0)) != 0) {
          strBuff = primRefDes.substring(0, primRefDes.lastIndexOf(intBuff));
        } else if (intKoska == -1) {
          strBuff = primRefDes.substring(intDefis, primRefDes.lastIndexOf(intBuff));
        } else if (intDefis == -1) {
          strBuff = primRefDes.substring(intKoska, primRefDes.lastIndexOf(intBuff));
        } else if (intKoska > intDefis) {
          strBuff = primRefDes.substring(intKoska, primRefDes.lastIndexOf(intBuff));
        } else if (intDefis > intKoska) {
          strBuff = primRefDes.substring(intDefis, primRefDes.lastIndexOf(intBuff));
        }
        if (intBuff + 1 == intCur)
        {
          if (strBuff.contains("-")) {
            primRefDes = primRefDes.replace(strBuff + intBuff, "- " + strCur + intCur);
          } else {
            primRefDes = primRefDes + "- " + strCur + intCur;
          }
        }
        else {
          primRefDes = primRefDes + ", " + strCur + intCur;
        }
      }
    }
    StringTokenizer st = new StringTokenizer(primRefDes, ",");
    StringBuffer sb = new StringBuffer();
    while (st.hasMoreTokens())
    {
      String subStr = st.nextToken();
      if (subStr.contains("-"))
      {
        int first = getLastNumberFromRefDes(subStr.substring(0, subStr.indexOf("-")));
        int second = getLastNumberFromRefDes(subStr.substring(subStr.indexOf("-") + 1));
        if (second - first == 1)
        {
          if (sb.length() == 0) {
            sb = sb.append(subStr.replace("-", ","));
          } else {
            sb = sb.append(",").append(subStr.replace("-", ","));
          }
        }
        else if (sb.length() == 0) {
          sb = sb.append(subStr);
        } else {
          sb = sb.append(",").append(subStr);
        }
      }
      else if (sb.length() == 0)
      {
        sb = sb.append(subStr);
      }
      else
      {
        sb = sb.append(",").append(subStr);
      }
    }
    primRefDes = sb.toString();
    primRefDes = primRefDes.replace("-", "...");
    
    return primRefDes;
  }
  
  private static int getLastNumberFromRefDes(String refdes)
  {
    int result = 0;
    String[] mas = refdes.split("\\D");
    
    String strNumber = mas[(mas.length - 1)];
    result = new Integer(strNumber).intValue();
    return result;
  }
  
  public static void main(String[] args)
  {
    Map<String, Double> mChars = new HashMap();
    mChars.put("Q", Double.valueOf(0.5D));
    mChars.put("Y", Double.valueOf(0.33D));
    mChars.put(".", Double.valueOf(0.1D));
    mChars.put("П", Double.valueOf(0.5D));
    mChars.put("±", Double.valueOf(0.2D));
    mChars.put("°", Double.valueOf(0.06D));
    
    String str = "QYП.";
    StringBuffer sb = new StringBuffer();
    

    double result = 0.0D;
    for (int i = 0; i < str.length();)
    {
      char ch = str.charAt(i++);
      System.out.println(ch);
      Double temp = (Double)mChars.get(Character.toString(ch));
      result += (temp != null ? temp.doubleValue() : 0.0D);
      if (result > 1.35D) {
        break;
      }
      sb.append(ch);
      System.out.println(sb.toString() + ",  result - " + result);
    }
  }
  
  public static String getAbreviatedDocType(String docType)
  {
    String abr = "";
    if (docType.equals("Чертеж детали")) {
      abr = "";
    } else if (docType.equals("Сборочный чертеж")) {
      abr = "СБ";
    } else if (docType.equals("Чертеж общего вида")) {
      abr = "ВО";
    } else if (docType.equals("Габаритный чертеж")) {
      abr = "ГЧ";
    } else if (docType.equals("Электромонтажный чертеж")) {
      abr = "МЭ";
    } else if (docType.equals("Монтажный чертеж")) {
      abr = "МЧ";
    } else if (docType.equals("Упаковочный чертеж")) {
      abr = "УЧ";
    } else if (docType.equals("Эскизный чертеж")) {
      abr = "?";
    } else if (docType.equals("Чертеж технологический")) {
      abr = "?";
    } else if (docType.equals("Теоретический чертеж")) {
      abr = "ТЧ";
    } else if (docType.equals("Отливка ЛЗ")) {
      abr = "ЛЗ";
    } else if (docType.equals("Отливка ЛВМ")) {
      abr = "ЛВМ";
    } else if (docType.equals("Отливка ЛВЖ")) {
      abr = "ЛВЖ";
    } else if (docType.equals("Отливка ЛД")) {
      abr = "ЛД";
    } else if (docType.equals("Отливка ЛГМ")) {
      abr = "ЛГМ";
    } else if (docType.equals("Отливка ЛК")) {
      abr = "ЛК";
    }
    return abr;
  }
  
  public static String getTimeZoneDate(long timeGMT)
  {
    String time = "";
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(timeGMT);
    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    format.setTimeZone(TimeZone.getTimeZone("Asia/Riyadh"));
    time = format.format(calendar.getTime());
    
    return time;
  }
}
