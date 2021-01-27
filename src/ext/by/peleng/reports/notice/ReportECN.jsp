<%@ page import="com.ptc.core.lwc.server.LWCNormalizedObject" %>
<%@ page import="wt.change2.*" %>
<%@ page import="com.ptc.core.meta.common.impl.WCTypeInstanceIdentifier" %>
<%@ page import="wt.org.WTUser" %>
<%@ page import="com.ptc.core.meta.container.common.AttributeTypeSummary" %>
<%@ page import="com.ptc.core.lwc.server.LWCEnumerationEntryValuesFactory" %>
<%@ page import="wt.change2.ChangeHelper2" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.doc.WTDocument" %>
<%@ page import="wt.epm.EPMDocument" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.File" %>
<%@ page import="net.sf.jasperreports.engine.util.JRLoader" %>
<%@ page import="net.sf.jasperreports.engine.data.JRBeanCollectionDataSource" %>
<%@ page import="wt.fc.*" %>
<%@ page import="wt.fc.collections.WTArrayList" %>
<%@ page import="wt.workflow.work.*" %>
<%@ page import="wt.workflow.engine.*" %>
<%@ page import="wt.workflow.engine.WfVotingEventAudit" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="net.sf.jasperreports.engine.*" %>
<%@ page import="com.ptc.core.meta.common.*" %>
<%@ page import="wt.iba.value.IBAHolder" %>
<%@ page import="wt.iba.value.service.IBAValueHelper" %>
<%@ page import="wt.iba.value.DefaultAttributeContainer" %>
<%@ page import="wt.iba.value.IBAValueUtility" %>
<%@ page import="wt.iba.value.litevalue.AbstractValueView" %>
<%@ page import="wt.iba.definition.litedefinition.AttributeDefDefaultView" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="com.ptc.netmarkets.model.NmOid" %>
<%@ page import="com.ptc.windchill.enterprise.workflow.WorkflowCommands" %>
<%@ page import="wt.vc.Iterated" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>TestJSP</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<p align="center"><strong>История согласования объектов изменения</strong></p>
<%
    String oid = request.getParameter("oid");
    ReferenceFactory refFact = new ReferenceFactory();
    WTReference wtRef = null;
    Locale locale = null;
    try {
        wtRef = refFact.getReference(oid);
        locale = SessionHelper.manager.getLocale();
    } catch (WTException e) {
        e.printStackTrace();
    }

    List<DataBean> listData = new ArrayList<DataBean>(); //коллекция с данными, которую мы будем передавать в отчет
    List<WorkItem> listWI = new ArrayList<WorkItem>();
    List<WorkItem> listWINotFinished = new ArrayList<WorkItem>();
    List<WTChangeActivity2> listWTCA2 = new ArrayList<WTChangeActivity2>();
    List<WTChangeActivity2> listWTCA2PLAN = new ArrayList<WTChangeActivity2>();
    List<WTChangeActivity2> listWTCA2TPP = new ArrayList<WTChangeActivity2>();
    Map<String, Object> params = new HashMap<String, Object>(); //мапа с параметрами, которую мы передаем в наш отчет (параметры нужны для заголовка в отчёте)
    List<WTPart> listPartAfter = new ArrayList<WTPart>();
    List<WTDocument> listDocumentAfter = new ArrayList<WTDocument>();
    List<EPMDocument> listEpmDocumentAfter = new ArrayList<EPMDocument>();
    final String CADDRAWING = "wt.epm.EPMDocumentType.CADDRAWING";

//    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);
//    DateFormat dateFormatTPP = new SimpleDateFormat("dd.MM.yy", locale);

    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);
    dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
    DateFormat dateFormatTPP = new SimpleDateFormat("dd.MM.yy", locale);
    dateFormatTPP.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));

    QueryResult qr;
    LWCNormalizedObject lwcNormalizedObject;
    Object refObject;
    Object tempObject;
    WTChangeActivity2 ca2;
    WTChangeOrder2 co2;
    Integer numberTPP;

    refObject = wtRef.getObject();

    //проверяем что у нас: извщение (WTChangeOrder2) или же "синий чемоданчик" (WTChangeActivity2)
    if (refObject instanceof WTChangeOrder2) {
        co2 = (WTChangeOrder2) refObject;
        try {
            lwcNormalizedObject = new LWCNormalizedObject(co2, null, locale, new DisplayOperationIdentifier());
            lwcNormalizedObject.load("ATR_COMPLEXITY", "ATR_ECN_TYPE");

            String changeNoticeName = co2.getDisplayType().getLocalizedMessage(locale) + " " + co2.getNumber();
            params.put("CHANGE_NOTICE_NAME", changeNoticeName);
            String changeNoticeComplexity = (String) lwcNormalizedObject.get("ATR_COMPLEXITY");
            String changeNoticeType = (String) lwcNormalizedObject.get("ATR_ECN_TYPE");
            params.put("CHANGE_NOTICE_COMPLEXITY", DataBean.getShortComplexity(changeNoticeComplexity, changeNoticeType));
        }catch (WTException e){
            e.printStackTrace();
        }

        try {
            qr = ChangeHelper2.service.getChangeActivities(co2); //получили все ChangeActivity (чемоданчики), которые присутствуют в извещении

            while (qr.hasMoreElements()){
                ca2 = (WTChangeActivity2) qr.nextElement();
                lwcNormalizedObject = new LWCNormalizedObject(ca2, null, locale, new DisplayOperationIdentifier());
                lwcNormalizedObject.load("ATR_TYPE_CA");
                if (lwcNormalizedObject.get("ATR_TYPE_CA") == null)
                    listWTCA2.add(ca2); //"добавляем синий чемоданчик"
                else if (lwcNormalizedObject.get("ATR_TYPE_CA").equals("TPP"))
                    listWTCA2TPP.add(ca2); //"добавляем светлозелёный чемоданчик"
                else if (lwcNormalizedObject.get("ATR_TYPE_CA").equals("PLAN"))
                    listWTCA2PLAN.add(ca2); //"добавляем темнозеленый чемоданчик"
            }

            Collections.sort(listWTCA2, new Comparator<WTChangeActivity2>() {
                @Override
                public int compare(WTChangeActivity2 o1, WTChangeActivity2 o2) {
                    return o1.getNumber().compareTo(o2.getNumber());
                }
            });

            Collections.sort(listWTCA2TPP, new Comparator<WTChangeActivity2>() {
                @Override
                public int compare(WTChangeActivity2 o1, WTChangeActivity2 o2) {
                    return o1.getNumber().compareTo(o2.getNumber());
                }
            });

            for (WTChangeActivity2 x1 : listWTCA2) {
                //для синего чемоданчика получаем все необходимые атрибуты: номер, когда и кем был создан, состояние и номер подразделения.
                lwcNormalizedObject = new LWCNormalizedObject(x1, null, locale, new DisplayOperationIdentifier());
                lwcNormalizedObject.load("number", "ATR_DEPARTMENT", "iterationInfo.modifier", "thePersistInfo.modifyStamp");
                String numberCA2 = (String) lwcNormalizedObject.get("number");
                String modifyCA2 = dateFormat.format(lwcNormalizedObject.get("thePersistInfo.modifyStamp")).toString();
                String stateCA2 = x1.getState().getState().getFullDisplay();

                WCTypeInstanceIdentifier tiiUserModifier = (WCTypeInstanceIdentifier) lwcNormalizedObject.get("iterationInfo.modifier");
                WTUser userModifier =  (WTUser) (new ReferenceFactory()).getReference(tiiUserModifier.getPersistenceIdentifier()).getObject();
                String modifierCA2 = userModifier.getFullName();

                String departmentCA2 = "";
                AttributeTypeSummary ats = lwcNormalizedObject.getAttributeDescriptor("ATR_DEPARTMENT");
                DataSet legalValueSet = ats.getLegalValueSet();
                EnumeratedSet enumSet = (EnumeratedSet) (legalValueSet.getIntersection(legalValueSet));
                EnumerationEntryIdentifier idntify = enumSet.getElementByKey(lwcNormalizedObject.get("ATR_DEPARTMENT"));
                if (idntify != null){
                    LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();
                    departmentCA2 = eevf.get(idntify, locale).getDisplay();
                }

                //получаем все результирующие объекты: WTPart, WTDocument и EPMDocument.
                qr = ChangeHelper2.service.getChangeablesAfter(x1);

                while (qr.hasMoreElements()){
                    tempObject = qr.nextElement();
                    if (tempObject instanceof WTPart)
                        listPartAfter.add((WTPart) tempObject);
                    if (tempObject instanceof WTDocument)
                        listDocumentAfter.add((WTDocument) tempObject);
                    if (tempObject instanceof EPMDocument)
                        listEpmDocumentAfter.add((EPMDocument) tempObject);
                }

                //результирующие объекты запихиваем в listData (в наш отчет)
                for (WTPart x : listPartAfter) {
                    listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                            x.getName(), x.getDisplayType().getLocalizedMessage(locale), x.getNumber(),
                            x.getVersionInfo().getIdentifier().getValue() + "." +
                                    x.getIterationInfo().getIdentifier().getValue()));
                }
                listPartAfter.clear();

                for (WTDocument x : listDocumentAfter) {
                    if (checkSoftType(x, "by.peleng.Documentation")){
                        LWCNormalizedObject lwcWtDoc = new LWCNormalizedObject(x, null, locale, new DisplayOperationIdentifier());
                        lwcWtDoc.load("ATR_DOC_TYPE");
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), (String) lwcWtDoc.get("ATR_DOC_TYPE"), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                    }else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), x.getDisplayType().getLocalizedMessage(locale), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                }
                listDocumentAfter.clear();

                for (EPMDocument x : listEpmDocumentAfter) {
                    if (x.getDocType().getStringValue().equals(CADDRAWING)){
                        LWCNormalizedObject lwcEpmDoc = new LWCNormalizedObject(x, null, locale, new DisplayOperationIdentifier());
                        lwcEpmDoc.load("ATR_DOC_TYPE");
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), (String) lwcEpmDoc.get("ATR_DOC_TYPE"), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                    } else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), x.getDisplayType().getLocalizedMessage(locale), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                }
                listEpmDocumentAfter.clear();

