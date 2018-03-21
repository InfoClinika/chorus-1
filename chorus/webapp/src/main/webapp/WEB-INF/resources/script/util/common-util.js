(function () {
    "use strict";

    angular.module("util", [])
        .service("CommonUtil", CommonUtilService);

    function CommonUtilService() {
        return {
            waitUntil: waitUntil
        };

        function waitUntil(condition, handler, interval) {
            if(interval == undefined) {
                interval = 50;  // default interval
            }

            if (condition()) {
                handler();
            } else {
                setTimeout(function () {
                    waitUntil(condition, handler, interval);
                }, interval);
            }
        }
    }
})();
