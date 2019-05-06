"use strict";

(function () {
    /*
     * This module contains small components used from within templates
     */
    angular.module("template-components", [])
        .directive("currentYear", currentYearDirective)
        .filter("currentHostPrefix", currentHostPrefixFilter);

    function currentYearDirective() {
        return {
            restrict: "E",
            replace: true,
            template: "<span id=\"current-year\" ng-bind=\"currentYear\"></span>",
            controller: function ($scope) {
                $scope.currentYear = new Date().getFullYear();
            }
        };
    }

    function currentHostPrefixFilter($location) {

        const HTTP_PORT = "80";
        const HTTPS_PORT = "443";

        var host = $location.host();
        var port = $location.port();

        if (port && port != HTTP_PORT && port != HTTPS_PORT) {
            host += ":" + port;
        }

        return function (value) {
            return host + value;
        };
    }

})();