//////////////////////////////////////////////////// WorkItem
                //здесь мы получаем все наши процессы (интересовать нас будут только процессы, находящиеся в любом состоянии, кроме состояния "Прекращено")
                Enumeration processes = WfEngineHelper.service.getAssociatedProcesses(x1, null, null);
                while (processes.hasMoreElements()) {
                    WfProcess process = (WfProcess) processes.nextElement();

                    if (!process.getState().equals(WfState.CLOSED_TERMINATED)) {
                        NmOid nmOid = new NmOid(process);
                        QueryResult status = WorkflowCommands.getRouteStatus(nmOid);

                        while (status.hasMoreElements()) {
                            Object obj = status.nextElement();

                            if (obj.getClass().isAssignableFrom(WorkItem.class) && ((WorkItem) obj).getStatus().equals(WfAssignmentState.COMPLETED)) {
                                listWI.add((WorkItem) obj);
                            } else if (obj.getClass().isAssignableFrom(WorkItem.class) && ((WorkItem) obj).getStatus().equals(WfAssignmentState.POTENTIAL)) {
                                listWINotFinished.add((WorkItem) obj);
                            } else if (obj.getClass().isAssignableFrom(WfVotingEventAudit.class)) {
                                WfVotingEventAudit wfVotingEventAudit = (WfVotingEventAudit) obj;
                                listWI.add(wfVotingEventAudit.getWorkItem());
                            }

                        }
                    }

                }

                int i = listWI.size() - 1;
                //проходим по всем WorkItem-ам и получаем все необходимые атрибуты
                for (WorkItem x : listWI) {
                    WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
                    WTArrayList wtArrayList = (WTArrayList) WfEngineHelper.service.getVotingEvents(wfaa.getParentProcess(), null, null, null);
                    wtRef = refFact.getReference(wtArrayList.get(i).toString());
                    WfVotingEventAudit vea = (WfVotingEventAudit) wtRef.getObject();
                    wtRef = vea.getUserRef();
                    WTUser user = (WTUser) wtRef.getObject();

                    //получаем поле комментарии, которые мы разбиваем, при необходимости, на более мелкие строки для красоты отчета
                    String commentary = vea.getUserComment();
                    boolean containsEnter = DataBean.isContainsEnter(commentary);
                    boolean moreThanField = DataBean.isMoreThenTextFieldInCommentary(commentary);
                    String[] arrEnter;
                    String[] arrMore;
                    ArrayList<String> listString = new ArrayList<String>();

                    if (containsEnter) {

                        arrEnter = commentary.split("\n");

                        for (String s : arrEnter) {

                            if (DataBean.isMoreThenTextFieldInCommentary(s)) {

                                arrMore = s.split(" ");
                                String tempStr1 = "";
                                for (int j = 0; j < arrMore.length; j++) {
                                    tempStr1 += " " + arrMore[j];

                                    if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                                        listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                                        tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                                    }

                                    if (j == arrMore.length - 1)
                                        listString.add(tempStr1.substring(1));
                                }

                            } else {

                                listString.add(s);

                            }

                        }

                        for (int j = 0; j < listString.size(); j++) {
                            if (j == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                        vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                        listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        " ", "", "", "", listString.get(j), ""));
                        }
                        listString.clear();

                    } else if (moreThanField) {

                        arrMore = commentary.split(" ");
                        String tempStr1 = "";

                        for (int j = 0; j < arrMore.length; j++) {
                            tempStr1 += " " + arrMore[j];

                            if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                                listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                                tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                            }

                            if (j == arrMore.length - 1)
                                listString.add(tempStr1.substring(1));
                        }

                        for (int j = 0; j < listString.size(); j++) {
                            if (j == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                        vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                        listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        " ", "", "", "", listString.get(j), ""));
                        }
                        listString.clear();

                    } else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                commentary, dateFormat.format(vea.getCreateTimestamp()).toString()));
                    i--;
                }
                listWI.clear();

                for (WorkItem x : listWINotFinished) {
                    WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
                    listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                            wfaa.getName(),x.getRole().getDisplay(locale), x.getOwnership().getOwner().getFullName(),
                            " ", " ", "Выполняется"));
                }
                listWINotFinished.clear();

                Collections.sort(listData, new Comparator<DataBean>() {
                    @Override
                    public int compare(DataBean o1, DataBean o2) {
                        if (o1.getNumberWT() == null || o2.getNumberWT() == null)
                            return 0;
                        else if (o1.getNumberCA2().equals(o2.getNumberCA2()))
                            return o1.getNumberWT().compareTo(o2.getNumberWT());
                        return 0;
                    }
                });
            }
