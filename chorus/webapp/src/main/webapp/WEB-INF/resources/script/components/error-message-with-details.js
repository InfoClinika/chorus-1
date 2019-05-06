(function () {
    "use strict";

    angular.module("experiments-front")
        .directive("errorMessageWithDetails", errorMessageWithDetailsDirective);

    function errorMessageWithDetailsDirective() {
        return {
            restrict: "E",
            templateUrl: "../../pages/component/error-message-with-details.html",
            replace: true,
            scope: {
                error: "="
            }
        };
    }
})();
