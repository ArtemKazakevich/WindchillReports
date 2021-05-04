<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.pds.StatementSpec" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.*" %>
<%@ page import="wt.inf.container.WTContainer" %>
<%@ page import="wt.pdmlink.PDMLinkProduct" %>
<%@ page import="wt.query.QueryException" %>
<%@ page import="wt.inf.library.WTLibrary" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.org.WTGroup" %>
<%@ page import="wt.inf.team.*" %>
<%@ page import="wt.project.Role" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Test</title>
</head>
<body>

<%
    List<WTUser> users = new ArrayList<>();
    String name = "СГТ - Литьё";
    String oid = "";
    ReferenceFactory refFact = new ReferenceFactory();

    List<WTContainer> containers = getAllContainersInWindchill();

    for (WTContainer container : containers) {

        if (container.getName().equals(name)) {
            oid = refFact.getReferenceString(container);

            ContainerTeamManaged teamManaged = (ContainerTeamManaged) container;
            Enumeration localEnumeration = ContainerTeamServerHelper.service.findUsers(teamManaged);

            while (localEnumeration.hasMoreElements()) {
                users.add((WTUser) localEnumeration.nextElement());
            }
        }

        /////////////////////////////////////////////////

//        ContainerTeamManaged teamManaged = (ContainerTeamManaged) container;
//        ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(teamManaged);
//        Vector<?> vector = team.getMembers();
//
//        for (Object object : vector) {
//             if (object instanceof WTUser) {
//                  WTUser user = (WTUser) object;
//                  users.add(user);
//             }
//        }

    }

    Collections.sort(users, new Comparator<WTUser>() {
        @Override
        public int compare(WTUser o1, WTUser o2) {
            return o1.getFullName().compareTo(o2.getFullName());
        }
    });


    for (WTUser u : users) {
%>
<p><%=oid%></p>
<p>
    <%=u.getFullName()%>
</p>

<%
    }
%>

<%!
    private static ArrayList<WTContainer> getAllContainersInWindchill() {
        List<WTContainer> containers = new ArrayList<WTContainer>();
        try {
            QuerySpec querySpec = new QuerySpec(WTContainer.class);
            QueryResult qr = PersistenceHelper.manager.find(querySpec);
            while (qr.hasMoreElements()) {
                Object object = qr.nextElement();
                if (object instanceof PDMLinkProduct || object instanceof WTLibrary) {
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
