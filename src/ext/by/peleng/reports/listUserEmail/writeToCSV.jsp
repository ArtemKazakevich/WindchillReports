<%@page import="java.util.Enumeration" %>
<%@page import="wt.org.AttributeHolder" %>
<%@page import="wt.fc.QueryResult" %>
<%@page import="wt.fc.PersistenceHelper" %>
<%@page import="wt.query.QuerySpec" %>
<%@page import="wt.org.WTUser" %>
<%@ page import="java.io.*" %>
<%@ page import="wt.util.WTException" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">


<html>
<head>
    <title>testPublish</title>
</head>
<body>
<%

    QuerySpec querySpec = new QuerySpec(WTUser.class);
    QueryResult qr = PersistenceHelper.manager.find(querySpec);
    String regular = "\\d+";

    try(FileOutputStream writer = new FileOutputStream(new File("D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\netmarkets\\jsp\\by\\peleng\\reports\\listUserEmail\\listUserEmail.csv"))){
        PrintStream printStream = new PrintStream(writer);

        while (qr.hasMoreElements()) {
            WTUser user = (WTUser) qr.nextElement();
            AttributeHolder attrh = user.getAttributes();
            Enumeration enattrvalue = attrh.getValues("uid");

            while (enattrvalue.hasMoreElements()) {
                String serviceNumber = (String) enattrvalue.nextElement();

                if (serviceNumber.matches(regular)) {

                    try {
                        printStream.println(serviceNumber + ',' + user.getPrincipalDisplayIdentifier() + ',' + user.getEMail());
                    } catch (WTException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    } catch (IOException ex) {
        ex.printStackTrace();
    }

%>

</body>
</html>