<jsp:directive.include file="includes/top.jsp" />
<c:if test="${not pageContext.request.secure}">
    <div id="msg" class="errors">
        <h2><spring:message code="screen.nonsecure.title" /></h2>
        <p><spring:message code="screen.nonsecure.message" /></p>
    </div>
</c:if>

<div id="cookiesDisabled" class="errors" style="display:none;">
    <h2><spring:message code="screen.cookies.disabled.title" /></h2>
    <p><spring:message code="screen.cookies.disabled.message" /></p>
</div>


<c:set var="registeredServiceLogo" value="images/webapp.png"/>


<div id="serviceui" class="serviceinfo">
    <table>
        <tr>
            <td><img src="${registeredServiceLogo}"></td>
            <td id="servicedesc">
                <h1><spring:message code="screen.welcome.serviceTitle"/></h1>
            </td>
        </tr>
    </table>
</div>
<p/>

<script type="text/javascript" src="<c:url value="/js/casLogin.js" />"></script>
<div class="box" id="login">
    <div style="margin-bottom: 30px">
        <spring:message code="screen.linking.message"/>
    </div>
    <div id="applicationTypeButtonsHolder">
        <button value="Login via Chorus" id="loginViaChorus">Link Chorus</button>
        <button value="Login via Panorama" id="loginViaPanorama">Link Panorama</button>
    </div>
    <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true" cssStyle="display: none">

        <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false" />

        <div style="margin-bottom: 10px">
            <a id="goToTheFirstStep" href="#" >Go Back</a>
        </div>
        <h2><spring:message code="screen.welcome.instructions" /></h2>
        <section class="row">
            <label for="username"><spring:message code="screen.welcome.label.netid" /></label>
            <c:choose>
                <c:when test="${not empty sessionScope.openIdLocalId}">
                    <strong><c:out value="${sessionScope.openIdLocalId}" /></strong>
                    <input type="hidden" id="username" name="username" value="<c:out value="${sessionScope.openIdLocalId}" />" />
                </c:when>
                <c:otherwise>
                    <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
                    <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />
                </c:otherwise>
            </c:choose>
        </section>

        <section class="row">
            <label for="password"><spring:message code="screen.welcome.label.password" /></label>
                <%--
                NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
                "autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
                information, see the following web page:
                http://www.technofundo.com/tech/web/ie_autocomplete.html
                --%>
            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
            <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
            <span id="capslock-on" style="display:none;"><p><img src="images/warning.png" valign="top"> <spring:message code="screen.capslock.on" /></p></span>
            <span id="forgot-password-wrapper"><p>
                <a id="forgot-password" href="#"><spring:message code="screen.welcome.forgotPassword"/></a>
                <a id="chorus-forgot-password" style="display: none" href="<spring:eval expression="@casProperties.getProperty('chorus.forgot.password.page.url')" />">Forgot password</a>
                <a id="panorama-forgot-password" style="display: none" href="<spring:eval expression="@casProperties.getProperty('panorama.forgot.password.page.url')" />">Forgot password</a>

            </p></span>
            <span id="create-account-wrapper"><p>
                <a id="create-account" href="#"><spring:message code="screen.welcome.createNewAccount"/></a>
                <a id="chorus-create-account" style="display: none" href="<spring:eval expression="@casProperties.getProperty('chorus.create.account.page.url')" />">Create new Chorus account</a>
                <a id="panorama-create-account" style="display: none" href="<spring:eval expression="@casProperties.getProperty('panorama.create.account.page.url')" />">Create new Panorama account</a>

            </p></span>
        </section>

        <form:input cssStyle="display: none" id="applicationType" size="25" path="applicationType" autocomplete="off" htmlEscape="true" /><%--Contains type of applciation to which user tries to login--%>
        <form:input cssStyle="display: none" id="uniqueID" size="25" path="uniqueID" autocomplete="off" htmlEscape="true" /><%--Contains user ID to link--%>

        <!--
        <section class="row check">
        <p>
        <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
        <label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
        <br/>
        <input id="publicWorkstation" name="publicWorkstation" value="false" tabindex="4" type="checkbox" />
        <label for="publicWorkstation"><spring:message code="screen.welcome.label.publicstation" /></label>
        <br/>
        <input type="checkbox" name="rememberMe" id="rememberMe" value="true" tabindex="5"  />
        <label for="rememberMe"><spring:message code="screen.rememberme.checkbox.title" /></label>
        </p>
        </section>
        -->

        <section class="row btn-row">
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />

            <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="6" type="submit" />
            <input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="7" type="reset" />
        </section>
    </form:form>
</div>

<div id="sidebar">
    <div class="sidebar-content">
        <p><spring:message code="screen.welcome.security" /></p>

        <c:if test="${!empty pac4jUrls}">
            <div id="list-providers">
                <h3><spring:message code="screen.welcome.label.loginwith" /></h3>
                <form>
                    <ul>
                        <c:forEach var="entry" items="${pac4jUrls}">
                            <li><a href="${entry.value}">${entry.key}</a></li>
                        </c:forEach>
                    </ul>
                </form>
            </div>
        </c:if>

    </div>
</div>

<jsp:directive.include file="includes/bottom.jsp" />
