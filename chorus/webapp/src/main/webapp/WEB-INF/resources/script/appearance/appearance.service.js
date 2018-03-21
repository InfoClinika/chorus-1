(function () {

    "use strict";

    angular.module("appearance").factory("Appearance", AppearanceService);

    AppearanceService.$inject = ["$resource"];

    function AppearanceService($resource) {
        return $resource("/appearance");
    }

})();
