<%@ page session="false" contentType="application/xml; charset=UTF-8" %>
<%@ page import="java.util.*, java.util.Map.Entry" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
    <cas:authenticationSuccess>
        <cas:user>${fn:escapeXml(principal.id)}</cas:user>
        <c:if test="${not empty pgtIou}">
            <cas:proxyGrantingTicket>${pgtIou}</cas:proxyGrantingTicket>
        </c:if>
        <c:if test="${fn:length(chainedAuthentications) > 0}">
            <cas:proxies>
                <c:forEach var="proxy" items="${chainedAuthentications}" varStatus="loopStatus" begin="0"
                           end="${fn:length(chainedAuthentications)}" step="1">
                    <cas:proxy>${fn:escapeXml(proxy.principal.id)}</cas:proxy>
                </c:forEach>
            </cas:proxies>
        </c:if>

        <c:if test="${fn:length(attributes) + fn:length(primaryAuthentication.attributes) > 0}">
            <cas:attributes>
                <c:forEach var="attr"
                           items="${attributes}"
                           varStatus="loopStatus" begin="0"
                           end="${fn:length(attributes)}"
                           step="1">

                    <c:forEach var="attrval" items="${attr.value}">
                        <cas:${fn:escapeXml(attr.key)}>${fn:escapeXml(attrval)}</cas:${fn:escapeXml(attr.key)}>
                    </c:forEach>
                </c:forEach>

                <%--[andrii.loboda protocol patch] All attributes of principal will go here to let Chorus and Panorama know which email should be used to authorize their users.--%>
                <c:forEach var="attr"
                           items="${primaryAuthentication.attributes}"
                           varStatus="loopStatus" begin="0"
                           end="${fn:length(primaryAuthentication.attributes)}"
                           step="1">

                    <c:forEach var="attrval" items="${attr.value}">
                        <cas:${fn:escapeXml(attr.key)}>${fn:escapeXml(attrval)}</cas:${fn:escapeXml(attr.key)}>
                    </c:forEach>
                </c:forEach>
            </cas:attributes>
        </c:if>

    </cas:authenticationSuccess>
</cas:serviceResponse>
