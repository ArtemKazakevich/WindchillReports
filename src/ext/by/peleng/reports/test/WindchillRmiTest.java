//package ext.by.peleng.reports.test;
//
//import wt.fc.PersistenceHelper;
//import wt.fc.QueryResult;
//import wt.method.RemoteAccess;
//import wt.part.WTPart;
//import wt.pds.StatementSpec;
//import wt.query.QuerySpec;
//import wt.query.SearchCondition;
//import wt.util.WTException;
//
//import java.io.Serializable;
//
//public class WindchillRmiTest implements RemoteAccess, Serializable {
//
//    public static String getProductStructureByPartNumber (String oid) throws WTException  {
//
//        WTPart part = null;
//        String result = "";
//
//        QuerySpec qs = new QuerySpec(wt.part.WTPart.class);
//
//        qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER, SearchCondition.LIKE, oid), new int[]{0,1} );
//
//        QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
//
//        while (qr.hasMoreElements()) {
//
//            part = (WTPart) qr.nextElement();
//
//            result = "Number: " + part.getNumber() +
//                    ", Name: " + part.getName() +
//                    ", Version: " + part.getVersionInfo().getIdentifier().getValue() +
//                    "." + part.getIterationInfo().getIdentifier().getValue();
//
//            System.out.println(result);
//
//        }
//
//        return result;
//
//    }
//
//    private WTPart getWTPartByPartNumber(String partNumber) {
//
//        WTPart part = null;
//
//
//
//        return part;
//    }
//
//}
