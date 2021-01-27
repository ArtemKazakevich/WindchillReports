
<%@page import="java.util.Enumeration"%>
<%@page import="wt.org.AttributeHolder"%>
<%@page import="wt.fc.QueryResult"%>
<%@page import="wt.fc.PersistenceHelper"%>
<%@page import="wt.query.QuerySpec"%>
<%@page import="wt.org.WTUser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">



<html>
<head>
    <title>testPublish</title>
</head>
<body>

<%
    QuerySpec querySpec = new QuerySpec(WTUser.class);
    QueryResult qr = PersistenceHelper.manager.find(querySpec);
    while (qr.hasMoreElements()) {
        WTUser user = (WTUser) qr.nextElement();
%>

<p>
    <%= user.getPrincipalDisplayIdentifier()%>
    <%= "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"%>

    <%= user.getEMail()%>
    <%= "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"%>

    <%
        AttributeHolder attrh = user.getAttributes();
        Enumeration enattrvalue = attrh.getValues("uid");
        while(enattrvalue.hasMoreElements()){
    %>
    <%= enattrvalue.nextElement()%>
    <%= "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"%>
    <%
        }
    %>

</p>

<%
    }
%>

</body>
</html>