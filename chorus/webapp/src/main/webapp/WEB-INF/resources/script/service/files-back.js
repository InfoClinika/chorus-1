"use strict";

(function () {
    angular.module("files-back", ["ngResource"])
        .factory("Files", function ($resource) {
            return $resource(
                "../files/:paged/:filter/:type/:id",
                {filter: "@filter", paged: "@paged", type: "@type", id: "@id"},
                {
                    "get": {method: "POST"},
                    "update": {method: "PUT"},
                    "bulkUpdateLabels": {method: "PUT", params: {paged: "bulk", filter: "labels"}},
                    "bulkUpdateSpecies": {method: "PUT", params: {paged: "bulk", filter: "species"}},
                    "archive": {method: "PUT", params: {type: "archive"}},
                    "unarchive": {method: "PUT", params: {type: "un-archive"}},
                    "deletePermanently": {method: "DELETE", params: {type: "delete-permanently"}},
                    "isReadyToUpload": {method: "POST", params: {type: "isReadyToUpload"}}
                }
            );
        })
        .factory("FileColumns", function ($resource) {
            return $resource("../files/column-view/:path/:id", {}, {
                "available": {method: "GET", isArray: true, params: {path: "all"}},
                "views": {method: "GET", isArray: true, params: {path: "views"}},
                "queryColumns": {method: "GET", isArray: true, params: {path: "views"}},
                "columnMap": {method: "GET", params: {path: "column-map"}},
                "default": {method: "GET", params: {path: "default"}},
                "defaultColumns": {method: "GET", isArray: true, params: {path: "default", id: "columns"}},
                "selectedColumnSet": {method: "GET", isArray: true, params: {path: "selected"}}
            });
        })
        .factory("FileDetails", function ($resource) {
            return $resource("../files/details/:id");
        })
        .factory("FileDownloadRequest", function ($resource) {
            //TODO: No matching in FilesController. Check and remove.
            return $resource("../files/downloadRequest");
        })
        .factory("FileDetailsWithConditions", function ($resource) {
            return $resource("../files/detailsWithConditions/:experimentId/:id");
        })
        .factory("FilesByExperiment", function ($resource) {
            return $resource("../files/:paged/by-experiment/:id/", {"paged": "@paged", "id": "@id"},
                {"get": {method: "POST"}}
            );
        })
        .factory("FilesByLab", function ($resource) {
            return $resource("../files/:paged/bylab/:id/", {"paged": "@paged", "id": "@id"},
                {"get": {method: "POST"}}
            );
        })
        .factory("FileDownloads", function ($resource) {
            return $resource("../download/:path/", {}, {
                "startDownload": {method: "GET", params: {path: "bulk"}},
                "moveToStorage": {method: "GET", params: {path: "moveToStorage"}}
            });
        });

})();
