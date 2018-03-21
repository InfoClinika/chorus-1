/**
 * Created by elena.philipenko on 18.03.14.
 */
angular.module("trash-back", ["ngResource"]).
    factory("Trash", function ($resource) {
        return $resource("../trash/:path", {}, {
            "listAll": {method: "GET", isArray: true, params: {path: "list"}},
            "restoreFile": {method: "GET", isArray: true, params: {path: "restoreFile"}},
            "restoreExperiment": {method: "GET", isArray: true, params: {path: "restoreExperiment"}},
            "restoreProject": {method: "GET", isArray: true, params: {path: "restoreProject"}},
            "readNotRestorableItems": {method: "GET", params: {path: "readNotRestorableItems"}}
        });
    });
