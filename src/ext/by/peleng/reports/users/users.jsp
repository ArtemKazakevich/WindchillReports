<%@ page import="wt.org.WTUser" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.pds.StatementSpec" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
    <head>
        <title>TestJSP</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="usersStyle.css">
    </head>

    <body>
        <%
            String userLastName = request.getParameter("lastName");
            List<WTUser> users = findUserByLastName(userLastName);

            if (!users.isEmpty()) {
        %>
        <div>
            <form method = "GET" action="reportForSelectedUser.jsp">
                <select name="selectedUser" size="10" required>
                    <%
                for (WTUser user : users) {
            %>
                    <option value="<%=user.getName()%>"><%=user.getFullName().replace(",", "")%></option>
            <%
                }
            %>
                </select>
                <br>
                <button><span>Ввод </span></button>
            </form>
        </div>
        <%
            } else {
                %>

                    <form method="get" action="search.jsp">
                        <h2>Пользователя не существует.</h2>
                        <button><span>На главную </span></button>
                    </form>

                <%
            }

        %>

    </body>

    <%!
        private static ArrayList<WTUser> findUserByLastName(String userLastName) {
            ArrayList<WTUser>  users = new ArrayList<WTUser>();
            try {
                QuerySpec qs = new QuerySpec(WTUser.class);
                qs.appendWhere(new SearchCondition(WTUser.class, WTUser.LAST, SearchCondition.LIKE, userLastName.replace("*","%"),false), null);
                QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);

                while (qr.hasMoreElements()) {
                    WTUser user = (WTUser) qr.nextElement();
                    if (!user.isDisabled()) {
                        users.add(user);
                    }

                }
            } catch (WTException e) {
                e.printStackTrace();
            }

            return users;
        }
    %>

</html>