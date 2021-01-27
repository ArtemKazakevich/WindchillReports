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
<%@ page import="wt.folder.CabinetReference" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.fc.WTReference" %>
<%@ page import="wt.folder.Folder" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="wt.doc.WTDocument" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
    <title>TestJSP</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>
<%
    String subfolderName = request.getParameter("subfolderName");
    SubFolder subFolder = getSubFolder(subfolderName);
    Map<String, ArrayList<Object>> subFolders = getSubFolders(subFolder);

%>
<c:set var="subFolderName" value="<%=subfolderName%>" scope="request"/>
<c:set var="subFolders" value="<%=subFolders%>" scope="request"/>

<div>
    <table>
        <caption>Список папок в subFolder "${subFolderName}"</caption>
        <tr>
            <th>Наименование</th>
            <th>Содержимое папки</th>
        </tr>
        <c:forEach items="${subFolders}" var="entry">
            <c:set var="entryVal" value="${entry.value}" scope="request"></c:set>
            <c:choose>
                <c:when test="${entry.key  == 'Doc'}" >
                    <c:forEach items="${entryVal}" var="oneDoc">
                        <tr>
                            <form action="subfolder.jsp" method="post">
                                <td>${oneDoc}</td>
                                <td><input type="submit" value="Doc"></td>
                                <input type="hidden" name="subfolderName" value="${oneDoc}" required>
                            </form>
                        </tr>
                    </c:forEach>
                </c:when>

                <c:when test="${entry.key == 'Sub'}" >
                    <%--<c:set var="docs" value="${entry.value}" scope="request"></c:set>--%>
                    <c:forEach items="${entryVal}" var="oneSub">
                        <tr>
                            <form action="subfolder.jsp" method="post">
                                <td>${oneSub}</td>
                                <td><input type="submit" value="Sub"></td>
                                <input type="hidden" name="subfolderName" value="${oneSub}" required>
                            </form>
                        </tr>
                    </c:forEach>
                </c:when>
                
            </c:choose>
        </c:forEach>
    </table>
</div>

<%!
    @SuppressWarnings("deprecation")
    private static SubFolder getSubFolder(String subFolderName) {
        SubFolder subFolder = null;

        try {
            QuerySpec criteria = new QuerySpec(SubFolder.class);
            criteria.appendWhere(new SearchCondition(SubFolder.class, SubFolder.NAME, SearchCondition.EQUAL, subFolderName, false));
            QueryResult results = PersistenceHelper.manager.find(criteria);

            if (results.hasMoreElements()) {
                subFolder = (SubFolder) results.nextElement();
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }

        return subFolder;
    }
%>
<%!
    @SuppressWarnings("deprecation")
    private static Map<String, ArrayList<Object>> getSubFolders(Folder folder) {
        Map<String, ArrayList<Object>> targets = new HashMap<String, ArrayList<Object>>();
        List<Object> listWTDoc = new ArrayList<Object>();
        List<Object> listSubFolders = new ArrayList<Object>();

        try {
            QueryResult results = FolderHelper.service.findFolderContents(folder);

            while (results.hasMoreElements()) {
                Object object = results.nextElement();
                if (object instanceof WTDocument) {
                    listWTDoc.add((WTDocument) object);
                } else if (object instanceof SubFolder) {
                    listSubFolders.add((SubFolder) object);
                }
            }
        } catch (WTException e) {
            e.printStackTrace();
        }

        targets.put("Doc", (ArrayList<Object>) listWTDoc);
        targets.put("Sub", (ArrayList<Object>) listSubFolders);

        return targets;
    }
%>
</body>
</html>