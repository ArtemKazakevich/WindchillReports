<%@ page import="wt.inf.library.WTLibrary" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.QueryException" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.folder.SubFolder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="wt.folder.FolderHelper" %>
<%@ page import="wt.folder.Cabinet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
    <title>TestJSP</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>
<%
    String libraryName = request.getParameter("library");
    WTLibrary wtLibrary = getWTLibrary(libraryName);
    List<SubFolder> subFolders = getSubFolders(wtLibrary.getDefaultCabinet());
%>

    <c:set var="libraryName" value="<%=libraryName%>"/>
    <c:set var="subFolders" value="<%=subFolders%>"/>

<div>
    <table>
        <caption>Список папок в библиотеке "${libraryName}"</caption>
        <tr>
            <th>Наименование</th>
            <th>Содержимое папки</th>
        </tr>
        <c:forEach items="${subFolders}" var="entry">
            <tr>
                <form action="subfolder.jsp" method="post">
                    <td>${entry.name}</td>
                    <td><input type="submit" value="Look"></td>
                    <input type="hidden" name="subfolderName" value="${entry.name}">
                </form>
            </tr>
        </c:forEach>
    </table>
</div>

<%!
    @SuppressWarnings("deprecation")
    private static WTLibrary getWTLibrary(String libraryName) {
        WTLibrary wtLibrary = null;

        try {
            QuerySpec criteria = new QuerySpec(WTLibrary.class);
            criteria.appendWhere(new SearchCondition(WTLibrary.class, WTLibrary.NAME, SearchCondition.EQUAL, libraryName, false));
            QueryResult results = PersistenceHelper.manager.find(criteria);

            if (results.hasMoreElements()) {
                wtLibrary = (WTLibrary) results.nextElement();
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }

        return wtLibrary;
    }
%>
<%!
    @SuppressWarnings("deprecation")
    private static ArrayList<SubFolder> getSubFolders(Cabinet cabinet) {
        List<SubFolder> subFolders = new ArrayList<SubFolder>();

        try {
            QueryResult results = FolderHelper.service.findSubFolders(cabinet);

            while (results.hasMoreElements()) {
                subFolders.add((SubFolder) results.nextElement());
            }
        } catch (WTException e) {
            e.printStackTrace();
        }

        return (ArrayList<SubFolder>) subFolders;
    }
%>
</body>
</html>