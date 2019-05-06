"use strict";

angular.module("admin-dashboard", ["admin-tools-back"])
    .controller("all-admin-tools", function ($scope, $http, AdminTools) {

        var ADMIN_CONTROLLER_ENDPOINT = "../../admin/tools/";

        $scope.vm = {
            archiving: {
                startSynchronization: startSynchronization,
                cancelSynchronization: cancelSynchronization,
                redirectToSynchronizationState: redirectToSynchronizationState
            },
            files: {
                checkIsFilesSizeConsistent: checkIsFilesSizeConsistent,
                unarchiveInconsistentFiles: unarchiveInconsistentFiles
            },
            billing: {
                runMigration: runMigration
            }
        };

        function sendGetRequestToAdminController(path, callback) {
            $http.get(ADMIN_CONTROLLER_ENDPOINT + path).then(callback);
        }

        function startSynchronization() {
            AdminTools.startSynchronization(function () {
                console.log("Start synchronization");
            });

        }

        function cancelSynchronization() {
            AdminTools.cancelSynchronization(function () {
                console.log("Cancel synchronization");
            });

        }

        function redirectToSynchronizationState() {
            AdminTools.redirectToSynchronizationState(function () {
                console.log("Redirect to synchronization state");
            });

        }

        function checkIsFilesSizeConsistent() {
            AdminTools.checkIsFilesSizeConsistent(function () {
                console.log("Check file size with file size on S3 and fill sizeIsConsistent flag");
            });
        }

        function runMigration() {
            AdminTools.runBillingMigration(function () {
                console.log("Run billing migration.");
            });
        }

        function unarchiveInconsistentFiles() {
            AdminTools.unarchiveInconsistentFiles(function () {
                console.log("Unarchive inconsistent files.");
            });
        }
    });
