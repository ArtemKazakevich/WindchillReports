<%@ page import="wt.util.WTException" %>
<%@ page import="wt.maturity.PromotionNotice" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.fc.WTReference" %>
<%@ page import="wt.team.Team" %>
<%@ page import="java.util.Vector" %>
<%@ page import="wt.project.Role" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="wt.org.WTGroup" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="wt.org.WTPrincipalReference" %>
<%@ page import="wt.org.WTPrincipal" %>
<%@ page import="wt.org.WTUser" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
    <head>
        <title>TestJSP</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>

    <body>
        <%
            String oid = request.getParameter("oid");
            PromotionNotice promotionNotice = gettingPromotionNoticeByOid(oid);
            Team team = (Team) promotionNotice.getTeamId().getObject();
            List<Role> roles = new ArrayList<Role>();
            try {
                Vector<?> vector = team.getRoles();
                for (Object obj : vector) {
                    if (obj instanceof Role) {
                        Role role = (Role) obj;
                        roles.add(role);
                    }
                }
            } catch (WTException e) {
                e.printStackTrace();
            }
            for (Role role : roles) {
                String s = "";
                    Enumeration<?> enumeration = team.getPrincipalTarget(role);
                    while (enumeration.hasMoreElements()) {
                        WTPrincipalReference principalReference = (WTPrincipalReference) enumeration.nextElement();
                        WTPrincipal principal = principalReference.getPrincipal();
                        if (principal instanceof WTUser || principal instanceof WTGroup) {
                            s = "qwerty";
                        }
                    }

        %>
            ROLE: <%=role.getDisplay()%>; <%=s%>
            <br>
        <%
            }
        %>
    </body>

    <%!
        private static PromotionNotice gettingPromotionNoticeByOid(String oid) {
            PromotionNotice promotionNotice = null;
            ReferenceFactory refFact = new ReferenceFactory();
            WTReference wtRef = null;

            try {
                wtRef = refFact.getReference(oid);
            } catch (WTException e) {
                e.printStackTrace();
            }

            Object refObject;
            refObject = wtRef != null ? wtRef.getObject() : null;

            if (refObject instanceof PromotionNotice) {
                promotionNotice = (PromotionNotice) refObject;
            }

            return promotionNotice;
        }
    %>



</html>