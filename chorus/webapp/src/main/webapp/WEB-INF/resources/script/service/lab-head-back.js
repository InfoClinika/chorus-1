"use strict";

angular.module("lab-head-back", ["ngResource"])
    .factory("LaboratoryUsers", function ($resource) {
        return $resource("../labhead/:labId/users/:user");
    })
    .factory("IsLabHead", function ($resource) {
        return $resource("../labhead/isLabHead");
    })
;
