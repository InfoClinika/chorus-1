angular.module("admin-dashboard", ["admin-tools-back"])
    .config(function ($routeProvider) {
    })
    .controller("all-admin-tools", function ($scope, AdminTools) {

        $scope.vm = {
            archiving: {
                startSynchronization: startSynchronization,
                cancelSynchronization: cancelSynchronization,
                redirectToSynchronizationState: redirectToSynchronizationState
            },
            runs: {
                restartAllCancelled: restartAllCancelled
            },
            pipelines:{
                createPostProcessingTemplates: createPostProcessingTemplates
            },
            files:{
                checkIsFilesSizeConsistent: checkIsFilesSizeConsistent,
                unarchiveInconsistentFiles: unarchiveInconsistentFiles
            },
            billing: {
                runMigration: runMigration
            },
            cdf: {
                generateDemoCdfDatabases: generateDemoCdfDatabases
            },
            translation: {
                metadataOnly: false,
                reTranslateAllNotTranslatedFilesOfExperiments: reTranslateAllNotTranslatedFilesOfExperiments
            }
        };

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
                console.log("Redirect to synchronization state")
            });

        }

        function restartAllCancelled() {
            AdminTools.restartAllCancelled(function () {
                console.log("Restart all processing runs which are cancelled now")
            });

        }

        function createPostProcessingTemplates(){
            AdminTools.createPostProcessingTemplates(function () {
                console.log("Creating post processing templates is completed.")
            });
        }

        function checkIsFilesSizeConsistent(){
            AdminTools.checkIsFilesSizeConsistent(function(){
                console.log("Check file size with file size on S3 and fill sizeIsConsistent flag");
            })
        }

        function runMigration() {
            AdminTools.runBillingMigration(function () {
                console.log("Run billing migration.")
            })
        }

        function unarchiveInconsistentFiles() {
            AdminTools.unarchiveInconsistentFiles(function () {
                console.log("Unarchive inconsistent files.")
            })
        }

        function generateDemoCdfDatabases(){
            AdminTools.generateDemoCdfDatabases(function () {
                console.log("Demo CDF databases were generated.")
            })
        }

        function generateDemoMicroArraysWorkflow(){
            AdminTools.generateDemoMicroArraysWorkflow(function () {
                console.log("MciroArrays workflow was generated.")
            })
        }

        function reTranslateAllNotTranslatedFilesOfExperiments() {
            console.log("Retranslation for all files of all experiments started.");
            AdminTools.reTranslateAllNotTranslatedFilesOfExperiments({metadataOnly: $scope.vm.translation.metadataOnly}, function () {
                console.log("Retranslation for all files of all experiments finished.")
            })
        }
    });
