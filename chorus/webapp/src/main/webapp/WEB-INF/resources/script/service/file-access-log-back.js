"use strict";

angular.module("file-access-log")
    .factory("fileAccessLog", function ($resource) {
        return $resource("../file-access-log");
    });
