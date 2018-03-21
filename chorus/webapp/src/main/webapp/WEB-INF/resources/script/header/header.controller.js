(function () {
    "use strict";

    angular.module("header")
        .controller("header-controller", HeaderController);

    HeaderController.$inject = ["$scope", "Appearance"];

    function HeaderController($scope, Appearance) {
        $scope.appearance = Appearance.get();
    }
})();