/////////////////////////////////////////////////// ChangeActivity2 PLAN
            //обрабатываем темнозеленый чемоданчик, который как правило включает в себя светлозелёные чемоданчики
            for (WTChangeActivity2 x1 : listWTCA2PLAN) {
                lwcNormalizedObject = new LWCNormalizedObject(x1, null, locale, new DisplayOperationIdentifier());
                lwcNormalizedObject.load("number", "ATR_DEPARTMENT", "iterationInfo.modifier", "thePersistInfo.modifyStamp");
                String numberCA2 = (String) lwcNormalizedObject.get("number");
                String modifyCA2 = dateFormat.format(lwcNormalizedObject.get("thePersistInfo.modifyStamp")).toString();
                String stateCA2 = x1.getState().getState().getFullDisplay();

                WCTypeInstanceIdentifier tiiUserModifier = (WCTypeInstanceIdentifier) lwcNormalizedObject.get("iterationInfo.modifier");
                WTUser userModifier =  (WTUser) (new ReferenceFactory()).getReference(tiiUserModifier.getPersistenceIdentifier()).getObject();
                String modifierCA2 = userModifier.getFullName();

                String departmentCA2 = "";
                AttributeTypeSummary ats = lwcNormalizedObject.getAttributeDescriptor("ATR_DEPARTMENT");
                DataSet ds = ats.getLegalValueSet();
                EnumeratedSet enumSet = (EnumeratedSet) (ds.getIntersection(ds));
                EnumerationEntryIdentifier idntify = enumSet.getElementByKey(lwcNormalizedObject.get("ATR_DEPARTMENT"));
                if (idntify != null){
                    LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();
                    departmentCA2 = eevf.get(idntify, locale).getDisplay();
                }

                Enumeration processes = WfEngineHelper.service.getAssociatedProcesses(x1, null, null);
                while (processes.hasMoreElements()) {
                    WfProcess process = (WfProcess) processes.nextElement();

                    if (!process.getState().equals(WfState.CLOSED_TERMINATED)) {
                        NmOid nmOid = new NmOid(process);
                        QueryResult status = WorkflowCommands.getRouteStatus(nmOid);

                        while (status.hasMoreElements()) {
                            Object obj = status.nextElement();

                            if (obj.getClass().isAssignableFrom(WorkItem.class) && ((WorkItem) obj).getStatus().equals(WfAssignmentState.COMPLETED)) {
                                listWI.add((WorkItem) obj);
                            } else if (obj.getClass().isAssignableFrom(WorkItem.class)) {
                                listWINotFinished.add((WorkItem) obj);
                            } else if (obj.getClass().isAssignableFrom(WfVotingEventAudit.class)) {
                                WfVotingEventAudit wfVotingEventAudit = (WfVotingEventAudit) obj;
                                listWI.add(wfVotingEventAudit.getWorkItem());
                            }

                        }
                    }

                }

                numberTPP = 1;
//////////////////////////////////////////////////////////// ChangeActivity2 TPP
                //прооходим по всем светлозеленым чемоданчикам и получаем необходимые атрибуты
                for (WTChangeActivity2 x : listWTCA2TPP) {
                    lwcNormalizedObject = new LWCNormalizedObject(x, null, locale, new DisplayOperationIdentifier());
                    lwcNormalizedObject.load("ATR_DEPARTMENT", "needDate", "ATR_CA_READY");
                    String readyCA = (String) lwcNormalizedObject.get("ATR_CA_READY");
                    String deadLineTPP = "";

                    if (readyCA != null){
                        if ((readyCA.equals("Выполнено") || readyCA.equals("Согл.графика")))
                            deadLineTPP = readyCA;
                        else if (lwcNormalizedObject.get("needDate") != null)
                            deadLineTPP = dateFormatTPP.format(lwcNormalizedObject.get("needDate")).toString();
                    } else if (lwcNormalizedObject.get("needDate") != null)
                        deadLineTPP = dateFormatTPP.format(lwcNormalizedObject.get("needDate")).toString();

                    String departmentTPP = "";
                    ats = lwcNormalizedObject.getAttributeDescriptor("ATR_DEPARTMENT");
                    ds = ats.getLegalValueSet();
                    enumSet = (EnumeratedSet) (ds.getIntersection(ds));
                    idntify = enumSet.getElementByKey(lwcNormalizedObject.get("ATR_DEPARTMENT"));
                    LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();

                    if (idntify != null){
                        departmentTPP = eevf.get(idntify, locale).getDisplay();
                    } else if (idntify == null){
                        IBAHolder holder = IBAValueHelper.service.refreshAttributeContainer(x, null, locale, null);
                        DefaultAttributeContainer dac = (DefaultAttributeContainer) holder.getAttributeContainer();
                        AttributeDefDefaultView addv = dac.getAttributeValues()[0].getDefinition();
                        AbstractValueView[] avv = dac.getAttributeValues(addv);

                        for (int i = 0; i < avv.length; i++) {
                            idntify = enumSet.getElementByKey(IBAValueUtility.getLocalizedIBAValueDisplayString(avv[i], locale));

                            if (idntify != null) {

                                String str = eevf.get(idntify,locale).getFullDisplay();

                                if (i != (avv.length - 1))
                                    departmentTPP +=  str + ", ";
                                else
                                    departmentTPP += str;

                            }
                        }
                    }

//////////////////////////////////////////////// checked length field DescriptionTPP
                    //получаем описание светлозеленого чемоданчика и разбиваем его на несколько записей при необходимости
                    String descriptionTPP = x.getDescription();
                    boolean containsEnter = DataBean.isContainsEnter(descriptionTPP);
                    boolean moreThanField = DataBean.isMoreThenTextField(descriptionTPP);
                    String[] arrEnter;
                    String[] arrMore;
                    ArrayList<String> listString = new ArrayList<String>();

                    if (containsEnter){
                        arrEnter = descriptionTPP.split("\r\n");
                        for (String s : arrEnter) {
                            if (DataBean.isMoreThenTextField(s)){
                                arrMore = s.split(" ");
                                String tempStr1 = "";
                                for (int i = 0; i < arrMore.length; i++) {
                                    tempStr1 += " " + arrMore[i];

                                    if (DataBean.isMoreThenTextField(tempStr1)){
                                        listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[i].length()));
                                        tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[i].length());
                                    }

                                    if (i == arrMore.length - 1)
                                        listString.add(tempStr1.substring(1));
                                }
                            } else
                                listString.add(s);
                        }

                        for (int i = 0; i < listString.size(); i++) {
                            if (i == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        numberTPP, listString.get(0), departmentTPP, deadLineTPP));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        0, listString.get(i), "", ""));
                        }
                        listString.clear();

                    } else if (moreThanField){
                        arrMore = descriptionTPP.split(" ");
                        String tempStr1 = "";
                        for (int i = 0; i < arrMore.length; i++) {
                            tempStr1 += " " + arrMore[i];

                            if (DataBean.isMoreThenTextField(tempStr1)){
                                listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[i].length()));
                                tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[i].length());
                            }

                            if (i == arrMore.length - 1)
                                listString.add(tempStr1.substring(1));
                        }

                        for (int i = 0; i < listString.size(); i++) {
                            if (i == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        numberTPP, listString.get(0), departmentTPP, deadLineTPP));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        0, listString.get(i), "", ""));
                        }
                        listString.clear();

                    } else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                numberTPP, descriptionTPP, departmentTPP, deadLineTPP));
                    numberTPP++;
                }

                int i = listWI.size() - 1;

                //!!!!!!!!!!!CHANGES!!!!!!!!!!!!!!!!
                //для темнозеленого чемоданчика получаем все WorkItem и обрабатываем их
                for (WorkItem x : listWI) {
                    WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
                    WTArrayList wtArrayList = (WTArrayList) WfEngineHelper.service.getVotingEvents(wfaa.getParentProcess(), null, null, null);
                    wtRef = refFact.getReference(wtArrayList.get(i).toString());
                    WfVotingEventAudit vea = (WfVotingEventAudit) wtRef.getObject();
                    wtRef = vea.getUserRef();
                    WTUser user = (WTUser) wtRef.getObject();

                    String commentary = vea.getUserComment();
                    boolean containsEnter = DataBean.isContainsEnter(commentary);
                    boolean moreThanField = DataBean.isMoreThenTextFieldInCommentary(commentary);
                    String[] arrEnter;
                    String[] arrMore;
                    ArrayList<String> listString = new ArrayList<String>();

                    if (containsEnter) {

                        arrEnter = commentary.split("\n");

                        for (String s : arrEnter) {

                            if (DataBean.isMoreThenTextFieldInCommentary(s)) {

                                arrMore = s.split(" ");
                                String tempStr1 = "";
                                for (int j = 0; j < arrMore.length; j++) {
                                    tempStr1 += " " + arrMore[j];

                                    if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                                        listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                                        tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                                    }

                                    if (j == arrMore.length - 1)
                                        listString.add(tempStr1.substring(1));
                                }

                            } else {

                                listString.add(s);

                            }

                        }

                        for (int j = 0; j < listString.size(); j++) {
                            if (j == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                        vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                        listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        " ", "", "", "", listString.get(j), ""));
                        }
                        listString.clear();

                    } else if (moreThanField) {

                        arrMore = commentary.split(" ");
                        String tempStr1 = "";

                        for (int j = 0; j < arrMore.length; j++) {
                            tempStr1 += " " + arrMore[j];

                            if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                                listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                                tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                            }

                            if (j == arrMore.length - 1)
                                listString.add(tempStr1.substring(1));
                        }

                        for (int j = 0; j < listString.size(); j++) {
                            if (j == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                        vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                        listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        " ", "", "", "", listString.get(j), ""));
                        }
                        listString.clear();

                    } else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                commentary, dateFormat.format(vea.getCreateTimestamp()).toString()));
                    i--;
                }
                listWI.clear();

                for (WorkItem x : listWINotFinished) {
                    WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
                    listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                            wfaa.getName(),x.getRole().getDisplay(locale), x.getOwnership().getOwner().getFullName(),
                            " ", " ", "Выполняется"));
                }
                listWINotFinished.clear();

            }
