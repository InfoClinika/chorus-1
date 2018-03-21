angular.module("current-year", [])
    .directive("currentYear", function () {
        return {
            restrict: "E",
            replace: true,
            template: "<span id=\"current-year\" ng-bind=\"currentYear\"></span>",
            controller: function ($scope) {
                $scope.currentYear = new Date().getFullYear();
            }
        };
    });