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

<%--
Процесс удаления длится долго, около 1 часа
--%>

<%
    List<WTUser> disabledUser = findDeleteUsersInDataBase();
    List<WTContainer> containers = getAllContainersInWindchill();

    ReferenceFactory refFact = new ReferenceFactory();
    String oidUser = "";
    String oidContainer = "";

    for (WTUser selectedUser : disabledUser) {

        oidUser = refFact.getReferenceString(selectedUser);
        System.out.println("Старт удаления " + selectedUser.getFullName());

        for (WTContainer container : containers) {

            oidContainer = refFact.getReferenceString(container);

            Transaction localTransaction = new Transaction();

            try {
                localTransaction.start();

                ContainerTeam localContainerTeam = ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged) container);

                ContainerTeamManaged teamManaged = (ContainerTeamManaged) container;
                List<Role> roles = getAllRolesInContainer(teamManaged);

                for (Role r : roles) {
                    localContainerTeam.deletePrincipalTarget(r, selectedUser);
                }

                localTransaction.commit();
                localTransaction = null;

%>

<p>
    <%=oidContainer%> /-/ <%=container.getName()%>
</p>
<p>
    <%=oidUser%> /-/ <%=selectedUser.getFullName()%>
</p>
<p></p>

<%
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (localTransaction != null) {
                    System.out.println("Error delete");
                    localTransaction.rollback();
                }
            }
        }

        System.out.println("Good Delete");
    }

    System.out.println("Finish Delete");
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
