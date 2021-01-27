<%@ page import="ext.by.peleng.reports.productStructure.Solution" %>
<%@ page import="ext.by.peleng.reports.productStructure.TreeNode" %>
<%@ page import="ext.by.peleng.reports.productStructure.Remark" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>TestJSP</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<%
    String oid = request.getParameter("oid");

    TreeNode tree = null;

    try {

        tree = Solution.start(oid);

    } catch (WTException e) {

        e.printStackTrace();

    }

    if (tree == null) {

%>

<p align="center"><strong>Отсутствует состав</strong></p>

<%

    } else {

        ArrayList<String> remarks = (ArrayList<String>) Remark.getRemarks();

        if (remarks.size() != 0) {

%>

<p align="center"><strong>Замечания по структуре изделия <%=tree.getWtPart().getNumber()%></strong></p>

<%

            for (String str : remarks) {

%>

<%=str%><br>

<%

            }

        } else {

%>

<p align="center"><strong>Замечаний по структуре изделия <%=tree.getWtPart().getNumber()%> нет</strong></p>

<%

        }

    }

%>

</html>