/////////////////////////////////////////////// Print ReportECN
            String jasperFile = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\ext\\by\\peleng\\CA\\listReport1.jasper"; //пусть к форме отчета на сервере (скомпилированный вариант)
            File reportFile1 = new File(jasperFile);
            String outFileName = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\netmarkets\\jsp\\by\\peleng\\" + params.get("CHANGE_NOTICE_NAME") + ".pdf"; //путь, где будет храниться файл на сервере (не best practice)
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listData);
            try {
                JasperReport jr = (JasperReport) JRLoader.loadObject(reportFile1);
                JasperPrint print = JasperFillManager.fillReport(jr, params, dataSource);
                JasperExportManager.exportReportToPdfFile(print, outFileName);
            } catch (JRException e){
                e.printStackTrace();
            }

%>
<p align="center">
    <a
            href="https://wch.peleng.jsc.local/Windchill/netmarkets/jsp/by/peleng/<%=params.get("CHANGE_NOTICE_NAME")%>.pdf"
            download="<%=params.get("CHANGE_NOTICE_NAME")%>">
        Просмотр отчета
    </a>
</p>
<%

} catch (WTException e){
%>
<p>Что-то пошло не так</p>
<%
    }
    //если мы получили по oid объект WTChangeActivity2, то обрабатываем его как было показано выше
} else if (refObject instanceof WTChangeActivity2) {
    ca2 = (WTChangeActivity2) refObject;
    try {
        lwcNormalizedObject = new LWCNormalizedObject(ca2, null, locale, new DisplayOperationIdentifier());
        lwcNormalizedObject.load("ATR_TYPE_CA");

        if (lwcNormalizedObject.get("ATR_TYPE_CA") == null){ //получаем отчет для синего чемоданчика
            listWTCA2.add(ca2);
            qr = ChangeHelper2.service.getChangeOrder(ca2);
            co2 = (WTChangeOrder2) qr.nextElement();
            lwcNormalizedObject = new LWCNormalizedObject(co2, null, locale, new DisplayOperationIdentifier());
            lwcNormalizedObject.load("ATR_COMPLEXITY", "ATR_ECN_TYPE");

            String changeNoticeName = co2.getDisplayType().getLocalizedMessage(locale) + " " + co2.getNumber();
            params.put("CHANGE_NOTICE_NAME", changeNoticeName);
            String changeNoticeComplexity = (String) lwcNormalizedObject.get("ATR_COMPLEXITY");
            String changeNoticeType = (String) lwcNormalizedObject.get("ATR_ECN_TYPE");
            params.put("CHANGE_NOTICE_COMPLEXITY", DataBean.getShortComplexity(changeNoticeComplexity, changeNoticeType));

            for (WTChangeActivity2 x1 : listWTCA2) {
                lwcNormalizedObject = new LWCNormalizedObject(x1, null, locale, new DisplayOperationIdentifier());
                lwcNormalizedObject.load("number", "ATR_DEPARTMENT", "iterationInfo.modifier", "thePersistInfo.modifyStamp");
                String numberCA2 = (String) lwcNormalizedObject.get("number");
                String modifyCA2 = dateFormat.format(lwcNormalizedObject.get("thePersistInfo.modifyStamp")).toString();
                String stateCA2 = x1.getState().getState().getFullDisplay();

                WCTypeInstanceIdentifier tiiUserModifier = (WCTypeInstanceIdentifier) lwcNormalizedObject.get("iterationInfo.modifier");
                WTUser userModifier =  (WTUser) (new ReferenceFactory()).getReference(tiiUserModifier.getPersistenceIdentifier()).getObject();
                String modifierCA2 = userModifier.getFullName();

                String departmentCA2 = "";
                AttributeTypeSummary ats = lwcNormalizedObject.getAttributeDescriptor("ATR_DEPARTMENT");
                DataSet legalValueSet = ats.getLegalValueSet();
                EnumeratedSet enumSet = (EnumeratedSet) (legalValueSet.getIntersection(legalValueSet));
                EnumerationEntryIdentifier idntify = enumSet.getElementByKey(lwcNormalizedObject.get("ATR_DEPARTMENT"));
                if (idntify != null){
                    LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();
                    departmentCA2 = eevf.get(idntify, locale).getDisplay();
                }

                qr = ChangeHelper2.service.getChangeablesAfter(x1);

                while (qr.hasMoreElements()){
                    tempObject = qr.nextElement();
                    if (tempObject instanceof WTPart)
                        listPartAfter.add((WTPart) tempObject);
                    if (tempObject instanceof WTDocument)
                        listDocumentAfter.add((WTDocument) tempObject);
                    if (tempObject instanceof EPMDocument)
                        listEpmDocumentAfter.add((EPMDocument) tempObject);
                }

                for (WTPart x : listPartAfter) {
                    listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                            x.getName(), x.getDisplayType().getLocalizedMessage(locale), x.getNumber(),
                            x.getVersionInfo().getIdentifier().getValue() + "." +
                                    x.getIterationInfo().getIdentifier().getValue()));
                }
                listPartAfter.clear();

                for (WTDocument x : listDocumentAfter) {
                    if (checkSoftType(x, "by.peleng.Documentation")){
                        LWCNormalizedObject lwcWtDoc = new LWCNormalizedObject(x, null, locale, new DisplayOperationIdentifier());
                        lwcWtDoc.load("ATR_DOC_TYPE");
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), (String) lwcWtDoc.get("ATR_DOC_TYPE"), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                    }else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), x.getDisplayType().getLocalizedMessage(locale), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                }
                listDocumentAfter.clear();

                for (EPMDocument x : listEpmDocumentAfter) {
                    if (x.getDocType().getStringValue().equals(CADDRAWING)){
                        LWCNormalizedObject lwcEpmDoc = new LWCNormalizedObject(x, null, locale, new DisplayOperationIdentifier());
                        lwcEpmDoc.load("ATR_DOC_TYPE");
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), (String) lwcEpmDoc.get("ATR_DOC_TYPE"), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                    } else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                x.getName(), x.getDisplayType().getLocalizedMessage(locale), x.getNumber(),
                                x.getVersionInfo().getIdentifier().getValue() + "." +
                                        x.getIterationInfo().getIdentifier().getValue()));
                }
                listEpmDocumentAfter.clear();

                Collections.sort(listData, new Comparator<DataBean>() {
                    @Override
                    public int compare(DataBean o1, DataBean o2) {
                        if (o1.getNumberWT() == null || o2.getNumberWT() == null)
                            return 0;
                        return o1.getNumberWT().compareTo(o2.getNumberWT());
                    }
                });

