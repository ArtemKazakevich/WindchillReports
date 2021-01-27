<%@ page import="wt.maturity.PromotionNotice" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.fc.WTReference" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="com.ptc.core.lwc.server.LWCNormalizedObject" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="com.ptc.core.meta.common.DisplayOperationIdentifier" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.maturity.MaturityHelper" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.doc.WTDocument" %>
<%@ page import="wt.epm.EPMDocument" %>
<%@ page import="java.util.*" %>
<%@ page import="wt.workflow.work.WorkItem" %>
<%@ page import="com.ptc.netmarkets.model.NmOid" %>
<%@ page import="com.ptc.windchill.enterprise.workflow.WorkflowCommands" %>
<%@ page import="wt.workflow.work.WfAssignedActivity" %>
<%@ page import="wt.fc.collections.WTArrayList" %>
<%@ page import="wt.org.WTUser" %>
<%@ page import="ext.by.peleng.reports.promotionNotice.ReportAction" %>
<%@ page import="wt.vc.Iterated" %>
<%@ page import="com.ptc.core.meta.common.TypeIdentifier" %>
<%@ page import="com.ptc.core.meta.common.TypeIdentifierHelper" %>
<%@ page import="wt.workflow.engine.*" %>
<%@ page import="wt.workflow.work.WfAssignmentState" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>TestJSP</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <style>
        table {
            font-family: "Lucida Sans Unicode", "Lucida Grande", Sans-Serif, sans-serif;
            font-size: 13px;
            background: white;
            max-width: 100%;
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }
        th {
            font-size: 14px;
            font-weight: normal;
            color: #039;
            padding: 10px 15px;
        }
        td {
            color: #669;
            border-top: 1px solid #e8edff;
            padding: 10px 15px;
        }
        tr:hover td {background: #e8edff;}
        caption {
            caption-side: top;
            text-align: center;
            padding: 10px 0;
            font-size: 18px;
        }
    </style>
</head>

<body>
<h3 align="center"><strong>История согласования объекта "Запрос на продвижение"</strong></h3><hr>

<%
    Locale locale = getLocale();

    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm ", locale);
    dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));

    String oid = request.getParameter("oid");
    PromotionNotice promotionNotice = gettingPromotionNoticeByOid(oid); //получаем объект PromotionNotice (запрос на продвижение)
    Map<String, Object> params = new HashMap<String, Object>();
    String[] attributesPromotionNotice = getAttributesPromotionNotice(promotionNotice, locale, dateFormat); //получаем необходимые атрибуты запроса
    params.put("CHANGE_PN_NAME", promotionNotice.getDisplayType().getLocalizedMessage(locale) + " " + attributesPromotionNotice[0]); //параметр, который будет передаваться в отчет

%>

<p>
    Запрос на продвижение: <%=attributesPromotionNotice[0]%> - <%=attributesPromotionNotice[1]%><br>
    Создал: <%=attributesPromotionNotice[2]%><br>
    Дата создания: <%=attributesPromotionNotice[3]%><br>
    Состояние запроса на продвижение: <%=attributesPromotionNotice[4]%><br>
</p>
<p align="center">
    <a
            href="https://wch.peleng.jsc.local/Windchill/netmarkets/jsp/by/peleng/<%=params.get("CHANGE_PN_NAME")%>.pdf"
            download="<%=params.get("CHANGE_PN_NAME")%>">
        Просмотр отчета
    </a>
