<%@ page import="wt.util.WTException" %>
<%@ page import="wt.inf.container.WTContainer" %>
<%@ page import="wt.inf.team.ContainerTeam" %>
<%@ page import="wt.inf.team.ContainerTeamHelper" %>
<%@ page import="wt.inf.team.ContainerTeamManaged" %>
<%@ page import="wt.project.Role" %>
<%@ page import="wt.org.WTPrincipalReference" %>
<%@ page import="wt.inf.team.StandardContainerTeamService" %>
<%@ page import="wt.org.WTGroup" %>
<%@ page import="wt.org.OrganizationServicesHelper" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.org.WTUser" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.pdmlink.PDMLinkProduct" %>
<%@ page import="wt.inf.library.WTLibrary" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Product search</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="displayLeadingTechnologistsStyle.css">
</head>

<body>
<%

    boolean flag = true;
    Role role;
    WTUser user;
    Map<String, String> users = new HashMap<String, String>();
    String leadProcessEngineerTheProduct = "Ведущий технолог по изделию";

    String numberProduct = request.getParameter("numberProduct");
    List<WTContainer> containers = getAllContainersInWindchill();

    for (WTContainer c : containers) {

         if (c.getName().contains(numberProduct)) {
              role = getRoleFromContainer(c, leadProcessEngineerTheProduct);

              ContainerTeamManaged teamManaged = (ContainerTeamManaged) c;
              user = getContainerUsersByRole(teamManaged, role);

              if (user != null) {
                  users.put(c.getName(), user.getFullName().replace(",", ""));
              } else {
                  users.put(c.getName(), "В данном изделии нет \"Ведущего технолога\"");
              }

              flag = false;
        }
    }

    if (!flag) {

    %>

        <table>
            <caption>Список ответственных за изделие</caption>

            <%

            for (Map.Entry<String, String> u : users.entrySet()) {

            %>

                <tr>
                    <td><%=u.getKey()%></td>
                    <td><%=leadProcessEngineerTheProduct%></td>
                    <td><%=u.getValue()%></td>
                </tr>

            <%
            }
            %>

        </table>

        <form method="get" action="search.jsp">
            <button><span>На главную</span></button>
        </form>

    <%

    } else {

    %>

        <form method="get" action="search.jsp">
            <h3>Изделие не найдено!</h3>
            <button><span>На главную</span></button>
        </form>

    <%

    }

    %>

</body>

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
        } catch (WTException e) {
            e.printStackTrace();
        }

        return (ArrayList<WTContainer>) containers;
    }
%>

<%!
    private static Role getRoleFromContainer(WTContainer container, String string) {
        Role role = null;

        try {
            PDMLinkProduct prod = (PDMLinkProduct) PersistenceHelper.manager.refresh(container);
            ContainerTeam team = ContainerTeamHelper.service.getContainerTeam(prod);

            Enumeration enum1 = ContainerTeamHelper.service.findContainerTeamGroups(team, ContainerTeamHelper.ROLE_GROUPS);
            while (enum1.hasMoreElements()) {
                WTGroup group = (WTGroup) enum1.nextElement();
                Role r = Role.toRole(group.getName());

                System.out.println(r.getDisplay().equals(string));

                if (r.getDisplay().equals(string)) {
                    role = r;
                }
            }
        } catch (WTException e) {
            e.printStackTrace();
        }

        return role;
    }
%>

<%!
    private static WTUser getContainerUsersByRole(ContainerTeamManaged teamManaged, Role role) {
        WTUser user = null;

        if (role == null) {
             return user;
        }

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
                            user = (WTUser) principal;
                        }
                    }
                }
            }
        } catch (WTException e) {
             e.printStackTrace();
        }

        return user;
    }
%>

</html>