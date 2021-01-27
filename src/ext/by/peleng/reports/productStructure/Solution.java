package ext.by.peleng.reports.productStructure;

import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;

public class Solution {

    public static TreeNode start(String oid) throws WTException {

        Remark.clearRemarksList();

        WTPart part = gettingWtPartByOid(oid);

        TreeNode treeHead = new TreeNode(part); //получаем голову изделия

        gettingBomForPart(part, treeHead); //получаем полный состав изделия для головы изделия

        int level = 0;

        Remark.structureCheck(treeHead, level);

        return treeHead;

    }

    @SuppressWarnings("deprecation")
    private static void gettingBomForPart(WTPart part, TreeNode parentNode) {

        QueryResult queryResult = null;

        try {

            queryResult = WTPartHelper.service.getUsesWTParts(part, new LatestConfigSpec());

        } catch (Exception e) {

            System.err.println(e.getMessage());

        }

        assert queryResult != null;
        if (queryResult.size() != 0) {

            while (queryResult.hasMoreElements()) {

                Persistable[] persistable = (Persistable[]) queryResult.nextElement();

                WTPart wtPart = null;

                Object object = persistable[1];

                if (object instanceof WTPart) {

                    wtPart = (WTPart) object;

                } else if (object instanceof WTPartMaster){

                    WTPartMaster master = (WTPartMaster) object;

                    String error = master.getNumber() + " - проблема с этой СЧ (не существует такого WTPart)";

                    if (!Remark.getRemarks().contains(error)) {

                        Remark.getRemarks().add(error);

                    }

                    continue;

                } else {

                    Remark.getRemarks().add("Неведомая фигня! За консультацией обращаться к Алексеевичу А.А.");

                    continue;

                }

                WTPartUsageLink partLink=(WTPartUsageLink)persistable[0];

                TreeNode node = new TreeNode(wtPart, partLink);

                parentNode.add(node);

                gettingBomForPart(wtPart, node);

            }

        }

    }

    private static WTPart gettingWtPartByOid(String oid) {

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

            return (WTPart) refObject;

        } else {

            return null;

        }

    }

}
