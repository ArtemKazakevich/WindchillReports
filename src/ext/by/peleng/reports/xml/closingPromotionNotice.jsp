<%@ page import="org.w3c.dom.Document" %>
<%@ page import="org.w3c.dom.Node" %>
<%@ page import="org.w3c.dom.NodeList" %>
<%@ page import="org.xml.sax.SAXException" %>
<%@ page import="wt.epm.EPMDocument" %>
<%@ page import="wt.fc.PersistenceHelper" %>
<%@ page import="wt.fc.QueryResult" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.fc.WTReference" %>
<%@ page import="wt.lifecycle.LifeCycleHelper" %>
<%@ page import="wt.lifecycle.State" %>
<%@ page import="wt.maturity.PromotionNotice" %>
<%@ page import="wt.part.WTPart" %>
<%@ page import="wt.part.WTPartHelper" %>
<%@ page import="wt.part.WTPartMaster" %>
<%@ page import="wt.query.QuerySpec" %>
<%@ page import="wt.query.SearchCondition" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="wt.vc.VersionControlHelper" %>
<%@ page import="wt.vc.wip.WorkInProgressHelper" %>
<%@ page import="javax.xml.parsers.DocumentBuilder" %>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>
<%@ page import="java.io.File" %>
<%@ page import="java.net.URI" %>
<%@ page import="java.net.URISyntaxException" %>
<%@ page import="java.util.Locale" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<html>
<head>
    <title>qwe</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<%
    DocumentBuilder documentBuilder = null;
    Document document = null;
    Node root = null;
    NodeList nodeList = null;
    PromotionNotice promotionNotice = null;

    String oid = request.getParameter("oid");

    ReferenceFactory rf = new ReferenceFactory();
    WTReference wtRef = null;
    Locale locale = null;
    try {
        wtRef = rf.getReference(oid);
        locale = SessionHelper.manager.getLocale();
    } catch (WTException e) {
        e.printStackTrace();
    }

    Object object = wtRef.getObject();
    promotionNotice = (PromotionNotice) object;//запрос на продвижение;
    String fileName = promotionNotice.getNumber() + ".xml";

    if (checkFileInFolder(fileName)) {  //проверяем: обработан ли наш запрос в 1С
                                        // (запрос обработан, если в папке "\\1Cv8\windchill" присутсвует файл xml с номером нашего запроса)
        if (!"APPROVED".equals(promotionNotice.getState().toString())) {    //проверяем состояние нашего запроса
            try {
                documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();//Создается построитель документа
                document = documentBuilder.parse("\\\\1Cv8\\windchill\\" + fileName);//Создается дерево DOM документа из файла
                root = document.getDocumentElement();//Получаем корневой элемент
                nodeList = root.getChildNodes();// Получаем все подэлементы корневого
                for (int i = 0; i < nodeList.getLength(); i++) {    //Парсим xml
                    Node node = nodeList.item(i);
                    if (node.getNodeType() != Node.TEXT_NODE) {
                        if ("CodeAssignment".equals(node.getNodeName())) {
                            NodeList partProps = node.getChildNodes();
                            String objectID = null;
                            String status = null;
                            for (int j = 0; j < partProps.getLength(); j++) {
                                Node partProp = partProps.item(j);
                                if (partProp.getNodeType() != Node.TEXT_NODE) {
                                    if ("ObjectID".equals(partProp.getNodeName())) {
                                        objectID = partProp.getTextContent();
                                    } else if ("Status".equals(partProp.getNodeName())) {
                                        status = partProp.getTextContent();
                                    }
                                }
                            }

                            String fullObjectID = "wt.part.WTPartMaster:" + objectID;
                            ReferenceFactory referenceFactory = new ReferenceFactory();
                            WTReference wtReference = referenceFactory.getReference(fullObjectID);
                            WTPartMaster partMaster = (WTPartMaster) wtReference.getObject();
                            QueryResult queryResult = VersionControlHelper.service.allVersionsOf(partMaster);   //получили все версии part
                            WTPart wtPart = null;
                            while (queryResult.hasMoreElements()) { //получаем последнюю версию нашего файла
                                WTPart tempPart = (WTPart) queryResult.nextElement();
                                if (tempPart.isLatestIteration()) {
                                    wtPart = tempPart;
                                    break;
                                }
                            }


                            if (!WorkInProgressHelper.isCheckedOut(wtPart)) {
                                QueryResult qr = WTPartHelper.service.getDescribedByDocuments(wtPart);
                                EPMDocument relatedEPMDocument = null;

                                while (qr.hasMoreElements()) {  //получаем все EPMDocument, связанные с нашим part
                                    Object tempObject = qr.nextElement();
                                    if (tempObject instanceof EPMDocument) {
                                        relatedEPMDocument = (EPMDocument) tempObject;
                                    }
                                }

                                if (relatedEPMDocument != null) {
                                    if (!WorkInProgressHelper.isCheckedOut(relatedEPMDocument)) {
                                        if ("присвоен".equals(status)) {
                                            LifeCycleHelper.service.setLifeCycleState(wtPart, State.toState("RELEASED"));
                                            LifeCycleHelper.service.setLifeCycleState(relatedEPMDocument, State.toState("RELEASED"));
                                        }
                                        else {
                                            LifeCycleHelper.service.setLifeCycleState(wtPart, State.toState("OBSOLETE"));
                                            LifeCycleHelper.service.setLifeCycleState(relatedEPMDocument, State.toState("OBSOLETE"));
                                        }
                                    } else {
                                        String message = "Ошибка... EPMDocument " + relatedEPMDocument.getNumber() + " взят на изменение." +
                                                " Пожалуйста, сдайте его на хранение и попробуйте обновить запрос снова!";
                                        throw new MyException(message);

                                    }
                                } else {
                                    if ("присвоен".equals(status)) {
                                        LifeCycleHelper.service.setLifeCycleState(wtPart, State.toState("RELEASED"));
                                    } else {
                                        LifeCycleHelper.service.setLifeCycleState(wtPart, State.toState("OBSOLETE"));
                                    }
                                }

                            } else {
                                String message = "Ошибка... Part " + wtPart.getNumber() + " взят на изменение." +
                                        " Пожалуйста, сдайте его на хранение и попробуйте обновить запрос снова!";
                                throw new MyException(message);
                            }

                        } else if ("PromotionNotice".equals(node.getNodeName())) {
                            NodeList promotionNoticeProps = node.getChildNodes();
                            String numberPromotionNotice = null;
                            for (int j = 0; j < promotionNoticeProps.getLength(); j++) {
                                Node promotionNoticeProp = promotionNoticeProps.item(j);
                                if (promotionNoticeProp.getNodeType() != Node.TEXT_NODE) {
                                    if ("Number".equals(promotionNoticeProp.getNodeName())) {
                                        numberPromotionNotice = promotionNoticeProp.getTextContent();
                                    }
                                }
                            }

//Search promotionNotice by numberPromotionNotice

                            QuerySpec querySpec = new QuerySpec(PromotionNotice.class);
                            querySpec.appendWhere(new SearchCondition(wt.maturity.PromotionNotice.class, "number", SearchCondition.EQUAL,numberPromotionNotice));
                            QueryResult qr = PersistenceHelper.manager.find(querySpec);
                            if (qr.size() != 0) {
                                promotionNotice = (PromotionNotice) qr.nextElement();
                                LifeCycleHelper.service.setLifeCycleState(promotionNotice, State.toState("APPROVED"));
                            }

//                            LifeCycleHelper.service.setLifeCycleState(promotionNotice, State.toState("APPROVED"));

                        }
                    }
                }
%>
<p>Запрос на присвоение кодов ОКП: <strong><%=promotionNotice.getNumber()%></strong> - переведён в состояние <b>"УТВЕРЖДЕНО"</b></p>
<%

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (WTException e) {
                e.printStackTrace();
            } catch (MyException myException) {
%>
<p><%=myException.getMessage()%></p>
<%
            }

        } else {

%>
<h4 align="center">Запрос на присвоение кодов ОКП: <%=promotionNotice.getNumber()%> - уже находится в состояние "УТВЕРЖДЕНО".</h4>
<%

        }

    } else {

%>
<h4 align="center">Запрос на присвоение кодов ОКП: <%=promotionNotice.getNumber()%> - ещё не обработан в системе 1С.</h4>
<%

    }

%>

<%!
    class MyException extends Exception {
        MyException(String msg) {
            super(msg);
        }
    }
%>

<%!
    private static boolean checkFileInFolder(String fileName) {

        boolean bool = false;
        final String folder_path = "file:////1Cv8/windchill/";
        URI uri = null;
        File folder = null;
        File[] listFiles = null;

        try {
            uri = new URI(folder_path);
        } catch (URISyntaxException e) {
            System.out.println("Incorrect URI");
        }

        folder = new File(uri);

        listFiles = folder.listFiles();

        for (File file : listFiles) {
            System.out.println(file.getName());
            if (fileName.equals(file.getName())) {
                return true;
            }
        }

        return bool;

    }
%>

</html>