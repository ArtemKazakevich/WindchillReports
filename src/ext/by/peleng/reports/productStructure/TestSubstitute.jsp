<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.part.WTPartHelper" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="wt.part.WTPartUsageLink" %>
<%@ page import="wt.vc.config.LatestConfigSpec" %>
<%@ page import="wt.part.WTPartMaster" %>
<%@ page import="wt.vc.VersionControlHelper" %>
<%@ page import="wt.fc.*" %>
<%@ page contentType="text/html;charset=UTF-8"%>

<html>
<head>
    <title>TestSubstitute.jsp</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body>

<%
    String oid = request.getParameter("oid");

    WTPart parentPart = gettingWTPartByOid(oid);

%>

<p><strong>HeadBOM</strong><br><%=parentPart.getNumber() + ", " + parentPart.getName()%></p><hr>

<%

    QueryResult queryResult = gettingQueryResultForGettingUsesWTParts(parentPart);

    List<WTPartUsageLink> bomComponentsPartUsageLink = gettingListBomComponentsPartUsageLink(queryResult);

    List<WTPartMaster> bomComponentsPartMaster = gettingListBomComponentsPartMaster(gettingQueryResultForGettingUsesWTParts(parentPart));

    bomComponentsPartMaster.add((WTPartMaster) parentPart.getMaster());

%>

<p><strong>BOMComponentsPartUsageLink</strong> (size = <%=bomComponentsPartUsageLink.size()%>)</p><hr>
<p><strong>BOMComponentsPartMaster</strong> (size = <%=bomComponentsPartMaster.size()%>)</p><hr>

<%

    List<WTPart> substitutes = findAllSubstitutes(bomComponentsPartUsageLink, bomComponentsPartMaster);

%>

<p><strong>Substitutes</strong> (size = <%=substitutes.size()%>)</p>

<%

    for (WTPart part : substitutes) {

%>

<%=part.getNumber() + " - " + part.getName() + " - Version: " + gettingVersionWtPart(part)%><br>

<%

    }

%>

<hr>
<b>ObjectID = <%=gettingObjectIdForWTPart(parentPart)%></b><br>

<%

%>

<%!

    private static WTPart gettingWTPartByOid(String oid) {

        WTPart part = null;

        ReferenceFactory refFact = new ReferenceFactory();

        WTReference wtRef = null;

        try {

            wtRef = refFact.getReference(oid);

        } catch (WTException e) {

            e.printStackTrace();

        }

        Object refObject;

        refObject = wtRef != null ? wtRef.getObject() : null;

        if (refObject instanceof WTPart) {

            part = (WTPart) refObject;

        }

        return part;

    }

%>

<%!

@SuppressWarnings("deprecation")
    private static QueryResult gettingQueryResultForGettingUsesWTParts(WTPart parentPart) {

        QueryResult queryResult = null;

        try {

            queryResult = WTPartHelper.service.getUsesWTParts(parentPart, new LatestConfigSpec());

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

%>

<%!

    private static QueryResult gettingQueryResultForGettingSubstitutesWTPartMasters(WTPartUsageLink partUsageLink) {

        QueryResult queryResult = null;

        try {

            queryResult = WTPartHelper.service.getSubstitutesWTPartMasters(partUsageLink);

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

%>

<%!

    private static QueryResult gettingQueryResultForGettingAlternatesWTPartMasters(WTPartMaster partMaster) {

        QueryResult queryResult = null;

        try {

            queryResult = WTPartHelper.service.getAlternatesWTPartMasters(partMaster);

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

%>

<%!

    private static QueryResult gettingQueryResultForGettingAllVersionsWTPart(WTPartMaster partMaster) {

        QueryResult queryResult = null;

        try {

            queryResult = VersionControlHelper.service.allVersionsOf(partMaster);

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

%>

<%!

    private static ArrayList<WTPartUsageLink> gettingListBomComponentsPartUsageLink(QueryResult queryResult) {

        List<WTPartUsageLink> bomComponents = new ArrayList<WTPartUsageLink>();

        if (queryResult.size() != 0) {

            while (queryResult.hasMoreElements()) {

                Persistable[] persistable = (Persistable[]) queryResult.nextElement();

                WTPartUsageLink partUsageLink = null;

                Object object = persistable[0];

                if (object instanceof WTPartUsageLink) {

                    partUsageLink = (WTPartUsageLink) object;

                    bomComponents.add(partUsageLink);

                }

            }

        }

        return (ArrayList<WTPartUsageLink>) bomComponents;

    }

%>

<%!

    private static ArrayList<WTPartMaster> gettingListBomComponentsPartMaster(QueryResult queryResult) {

        List<WTPartMaster> bomComponents = new ArrayList<WTPartMaster>();

        if (queryResult.size() != 0) {

            while (queryResult.hasMoreElements()) {

                Persistable[] persistable = (Persistable[]) queryResult.nextElement();

                WTPart part = null;

                Object object = persistable[1];

                if (object instanceof WTPart) {

                    part = (WTPart) object;

                    bomComponents.add((WTPartMaster) part.getMaster());

                }

            }

        }

        return (ArrayList<WTPartMaster>) bomComponents;

    }

%>

<%!

    private static ArrayList<WTPart> findAllSubstitutes(List<WTPartUsageLink> bomComponentsPartUsageLink, List<WTPartMaster> bomComponentsPartMaster) {

        List<WTPart> substitutes = new ArrayList<WTPart>();

        for (WTPartUsageLink partUsageLink : bomComponentsPartUsageLink) {

            QueryResult queryResult = gettingQueryResultForGettingSubstitutesWTPartMasters(partUsageLink);

            if (queryResult.size() != 0) {

                while (queryResult.hasMoreElements()) {

                    WTPartMaster substitutePartMaster = (WTPartMaster) queryResult.nextElement();

                    QueryResult queryResultForSubstitutePartMaster = gettingQueryResultForGettingAllVersionsWTPart(substitutePartMaster);

                    while (queryResultForSubstitutePartMaster.hasMoreElements()) {

                        WTPart part = (WTPart) queryResultForSubstitutePartMaster.nextElement();

                        if (part.isLatestIteration()) {

                            substitutes.add(part);

                            break;

                        }

                    }

                }

            }

        }

        for (WTPartMaster partMaster : bomComponentsPartMaster) {

            QueryResult queryResult = gettingQueryResultForGettingAlternatesWTPartMasters(partMaster);

            if (queryResult.size() != 0) {

                while (queryResult.hasMoreElements()) {

                    WTPartMaster alternates = (WTPartMaster) queryResult.nextElement();

                    QueryResult queryResultForSubstitutePartMaster = gettingQueryResultForGettingAllVersionsWTPart(alternates);

                    while (queryResultForSubstitutePartMaster.hasMoreElements()) {

                        WTPart part = (WTPart) queryResultForSubstitutePartMaster.nextElement();

                        if (part.isLatestIteration()) {

                            substitutes.add(part);

                            break;

                        }

                    }

                }

            }

        }

        return (ArrayList<WTPart>) substitutes;

    }

%>

<%!

    private static String gettingVersionWtPart(WTPart part) {

        return part.getVersionInfo().getIdentifier().getValue() + "." + part.getIterationInfo().getIdentifier().getValue();

    }

%>

<%!

    private static long gettingObjectIdForWTPart(WTPart part) {

        WTPartMaster partMaster = (WTPartMaster) part.getMaster();//получение парт мастера

        return PersistenceHelper.getObjectIdentifier(partMaster).getId();

    }

%>

<hr>
</body>

</html>