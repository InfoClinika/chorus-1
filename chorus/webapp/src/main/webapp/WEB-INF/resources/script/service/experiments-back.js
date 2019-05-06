"use strict";

angular.module("experiments-back", ["ngResource"])
    .factory("Experiments", function ($resource) {
        return $resource("../experiments/:paged/:filter", {paged: "@paged", filter: "@filter"}, {
            update: {method: "PUT"},
            get: {method: "POST"}
        });
    })
    .factory("NGSExperiments", function ($resource) {
        return $resource("../experiments/ngs/:path", {}, {
            import: {method: "POST", params: {path: "import"}},
            libraryPrepTypes: {method: "GET", params: {path: "getLibraryPrepTypes"}},
            experimentPrepMethodsByType: {method: "GET", params: {path: "getExperimentPrepMethodsByType"}},
            ntExtractionMethods: {method: "GET", params: {path: "getNtExtractionMethods"}},
            ngsExperimentTypes: {method: "GET", params: {path: "getNgsExperimentTypes"}}
        });
    })
    .factory("ExperimentDetails", function ($resource) {
        return $resource("../experiments/details/:id/:path", {}, {
            levels: {method: "GET", isArray: true, params: {path: "levels"}}
        });
    })
    .factory("ExperimentInstrumentModels", function ($resource) {
        return $resource("../experiments/new/instrumentModels");
    })
    .factory("ExperimentInstrumentTypes", function ($resource) {
        return $resource("../experiments/new/instrumentTypes");
    })
    .factory("ExperimentInstruments", function ($resource) {
        return $resource("../experiments/new/instruments");
    })
    .factory("ExperimentRestriction", function ($resource) {
        return $resource("../experiments/new/restriction");
    })
    .factory("ExperimentFiles", function ($resource) {
        return $resource("../experiments/:path/files/:operation/:id", {}, {
            usedInOtherExperiments: {method: "GET", isArray: true, params: {path: "usedInOtherExperiments"}},
            exist: {method: "GET", params: {path: "new", operation: "exist"}}
        });
    })
    .factory("ExperimentDetailsFiles", function ($resource) {
        return $resource("../experiments/details/:experiment/files", {}, {
            byExperiment: {method: "GET", isArray: true}
        });
    })
    .factory("ExperimentsByProject", function ($resource) {
        return $resource("../experiments/by-project/:id/", {id: "@id"}, {
            getPage: {method: "POST"}
        });
    })
    .factory("ExperimentTypes", function ($resource) {
        return $resource("../experiments/new/experimentTypes");
    })
    .factory("ExperimentLabels", function ($resource) {
        return $resource("../experiments/new/labels");
    })
    .factory("ExperimentLabelTypes", function ($resource) {
        return $resource("../experiments/new/labelTypes");
    })
    .factory("ExperimentSpecies", function ($resource) {
        return $resource("../experiments/new/species/:id/", {}, {
            "specie": {method: "GET", isArray: false},
            "defaultSpecie": {method: "GET", params: {id: "default"}}
        });
    })
    .factory("ExperimentAttachments", function ($resource) {
        return $resource("../attachments/experiment/:path/:id", {}, {
            "read": {method: "GET", isArray: true},
            "postMetadata": {method: "POST", params: {path: "items"}},
            "attachToExperiment": {method: "POST", params: {path: "attach"}},
            "remove": {method: "DELETE", params: {path: "interrupt"}},
            "getDestinationPath": {method: "GET", params: {path: "destination"}},
            "maxSizeInBytes": {method: "GET", params: {path: "maxSizeInBytes"}}
        });
    })
    .factory("ExperimentAnnotationAttachment", function ($resource) {
        return $resource("../annotations/experiment/:path/:id", {}, {
            "read": {method: "GET", isArray: true},
            "postMetadata": {method: "POST", params: {path: "items"}},
            "attachToExperiment": {method: "POST", params: {path: "attach"}},
            "remove": {method: "DELETE", params: {path: "interrupt"}},
            "getDestinationPath": {method: "GET", params: {path: "destination"}},
            "maxSizeInBytes": {method: "GET", params: {path: "maxSizeInBytes"}}
        });
    })
    .factory("ExperimentShortDetails", function ($resource) {
        return $resource("../experiments/:filter/shortDetails");
    })
    .factory("ExperimentMoveToStorage", function ($resource) {
        return $resource("../experiments/moveToStorage");
    })
    .factory("ExperimentFilesArchiving", function ($resource) {
        return $resource("../experiments/:path/:id", {id: "@id"}, {
            "archive": {method: "PUT", params: {path: "archive"}},
            "unarchive": {method: "PUT", params: {path: "unarchive"}}
        });
    })
    .factory("ExperimentQC", function ($resource) {
        return $resource("../experiments/markAsFailed/:id/:failed", {id: "@id", failed: "@failed"}, {
            "markAsFailed": {method: "PUT"}
        });
    })
    .factory("ExperimentColumns", function ($resource) {
        return $resource("../experiments/column-view/:path/:id", {}, {
            "available": {method: "GET", isArray: true, params: {path: "all"}},
            "views": {method: "GET", isArray: true, params: {path: "views"}},
            "queryColumns": {method: "GET", isArray: true, params: {path: "views"}},
            "columnMap": {method: "GET", params: {path: "column-map"}},
            "default": {method: "GET", params: {path: "default"}},
            "defaultColumns": {method: "GET", isArray: true, params: {path: "default", id: "columns"}},
            "selectedColumnSet": {method: "GET", isArray: true, params: {path: "selected"}}
        });
    });
