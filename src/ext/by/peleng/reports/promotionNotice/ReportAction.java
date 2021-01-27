package ext.by.peleng.reports.promotionNotice;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.meta.common.DisplayOperationIdentifier;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.fc.collections.WTArrayList;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfVotingEventAudit;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportAction {
    private final String oid;
    private final Locale locale = getLocale();
    private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm ", locale);
    private final String[] attrsPN;
    private final Map<String, ArrayList<Object>> promotionTargets;
    private final Map<String, ArrayList<WorkItem>> workItems;
    private final Map<String, Object> params;
    private final List<DataReport> listData = new ArrayList<DataReport>();

    public ReportAction(String oid ,String[] attrsPN, Map<String, ArrayList<Object>> promotionTargets,
                        Map<String, ArrayList<WorkItem>> workItems, Map<String, Object> params) {
        this.oid = oid;
        this.attrsPN = attrsPN;
        this.promotionTargets = promotionTargets;
        this.workItems = workItems;
        this.params = params;
    }

    public void reportGeneration() {
        handlingPromotionTargets();
        handlingWorkItems();
        generatePdf();
    }

    private void handlingPromotionTargets(){
        Iterator<Map.Entry<String, ArrayList<Object>>> iteratorPT = promotionTargets.entrySet().iterator();

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
                    if (wtDocument.getDisplayType().getLocalizedMessage(locale).trim().equals("Документация")) {
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
                    if (epmDocument.getDocType().getStringValue().equals("wt.epm.EPMDocumentType.CADDRAWING")) {
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
                listData.add(new DataReport(attrsPN[0], attrsPN[1], attrsPN[2], attrsPN[3], attrsPN[4],
                        typePromotionTarget, numberPromotionTarget, namePromotionTarget, versionPromotionTarget, statePromotionTarget));
            }
        }
    }

    private void handlingWorkItems(){
        Iterator<Map.Entry<String, ArrayList<WorkItem>>> iteratorWI = workItems.entrySet().iterator();

        while (iteratorWI.hasNext()) {
            Map.Entry<String, ArrayList<WorkItem>> entry = iteratorWI.next();
            int i = entry.getValue().size() - 1;

            for (WorkItem workItem : entry.getValue()) {
                String nameWorkItem = "";
                String roleForWorkItem = "";
                String userWhoCompletedWorkItem = "";
                String choiceForWorkItem = "";
                String commentForWorkItem = "";
                String dateFinishedWorkItem = "";
                WfAssignedActivity wfaa = (WfAssignedActivity) workItem.getSource().getObject();
                ArrayList<String> listStringInComment = new ArrayList<String>();

                if ("listWI".equals(entry.getKey())) {
                    WfVotingEventAudit vea = getWfVotingEventAudit(wfaa, i, oid);
                    WTUser user = (WTUser) vea.getUserRef().getObject();
                    nameWorkItem = vea.getActivityName();
                    roleForWorkItem = vea.getRole().getLocalizedMessage(locale);
                    userWhoCompletedWorkItem = user.getFullName().replace(",", "");
                    choiceForWorkItem = vea.getEventList().toString().substring(1, vea.getEventList().toString().length() - 1).replace("_", " ");
                    commentForWorkItem = vea.getUserComment();
                    listStringInComment.addAll(parseUserComment(commentForWorkItem));
                    dateFinishedWorkItem = dateFormat.format(vea.getCreateTimestamp());
                    i--;
                } else if ("listWINotFinished".equals(entry.getKey())) {
                    nameWorkItem = wfaa.getName();
                    roleForWorkItem = workItem.getRole().getDisplay(locale);
                    userWhoCompletedWorkItem = workItem.getOwnership().getOwner().getFullName().replace(",", "");
                    dateFinishedWorkItem = "Выполняется";
                }

                if (listStringInComment.size() > 0) {
                    for (int j = 0; j < listStringInComment.size(); j++) {
                        if (j == 0) {
                            listData.add(new DataReport(attrsPN[0], attrsPN[1], attrsPN[2], attrsPN[3], attrsPN[4],
                                    nameWorkItem, roleForWorkItem, userWhoCompletedWorkItem, choiceForWorkItem, listStringInComment.get(j), dateFinishedWorkItem));
                        } else {
                            listData.add(new DataReport(attrsPN[0], attrsPN[1], attrsPN[2], attrsPN[3], attrsPN[4],
                                    " ", "", "", "", listStringInComment.get(j), ""));
                        }
                    }
                } else {
                    listData.add(new DataReport(attrsPN[0], attrsPN[1], attrsPN[2], attrsPN[3], attrsPN[4],
                            nameWorkItem, roleForWorkItem, userWhoCompletedWorkItem, choiceForWorkItem, commentForWorkItem, dateFinishedWorkItem));
                }
            }
        }
    }

    private Locale getLocale() {

        Locale locale = null;

        try {

            locale = SessionHelper.manager.getLocale();

        } catch (WTException e) {

            e.printStackTrace();

        }

        return locale;

    }

    private String getTypeDocument(Object object, Locale locale) {

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

    private WfVotingEventAudit getWfVotingEventAudit(WfAssignedActivity wfaa, int i, String oid) {

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

    private void generatePdf() {
        String jasperFile = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\ext\\by\\peleng\\reports\\promotionNotice\\listReport2.jasper";
        File reportFile1 = new File(jasperFile);
        String outFileName = "D:\\ptc\\Windchill_11.0\\Windchill\\codebase\\netmarkets\\jsp\\by\\peleng\\" + params.get("CHANGE_PN_NAME") + ".pdf";
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listData);
        try {
            JasperReport jr = (JasperReport) JRLoader.loadObject(reportFile1);
            JasperPrint print = JasperFillManager.fillReport(jr, params, dataSource);
            JasperExportManager.exportReportToPdfFile(print, outFileName);
        } catch (JRException e){
            e.printStackTrace();
        }
    }

    private boolean isContainsEnter(String description){
        return (description.contains("\n"));
    }

    private boolean isMoreThenTextFieldInCommentary(String commentary){
        return (commentary.length() > 50);
    }

    private ArrayList<String> gettingListString(String[] arrMore) {
        ArrayList<String> listString = new ArrayList<String>();
        String tempStr1 = "";
        for (int j = 0; j < arrMore.length; j++) {
            tempStr1 += " " + arrMore[j];
            if (isMoreThenTextFieldInCommentary(tempStr1)){
                listString.add(tempStr1.substring(1, tempStr1.length() - arrMore[j].length()));
                tempStr1 = tempStr1.substring(tempStr1.length() - arrMore[j].length());
            }
            if (j == arrMore.length - 1)
                listString.add(tempStr1.substring(1));
        }
        return listString;
    }

    private ArrayList<String> parseUserComment(String commentary) {
        boolean containsEnter = isContainsEnter(commentary);
        boolean moreThanField = isMoreThenTextFieldInCommentary(commentary);
        String[] arrEnter;
        String[] arrMore;
        ArrayList<String> listString = new ArrayList<String>();

        if (containsEnter) {
            arrEnter = commentary.split("\n");

            for (String s : arrEnter) {
                if (isMoreThenTextFieldInCommentary(s)) {
                    arrMore = s.split(" ");
                    listString.addAll(gettingListString(arrMore));
                } else {
                    listString.add(s);
                }
            }
        } else if (moreThanField) {
            arrMore = commentary.split(" ");
            listString.addAll(gettingListString(arrMore));
        }

        return listString;
    }

    public String[] getAttrsPN() {
        return attrsPN;
    }

    public Map<String, ArrayList<Object>> getPromotionTargets() {
        return promotionTargets;
    }

    public Map<String, ArrayList<WorkItem>> getWorkItems() {
        return workItems;
    }
}