//////////////////////////////////////////////////// WorkItem
                Enumeration processes = WfEngineHelper.service.getAssociatedProcesses(x1, null, null);
                while (processes.hasMoreElements()) {
                    WfProcess process = (WfProcess) processes.nextElement();

                    if (!process.getState().equals(WfState.CLOSED_TERMINATED)) {
                        NmOid nmOid = new NmOid(process);
                        QueryResult status = WorkflowCommands.getRouteStatus(nmOid);

                        while (status.hasMoreElements()) {
                            Object obj = status.nextElement();

                            if (obj.getClass().isAssignableFrom(WorkItem.class) && ((WorkItem) obj).getStatus().equals(WfAssignmentState.COMPLETED)) {
                                listWI.add((WorkItem) obj);
                            } else if (obj.getClass().isAssignableFrom(WorkItem.class)) {
                                listWINotFinished.add((WorkItem) obj);
                            } else if (obj.getClass().isAssignableFrom(WfVotingEventAudit.class)) {
                                WfVotingEventAudit wfVotingEventAudit = (WfVotingEventAudit) obj;
                                listWI.add(wfVotingEventAudit.getWorkItem());
                            }

                        }
                    }

                }

                int i = listWI.size() - 1;

                for (WorkItem x : listWI) {
                    WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
                    WTArrayList wtArrayList = (WTArrayList) WfEngineHelper.service.getVotingEvents(wfaa.getParentProcess(), null, null, null);
                    wtRef = refFact.getReference(wtArrayList.get(i).toString());
                    WfVotingEventAudit vea = (WfVotingEventAudit) wtRef.getObject();
                    wtRef = vea.getUserRef();
                    WTUser user = (WTUser) wtRef.getObject();

                    String commentary = vea.getUserComment();
                    boolean containsEnter = DataBean.isContainsEnter(commentary);
                    boolean moreThanField = DataBean.isMoreThenTextFieldInCommentary(commentary);
                    String[] arrEnter;
                    String[] arrMore;
                    ArrayList<String> listString = new ArrayList<String>();

                    if (containsEnter) {

                        arrEnter = commentary.split("\n");

                        for (String s : arrEnter) {

                            if (DataBean.isMoreThenTextFieldInCommentary(s)) {

                                arrMore = s.split(" ");
                                String tempStr1 = "";
                                for (int j = 0; j < arrMore.length; j++) {
                                    tempStr1 += " " + arrMore[j];

                                    if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                                        listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                                        tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                                    }

                                    if (j == arrMore.length - 1)
                                        listString.add(tempStr1.substring(1));
                                }

                            } else {

                                listString.add(s);

                            }

                        }

                        for (int j = 0; j < listString.size(); j++) {
                            if (j == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                        vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                        listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        " ", "", "", "", listString.get(j), ""));
                        }
                        listString.clear();

                    } else if (moreThanField) {

                        arrMore = commentary.split(" ");
                        String tempStr1 = "";

                        for (int j = 0; j < arrMore.length; j++) {
                            tempStr1 += " " + arrMore[j];

                            if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                                listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                                tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                            }

                            if (j == arrMore.length - 1)
                                listString.add(tempStr1.substring(1));
                        }

                        for (int j = 0; j < listString.size(); j++) {
                            if (j == 0)
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                        vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                        listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                            else
                                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                        " ", "", "", "", listString.get(j), ""));
                        }
                        listString.clear();

                    } else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                commentary, dateFormat.format(vea.getCreateTimestamp()).toString()));
                    i--;
                }
                listWI.clear();

                for (WorkItem x : listWINotFinished) {
                    WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
                    listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                            wfaa.getName(),x.getRole().getDisplay(locale), x.getOwnership().getOwner().getFullName(),
                            " ", " ", "Выполняется"));
                }
                listWINotFinished.clear();

                String jasperFile = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\ext\\by\\peleng\\CA\\listReport1.jasper";
                File reportFile1 = new File(jasperFile);
                String outFileName = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\netmarkets\\jsp\\by\\peleng\\" + params.get("CHANGE_NOTICE_NAME") + ".pdf";
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listData);
                try {
                    JasperReport jr = (JasperReport) JRLoader.loadObject(reportFile1);
                    JasperPrint print = JasperFillManager.fillReport(jr, params, dataSource);
                    JasperExportManager.exportReportToPdfFile(print, outFileName);
                } catch (JRException e){
                    e.printStackTrace();
                }
