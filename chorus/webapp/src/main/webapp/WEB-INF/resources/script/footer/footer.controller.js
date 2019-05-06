"use strict";

(function () {

    angular.module("footer")
        .controller("footer-controller", FooterController);

    FooterController.$inject = ["$scope", "Appearance", "Features"];

    function FooterController($scope, Appearance, Features) {
        $scope.appearance = Appearance.get();
        $scope.isPrivateInstall = true;
        Features.getPrivateInstallProperties(function (properties) {
            $scope.isPrivateInstall = properties.enabled;
        });
    }
})();
