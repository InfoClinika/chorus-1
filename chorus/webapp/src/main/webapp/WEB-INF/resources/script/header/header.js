"use strict";

(function () {

    angular.module("header", ["appearance"])
        .directive("profileDialogLink", function ($location) {
            return {
                restrict: "A",
                link: function ($scope, elem) {

                    var openProfileDialog = function () {
                        if ($scope.isDashboard) {
                            $location.path("/profile");
                            setTimeout(function () {
                                $scope.$apply();
                            }, 0);
                        } else {
                            window.location = "../pages/dashboard.html#/profile";
                        }
                    };

                    elem.bind("click", openProfileDialog);
                }
            };
        });
})();