</p>
<hr>
<table>
    <caption><b>Объекты продвижения:</b></caption>
    <tr>
        <th><b>Тип объекта</b></th>
        <th><b>Номер</b></th>
        <th><b>Наименование</b></th>
        <th><b>Версия</b></th>
        <th><b>Текущее состояние</b></th>
    </tr>

    <%

        Map<String, ArrayList<Object>> promotionTargets = getPromotionTargets(promotionNotice); //получаем все объекты для продвижения
        Iterator<Map.Entry<String, ArrayList<Object>>> iteratorPT = promotionTargets.entrySet().iterator();

        //проходим по всем объектам продвижения и выводим их в jsp в виде таблицы
        while (iteratorPT.hasNext()) {
            Map.Entry<String, ArrayList<Object>> entry = iteratorPT.next();

            for (Object object : entry.getValue()) {
                String typePromotionTarget = "";
                String numberPromotionTarget = "";
                String namePromotionTarget = "";
                String versionPromotionTarget = "";
                String statePromotionTarget = "";
                if ("WTPart".equals(entry.getKey())) {
                    WTPart part = (WTPart) object;
                    typePromotionTarget = part.getDisplayType().getLocalizedMessage(locale);
                    numberPromotionTarget = part.getNumber();
                    namePromotionTarget = part.getName();
                    versionPromotionTarget = part.getVersionInfo().getIdentifier().getValue() + "." +
                            part.getIterationInfo().getIdentifier().getValue();
                    statePromotionTarget = part.getLifeCycleState().getDisplay(locale);
                } else if ("WTDocument".equals(entry.getKey())) {
                    WTDocument wtDocument = (WTDocument) object;

                    //старый вариант
//                    if (wtDocument.getDisplayType().getLocalizedMessage(locale).trim().equals("Документация")) {
//                        typePromotionTarget = getTypeDocument(wtDocument, locale);
//                    }

                    //новый вариант
                    if (checkSoftType(wtDocument, "by.peleng.Documentation")) {
                        typePromotionTarget = getTypeDocument(wtDocument, locale);
                    } else {
                        typePromotionTarget = wtDocument.getDisplayType().getLocalizedMessage(locale);
                    }
                    numberPromotionTarget = wtDocument.getNumber();
                    namePromotionTarget = wtDocument.getName();
                    versionPromotionTarget = wtDocument.getVersionInfo().getIdentifier().getValue() + "." +
                            wtDocument.getIterationInfo().getIdentifier().getValue();
                    statePromotionTarget = wtDocument.getLifeCycleState().getDisplay(locale);
                } else if ("EPMDocument".equals(entry.getKey())) {
                    EPMDocument epmDocument = (EPMDocument) object;
                    if (epmDocument.getDocType().getStringValue().equals("wt.epm.EPMDocumentType.CADDRAWING")){
                        typePromotionTarget = getTypeDocument(epmDocument, locale);
                    } else {
                        typePromotionTarget = epmDocument.getDisplayType().getLocalizedMessage(locale);
                    }
                    numberPromotionTarget = epmDocument.getNumber();
                    namePromotionTarget = epmDocument.getName();
                    versionPromotionTarget = epmDocument.getVersionInfo().getIdentifier().getValue() + "." +
                            epmDocument.getIterationInfo().getIdentifier().getValue();
                    statePromotionTarget = epmDocument.getLifeCycleState().getDisplay(locale);
                }

    %>

    <tr>
        <td><%=typePromotionTarget%></td>
        <td><%=numberPromotionTarget%></td>
        <td><%=namePromotionTarget%></td>
        <td><%=versionPromotionTarget%></td>
        <td><%=statePromotionTarget%></td>
    </tr>

    <%

            }

        }

    %>

</table>
<table>
    <caption><b>История согласования</b></caption>
    <tr>
        <th><b>Наименование</b></th>
        <th><b>Роль</b></th>
        <th><b>Исполнитель</b></th>
        <th><b>Выбор</b></th>
        <th><b>Комментарий</b></th>
        <th><b>Дата завершения</b></th>
    </tr>

    <%
        Map<String, ArrayList<WorkItem>> workItems = getWorkItems(promotionNotice); //получаем все WorkItem
        Iterator<Map.Entry<String, ArrayList<WorkItem>>> iteratorWI = workItems.entrySet().iterator();

        //выводим все WorkItem в jsp в удобном для нас виде
        while (iteratorWI.hasNext()) {
            Map.Entry<String, ArrayList<WorkItem>> entry = iteratorWI.next();
            ArrayList<WorkItem> values = entry.getValue();

            for (int i = values.size() - 1; i >= 0 ; i--) {
                WorkItem workItem = values.get(i);
                String nameWorkItem = "";
                String roleForWorkItem = "";
                String userWhoCompletedWorkItem = "";
                String choiceForWorkItem = "";
                String commentForWorkItem = "";
                String dateFinishedWorkItem = "";
                WfAssignedActivity wfaa = (WfAssignedActivity) workItem.getSource().getObject();

                if ("listWI".equals(entry.getKey())) {
                    WfVotingEventAudit vea = getWfVotingEventAudit(wfaa, i, oid);
                    WTUser user = (WTUser) vea.getUserRef().getObject();
                    WfActivity wfActivity = (WfActivity) workItem.getSource().getObject();
                    nameWorkItem = vea.getActivityName();
                    roleForWorkItem = vea.getRole().getLocalizedMessage(locale);
                    userWhoCompletedWorkItem = user.getFullName().replace(",", "");
                    choiceForWorkItem = vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1).replace("_", " ");

                    commentForWorkItem = vea.getUserComment().replaceAll("[\\n]{2,}", "<br><br>").replace("\n", "<br>");

                    dateFinishedWorkItem = dateFormat.format(vea.getCreateTimestamp());
                } else if ("listWINotFinished".equals(entry.getKey())) {
                    nameWorkItem = wfaa.getName();
                    roleForWorkItem = workItem.getRole().getDisplay(locale);
                    userWhoCompletedWorkItem = workItem.getOwnership().getOwner().getFullName().replace(",", "");
                    dateFinishedWorkItem = "Выполняется";
                }

    %>
    <tr>
        <td><%=nameWorkItem%></td>
        <td><%=roleForWorkItem%></td>
        <td><%=userWhoCompletedWorkItem%></td>
        <td><%=choiceForWorkItem%></td>
        <td><%=commentForWorkItem%></td>
        <td><%=dateFinishedWorkItem%></td>
    </tr>
    <%
            }

            //создаем отчет в pdf формате и класс ReportAction нам поможет в этом
            ReportAction reportAction = new ReportAction(oid ,attributesPromotionNotice, promotionTargets, workItems, params);
            reportAction.reportGeneration();
        }
    %>
