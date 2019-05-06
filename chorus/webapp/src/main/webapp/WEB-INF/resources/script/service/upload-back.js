"use strict";

angular.module("upload-back", ["ngResource"])
    .factory("Upload", function ($resource) {
        return $resource("../upload/:path", {}, {
            "uploadItems": {method: "POST", params: {path: "items"}},
            "cancelUpload": {method: "POST", params: {path: "cancel"}},
            "cancelFileUpload": {method: "POST", params: {path: "cancel"}},
            "checkUploadLimit": {method: "GET", params: {path: "checkUploadLimit"}},
            "checkMultipleFilesValid": {method: "GET", params: {path: "checkMultipleFilesValid"}}
        });
    })
    .factory("UnfinishedUploads", function ($resource) {
        return $resource("../upload/unfinished-uploads", {id: "@id"},
            {"remove": {method: "DELETE"}, params: {}}
        );
    })
    .factory("FileUploadPaths", function ($resource) {
        return $resource("../upload/destination/:fileId", {},
            {"getDestinationPath": {method: "GET"}, params: {}}
        );
    })
    .factory("Ping", function ($resource) {
        return $resource("../upload/ping");
    });
