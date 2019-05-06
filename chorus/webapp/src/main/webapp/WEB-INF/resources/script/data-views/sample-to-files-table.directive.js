"use strict";

(function () {
    angular.module("dataViews")

        .directive("sampleToFilesTable", function () {
            return {
                restrict: "E",
                scope: {
                    mapping: "="
                },
                templateUrl: "../script/data-views/sample-to-files-table.template.html",
                replace: true
            };
        });

})();
