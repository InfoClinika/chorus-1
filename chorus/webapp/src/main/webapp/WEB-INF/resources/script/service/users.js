"use strict";

(function () {
    angular.module("users", ["ngResource"]).factory("Users", function ($resource) {
        return $resource("../users");
    }).factory("Collaborators", function ($resource) {
        return $resource("../users/project-collaborators/:experimentId");
    });

})();
