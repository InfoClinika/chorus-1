"use strict";

angular.module("preferences-back", ["ngResource"])
    .factory("Preferences", function ($resource) {
        return $resource("../preferences", {}, {});
    });
