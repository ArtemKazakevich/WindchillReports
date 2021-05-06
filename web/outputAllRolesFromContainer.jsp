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
    String name = "7084 - Сосна";
    String oid = "";
    ReferenceFactory refFact = new ReferenceFactory();

    List<Role> roles = new ArrayList<>();
    List<WTContainer> containers = getAllContainersInWindchill();

    for (WTContainer container : containers) {

        if (container.getName().equals(name)) {
            oid = refFact.getReferenceString(container);

            ContainerTeamManaged teamManaged = (ContainerTeamManaged) container;

            roles = getAllRolesInContainer(teamManaged);
        }
    }

    Collections.sort(roles, new Comparator<Role>() {
        @Override
        public int compare(Role o1, Role o2) {
            return o1.getDisplay(new Locale("ru", "RU")).compareTo(o2.getDisplay(new Locale("ru", "RU")));
        }
    });


    for (Role u : roles) {
%>
<p><%=oid%>
</p>
<p>
    <%=u.getDisplay(new Locale("ru", "RU"))%>
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

<%!
    private static List<Role> getAllRolesInContainer(ContainerTeamManaged teamManaged) {
        List<Role> roles = new ArrayList<>();

        try {
            if (teamManaged != null) {
                ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(teamManaged);
                Vector<?> vector = team.getRoles();
                if (vector == null) {
                    return roles;
                }

                for (Object obj : vector) {
                    if (obj instanceof Role) {
                        Role role = (Role) obj;
                        roles.add(role);
                    }
                }
            }
        } catch (WTException e) {
            e.printStackTrace();
        }

        return roles;
    }
%>

</body>
</html>
