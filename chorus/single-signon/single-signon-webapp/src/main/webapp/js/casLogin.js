"use strict";

function jqueryReady() { //jquery is loaded

    loginFormComponent();

    function loginFormComponent() {
        var eventBus = $({});
        var events = {};
        var commands = {
            DISPLAY_FIRST_STEP: "display_first_step",
            DISPLAY_SECOND_STEP: "display_second_step"
        };
        var domComponents = {
            applicationTypeButtonHolder: getJqueryEl("#applicationTypeButtonsHolder"),
            loginViaPanoramaButton: getJqueryEl("#loginViaPanorama"),
            loginViaChorusButton: getJqueryEl("#loginViaChorus"),
            authenticationTypeInput: getJqueryEl("#applicationType"),
            goToTheFirstStepLink: getJqueryEl("#goToTheFirstStep"),

            forgetPassword: {
                link: getJqueryEl("#forgot-password"),
                chorusLink: getJqueryEl("#chorus-forgot-password"),
                panoramaLink: getJqueryEl("#panorama-forgot-password")
            },
            createNewAccount: {
                link: getJqueryEl("#create-account"),
                chorusLink: getJqueryEl("#chorus-create-account"),
                panoramaLink: getJqueryEl("#panorama-create-account")
            },
            form: getJqueryEl("#fm1"),
            usernameInput: getJqueryEl("#username"),
            passwordInput: getJqueryEl("#password")
        };
        var CHORUS_AUTH_TYPE = "CHORUS";
        var PANORAMA_AUTH_TYPE = "PANORAMA";


        activate();


        function activate() {
            $(eventBus).on(commands.DISPLAY_FIRST_STEP, displayFirstStep);
            $(eventBus).on(commands.DISPLAY_SECOND_STEP, displaySecondStep);
            domComponents.loginViaChorusButton.on("click", chorusButtonClickHandler);
            domComponents.loginViaPanoramaButton.on("click", panoramaButtonClickHandler);
            domComponents.forgetPassword.link.on("click", forgetPasswordClickHandler);
            domComponents.createNewAccount.link.on("click", createNewAccountClickHandler);
            domComponents.goToTheFirstStepLink.on("click", goToTheFirstStepClickHandler);
            domComponents.form.on("reset", resetFormHandler);

            if (domComponents.form.find("#msg").length == 0) {
                eventBus.trigger(commands.DISPLAY_FIRST_STEP);
            } else {
                eventBus.trigger(commands.DISPLAY_SECOND_STEP);
            }


            function displayFirstStep() {
                domComponents.form.hide();
                domComponents.applicationTypeButtonHolder.show();
            }

            function goToTheFirstStepClickHandler(event) {
                event.preventDefault();
                displayFirstStep();
            }

            function resetFormHandler(event) {
                domComponents.usernameInput.val("");
                domComponents.passwordInput.val("");
                event.preventDefault();
            }

            function displaySecondStep() {
                domComponents.form.show();
                domComponents.applicationTypeButtonHolder.hide();
            }

            function chorusButtonClickHandler() {
                domComponents.authenticationTypeInput.val(CHORUS_AUTH_TYPE);
                eventBus.trigger(commands.DISPLAY_SECOND_STEP);

            }

            function panoramaButtonClickHandler() {
                domComponents.authenticationTypeInput.val(PANORAMA_AUTH_TYPE);
                eventBus.trigger(commands.DISPLAY_SECOND_STEP);
            }

            function forgetPasswordClickHandler(event) {
                event.preventDefault();
                var authType = domComponents.authenticationTypeInput.val();
                var authenticationManager = getAuthenticationManager(authType);
                authenticationManager.redirectToForgetPasswordPage();

            }

            function createNewAccountClickHandler(event) {
                event.preventDefault();
                var authType = domComponents.authenticationTypeInput.val();
                var authenticationManager = getAuthenticationManager(authType);
                authenticationManager.redirectToCreateNewAccountPage();

            }

            function getAuthenticationManager(authType) {
                var forgetPasswordLink = null;
                var createNewAccountLink = null;
                if (authType == PANORAMA_AUTH_TYPE) {
                    forgetPasswordLink = domComponents.forgetPassword.panoramaLink.attr("href");
                    createNewAccountLink = domComponents.createNewAccount.panoramaLink.attr("href");
                } else if (authType == CHORUS_AUTH_TYPE) {
                    forgetPasswordLink = domComponents.forgetPassword.chorusLink.attr("href");
                    createNewAccountLink = domComponents.createNewAccount.chorusLink.attr("href");
                } else {
                    throw "Unknown authType: " + authType;
                }

                return {
                    redirectToForgetPasswordPage: createRedirect(forgetPasswordLink),
                    redirectToCreateNewAccountPage: createRedirect(createNewAccountLink)
                };

                function createRedirect(link) {
                    return function () {
                        window.location = link;
                    };
                }

            }
        }


        /*Gets a jquery element or throws an exception*/
        function getJqueryEl(selector) {
            var jqueryEl = $(selector);
            if (jqueryEl.length == 0) {
                throw "Can't find element with selector: " + selector;
            }
            return jqueryEl;
        }


    }

}
