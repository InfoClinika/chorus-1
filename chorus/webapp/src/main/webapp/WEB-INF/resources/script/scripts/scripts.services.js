"use strict";

(function () {

    angular.module("scripts.services", ["ngResource"])
        .factory("Scripts", function ($resource) {
            return $resource("../scripts/:path/:filter", {}, {
                duplicate: {method: "POST", params: {path: "duplicate"}},
                share: {method: "PUT", params: {path: "share"}}
            });
        })
        .factory("ScriptDetails", function ($resource) {
            return $resource("../scripts/:id/:path", {id: "@id"}, {
                parameters: {method: "GET", params: {path: "parameters"}}
            });
        });

})();
