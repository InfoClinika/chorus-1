"use strict";

(function () {
    angular.module("search-back", ["ngResource"]).factory("Search", function ($resource) {
        return $resource("../search/:paged/:kind", {}, {
            count: {method: "GET", params: {kind: "count"}}
        });
    });

})();
