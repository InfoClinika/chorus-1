<jsp:directive.include file="includes/top.jsp"/>
<div id="msg" class="errors">
    <h2><spring:message code="screen.linkingError.heading" /></h2>

    <p><spring:message code="screen.linkingError.message" /></p>

    <p>
        <a id="login" href="<spring:eval expression="@casProperties.getProperty('cas.login.page.url')" />">
            <spring:message code="screen.linkingError.loginRedirect" /></a>
    </p>
</div>
<jsp:directive.include file="includes/bottom.jsp"/>
