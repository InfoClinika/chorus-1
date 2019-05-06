/**
 * herman.zamula on 10/27/2014.
 * andrii.loboda modified this file on 2016-03-09
 */
"use strict";

(function () {

    angular.module("admin-tools-back", ["ngResource"])
        .factory("AdminNotifications", function ($resource) {
            return $resource("../admin/tools/notification");
        })
        .factory("AdminTools", function ($resource) {
            return $resource("../../admin/tools/:path", {}, {
                startSynchronization: {method: "GET", params: {path: "synchronize-s3-state-with-db"}},
                cancelSynchronization: {method: "GET", params: {path: "synchronize-s3-state-with-db-cancel"}},
                redirectToSynchronizationState: {method: "GET", params: {path: "synchronize-s3-state-with-db-check"}},
                checkIsFilesSizeConsistent: {method: "GET", params: {path: "check-is-file-size-consistent"}},
                runBillingMigration: {method: "GET", params: {path: "run-billing-migration"}},
                unarchiveInconsistentFiles: {method: "GET", params: {path: "unarchive-inconsistent-files"}},
            });
        });

})();
