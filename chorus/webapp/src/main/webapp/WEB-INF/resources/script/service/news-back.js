"use strict";

angular.module("news-back", ["ngResource"])
    .factory("News", function ($resource) {
        return $resource("../news/:path/:id", {}, {
            "update": {method: "PUT"}
        });
    });
