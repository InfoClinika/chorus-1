/**
 * herman.zamula on 10/27/2014.
 * andrii.loboda modified this file on 2016-03-09
 */
angular.module("admin-tools-back", ["ngResource"])
    .factory("AdminNotifications", function ($resource) {
        return $resource("../admin/tools/notification");
    })
    .factory("AdminTools", function ($resource) {
        return $resource("../../admin/tools/:path", {}, {
            startSynchronization: {method: "GET", params: {path: "synchronize-s3-state-with-db"}},
            cancelSynchronization: {method: "GET", params: {path: "synchronize-s3-state-with-db-cancel"}},
            redirectToSynchronizationState: {method: "GET", params: {path: "synchronize-s3-state-with-db-check"}},
            restartAllCancelled: {method: "GET", params: {path: "restart-all-cancelled-runs"}},
            createPostProcessingTemplates: {method: "GET", params: {path: "generatePostProcessingPipelines"}},
            checkIsFilesSizeConsistent: {method: "GET", params: {path: "check-is-file-size-consistent"}},
            runBillingMigration: {method: "GET", params: {path: "run-billing-migration"}},
            unarchiveInconsistentFiles: {method: "GET", params: {path: "unarchive-inconsistent-files"}},
            generateDemoCdfDatabases: {method: "POST", params: {path: "generate-demo-cdf-databases"}},
            reTranslateAllNotTranslatedFilesOfExperiments: {method: "POST", params: {path: "retranslate-all-not-translated-files-of-experiments"}}
        });
    });
