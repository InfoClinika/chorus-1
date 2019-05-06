"use strict";
(function () {

    angular.module("instrument-models-back", [])
        .factory("InstrumentModels", function ($resource) {
            return $resource("../instrument-models/:path", {}, {
                update: {method: "PUT"},
                paged: {method: "GET", params: {path: "paged"}},
                byVendor: {method: "GET", params: {path: "byVendor"}},
                instrumentTypesByTechTypeAndVendor: {
                    method: "GET",
                    params: {path: "instrumentTypesByTechnologyTypeAndVendor"}
                },
                extensionsByTechTypeAndVendor: {
                    method: "GET",
                    params: {path: "vendorExtensionsByTechnologyTypeAndVendor"}
                },
                getByTechnologyTypeAndVendor: {method: "GET", params: {path: "getByTechnologyTypeAndVendor"}},
                isNameUnique: {method: "GET", params: {path: "isNameUnique"}}
            });
        });
})();


