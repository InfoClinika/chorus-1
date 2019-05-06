"use strict";

angular.module("groups", ["ngResource"])
    .factory("Groups", function ($resource) {
        return $resource("../groups/:id", {},
            {"update": {method: "PUT"}}
        );
    });


