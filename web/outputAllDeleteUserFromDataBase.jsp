<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.pds.StatementSpec" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.*" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.inf.container.WTContainer" %>
<%@ page import="wt.pom.Transaction" %>
<%@ page import="wt.inf.team.*" %>
<%@ page import="wt.project.Role" %>
<%@ page import="wt.query.QueryException" %>
<%@ page import="wt.pdmlink.PDMLinkProduct" %>
<%@ page import="wt.inf.library.WTLibrary" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Delete User</title>
</head>
<body>

<%
    List<WTUser> disabledUser = findDeleteUsersInDataBase();

    ReferenceFactory refFact = new ReferenceFactory();
    String oidUser = "";

    for (WTUser selectedUser : disabledUser) {
        oidUser = refFact.getReferenceString(selectedUser);

%>
<p>
    <a href="https://wch.peleng.jsc.local/Windchill/app/#ptc1/tcomp/infoPage?oid=<%=oidUser%>" target="_blank"><%=oidUser%> /-/ <%=selectedUser.getFullName()%></a>
</p>
<p></p>

<%
    }
%>

<%!
    private static ArrayList<WTUser> findDeleteUsersInDataBase() {
        ArrayList<WTUser> users = new ArrayList<WTUser>();
        try {
            QuerySpec qs = new QuerySpec(WTUser.class);
            qs.appendWhere(new SearchCondition(WTUser.class, WTUser.NAME, SearchCondition.LIKE, "%", false), new int[]{});
            QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);

            while (qr.hasMoreElements()) {
                WTUser user = (WTUser) qr.nextElement();

                if (user.isDisabled()) {
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
