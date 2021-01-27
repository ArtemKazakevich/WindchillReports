package ext.by.peleng.reports.productStructure;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.meta.common.DisplayOperationIdentifier;
import wt.fc.QueryResult;
import wt.part.*;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Remark {

    private static List<String> remarks = new ArrayList<String>(); //сделать не List, а Map<partNumber, List<Код ошибки>>

    public static void structureCheck(TreeNode treeHead, int level) {

        int countOfChildrenInTheNodeWithoutPositionNumber = 0;

        WTPart headPart = treeHead.getWtPart(); //part - голова сборки или подсборки

        LWCNormalizedObject lwcNormalizedObject = gettingLwcForPart(headPart);

        String specificationSection = gettingSpecificationSectionForPart(lwcNormalizedObject);

        checkPartForSpecificationSection(headPart.getNumber(), specificationSection);

        checkPartForTheRightNumber(headPart.getNumber(), specificationSection, headPart.getName());

        String materialName = gettingMaterialNameForPartWithSpecificationSectionEqualDetail(lwcNormalizedObject);

        checkDetailForFillingFieldMaterialName(headPart.getNumber(), specificationSection, materialName);

        String primaryUsage = gettingPrimaryUsageForPart(lwcNormalizedObject);

        checkPartForFillingFieldPrimaryUsage(headPart.getNumber(), specificationSection, primaryUsage);

        TreeNode[] children = treeHead.getChildren();

        level++;

        for (TreeNode child : children) {

            WTPart childPart = child.getWtPart();

            WTPartUsageLink childPartUsageLink = child.getWtPartUsageLink();

            LWCNormalizedObject childLwcNormalizedObject = gettingLwcForPart(childPart);

            String childSpecificationSection = gettingSpecificationSectionForPart(childLwcNormalizedObject);

            checkPartForSpecificationSection(childPart.getNumber(), childSpecificationSection);

            checkPartForTheRightNumber(childPart.getNumber(), childSpecificationSection, childPart.getName());

            materialName = gettingMaterialNameForPartWithSpecificationSectionEqualDetail(childLwcNormalizedObject);

            checkDetailForFillingFieldMaterialName(childPart.getNumber(), childSpecificationSection, materialName);

            primaryUsage = gettingPrimaryUsageForPart(childLwcNormalizedObject);

            checkPartForFillingFieldPrimaryUsage(childPart.getNumber(), childSpecificationSection, primaryUsage);

            String positionNumber = gettingPositionNumberForPart(childPartUsageLink, level);

            countOfChildrenInTheNodeWithoutPositionNumber =
                    gettingCountOfChildrenInTheNodeWithoutPositionNumber(countOfChildrenInTheNodeWithoutPositionNumber, positionNumber);

            checkPartForBlank(specificationSection, childSpecificationSection, children.length, headPart, childPart, childLwcNormalizedObject);

            if (child.hasChildren()) { // поиск и обработка WTPart-ов, у которых имеется состав

                structureCheck(child, level);

            }

            //QueryResult для замен
            QueryResult queryResult = gettingQueryResultForGettingSubstitutesWTPartMasters(childPartUsageLink);

            if (queryResult.size() != 0) {

                checkSubstitutesForTheRightNumber(queryResult);

            }

            //QueryResult для глобальных замен
            queryResult = gettingQueryResultForGettingAlternatesWTPartMasters((WTPartMaster) childPart.getMaster());

            if (queryResult.size() != 0) {

                addingErrorAlternativeParts(childPart);

                checkSubstitutesForTheRightNumber(queryResult);

            }

        }

        checkChildrenForPositionNumber(countOfChildrenInTheNodeWithoutPositionNumber, children.length, headPart.getNumber());

    }

    public static List<String> getRemarks() {

        return remarks;

    }

    public static void clearRemarksList() {

        remarks.clear();

    }

    private static LWCNormalizedObject gettingLwcForPart(WTPart part) {

        LWCNormalizedObject lwcNormalizedObject = null;

        try {

            Locale locale = wt.session.SessionHelper.manager.getLocale();

            lwcNormalizedObject = new LWCNormalizedObject(part, null, locale, new DisplayOperationIdentifier());

            lwcNormalizedObject.load("ATR_MATERIAL_DESCRIPTION","ATR_BOM_RS","ATR_PRIMARY_USE", "ATR_NUMBER_COMPOSITE");

        } catch (WTException e) {

            e.printStackTrace();

        }

        return lwcNormalizedObject;

    }

    private static String gettingPositionNumberForPart(WTPartUsageLink partUsageLink, int level) {

        if (level > 0) {

            LineNumber lineNumber = partUsageLink.getLineNumber();

            if (lineNumber != null) {

                return lineNumber.toString();

            } else {

                return "no position";

            }

        } else {

            return "Голова сборки";

        }

    }

    private static String gettingSpecificationSectionForPart(LWCNormalizedObject lwcNormalizedObject) {

        try {

            Object object = lwcNormalizedObject.get("ATR_BOM_RS");

            if (object instanceof Object[]) {

                Object[] values = (Object[]) object;

                StringBuilder result = new StringBuilder();

                for (int i = 0; i < values.length; i++) {

                    if (i == values.length - 1) {

                        result.append(values[i].toString());

                    } else {

                        result.append(values[i].toString()).append(", ");

                    }

                }

                return result.toString();

            } else {

                return (String) lwcNormalizedObject.get("ATR_BOM_RS");

            }

        } catch (WTException e) {

            return e.getMessage();

        }


    }

    private static String gettingMaterialNameForPartWithSpecificationSectionEqualDetail(LWCNormalizedObject lwcNormalizedObject) {

        try {

            Object object = lwcNormalizedObject.get("ATR_MATERIAL_DESCRIPTION");

            if (object instanceof Object[]) {

                Object[] values = (Object[]) object;

                StringBuilder result = new StringBuilder();

                for (int i = 0; i < values.length; i++) {

                    if (i == values.length - 1) {

                        result.append(values[i].toString());

                    } else {

                        result.append(values[i].toString()).append(", ");

                    }

                }

                return result.toString();

            } else {

                return (String) lwcNormalizedObject.get("ATR_MATERIAL_DESCRIPTION");

            }

        } catch (WTException e) {

            return e.getMessage();

        }

    }

    private static String gettingPrimaryUsageForPart(LWCNormalizedObject lwcNormalizedObject) {

        try {

            Object object = lwcNormalizedObject.get("ATR_PRIMARY_USE");

            if (object instanceof Object[]) {

                Object[] values = (Object[]) object;

                StringBuilder result = new StringBuilder();

                for (int i = 0; i < values.length; i++) {

                    if (i == values.length - 1) {

                        result.append(values[i].toString());

                    } else {

                        result.append(values[i].toString()).append(", ");

                    }

                }

                return result.toString();

            } else {

                return (String) lwcNormalizedObject.get("ATR_PRIMARY_USE");

            }

        } catch (WTException e) {

            return e.getMessage();

        }

    }

    private static QueryResult gettingQueryResultForGettingSubstitutesWTPartMasters(WTPartUsageLink partUsageLink) {

        QueryResult queryResult = null;

        try {

            queryResult = WTPartHelper.service.getSubstitutesWTPartMasters(partUsageLink);

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

    private static QueryResult gettingQueryResultForGettingAlternatesWTPartMasters(WTPartMaster partMaster) {

        QueryResult queryResult = null;

        try {

            queryResult = WTPartHelper.service.getAlternatesWTPartMasters(partMaster);

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

    private static QueryResult gettingQueryResultForGettingAllVersionsWTPart(WTPartMaster partMaster) {

        QueryResult queryResult = null;

        try {

            queryResult = VersionControlHelper.service.allVersionsOf(partMaster);

        } catch (WTException e) {

            e.printStackTrace();

        }

        return queryResult;

    }

    private static int gettingCountOfChildrenInTheNodeWithoutPositionNumber(int countOfChildrenInTheNodeWithoutPositionNumber, String positionNumber) {

        if ("no position".equals(positionNumber)) {

            return countOfChildrenInTheNodeWithoutPositionNumber + 1;

        } else {

            return countOfChildrenInTheNodeWithoutPositionNumber;

        }

    }

    private static void checkDetailForFillingFieldMaterialName(String partNumber, String specificationSection, String materialName) {

        if ("Детали".equals(specificationSection)) {

            if ((materialName == null) || (materialName.trim().isEmpty())) {

                String error = partNumber + " - не заполнен атрибут \"Наименование материала\"";

                if (!remarks.contains(error))
                    remarks.add(error);

            }
        }

    }

    private static void checkPartForFillingFieldPrimaryUsage(String partNumber, String specificationSection, String primaryUsage) {

        if (("Сборочные единицы".equals(specificationSection)) || ("Детали".equals(specificationSection)) || ("Комплекты".equals(specificationSection)) || ("Комплексы".equals(specificationSection))) {

            if ((primaryUsage == null) || (primaryUsage.trim().isEmpty())) {

                String error = partNumber + " - не заполнен атрибут \"Первичная применяемость\"";

                if (!remarks.contains(error))
                    remarks.add(error);

            }

        }

    }

    private static void checkChildrenForPositionNumber(int countOfChildrenInTheNodeWithoutPositionNumber, int countOfChildrenInTheNode, String partNumber) {

        if ((countOfChildrenInTheNodeWithoutPositionNumber > 0) && (countOfChildrenInTheNodeWithoutPositionNumber < countOfChildrenInTheNode)) {

            String error = partNumber + " - в данном узле не заполнены позиции (позиции в узле заполняются либо для каждой части, входящей в узел, либо вовсе не заполняются)";

            if (!remarks.contains(error)) {

                remarks.add(error);

            }

        }

    }

    private static void checkPartForTheRightNumber(String partNumber, String specificationSection, String partName) {

        boolean lengthNumberEqualsTwelve = (partNumber.length() == 12);

        boolean numberContainsOnlyDigits = isOnlyDigits(partNumber);

        if (("Прочие изделия".equals(specificationSection)) || ("Материалы".equals(specificationSection))) {

            checkCodeOKP(lengthNumberEqualsTwelve, numberContainsOnlyDigits, partNumber, partName);

        } else if ("Стандартные изделия".equals(specificationSection)) {

            String[] arrNum = partNumber.split("\\.");

            if (arrNum.length > 0) {

                for (String str : arrNum) {

                    if (!isOnlyDigitsOrDigitsAndDash(str)) {

                        String error = partNumber + " - неправильный код у стандартного изделия";

                        if (!remarks.contains(error)) {

                            remarks.add(error);

                        }

                        break;

                    }

                }

            } else {

                checkCodeOKP(lengthNumberEqualsTwelve, numberContainsOnlyDigits, partNumber, partName);

            }

        } else {

            if (partNumber.contains("_") || partNumber.contains("МД") || partNumber.contains(" ")) {

                String error = partNumber + " - неправильный код ДСЕ";

                if (!remarks.contains(error)) {

                    remarks.add(error);

                }

            }

        }

    }

    private static boolean isOnlyDigits(String partNumber) {

        return partNumber.matches("[\\d]+");

    }

    private static boolean isOnlyDigitsOrDigitsAndDash(String partNumber) {

        return partNumber.matches("(^[\\d]+)(-[\\d]+)?");

    }

    private static void checkCodeOKP(boolean lengthNumberEqualsTwelve, boolean numberContainsOnlyDigits, String partNumber, String partName) {

        if (!lengthNumberEqualsTwelve || (!numberContainsOnlyDigits)) {

            String error = partNumber + " - " + partName + " - присвоен временный код";

            if (!remarks.contains(error)) {

                remarks.add(error);

            }

        }

    }

    private static void checkPartForSpecificationSection(String partNumber, String specificationSection) {

        if ("Документация".equals(specificationSection) || "Отливки и оснастка".equals(specificationSection) || "Отсутствует".equals(specificationSection)) {

            String error = partNumber + " - неправильный раздел спецификации";

            if (!remarks.contains(error)){

                remarks.add(error);

            }

        }

    }

    private static void checkPartForBlank(String specificationSection, String childSpecificationSection, int countChildren, WTPart headPart, WTPart childPart, LWCNormalizedObject childLWC) { //Заготовка

        if ("Детали".equals(specificationSection)) { //входимость (заготовка)

            if ((!"Детали".equals(childSpecificationSection) && (!"Сборочные единицы".equals(childSpecificationSection)) &&
                    (!"Прочие изделия".equals(childSpecificationSection)) && (!"Стандартные изделия".equals(childSpecificationSection))) ||
                    countChildren > 1) {

                String error = "Для " + headPart.getNumber() + " неправильно назначена заготовка";

                if (!remarks.contains(error)) {

                    remarks.add(error);

                }

            }

        } else if ("Прочие изделия".equals(specificationSection)) {

            if (("Прочие изделия").equals(childSpecificationSection) || ("Стандартные изделия").equals(childSpecificationSection)) {

                String compositeAttribute = null;

                try {

                    compositeAttribute = (String) childLWC.get("ATR_NUMBER_COMPOSITE");

                } catch (WTException e) {

                    e.printStackTrace();

                }

                if (compositeAttribute == null || compositeAttribute.trim().equals("")) {

                    String error = "Т.к. " + headPart.getNumber() + " - " + headPart.getName() + " - является составным ПКИ, то для " +
                            childPart.getNumber() + " - " + childPart.getName() + " - д.б. заполнен атрибут \"Обозначение сложносоставного компонента\"";

                    if (!remarks.contains(error)) {

                        remarks.add(error);

                    }

                }

            } else {

                String error = headPart.getNumber() + " - " + headPart.getName() + " - ПКИ может состоять только из других ПКИ или Стандартных изделий";

                if (!remarks.contains(error)) {

                    remarks.add(error);

                }

            }

        }

    }

    private static void checkSubstitutesForTheRightNumber(QueryResult queryResult) {

        while (queryResult.hasMoreElements()) {

            WTPartMaster substitutePartMaster = (WTPartMaster) queryResult.nextElement();

            QueryResult queryResultForSubstitutePartMaster = gettingQueryResultForGettingAllVersionsWTPart(substitutePartMaster);

            while (queryResultForSubstitutePartMaster.hasMoreElements()) {

                WTPart part = (WTPart) queryResultForSubstitutePartMaster.nextElement();

                if (part.isLatestIteration()) {

                    checkPartForTheRightNumber(part.getNumber(), gettingSpecificationSectionForPart(gettingLwcForPart(part)), part.getName());

                    break;

                }

            }

        }

    }

    private static void addingErrorAlternativeParts(WTPart part) {

        String error = "Для " + part.getNumber() + " - " + part.getName() + " - назначены <b>глобальные замены</b> (глобальные замены запрещены в windchill)";

        if (!remarks.contains(error)) {

            remarks.add(error);

        }

    }

}