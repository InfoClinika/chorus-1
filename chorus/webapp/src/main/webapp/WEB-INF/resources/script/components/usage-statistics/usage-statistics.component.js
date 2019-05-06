"use strict";

(function () {
    angular.module("index-page")
        .directive("usageStatistics", function (Features, Statistics, UserDetailsProvider) {
            return {
                restrict: "E",
                replace: true,
                templateUrl: "../../script/components/usage-statistics/usage-statistics.component.html",
                scope: {configuration: "="},
                controller: function ($scope) {
                    $scope.ssoProperties = {};
                    $scope.isUserLoggedIn = UserDetailsProvider.isUserLoggedIn;

                    Features.getSsoProperties(function (response) {
                        $scope.ssoProperties = response;
                    });

                    $scope.usage = Statistics.usage();
                }
            };
        });

})();
