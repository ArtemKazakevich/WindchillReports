<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.pds.StatementSpec" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test</title>
</head>
<body>

<%
    //    List<WTUser> users = findUserByLastName("**Трусов**");
    List<WTUser> users = new ArrayList<>();

    QuerySpec qs = new QuerySpec(WTUser.class);
    qs.appendWhere( new SearchCondition(WTUser.class, WTUser.NAME, SearchCondition.LIKE, "%", false), new int[] {});
    QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);

    while (qr.hasMoreElements()) {
        WTUser user = (WTUser) qr.nextElement();
        users.add(user);
    }

    Collections.sort(users, new Comparator<WTUser>() {
        @Override
        public int compare(WTUser o1, WTUser o2) {
            return o1.getFullName().compareTo(o2.getFullName());
        }
    });

    for (WTUser u : users) {
%>

<p>
    <%=u%>
</p>

<%
    }
%>

<%!
    private static ArrayList<WTUser> findUserByLastName(String userLastName) {
        ArrayList<WTUser> users = new ArrayList<WTUser>();
        try {
            QuerySpec qs = new QuerySpec(WTUser.class);
            qs.appendWhere(new SearchCondition(WTUser.class, WTUser.LAST, SearchCondition.LIKE, userLastName.replace("*", "%"), false), null);
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

</body>
</html>
