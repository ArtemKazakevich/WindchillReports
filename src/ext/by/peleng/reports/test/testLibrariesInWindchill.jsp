<%@ page import="wt.inf.container.WTContainer" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.inf.library.WTLibrary" %>
<%@ page import="wt.query.QueryException" %>
<%@ page import="wt.util.WTException" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
    <title>TestJSP</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>
    <div>
        <table>
            <caption>Список библиотек в Windchill</caption>
            <tr>
                <th>Наименование</th>
                <th>Содержимое папки</th>
            </tr>
            <c:set var="libraries" value="<%=getAllContainersInWindchill()%>"/>
            <c:forEach items="${libraries}" var="container">
                <tr>
                    <form action="library.jsp" method="post">
                        <td>${container.name}</td>
                        <td><input type="submit" value="Look"></td>
                        <input type="hidden" name="library" value="${container.name}">
                    </form>
                </tr>
            </c:forEach>
        </table>
    </div>



<%!
    @SuppressWarnings("deprecation")
    private static ArrayList<WTContainer> getAllContainersInWindchill() {
        List<WTContainer> containers = new ArrayList<WTContainer>();
        try {
            QuerySpec querySpec = new QuerySpec(WTContainer.class);
            QueryResult qr = PersistenceHelper.manager.find(querySpec);
            while (qr.hasMoreElements()) {
                Object object = qr.nextElement();
                if (object instanceof WTLibrary) {
                    containers.add((WTContainer) object);
                }
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }

        return (ArrayList<WTContainer>) containers;
    }
%>
</body>
</html>