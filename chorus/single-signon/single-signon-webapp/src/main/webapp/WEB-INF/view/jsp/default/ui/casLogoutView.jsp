<jsp:directive.include file="includes/top.jsp" />
<div id="msg" class="success">
    <h2><spring:message code="screen.logout.header" /></h2>
    <p><spring:message code="screen.logout.success" /></p>
    <p><spring:message code="screen.logout.security" /></p>
    <p>
        <a href="<spring:eval expression="@casProperties.getProperty('chorus.afterlogout.page.url')" />">
            <spring:message code="screen.logout.chorusRedirect"/></a>
        <a href="<spring:eval expression="@casProperties.getProperty('panorama.afterlogout.page.url')" />">
            <spring:message code="screen.logout.panoramaRedirect"/></a>
    </p>
</div>
<jsp:directive.include file="includes/bottom.jsp" />
