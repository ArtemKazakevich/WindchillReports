<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="wt.org.AttributeHolder" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.IOException" %>
<%@ page import="wt.util.WTPropertyVetoException" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

<%
    //считываю с файла
    String csvFile = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\netmarkets\\jsp\\by\\peleng\\reports\\listUserEmail\\Clean_AD_Users_new.csv";
    String line = "";
    String cvsSplitBy = ",";
    String separator = ";";

    Map<String, String> mapUsersCSV = new HashMap<>();

    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

        while ((line = br.readLine()) != null) {

            List<String> list = Arrays.asList(line.split(cvsSplitBy));

            for (String str: list) {
                List<String> s = Arrays.asList(str.split(separator));
                mapUsersCSV.put(s.get(0), s.get(1));
            }
        }

    } catch (IOException e) {
        e.printStackTrace();
    }

    //считываю с Windchill
    QuerySpec querySpec = new QuerySpec(WTUser.class);
    QueryResult qr = PersistenceHelper.manager.find(querySpec);

    while (qr.hasMoreElements()) {

        String regular = "\\d+";
        WTUser user = (WTUser) qr.nextElement();
        AttributeHolder attrh = user.getAttributes();
        Enumeration enattrvalue = attrh.getValues("uid");

        while (enattrvalue.hasMoreElements()) {

            String serviceNumber = (String) enattrvalue.nextElement();

            if (serviceNumber.matches(regular)) {

                 //сравниваю табельные номера и перезаписываю email
                for (Map.Entry<String, String> itemCSV : mapUsersCSV.entrySet()) {

                     if (itemCSV.getKey().equals(serviceNumber)) {
                         try {
                             user.setEMail(itemCSV.getValue());
                             wt.fc.PersistenceHelper.manager.save(user);
                         } catch (WTException e) {
                             e.printStackTrace();
                         } catch (WTPropertyVetoException e) {
                             e.printStackTrace();
                         }
                     }
                }
            }
        }
    }

%>

</body>
</html>
