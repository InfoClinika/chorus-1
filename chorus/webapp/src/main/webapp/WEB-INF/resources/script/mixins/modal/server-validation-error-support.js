angular.module("mixins").factory("ServerValidationErrorSupport", function () {
    return {
        init: function ($scope) {
            $scope.serverValidationErrors = [];

            $scope.resetServerValidationErrors = function () {
                $scope.serverValidationErrors.length = 0;
            };
            $scope.hasServerValidationErrors = function () {
                return $scope.serverValidationErrors.length !== 0;
            }
        }
    }
});