%>
<p align="center">
    <a
            href="https://wch.peleng.jsc.local/Windchill/netmarkets/jsp/by/peleng/<%=params.get("CHANGE_NOTICE_NAME")%>.pdf"
            download="<%=params.get("CHANGE_NOTICE_NAME")%>">
        Просмотр отчета
    </a>
</p>
<%
    }
}
else if (lwcNormalizedObject.get("ATR_TYPE_CA").equals("TPP")){ //если мы пытаемся получить отчет для светлозеленого чемоданчика
%>
<p align="center">Не имеет связанной КД.</p>
<%
}
else if (lwcNormalizedObject.get("ATR_TYPE_CA").equals("PLAN")){ //получаем отчет для темнозеленого чемоданчика
    qr = ChangeHelper2.service.getChangeOrder(ca2);
    co2 = (WTChangeOrder2) qr.nextElement();

    lwcNormalizedObject = new LWCNormalizedObject(co2, null, locale, new DisplayOperationIdentifier());
    lwcNormalizedObject.load("ATR_COMPLEXITY", "ATR_ECN_TYPE");

    String changeNoticeName = co2.getDisplayType().getLocalizedMessage(locale) + " " + co2.getNumber();
    params.put("CHANGE_NOTICE_NAME", changeNoticeName);
    String changeNoticeComplexity = (String) lwcNormalizedObject.get("ATR_COMPLEXITY");
    String changeNoticeType = (String) lwcNormalizedObject.get("ATR_ECN_TYPE");
    params.put("CHANGE_NOTICE_COMPLEXITY", DataBean.getShortComplexity(changeNoticeComplexity, changeNoticeType));


    qr = ChangeHelper2.service.getChangeActivities(co2);
    while (qr.hasMoreElements()){
        ca2 = (WTChangeActivity2) qr.nextElement();
        lwcNormalizedObject = new LWCNormalizedObject(ca2, null, locale, new DisplayOperationIdentifier());
        lwcNormalizedObject.load("ATR_TYPE_CA");
        if (lwcNormalizedObject.get("ATR_TYPE_CA") != null){
            if (lwcNormalizedObject.get("ATR_TYPE_CA").equals("TPP"))
                listWTCA2TPP.add(ca2);
            else if (lwcNormalizedObject.get("ATR_TYPE_CA").equals("PLAN"))
                listWTCA2PLAN.add(ca2);
        }
    }

    Collections.sort(listWTCA2TPP, new Comparator<WTChangeActivity2>() {
        @Override
        public int compare(WTChangeActivity2 o1, WTChangeActivity2 o2) {
            return o1.getNumber().compareTo(o2.getNumber());
        }
    });

    for (WTChangeActivity2 x1 : listWTCA2PLAN) {
        lwcNormalizedObject = new LWCNormalizedObject(x1, null, locale, new DisplayOperationIdentifier());
        lwcNormalizedObject.load("number", "ATR_DEPARTMENT", "iterationInfo.modifier", "thePersistInfo.modifyStamp");
        String numberCA2 = (String) lwcNormalizedObject.get("number");
        String modifyCA2 = dateFormat.format(lwcNormalizedObject.get("thePersistInfo.modifyStamp")).toString();
        String stateCA2 = x1.getState().getState().getFullDisplay();

        WCTypeInstanceIdentifier tiiUserModifier = (WCTypeInstanceIdentifier) lwcNormalizedObject.get("iterationInfo.modifier");
        WTUser userModifier =  (WTUser) (new ReferenceFactory()).getReference(tiiUserModifier.getPersistenceIdentifier()).getObject();
        String modifierCA2 = userModifier.getFullName();

        String departmentCA2 = "";
        AttributeTypeSummary ats = lwcNormalizedObject.getAttributeDescriptor("ATR_DEPARTMENT");
        DataSet ds = ats.getLegalValueSet();
        EnumeratedSet enumSet = (EnumeratedSet) (ds.getIntersection(ds));
        EnumerationEntryIdentifier idntify = enumSet.getElementByKey(lwcNormalizedObject.get("ATR_DEPARTMENT"));
        if (idntify != null){
            LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();
            departmentCA2 = eevf.get(idntify, locale).getDisplay();
        }

        Enumeration processes = WfEngineHelper.service.getAssociatedProcesses(x1, null, null);
        while (processes.hasMoreElements()) {
            WfProcess process = (WfProcess) processes.nextElement();

            if (!process.getState().equals(WfState.CLOSED_TERMINATED)) {
                NmOid nmOid = new NmOid(process);
                QueryResult status = WorkflowCommands.getRouteStatus(nmOid);

                while (status.hasMoreElements()) {
                    Object obj = status.nextElement();

                    if (obj.getClass().isAssignableFrom(WorkItem.class) && ((WorkItem) obj).getStatus().equals(WfAssignmentState.COMPLETED)) {
                        listWI.add((WorkItem) obj);
                    } else if (obj.getClass().isAssignableFrom(WorkItem.class)) {
                        listWINotFinished.add((WorkItem) obj);
                    } else if (obj.getClass().isAssignableFrom(WfVotingEventAudit.class)) {
                        WfVotingEventAudit wfVotingEventAudit = (WfVotingEventAudit) obj;
                        listWI.add(wfVotingEventAudit.getWorkItem());
                    }

                }
            }

        }

        numberTPP = 1;
//////////////////////////////////////////////////////////// ChangeActivity2 TPP
        for (WTChangeActivity2 x : listWTCA2TPP) {
            lwcNormalizedObject = new LWCNormalizedObject(x, null, locale, new DisplayOperationIdentifier());
            lwcNormalizedObject.load("ATR_DEPARTMENT", "needDate", "ATR_CA_READY");
            String readyCA = (String) lwcNormalizedObject.get("ATR_CA_READY");
            String deadLineTPP = "";

            if (readyCA != null){
                if ((readyCA.equals("Выполнено") || readyCA.equals("Согл.графика")))
                    deadLineTPP = readyCA;
                else if (lwcNormalizedObject.get("needDate") != null)
                    deadLineTPP = dateFormatTPP.format(lwcNormalizedObject.get("needDate")).toString();
            } else if (lwcNormalizedObject.get("needDate") != null)
                deadLineTPP = dateFormatTPP.format(lwcNormalizedObject.get("needDate")).toString();

            String departmentTPP = "";
            ats = lwcNormalizedObject.getAttributeDescriptor("ATR_DEPARTMENT");
            ds = ats.getLegalValueSet();
            enumSet = (EnumeratedSet) (ds.getIntersection(ds));
            idntify = enumSet.getElementByKey(lwcNormalizedObject.get("ATR_DEPARTMENT"));
            LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();

            if (idntify != null){
                departmentTPP = eevf.get(idntify, locale).getDisplay();
            } else if (idntify == null){
                IBAHolder holder = IBAValueHelper.service.refreshAttributeContainer(x, null, locale, null);
                DefaultAttributeContainer dac = (DefaultAttributeContainer) holder.getAttributeContainer();
                AttributeDefDefaultView addv = dac.getAttributeValues()[0].getDefinition();
                AbstractValueView[] avv = dac.getAttributeValues(addv);

                for (int i = 0; i < avv.length; i++) {
                    idntify = enumSet.getElementByKey(IBAValueUtility.getLocalizedIBAValueDisplayString(avv[i], locale));
                    String str = eevf.get(idntify,locale).getFullDisplay();
                    if (i != (avv.length - 1))
                        departmentTPP +=  str + ", ";
                    else
                        departmentTPP += str;
                }
            }
//////////////////////////////////////////////// checked length field DescriptionTPP
            String descriptionTPP = x.getDescription();
            boolean containsEnter = DataBean.isContainsEnter(descriptionTPP);
            boolean moreThanField = DataBean.isMoreThenTextField(descriptionTPP);
            String[] arrEnter;
            String[] arrMore;
            ArrayList<String> listString = new ArrayList<String>();

            if (containsEnter){
                arrEnter = descriptionTPP.split("\r\n");
                for (String s : arrEnter) {
                    if (DataBean.isMoreThenTextField(s)){
                        arrMore = s.split(" ");
                        String tempStr1 = "";
                        for (int i = 0; i < arrMore.length; i++) {
                            tempStr1 += " " + arrMore[i];

                            if (DataBean.isMoreThenTextField(tempStr1)){
                                listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[i].length()));
                                tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[i].length());
                            }

                            if (i == arrMore.length - 1)
                                listString.add(tempStr1.substring(1));
                        }
                    } else
                        listString.add(s);
                }

                for (int i = 0; i < listString.size(); i++) {
                    if (i == 0)
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                numberTPP, listString.get(0), departmentTPP, deadLineTPP));
                    else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                0, listString.get(i), "", ""));
                }
                listString.clear();

            } else if (moreThanField){
                arrMore = descriptionTPP.split(" ");
                String tempStr1 = "";
                for (int i = 0; i < arrMore.length; i++) {
                    tempStr1 += " " + arrMore[i];

                    if (DataBean.isMoreThenTextField(tempStr1)){
                        listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[i].length()));
                        tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[i].length());
                    }

                    if (i == arrMore.length - 1)
                        listString.add(tempStr1.substring(1));
                }

                for (int i = 0; i < listString.size(); i++) {
                    if (i == 0)
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                numberTPP, listString.get(0), departmentTPP, deadLineTPP));
                    else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                0, listString.get(i), "", ""));
                }
                listString.clear();

            } else
                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                        numberTPP, descriptionTPP, departmentTPP, deadLineTPP));
            numberTPP++;
        }

        int i = listWI.size() - 1;

        for (WorkItem x : listWI) {
            WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
            WTArrayList wtArrayList = (WTArrayList) WfEngineHelper.service.getVotingEvents(wfaa.getParentProcess(), null, null, null);
            wtRef = refFact.getReference(wtArrayList.get(i).toString());
            WfVotingEventAudit vea = (WfVotingEventAudit) wtRef.getObject();
            wtRef = vea.getUserRef();
            WTUser user = (WTUser) wtRef.getObject();

            String commentary = vea.getUserComment();
            boolean containsEnter = DataBean.isContainsEnter(commentary);
            boolean moreThanField = DataBean.isMoreThenTextFieldInCommentary(commentary);
            String[] arrEnter;
            String[] arrMore;
            ArrayList<String> listString = new ArrayList<String>();

            if (containsEnter) {

                arrEnter = commentary.split("\n");

                for (String s : arrEnter) {

                    if (DataBean.isMoreThenTextFieldInCommentary(s)) {

                        arrMore = s.split(" ");
                        String tempStr1 = "";
                        for (int j = 0; j < arrMore.length; j++) {
                            tempStr1 += " " + arrMore[j];

                            if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                                listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                                tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                            }

                            if (j == arrMore.length - 1)
                                listString.add(tempStr1.substring(1));
                        }

                    } else {

                        listString.add(s);

                    }

                }

                for (int j = 0; j < listString.size(); j++) {
                    if (j == 0)
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                    else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                " ", "", "", "", listString.get(j), ""));
                }
                listString.clear();

            } else if (moreThanField) {

                arrMore = commentary.split(" ");
                String tempStr1 = "";

                for (int j = 0; j < arrMore.length; j++) {
                    tempStr1 += " " + arrMore[j];

                    if (DataBean.isMoreThenTextFieldInCommentary(tempStr1)){
                        listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                        tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
                    }

                    if (j == arrMore.length - 1)
                        listString.add(tempStr1.substring(1));
                }

                for (int j = 0; j < listString.size(); j++) {
                    if (j == 0)
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                                vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                                listString.get(0), dateFormat.format(vea.getCreateTimestamp()).toString()));
                    else
                        listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                                " ", "", "", "", listString.get(j), ""));
                }
                listString.clear();

            } else
                listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                        vea.getActivityName(), vea.getRole().getLocalizedMessage(locale), user.getFullName(),
                        vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1),
                        commentary, dateFormat.format(vea.getCreateTimestamp()).toString()));
            i--;
        }
        listWI.clear();

        for (WorkItem x : listWINotFinished) {
            WfAssignedActivity wfaa = (WfAssignedActivity) x.getSource().getObject();
            listData.add(new DataBean(numberCA2, departmentCA2, modifierCA2, modifyCA2, stateCA2,
                    wfaa.getName(),x.getRole().getDisplay(locale), x.getOwnership().getOwner().getFullName(),
                    " ", " ", "Выполняется"));
        }
        listWINotFinished.clear();

    }
