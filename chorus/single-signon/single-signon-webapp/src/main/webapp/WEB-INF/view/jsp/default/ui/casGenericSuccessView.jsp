<jsp:directive.include file="includes/top.jsp"/>
<c:set var="logoutUrl" value="logout"/>
<div id="msg" class="success">
    <h2><spring:message code="screen.success.header"/></h2>

    <p><spring:message code="screen.success.success" arguments="${principal.id}"/></p>

    <p><spring:message code="screen.success.security"/></p>

    <p>
        <a href="<spring:eval expression="@casProperties.getProperty('chorus.afterlogin.page.url')" />">
            <spring:message code="screen.success.chorusRedirect"/></a>
        <a href="<spring:eval expression="@casProperties.getProperty('panorama.afterlogin.page.url')" />">
            <spring:message code="screen.success.panoramaRedirect"/></a>
    </p>

    <p><a href="${logoutUrl}">Logout</a></p>
</div>
<jsp:directive.include file="includes/bottom.jsp"/>
