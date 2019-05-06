"use strict";

(function () {
    angular.module("preferences-front", ["ngResource", "preferences-back"])
        .controller("user-preferences", function ($scope, Preferences) {
            $scope.page.title = "User Preferences";
            $scope.preferences = {};

            Preferences.get(function (preferences) {
                $scope.preferences = preferences;
            });

            $scope.isFormInvalid = function () {
                return !$scope.preferences.rtLeft || !$scope.preferences.rtRight || !$scope.preferences.mzLeft ||
                    !$scope.preferences.mzRight;
            };

            $scope.savePreferences = function () {
                Preferences.save($scope.preferences, function () {
                    $(".preferences-dialog").trigger("hide");
                });
            };
        })
        .directive("floatValidator", function () {
            return {
                require: "ngModel",
                link: function (scope, elem, attr, ngModel) {
                    ngModel.$parsers.unshift(function (value) {
                        var valid = validate(value);
                        ngModel.$setValidity("parameterValue", valid);
                        return valid ? value : null;
                    });

                    function validate(value) {
                        if (!value) {
                            return true;
                        }
                        return value.match(/^[+-]?\d+(\.\d+)?$/);
                    }
                }
            };
        });
})();
