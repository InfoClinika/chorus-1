"use strict";

(function () {

    //inspired by http://www.espeo.pl/2012/02/26/authentication-in-angularjs-application
    angular.module("sessionTimeoutHandler", ["error-catcher"])
        .config(function ($httpProvider) {
            function sessionTimeoutInterceptor($q, $log, $rootScope) {
                function error(response) {
                    var status = response.status;
                    $log.error("Response status: " + status + ". " + response);

                    if (status == "903") {
                        sessionTimeoutHandler($rootScope.getSsoProperties().enabled);
                    }
                    if (status == "500") {
                        setTimeout(error500Handler, 200);
                        $rootScope.$broadcast("chorus.serverError");
                    }
                    if (status == "904" || status == "403") {
                        hideModal(function () {
                            window.location =
                                "dashboard.html#/projects/all" + "?page=1&items=25&sortingField=name&asc=true";
                        });
                    }
                    if (status == "905") {
                        hideModal(function () {
                            $rootScope.$broadcast("chorus.serverPipelineError", response);
                        });
                    }

                    return $q.reject(response); //similar to throw response;
                }

                return function (promise) {
                    return promise.then(null, error);
                };
            }

            $httpProvider.responseInterceptors.push(sessionTimeoutInterceptor);
        });

    function error500Handler() {
        $(".modal").modal("hide");

        var dialogButtons = [
            {
                text: "Close",
                click: function () {
                    $(this).dialog("close");
                },
                class: "secondary-action"
            }
        ];

        // if issue reporter is available than add an according button to error dialog
        if (angular.element("#issue-reporter") && angular.element("#issue-reporter").scope()) {
            dialogButtons.push({
                text: "Report an issue",
                click: function () {
                    var scope = angular.element("#issue-reporter").scope();
                    var reporter = scope.reporter;
                    reporter.showPopup();
                    $(this).dialog("close");
                }
            });
        }

        $("#server-error-message").dialog({
            title: "Server Error",
            draggable: false,
            dialogClass: "message-dialog error",
            modal: true,
            resizable: false,
            width: 450,
            buttons: dialogButtons
        });
    }

    function sessionTimeoutHandler(isSsoEnabled) {
        $(".modal").modal("hide");

        $("#session-timeout-message").dialog({
            title: "Session Timeout",
            draggable: false,
            dialogClass: "message-dialog warning",
            modal: true,
            resizable: false,
            width: 450,
            buttons: {
                "Sign In again": function () {
                    $(this).dialog("close");
                }
            },
            close: function () {
                if (isSsoEnabled) {
                    location.reload();
                } else {
                    var currentPath = encodeURIComponent(location.pathname + location.search + location.hash);
                    window.location = "/pages/authentication.html#/login/" + encodeURIComponent(currentPath);
                }

            }
        });
    }

})();
