"use strict";

(function () {

    const DELAY = 1000;

    const STATUS_DONE = "done";
    const STATUS_FAILED = "failed";
    const STATUS_IN_PROGRESS = "in-progress";
    const STATUS_WAITING = "waiting";


    angular.module("s3UploadModule")

        .controller(
            "uploadTransferProcessDialogController",
            function ($scope, $routeParams, $timeout, UploadService, UploadBackgroundService) {
                var uploadId = parseInt($routeParams.id);

                $scope.vm = {
                    uploadFinished: false,
                    uploadItems: []
                };

                $scope.goBackground = function () {
                    UploadBackgroundService.moveToBackground({id: uploadId});
                    $(".modal").modal("hide");
                };

                $scope.cancel = function () {
                    UploadService.cancelCopy(uploadId);
                    $(".modal").modal("hide");
                };

                $scope.$on("REMOVE_UPLOADING_ITEM_EVENT", function (event, fileId) {
                    UploadService.cancelCopyFile(uploadId, fileId);
                });

                $timeout(updateStatus, DELAY);

                function updateStatus() {
                    UploadService
                        .status(uploadId)
                        .then(function (files) {
                            $scope.vm.uploadItems.length = 0;
                            Array.prototype.push.apply($scope.vm.uploadItems, files.map(makeUploadItem));

                            if (allFilesUploaded($scope.vm.uploadItems)) {
                                $scope.vm.uploadFinished = true;
                            } else {
                                $timeout(updateStatus, DELAY);
                            }
                        }, function (response, status) {
                            //TODO show error
                            $scope.vm.uploadFinished = true;
                            console.log("Upload status", response, status);
                        });
                }
            }
        );

    function allFilesUploaded(uploadItems) {
        var inProgressItem = uploadItems.find(function (item) {
            return item.status !== STATUS_DONE && item.status !== STATUS_FAILED;
        });
        return !angular.isDefined(inProgressItem);
    }

    function makeUploadItem(file) {
        return angular.extend(file, {
            get status() {
                switch (file.status) {
                    case "FAILED":
                        return STATUS_FAILED;
                    case "STARTED":
                    case "UPLOADED":
                        return STATUS_IN_PROGRESS;
                    case "REGISTERED":
                        return STATUS_DONE;
                    default:
                        return STATUS_WAITING;
                }
            },
            get uploaded() {
                return file.sizeUploaded;
            },
            get completePercentageFormatted() {
                return Math.floor(100 * file.sizeUploaded / file.size);
            },
            get remainingTimeFormatted() {
                return "Unknown";
            }
        });
    }

})();
