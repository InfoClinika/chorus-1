<jsp:directive.include file="includes/top.jsp"/>
<div id="msg" class="errors">
    <h2>Authentication Endpoint Internal Server Error</h2>

    <p>Something went wrong during authentication at the endpoint server.
        Please contact the application administrator(<a
                href="mailto:&#115;&#117;&#112;&#112;&#111;&#114;&#116;&#064;&#099;&#104;&#111;&#114;&#117;&#115;&#112;&#114;&#111;&#106;&#101;&#099;&#116;&#046;&#111;&#114;&#103;">support@chorusproject.org</a>).
    </p>

    <p>
        <a href="<spring:eval expression="@casProperties.getProperty('chorus.afterlogout.page.url')" />">
            <spring:message code="screen.logout.chorusRedirect"/></a>
        <a href="<spring:eval expression="@casProperties.getProperty('panorama.afterlogout.page.url')" />">
            <spring:message code="screen.logout.panoramaRedirect"/></a>
    </p>
</div>
<jsp:directive.include file="includes/bottom.jsp"/>