</table>
<hr>

<%!

    private static Locale getLocale() {

        Locale locale = null;

        try {

            locale = SessionHelper.manager.getLocale();

        } catch (WTException e) {

            e.printStackTrace();

        }

        return locale;

    }

%>

<%!

    private static QueryResult getQueryResultForGettingPromotionTargets(PromotionNotice promotionNotice) {

        QueryResult queryResult = null;

        try {

            queryResult = MaturityHelper.getService().getPromotionTargets(promotionNotice);

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

%>

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

<%!

    private static LWCNormalizedObject gettingLwcForPromotionNotice(PromotionNotice promotionNotice, Locale locale) {

        LWCNormalizedObject lwcNormalizedObject = null;

        try {

            lwcNormalizedObject = new LWCNormalizedObject(promotionNotice, null, locale, new DisplayOperationIdentifier());

            lwcNormalizedObject.load("name", "number", "thePersistInfo.createStamp");

        } catch (WTException e) {

            e.printStackTrace();

        }

        return lwcNormalizedObject;

    }

%>

<%!

    private static String[] getAttributesPromotionNotice(PromotionNotice promotionNotice, Locale locale, DateFormat dateFormat) {

        String[] attributes = new String[5];

        LWCNormalizedObject lwcNormalizedObject = gettingLwcForPromotionNotice(promotionNotice, locale);

        try {

            attributes[0] = (String) lwcNormalizedObject.get("number");

            attributes[1] = (String) lwcNormalizedObject.get("name");

            attributes[2] = promotionNotice.getCreatorFullName().replace(",", "");

            attributes[3] = dateFormat.format(lwcNormalizedObject.get("thePersistInfo.createStamp"));

            attributes[4] = promotionNotice.getState().getState().getFullDisplay();

        } catch (WTException e) {

            e.printStackTrace();

        }

        return attributes;

    }

%>

<%!

    private static HashMap<String, ArrayList<Object>> getPromotionTargets(PromotionNotice promotionNotice) {

        Map<String, ArrayList<Object>> promotionTargets = new HashMap<String, ArrayList<Object>>();

        List<Object> listWTPart = new ArrayList<Object>();

        List<Object> listWTDocument = new ArrayList<Object>();

        List<Object> listEPMDocument = new ArrayList<Object>();

        QueryResult queryResult =getQueryResultForGettingPromotionTargets(promotionNotice);

        while (queryResult.hasMoreElements()) {

            Object object = queryResult.nextElement();

            if (object instanceof WTPart) {

                listWTPart.add(object);

            } else if (object instanceof WTDocument) {

                listWTDocument.add(object);

            } else if (object instanceof EPMDocument) {

                listEPMDocument.add(object);

            }

        }

        promotionTargets.put("WTPart", (ArrayList<Object>) listWTPart);

        promotionTargets.put("WTDocument", (ArrayList<Object>) listWTDocument);

        promotionTargets.put("EPMDocument", (ArrayList<Object>) listEPMDocument);

        return (HashMap<String, ArrayList<Object>>) promotionTargets;

    }

%>

<%!

    private static String getTypeDocument(Object object, Locale locale) {

        String typeDocument = "";

        LWCNormalizedObject lwcNormalizedObject = null;

        if (object instanceof WTDocument) {

            try {

                lwcNormalizedObject = new LWCNormalizedObject((WTDocument) object, null, locale, new DisplayOperationIdentifier());

                lwcNormalizedObject.load("ATR_DOC_TYPE");

                typeDocument = (String) lwcNormalizedObject.get("ATR_DOC_TYPE");

            } catch (WTException e) {

                e.printStackTrace();

            }

        } else if (object instanceof EPMDocument) {

            try {

                lwcNormalizedObject = new LWCNormalizedObject((EPMDocument) object, null, locale, new DisplayOperationIdentifier());

                lwcNormalizedObject.load("ATR_DOC_TYPE");

                typeDocument = (String) lwcNormalizedObject.get("ATR_DOC_TYPE");

            } catch (WTException e) {

                e.printStackTrace();

            }

        }

        return typeDocument;

    }

%>

<%!

    private static WfVotingEventAudit getWfVotingEventAudit(WfAssignedActivity wfaa, int i, String oid) {
        WTArrayList wtArrayList = null;

        try {
            wtArrayList = (WTArrayList) WfEngineHelper.service.getVotingEvents(wfaa.getParentProcess(), null, null, null);
        } catch (WTException e) {
            e.printStackTrace();
        }

//                Collections.reverse(wtArrayList);

        WTReference wtRef = null;
        try {
            wtRef = new ReferenceFactory().getReference(wtArrayList.get(i).toString());
        } catch (WTException e) {
            e.printStackTrace();
        }

        return (WfVotingEventAudit) wtRef.getObject();
    }
%>

<%!
    private static TreeMap<String, ArrayList<WorkItem>> getWorkItems(PromotionNotice promotionNotice) {
        Map<String, ArrayList<WorkItem>> workItems = new TreeMap<String, ArrayList<WorkItem>>();
        List<WorkItem> listWI = new ArrayList<WorkItem>();
        List<WorkItem> listWINotFinished = new ArrayList<WorkItem>();
        Enumeration processes = null;

        try {
            processes = WfEngineHelper.service.getAssociatedProcesses(promotionNotice, null, null);
        } catch (WTException e) {
            e.printStackTrace();
        }

        while (processes.hasMoreElements()) {
            WfProcess process = (WfProcess) processes.nextElement();

            if (!process.getState().equals(WfState.CLOSED_TERMINATED)) {
                NmOid nmOid = null;
                QueryResult status = null;

                try {
                    nmOid = new NmOid(process);
                } catch (WTException e) {
                    e.printStackTrace();
                }

                try {
                    status = WorkflowCommands.getRouteStatus(nmOid);
                } catch (WTException e) {
                    e.printStackTrace();
                }

                assert status != null;
                while (status.hasMoreElements()) {
                    Object obj = status.nextElement();

                    if (obj.getClass().isAssignableFrom(WorkItem.class) && ((WorkItem) obj).getStatus().equals(WfAssignmentState.COMPLETED)) {
                        listWI.add((WorkItem) obj);
                    } else if (obj.getClass().isAssignableFrom(WfVotingEventAudit.class)) {
                        WfVotingEventAudit wfVotingEventAudit = (WfVotingEventAudit) obj;
                        listWI.add(wfVotingEventAudit.getWorkItem());
                    } else if (obj.getClass().isAssignableFrom(WorkItem.class) && ((WorkItem) obj).getStatus().equals(WfAssignmentState.POTENTIAL)) {
                        listWINotFinished.add((WorkItem) obj);
                    }
                }
            }
        }

        workItems.put("listWI", (ArrayList<WorkItem>) listWI);
        workItems.put("listWINotFinished", (ArrayList<WorkItem>) listWINotFinished);

        return (TreeMap<String, ArrayList<WorkItem>>) workItems;
    }
%>

<%!
    private static boolean checkSoftType(Iterated object, String softType) {
        TypeIdentifier typeIdentifier = TypeIdentifierHelper.getType(object);
        TypeIdentifier root = TypeIdentifierHelper.getTypeIdentifier(softType);


        return typeIdentifier.isDescendedFrom(root);
    }
%>
</body>
</html>