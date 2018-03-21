angular.module("statistics-back", ["ngResource"]).
    factory("Statistics", function ($resource) {
        return $resource("../statistics/:path", {}, {
            "usage": {method: "GET", params: {path: "usage"}}
        });
    });
