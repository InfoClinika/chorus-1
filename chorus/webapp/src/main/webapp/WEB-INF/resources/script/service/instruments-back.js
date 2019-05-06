"use strict";

angular.module("instruments-back", ["ngResource"])
    .factory("Instruments", function ($resource) {
        return $resource("../instruments/:path/:id", {}, {
            update: {method: "PUT"},
            byLab: {method: "GET", params: {path: "bylab"}, isArray: true},
            createDefault: {method: "POST", params: {path: "createDefaultInstrument"}},
            byLabAndStudyType: {method: "GET", params: {path: "byLabAndStudyType"}, isArray: true}
        });
    })
    .factory("InstrumentExtendedInfo", ["$resource", function ($resource) {
        return $resource("../instruments/:id/:path", {}, {
            model: {method: "GET", params: {path: "model"}}
        });
    }])
    .factory("InstrumentVendors", function ($resource) {
        return $resource("../instruments/:path", {}, {
            all: {
                method: "GET",
                isArray: true,
                params: {path: "vendors"}
            },
            byTechnologyType: {
                method: "GET",
                isArray: true,
                params: {path: "vendorsByStudyType"}
            }
        });
    })
    .factory("InstrumentTechnologyTypes", function ($resource) {
        return $resource("../instruments/studyTypes");
    })
    .factory("OperatedInstruments", function ($resource) {
        return $resource("../instruments/operated");
    })
    .factory("AvailableOperators", function ($resource) {
        return $resource("../instruments/operators");
    });
