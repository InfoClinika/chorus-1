"use strict";

(function () {

    angular.module("anonymous-download", ["ngResource"])
        .factory("AnonymousDownloadEmailer", function ($resource) {
            return $resource("../anonymous/download/:type/:path", {}, {
                "experiment": {method: "POST", params: {type: "experiment", path: "send"}}
            });
        });

})();
