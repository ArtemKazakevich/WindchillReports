package ext.by.peleng.reports.xml;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.meta.common.DisplayOperationIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.session.SessionHelper;
import wt.util.WTException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XmlCreator {

    public String createXml(String oid, String fileName) {

        ReferenceFactory referenceFactory = new ReferenceFactory();
        WTReference wtReference = null;
        Locale locale = null;
        try {
            wtReference = referenceFactory.getReference(oid);
            locale = SessionHelper.manager.getLocale();
        } catch (WTException e) {
            e.printStackTrace();
        }

        Object object = wtReference.getObject();
        PromotionNotice promotionNotice = (PromotionNotice) object;//запрос на продвижение;
        LWCNormalizedObject lwcNormalizedObject = null;
        String namePromotionNotice = "";
        String numberPromotionNotice = "";
        String primaryUsePromotionNotice = "";
        String creatorPromotionNotice = "";
        Document document = null;

        try {
            lwcNormalizedObject = new LWCNormalizedObject(promotionNotice, null, locale, new DisplayOperationIdentifier());
            lwcNormalizedObject.load("name", "number", "ATR_BOM_USE");
            namePromotionNotice = (String) lwcNormalizedObject.get("name");
            numberPromotionNotice = (String) lwcNormalizedObject.get("number");
            primaryUsePromotionNotice = (String) lwcNormalizedObject.get("ATR_BOM_USE");
            creatorPromotionNotice = promotionNotice.getCreatorFullName().replace(",", "");
        } catch (WTException e) {
            e.printStackTrace();
        }

        QueryResult promotionTargets = null;//получение "объектов для продвижения (promotion targets)";
        try {
            promotionTargets = MaturityHelper.getService().getPromotionTargets(promotionNotice);
        } catch (WTException e) {
            e.printStackTrace();
        }

        List<WTPart> listParts = new ArrayList<WTPart>();

        while (promotionTargets.hasMoreElements()) {
            Object obj = promotionTargets.nextElement();//получение парта
            if ("WTPart".equals(obj.getClass().getSimpleName())) {
                listParts.add((WTPart) obj);
            }
        }

        OutputStream fos = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element addedParts = document.createElement("AddedParts");
            document.appendChild(addedParts);
            addedParts.setAttribute("NAME", "AddedParts");
            addedParts.setAttribute("TYPE", "Unknown");
            addedParts.setAttribute("STATUS", "0");

            for (WTPart part : listParts) {
                WTPartMaster partMaster = (WTPartMaster) part.getMaster();//получение парт мастера
                long objectID = PersistenceHelper.getObjectIdentifier(partMaster).getId();//получение objectID парт мастера
                String numberWTPart = part.getNumber();
                String nameWTPart = part.getName();
                String rsWTPart = "";
                String defaultUnitWTPart = part.getDefaultUnit().getStringValue().substring(part.getDefaultUnit().getStringValue().lastIndexOf(".") + 1);
                String standartWTPart = "";

                try {
                    lwcNormalizedObject = new LWCNormalizedObject(part, null, locale, new DisplayOperationIdentifier());
                    lwcNormalizedObject.load("ATR_BOM_RS", "ATR_SUPPLIER_DOC_NUMBER");
                    rsWTPart = (String) lwcNormalizedObject.get("ATR_BOM_RS");
                    standartWTPart = (String) lwcNormalizedObject.get("ATR_SUPPLIER_DOC_NUMBER");
                    if (standartWTPart == null)
                        standartWTPart = "";
                } catch (WTException e) {
                    e.printStackTrace();
                }

                Element elementPart = document.createElement("Part");
                addedParts.appendChild(elementPart);

                Element objectIDElement = document.createElement("ObjectID");
                objectIDElement.setTextContent(String.valueOf(objectID));
                elementPart.appendChild(objectIDElement);

                Element numberWTPartElement = document.createElement("Number");
                numberWTPartElement.setTextContent(numberWTPart);
                elementPart.appendChild(numberWTPartElement);

                Element nameWTPartElement = document.createElement("Name");
                nameWTPartElement.setTextContent(nameWTPart);
                elementPart.appendChild(nameWTPartElement);

                Element rsWTPartElement = document.createElement("RS");
                rsWTPartElement.setTextContent(rsWTPart);
                elementPart.appendChild(rsWTPartElement);

                Element defaultUnitWTPartElement = document.createElement("DefaultUnit");
                defaultUnitWTPartElement.setTextContent(defaultUnitWTPart);
                elementPart.appendChild(defaultUnitWTPartElement);

                Element standartWTPartElement = document.createElement("Standart");
                standartWTPartElement.setTextContent(standartWTPart);
                elementPart.appendChild(standartWTPartElement);

                Element targetIDElement = document.createElement("TargetID");
                targetIDElement.setTextContent("ONEC");
                elementPart.appendChild(targetIDElement);
            }

            Element elementPromotionNotice = document.createElement("PromotionNotice");
            addedParts.appendChild(elementPromotionNotice);

            Element namePromotionNoticeElement = document.createElement("Name");
            namePromotionNoticeElement.setTextContent(namePromotionNotice);
            elementPromotionNotice.appendChild(namePromotionNoticeElement);

            Element numberPromotionNoticeElement = document.createElement("Number");
            numberPromotionNoticeElement.setTextContent(numberPromotionNotice);
            elementPromotionNotice.appendChild(numberPromotionNoticeElement);

            Element primaryUsePromotionNoticeElement = document.createElement("PrimaryUse");
            primaryUsePromotionNoticeElement.setTextContent(primaryUsePromotionNotice);
            elementPromotionNotice.appendChild(primaryUsePromotionNoticeElement);

            Element creatorPromotionNoticeElement = document.createElement("Creator");
            creatorPromotionNoticeElement.setTextContent(creatorPromotionNotice);
            elementPromotionNotice.appendChild(creatorPromotionNoticeElement);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Result result = new StreamResult(byteArrayOutputStream);
            transformer.transform(source, result);
            File file = new File("D:\\ptc\\Windchill\\Windchill\\codebase\\netmarkets\\jsp\\by\\peleng\\XML\\export\\" + fileName);
            fos = new FileOutputStream(file);
            byteArrayOutputStream.writeTo(fos);

        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return "!!!!!SUCCESS!!!!!";
    }
}