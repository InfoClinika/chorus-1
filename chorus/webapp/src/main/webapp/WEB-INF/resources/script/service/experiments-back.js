angular.module("experiments-back", ["ngResource"])
    .factory("Experiments", function ($resource) {
        return $resource("../experiments/:paged/:filter", {paged: "@paged", filter: "@filter"}, {
            update: {method: "PUT"},
            get: {method: "POST"}
        });
    })
    .factory("MicroArraysExperiments", function ($resource) {
        return $resource("../microarray-experiments/:path", {}, {
            import: {method: "POST", params: {path: "import"}}
        });
    })
    .factory("ExperimentDetails", function($resource){
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
            query: {method: "GET", isArray: true, params: {path: "new"}},
            usedInOtherExperiments: {method: "GET", isArray: true, params: {path: "usedInOtherExperiments"}},
            haveSameSpecies: {method: "POST", params: {path: "haveSameSpecies"}},
            exist: {method: "GET", params: {path: "new", operation: "exist"}}
        });
    })
    .factory("PagedExperimentFiles", function($resource){
        return $resource("../experiments/paged/:path/files/:id", {}, {
            get: {method: "GET", isArray: false, params: {path: "new"}}
        })
    })
    .factory("ExperimentDetailsFiles", function ($resource) {
        return $resource("../experiments/details/:experiment/files", {}, {
            byExperiment: {method: "GET", isArray: true}
        });
    })
    .factory("ExperimentsByProject",function ($resource) {
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
            "specie": {method: "GET", isArray: false}
        });
    })
    .factory("ExperimentAttachments", function ($resource) {
        return $resource("../attachments/experiment/:path/:id", {}, {
            "read":{method:"GET", isArray: true},
            "postMetadata":{method:"POST", params:{path:"items"}},
            "attachToExperiment":{method:"POST", params:{path:"attach"}},
            "remove": {method: "DELETE", params: {path: "interrupt"}},
            "getDestinationPath": {method: "GET", params: {path: "destination"}},
            "maxSizeInBytes": {method: "GET", params: {path: "maxSizeInBytes"}}
        });
    })
    .factory("ExperimentAnnotationAttachment", function ($resource) {
        return $resource("../annotations/experiment/:path/:id", {}, {
            "read":{method:"GET", isArray: true},
            "postMetadata":{method:"POST", params:{path:"items"}},
            "attachToExperiment":{method:"POST", params:{path:"attach"}},
            "remove": {method: "DELETE", params: {path: "interrupt"}},
            "getDestinationPath": {method: "GET", params: {path: "destination"}},
            "maxSizeInBytes": {method: "GET", params: {path: "maxSizeInBytes"}}
        });
    })
    .factory("ExperimentShortDetails", function($resource){
        return $resource("../experiments/:filter/shortDetails");
    })
    .factory("ExperimentMoveToStorage", function($resource){
        return $resource("../experiments/moveToStorage");
    })
    .factory("ExperimentTranslation", function($resource){
        return $resource("../experiments/:path/:id/:chargedLab", {id: "@id", chargedLab: "@chargedLab"}, {
            "translate":{method:"PUT", params:{path:"translate"}},
            "precache":{method:"GET", params:{path:"precache"}},
            "status":{method:"GET", params:{path:"translationstatus"}},
            "translateNotTranslated": {method: "PUT", params: {path: "translateNotTranslated"}},
            "deleteTranslationData": {method: "DELETE", params: {path: "deleteTranslationData"}}
        });
    })
    .factory("ExperimentFilesArchiving", function($resource){
        return $resource("../experiments/:path/:id", {id: "@id"}, {
            "archive": {method: "PUT", params: {path: "archive"}},
            "unarchive": {method: "PUT", params: {path: "unarchive"}}
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
