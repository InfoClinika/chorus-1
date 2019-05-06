"use strict";

(function () {
    angular.module("aggregate-statistics-back", ["ngResource"])
        .factory("AggregateStatistics", function ($resource) {
            return $resource("../admin/tools/:path", {}, {
                "aggregateStatistics": {method: "POST", params: {path: "aggregateStatistics"}},
                "getStatistics": {method: "POST", params: {path: "getStatistics"}, isArray: true},
                "getStatisticsSearches": {method: "POST", params: {path: "getStatisticsSearches"}, isArray: true}
            });
        });
})();
