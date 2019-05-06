"use strict";

angular.module("projects", ["ngResource"])
    .factory("Projects", function ($resource) {
        return $resource("../projects/:paged/:filter/:id", {userId: "@id"},
            {"update": {method: "PUT"}}
        );
    })
    .factory("ProjectCopy", function ($resource) {
        return $resource("../projects/copy/:action/:id", {}, {
                "copy": {method: "GET", isArray: false},
                "sendCopyConfirmation": {method: "POST", params: {action: "request"}}
            }
        );
    })
    .factory("ProjectDetails", function ($resource) {
        return $resource("../projects/details/:id/:size/", {},
            {
                "short": {method: "GET", params: {size: "short"}, isArray: false}
            }
        );
    })
    .factory("ProjectColumns", function ($resource) {
        return $resource("../projects/column-view/:path/:id", {}, {
            "available": {method: "GET", isArray: true, params: {path: "all"}},
            "views": {method: "GET", isArray: true, params: {path: "views"}},
            "queryColumns": {method: "GET", isArray: true, params: {path: "views"}},
            "columnMap": {method: "GET", params: {path: "column-map"}},
            "default": {method: "GET", params: {path: "default"}},
            "defaultColumns": {method: "GET", isArray: true, params: {path: "default", id: "columns"}},
            "selectedColumnSet": {method: "GET", isArray: true, params: {path: "selected"}}
        });
    })
    .factory("ProjectAttachments", function ($resource) {
        return $resource("../attachments/project/:path/:id", {id: "@id"}, {
            "read": {method: "GET", isArray: true},
            "postMetadata": {method: "POST", params: {path: "items"}},
            "attachToProject": {method: "POST", params: {path: "attach"}},
            "remove": {method: "DELETE", params: {path: "interrupt"}},
            "getDestinationPath": {method: "GET", params: {path: "destination"}},
            "maxSizeInBytes": {method: "GET", params: {path: "maxSizeInBytes"}}
        });
    })
    .factory("ProjectShortDetails", function ($resource) {
        return $resource("../projects/:filter/shortDetails");
    });