/////////////////////////////////////////////// Print ReportECN
    String jasperFile = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\ext\\by\\peleng\\CA\\listReport1.jasper";
    File reportFile1 = new File(jasperFile);
    String outFileName = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\netmarkets\\jsp\\by\\peleng\\" + params.get("CHANGE_NOTICE_NAME") + ".pdf";
    JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listData);
    try {
        JasperReport jr = (JasperReport) JRLoader.loadObject(reportFile1);
        JasperPrint print = JasperFillManager.fillReport(jr, params, dataSource);
        JasperExportManager.exportReportToPdfFile(print, outFileName);
    } catch (JRException e){
        e.printStackTrace();
    }
%>
<p align="center">
    <a href="https://wch.peleng.jsc.local/Windchill/netmarkets/jsp/by/peleng/<%=params.get("CHANGE_NOTICE_NAME")%>.pdf"
       download="<%=params.get("CHANGE_NOTICE_NAME")%>">Просмотр отчета</a>
</p>
<%
    }

} catch (WTException e) {
%>
<p>Что-то пошло не так</p>
<%
        }

    }
%>

<%!
    public static class DataBean {
        private String numberCA2;
        private String departmentCA2;
        private String modifierCA2;
        private String modifyCA2;
        private String stateCA2;
        private String nameWT;
        private String typeWT;
        private String numberWT;
        private String versionWT;
        private String nameVEA;
        private String roleVEA;
        private String userVEA;
        private String voteVEA;
        private String userCommentVEA;
        private String deadLineVEA;
        private Integer numberTPP;
        private String nameTPP;
        private String departmentTPP;
        private String deadLineTPP;

        public DataBean(String numberCA2, String departmentCA2, String modifierCA2, String modifyCA2,
                        String stateCA2, String nameVEA, String roleVEA, String userVEA, String voteVEA,
                        String userCommentVEA, String deadLineVEA) {
            this.numberCA2 = numberCA2;
            this.departmentCA2 = departmentCA2;
            this.modifierCA2 = modifierCA2;
            this.modifyCA2 = modifyCA2;
            this.stateCA2 = stateCA2;
            this.nameVEA = nameVEA;
            this.roleVEA = roleVEA;
            this.userVEA = userVEA;
            this.voteVEA = voteVEA;
            this.userCommentVEA = userCommentVEA;
            this.deadLineVEA = deadLineVEA;
        }

        public DataBean(String numberCA2, String departmentCA2, String modifierCA2, String modifyCA2,
                        String stateCA2, String nameWT, String typeWT, String numberWT, String versionWT) {
            this.numberCA2 = numberCA2;
            this.departmentCA2 = departmentCA2;
            this.modifierCA2 = modifierCA2;
            this.modifyCA2 = modifyCA2;
            this.stateCA2 = stateCA2;
            this.nameWT = nameWT;
            this.typeWT = typeWT;
            this.numberWT = numberWT;
            this.versionWT = versionWT;
        }

        public DataBean(String numberCA2, String departmentCA2, String modifierCA2, String modifyCA2,
                        String stateCA2, Integer numberTPP, String nameTPP, String departmentTPP, String deadLineTPP) {
            this.numberCA2 = numberCA2;
            this.departmentCA2 = departmentCA2;
            this.modifierCA2 = modifierCA2;
            this.modifyCA2 = modifyCA2;
            this.stateCA2 = stateCA2;
            this.numberTPP = numberTPP;
            this.nameTPP = nameTPP;
            this.departmentTPP = departmentTPP;
            this.deadLineTPP = deadLineTPP;
        }

        public String getNumberCA2() {
            return numberCA2;
        }

        public void setNumberCA2(String numberCA2) {
            this.numberCA2 = numberCA2;
        }

        public String getDepartmentCA2() {
            return departmentCA2;
        }

        public void setDepartmentCA2(String departmentCA2) {
            this.departmentCA2 = departmentCA2;
        }

        public String getModifierCA2() {
            return modifierCA2;
        }

        public void setModifierCA2(String modifierCA2) {
            this.modifierCA2 = modifierCA2;
        }

        public String getModifyCA2() {
            return modifyCA2;
        }

        public void setModifyCA2(String modifyCA2) {
            this.modifyCA2 = modifyCA2;
        }

        public String getStateCA2() {
            return stateCA2;
        }

        public void setStateCA2(String stateCA2) {
            this.stateCA2 = stateCA2;
        }

        public String getNameWT() {
            return nameWT;
        }

        public void setNameWT(String nameWT) {
            this.nameWT = nameWT;
        }

        public String getTypeWT() {
            return typeWT;
        }

        public void setTypeWT(String typeWT) {
            this.typeWT = typeWT;
        }

        public String getNumberWT() {
            return numberWT;
        }

        public void setNumberWT(String numberWT) {
            this.numberWT = numberWT;
        }

        public String getVersionWT() {
            return versionWT;
        }

        public void setVersionWT(String versionWT) {
            this.versionWT = versionWT;
        }

        public String getNameVEA() {
            return nameVEA;
        }

        public void setNameVEA(String nameVEA) {
            this.nameVEA = nameVEA;
        }

        public String getRoleVEA() {
            return roleVEA;
        }

        public void setRoleVEA(String roleVEA) {
            this.roleVEA = roleVEA;
        }

        public String getUserVEA() {
            return userVEA;
        }

        public void setUserVEA(String userVEA) {
            this.userVEA = userVEA;
        }

        public String getVoteVEA() {
            return voteVEA;
        }

        public void setVoteVEA(String voteVEA) {
            this.voteVEA = voteVEA;
        }

        public String getUserCommentVEA() {
            return userCommentVEA;
        }

        public void setUserCommentVEA(String userCommentVEA) {
            this.userCommentVEA = userCommentVEA;
        }

        public String getDeadLineVEA() {
            return deadLineVEA;
        }

        public void setDeadLineVEA(String deadLineVEA) {
            this.deadLineVEA = deadLineVEA;
        }

        public Integer getNumberTPP() {
            return numberTPP;
        }

        public void setNumberTPP(Integer numberTPP) {
            this.numberTPP = numberTPP;
        }

        public String getNameTPP() {
            return nameTPP;
        }

        public void setNameTPP(String nameTPP) {
            this.nameTPP = nameTPP;
        }

        public String getDepartmentTPP() {
            return departmentTPP;
        }

        public void setDepartmentTPP(String departmentTPP) {
            this.departmentTPP = departmentTPP;
        }

        public String getDeadLineTPP() {
            return deadLineTPP;
        }

        public void setDeadLineTPP(String deadLineTPP) {
            this.deadLineTPP = deadLineTPP;
        }

        public static String getShortComplexity(String changeNoticeComplexity, String changeNoticeType){
            if ((changeNoticeComplexity + " " + changeNoticeType).equals("Подготовка производства ИИ"))
                return "П";
            else if ((changeNoticeComplexity + " " + changeNoticeType).equals("Подготовка производства ПИ"))
                return "ПИП";
            else if ((changeNoticeComplexity + " " + changeNoticeType).equals("Без подготовки производства ИИ"))
                return "";
            else
                return "ПИ";
        }

        public static boolean isContainsEnter(String description){
            return ((description.contains("\n")) ? true : false);
        }

        public static boolean isMoreThenTextField(String description){
            return ((description.length() > 110) ? true : false);
        }

        public static boolean isMoreThenTextFieldInCommentary(String commentary){
            return ((commentary.length() > 50) ? true : false);
        }

        public static String[] splitStringOnArray(String description){
            String[] arrStr;
            return null;
        }

    }
%>

<%!
    private static boolean checkSoftType(Iterated object, String softType) {
        TypeIdentifier typeIdentifier = TypeIdentifierHelper.getType(object);
        TypeIdentifier root = TypeIdentifierHelper.getTypeIdentifier(softType);


        return typeIdentifier.isDescendedFrom(root);
    }
%>

</html>