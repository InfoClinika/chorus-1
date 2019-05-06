"use strict";

(function () {

    angular.module("error-catcher", [])
        .config(function ($provide) {
            $provide.decorator("$exceptionHandler", ["$delegate", function ($delegate) {
                return function (exception, cause) {
                    CommonLogger.exception(exception);
                    $delegate(exception, cause);
                };
            }]);
        });
})();
