"use strict";

(function () {

    angular.module("s3UploadModule")
        .filter("timeInterval", function ($filter) {
            return function (miliseconds) {
                return $filter("date")(new Date(miliseconds), "mm:ss", "UTC");
            };
        });

})();
