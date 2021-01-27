<%@ page language="java" %>
<%@ page import="com.ptc.core.lwc.server.LWCNormalizedObject" %>
<%@ page import="com.ptc.core.meta.common.DisplayOperationIdentifier" %>
<%@ page import="ext.by.peleng.reports.xml.XmlCreator" %>
<%@ page import="wt.fc.ReferenceFactory" %>
<%@ page import="wt.fc.WTReference" %>
<%@ page import="wt.maturity.PromotionNotice" %>
<%@ page import="wt.session.SessionHelper" %>
<%@ page import="wt.util.WTException" %>
<%@ page import="java.util.Locale" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Test</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<%
    String oid = request.getParameter("oid");

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
    PromotionNotice promotionNotice = (PromotionNotice) object;//получили наш запрос на продвижение;
    LWCNormalizedObject lwcNormalizedObject = null;
    String namePromotionNotice = "";
    String numberPromotionNotice = "";
    String primaryUsePromotionNotice = "";
    String creatorPromotionNotice = "";

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

    %>
        <p><strong>Наименование запроса на продвижение = </strong><%=namePromotionNotice%></p>
        <p><strong>Номер запроса на продвижение = </strong><%=numberPromotionNotice%></p>
        <p><strong>Узел вхождения = </strong><%=primaryUsePromotionNotice%></p>
        <p><strong>Создатель запроса на продвижение = </strong><%=creatorPromotionNotice%></p>
    <%

        String str = (new XmlCreator()).createXml(oid, "PR_" + numberPromotionNotice + ".xml");
%>

<p align="center"><strong><%=str%></strong></p>
<p align="center">
    <a href="https://windchill.peleng.by/Windchill/netmarkets/jsp/by/peleng/XML/export/<%="PR_" + numberPromotionNotice%>.xml"
       download="<%="PR_" + numberPromotionNotice + ".xml"%>">Download</a>
</p>

</html>