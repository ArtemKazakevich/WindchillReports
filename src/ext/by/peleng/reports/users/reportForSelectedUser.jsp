<%@ page import="wt.util.WTException" %>
<%@ page import="wt.inf.container.WTContainer" %>
<%@ page import="wt.inf.team.ContainerTeam" %>
<%@ page import="wt.inf.team.ContainerTeamHelper" %>
<%@ page import="wt.inf.team.ContainerTeamManaged" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="wt.project.Role" %>
<%@ page import="wt.org.WTPrincipalReference" %>
<%@ page import="wt.inf.team.StandardContainerTeamService" %>
<%@ page import="wt.org.WTGroup" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="wt.org.OrganizationServicesHelper" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.QueryException" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.pdmlink.PDMLinkProduct" %>
<%@ page import="wt.inf.library.WTLibrary" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
    <head>
        <title>TestJSP</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="reportForSelectedUserStyle.css">
    </head>

    <body>
    <%
        WTUser selectedUser = getUserByName(request.getParameter("selectedUser"));
        List<WTContainer> containers = getAllContainersInWindchill();
        Map<WTContainer, HashSet<Role>> containersWithSelectedUser = new HashMap<WTContainer, HashSet<Role>>();

        for (WTContainer container : containers) {
            ContainerTeamManaged teamManaged = (ContainerTeamManaged) container;
            Set<Role> roles = getContainerTeamRolesWithSelectedUser(teamManaged, selectedUser);
            if (roles.size() > 0) {
                containersWithSelectedUser.put(container, (HashSet<Role>) roles);
            }
        }

        if (!containersWithSelectedUser.isEmpty()) {
            %>
                <table>
                    <caption>Изделия, к которым имеет доступ <%=selectedUser.getFullName().replace(",", "")%></caption>
                    <tr>
                        <th>Изделие</th>
                        <th>Роль</th>
                    </tr>
            <%
					wt.fc.ReferenceFactory refFact = new wt.fc.ReferenceFactory();
                    for (Map.Entry<WTContainer, HashSet<Role>> entries : containersWithSelectedUser.entrySet()) {
                        WTContainer container = entries.getKey();
                        HashSet<Role> roles = entries.getValue();
                        boolean isFirstElement = true;

                       for (Role role : roles) {
                           if (isFirstElement) {
                                %>
                                    <tr>
                                        <td>
                                            <a href="https://windchill.peleng.by/Windchill/app/#ptc1/library/listTeam?oid=<%=refFact.getReferenceString(container)%>" target="_blank"><%=container.getName()%></a>
                                        </td>
                               <%
                                isFirstElement = false;
                           } else {
                                %>
                                    <tr>
                                        <td></td>
                                <%
                           }
                            %>
                                        <td><%=role.getDisplay()%></td>
                                    </tr>
                            <%
                       }
                    }
            %>

                </table>

                <form method="get" action="search.jsp">
                    <button><span>На главную</span></button>
                </form>

            <%
        } else {
            %>

            <h3>Пользователь <b><%=selectedUser.getFullName().replace(",", "")%></b> не имеет доступа к контекстам.</h3>

            <form method="get" action="search.jsp">
                <button><span>На главную</span></button>
            </form>

            <%
        }
    %>

    <%!
        private static HashSet<Role> getContainerTeamRolesWithSelectedUser(ContainerTeamManaged teamManaged, WTUser user) {
            HashSet<Role> roles = new HashSet<Role>();

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
                            if (getContainerUsersByRole(teamManaged, role).contains(user)) {
                                roles.add(role);
                            }
                        }
                    }
                }
            } catch (WTException e) {

            }

            return roles;
        }
    %>

    <%!
        private static HashSet<WTUser> getContainerUsersByRole(ContainerTeamManaged teamManaged, Role role) {
            HashSet<WTUser> users = new HashSet<WTUser>();

            try {
                if (teamManaged != null) {
                    ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(teamManaged);
                    StandardContainerTeamService standardContainer = StandardContainerTeamService.newStandardContainerTeamService();
                    WTGroup group = standardContainer.findContainerTeamGroup(team, ContainerTeamHelper.ROLE_GROUPS, role.toString());
                    if (group != null) {
                        Enumeration<?> enumeration = OrganizationServicesHelper.manager.members(group, false, true);
                        while (enumeration.hasMoreElements()) {
                            WTPrincipalReference principalReference = WTPrincipalReference.newWTPrincipalReference((WTPrincipal) enumeration.nextElement());
                            WTPrincipal principal = principalReference.getPrincipal();
                            if (principal instanceof WTUser) {
                                users.add((WTUser) principal);
                            } else if (principal instanceof WTGroup) {
                                users.addAll(getGroupMembersOfUser((WTGroup) principal));
                            }
                        }
                    }
                }
            } catch (WTException e) {

            }

            return users;
        }
    %>

    <%!
        private static HashSet<WTUser> getGroupMembersOfUser(WTGroup group) {
            HashSet<WTUser> members = new HashSet<WTUser>();

            try {
                Enumeration<?> member = group.members();
                while (member.hasMoreElements()) {
                    WTPrincipal principal = (WTPrincipal) member.nextElement();
                    if (principal instanceof WTUser) {
                        members.add((WTUser) principal);
                    } else if (principal instanceof WTGroup) {
                        members.addAll(getGroupMembersOfUser((WTGroup) principal));
                    }
                }
            } catch (WTException e) {

            }

            return members;
        }
    %>

    <%!
        @SuppressWarnings("deprecation")
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
        @SuppressWarnings("deprecation")
        private static WTUser getUserByName(String userName) {
            try {
                return OrganizationServicesHelper.manager.getUser(userName);
            } catch (WTException e) {

            }
            return null;
        }
    %>
    </body>
</html>