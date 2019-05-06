"use strict";

(function () {

    angular.module("background-upload", ["js-upload", "upload-back", "error-catcher"])
        .factory("updateIndicators", function () {
            return function ($scope) {
                //set the upload indicator updates
                var scheduleIndicatorUpdate = function (indicatorUpdateInterval) {
                    setTimeout(function () {
                        if (!$scope.uploadFinished) {
                            $scope.$apply();
                            scheduleIndicatorUpdate(indicatorUpdateInterval);
                        }
                    }, indicatorUpdateInterval);
                };
                scheduleIndicatorUpdate(1000);
            };
        })
        .factory("uploadStats", function () {
            return function ($scope) {
                return {
                    uploadSpeed: 0,
                    uploadStartTime: 0,
                    currentIndex: 0,

                    totalUploaded: function () {
                        var totalUploaded = 0;
                        $($scope.uploadItems).each(function () {
                            if (this.uploaded) {
                                totalUploaded = totalUploaded + this.uploaded;
                            }
                        });
                        return totalUploaded;
                    },

                    totalSize: function () {
                        var totalBytes = 0;
                        $($scope.uploadItems).each(function () {
                            if (this.status != "select-file") {
                                totalBytes = totalBytes + this.size;
                            }
                        });
                        return totalBytes;
                    },

                    uploadProgressFormatted: function () {
                        var totalBytes = $scope.stats.totalSize();
                        var bytesUploaded = $scope.stats.totalUploaded();
                        if ($scope.zipItems) {
                            return Math.floor(100 * bytesUploaded / (totalBytes * ($scope.zipItems.length + 1)));
                        }
                        return Math.floor(100 * bytesUploaded / totalBytes);


                    },

                    remainingTimeFormatted: function () {
                        if ($scope.stats.uploadSpeed === 0) {
                            return "unknown";
                        }
                        var time = Math.floor(($scope.stats.totalSize() - $scope.stats.totalUploaded()) /
                            $scope.stats.uploadSpeed);
                        return remainingTimeFormat(time);

                    }
                };
            };
        })
        .factory("UploadItem", function () {
            var UploadItem = function (scope, fileItemID, name, size) {
                this.fileItemID = fileItemID;
                this.uploaded = 0;
                this.status = "waiting";
                this.size = size;
                this.name = name;
                this.scope = scope;
            };

            UploadItem.prototype.completePercentageFormatted = function () {
                return Math.floor(100 * this.uploaded / this.size);
            };

            UploadItem.prototype.remainingTimeFormatted = function () {
                if (this.scope.stats.uploadSpeed === 0) {
                    return "Unknown";
                }
                return remainingTimeFormat(Math.floor((this.size - this.uploaded) / this.scope.stats.uploadSpeed));

            };

            return UploadItem;
        })
        .factory("ZipItem", function () {
            var ZipItem = function (scope, name) {
                this.status = "zipping-wait";
                this.name = name;
                this.scope = scope;
            };
            return ZipItem;
        })
        .factory("updateBackgroundUpload", function ($rootScope) {
            return function () {
                $rootScope.$broadcast("refreshFileUpload");
            };
        })
        .factory("backgroundUpload", function ($rootScope) {

            return function () {

                if (!$rootScope.uploadItems) {
                    CommonLogger.log("$scope.uploadItems is undefined");
                    $rootScope.uploadInProgress = false;
                    return;
                }

                function onAllFilesUploaded() {
                    CommonLogger.log("All files have been successfully uploaded.");
                    $rootScope.uploadInProgress = false;
                }

            };
        })
        .factory(
            "backgroundUploader",
            function ($rootScope,
                      startNewUpload,
                      UnfinishedUploads,
                      updateIndicators,
                      FileUploadPaths,
                      uploadStats,
                      Ping) {
                var uploadItemAtIndex = null;

                $rootScope.uploadItems = []; //items in the progress dialog
                $rootScope.stats = uploadStats($rootScope);
                $rootScope.uploadInProgress = false;
                $rootScope.filesToUpload = [];
                $rootScope.zipItems = [];

                //$rootScope.filesToZip = [];

                $rootScope.multipleFilesItems = [];

                window.onbeforeunload = function (e) {
                    if (!$rootScope.uploadInProgress) {
                        return;
                    }
                    return "Some files are uploading in background";
                };

                function pong() {
                    $.each($rootScope.uploadItems, function (i, item) {
                        if (item.originalFile && item.originalFile.archive && item.status != "done") {
                            Ping.save(item.fileItemID);
                        }
                    });
                }

                var pingId = null;

                var uploadHelper = function ($scope, uploadingFunctions) {
                    $scope.uploader = {
                        onUploadStart: uploadingFunctions && uploadingFunctions.onUploadStart,
                        onUploadFinished: uploadingFunctions && uploadingFunctions.onUploadFinished,
                        onUploadFailed: uploadingFunctions && uploadingFunctions.onUploadFailed,
                        progressHandler: uploadingFunctions && uploadingFunctions.progressHandler
                    };
                    return {
                        start: function (forceUpload, isResume) {
                            doUpload($scope, forceUpload, isResume);
                        },
                        cancel: function (onCancel) {
                            if ($rootScope.activeUpload) {
                                CommonLogger.log("Stopping active background upload, if any");
                                $rootScope.activeUpload.stop();
                            }
                            if (onCancel) {
                                onCancel();
                            }
                            clearRoot();
                        },
                        stopActive: function (onStop) {
                            if ($rootScope.activeUpload) {
                                CommonLogger.log("Stopping active background upload, if any");
                                $rootScope.activeUpload.stop();
                            }
                            if (onStop) {
                                onStop();
                            }
                        },
                        pause: function (onPause) {
                            if ($rootScope.activeUpload) {
                                $rootScope.activeUpload.pause();
                            }
                            if (onPause) {
                                onPause();
                            }
                        }
                    };
                };

                function clearRoot() {
                    $rootScope.uploadInProgress = false;
                    $rootScope.uploadFinished = true;
                    $rootScope.zipItems = [];
                    $rootScope.filesToUpload = [];
                    //$rootScope.filesToZip = [];
                    $rootScope.multipleFilesItems = [];

                    //Prevent infinite call of $watch("uploadItems");
                    if ($rootScope.uploadItems.length != 0 || $rootScope.zipItems.length != 0) {
                        $rootScope.zipItems = [];
                        $rootScope.uploadItems = [];
                    }

                    //TODO [herman.zamula]: move to another place
                    $rootScope.zippingProcess = {isZipping: false, progress: 0, completed: 0};
                    clearInterval(pingId);
                    delete $rootScope.zipHelper;
                    delete $rootScope.isZipping;

                    delete $rootScope.paused;
                    delete $rootScope.activeUpload;
                }

                $rootScope.$on("clearRootEvent", clearRoot);


                function doUpload($scope, forceUpload, isResume) {

                    pingId = setInterval(pong, 5000);

                    var onAllFilesUploaded = function () {
                        CommonLogger.log("All files have been successfully uploaded.");
                        $rootScope.uploadFinished = true;
                        $rootScope.uploadInProgress = false;
                        $rootScope.activeUploadInProgress = false;
                        $rootScope.uploadStarted = false;
                        clearInterval(pingId);
                    };

                    updateIndicators($rootScope);

                    uploadItemAtIndex = function (currentItemIndex) {

                        $rootScope.uploadStarted = false;
                        if (isResume) {
                            if ($rootScope.uploadItems[currentItemIndex].status !== "waiting") {
                                if (currentItemIndex < $rootScope.uploadItems.length - 1) {
                                    uploadItemAtIndex(currentItemIndex + 1);
                                } else {
                                    onAllFilesUploaded();
                                }
                                return;
                            }
                        }

                        $rootScope.stats.uploadStartTime = (new Date()).getTime();

                        $rootScope.stats.currentIndex = currentItemIndex + 1;
                        var getViewItemByFileId = function (fileId) {
                            return $.grep($rootScope.uploadItems, function (anItem) {
                                return anItem.fileItemID == fileId;
                            })[0];
                        };
                        var fileUploadStartTime;

                        function startUpload(response) {
                            var file = $rootScope.uploadItems[currentItemIndex].originalFile;
                            if (isResume) {
                                file.fileItemID = $rootScope.uploadItems[currentItemIndex].fileItemID;
                            }
                            $rootScope.activeUpload = startNewUpload(
                                {
                                    confirmUrl: "../cors/confirm",
                                    startMultipartUrl: response ? "../cors/startmultipart" : undefined,
                                    file: file,
                                    destinationPath: response ? response.destinationPath : undefined,
                                    unfinishedUpload: $rootScope.uploadItems[currentItemIndex].unfinishedUpload,
                                    logger: function (message) {
                                        CommonLogger.log(message);
                                    },
                                    onUploadStart: function (fileId) {
                                        $rootScope.activeUploadInProgress = true;
                                        CommonLogger.log("Starting upload of file #" + currentItemIndex);
                                        var viewItem = getViewItemByFileId(fileId);
                                        viewItem.status = "in-progress";
                                        fileUploadStartTime = (new Date()).getTime();
                                        if ($scope.uploader.onUploadStart) {
                                            $scope.uploader.onUploadStart(fileId);
                                        }
                                        $rootScope.uploadStarted = true;
                                        setTimeout(function () {
                                            $scope.$apply();
                                        });
                                    },
                                    progressHandler: function (fileId, uploaded, uploadSpeed) {
                                        var viewItem = getViewItemByFileId(fileId);
                                        if (viewItem) {
                                            viewItem.uploaded = uploaded;
                                        }
                                        $scope.stats.uploadSpeed = uploadSpeed;
                                        if ($scope.uploader.progressHandler) {
                                            $scope.uploader.progressHandler(fileId, uploaded, uploadSpeed);
                                        }
                                        setTimeout(function () {
                                            $scope.$apply();
                                        });
                                    },
                                    onUploadFinished: function (fileId) {

                                        var viewItem = getViewItemByFileId(fileId);
                                        viewItem.status = "done";

                                        if (currentItemIndex < $rootScope.uploadItems.length - 1) {
                                            uploadItemAtIndex(currentItemIndex + 1);
                                        } else if ($rootScope.zipItems.length === 0) {
                                            onAllFilesUploaded();
                                        }
                                        if ($scope.uploader.onUploadFinished) {
                                            $scope.uploader.onUploadFinished(fileId);
                                        }
                                        setTimeout(function () {
                                            $scope.$apply();
                                        });
                                    },
                                    onUploadFailed: function (fileId) {
                                        var viewItem = getViewItemByFileId(fileId);
                                        viewItem.status = "failed";

                                        if (currentItemIndex < $rootScope.uploadItems.length - 1) {
                                            uploadItemAtIndex(currentItemIndex + 1);
                                        } else if ($rootScope.zipItems.length === 0) {
                                            onAllFilesUploaded();
                                        }
                                        if ($scope.uploader.onUploadFailed) {
                                            $scope.uploader.onUploadFailed(fileId);
                                        }
                                        setTimeout(function () {
                                            $scope.$apply();
                                        });
                                    },
                                    onUploadVerificationFailed: function (fileId) {
                                        CommonLogger.log("Retrying uploading. File ID:", fileId);
                                        uploadItemAtIndex(currentItemIndex);
                                    }
                                });
                        }

                        if (isResume) {
                            startUpload();
                        } else {
                            FileUploadPaths.getDestinationPath(
                                {fileId: $rootScope.uploadItems[currentItemIndex].fileItemID},
                                function (response) {
                                    startUpload(response);
                                }
                            );
                        }
                    };
                    if (forceUpload) {
                        uploadItemAtIndex(0);
                    }
                }

                $rootScope.$watch("uploadItems.length", function (newValue, oldValue) {

                    var waitingItems = $.grep($rootScope.uploadItems, function (item) {
                        return item.status == "waiting";
                    });
                    if (newValue == 0 ||
                        waitingItems.length == 0) {
                        return;
                    }
                    if ($rootScope.isNewUpload && !$rootScope.newUpload.paused
                        || !$rootScope.isNewUpload && !$rootScope.resumeUpload.paused) {
                        evaluateIndexAndStartUpload(newValue);
                    }
                });

                $rootScope.$watch("uploadFinished", function (newValue, oldValue) {
                    if (newValue != oldValue) {
                        if (newValue == true && $rootScope.zipItems.length == 0 && $rootScope.uploadInBackground) {
                            clearRoot();
                        }
                    }
                });


                function evaluateIndexAndStartUpload(itemsLength) {
                    var index;
                    for (var i = 0; i < itemsLength; i++) {
                        if ($rootScope.uploadItems[i].status == "in-progress") {
                            return;
                        }
                    }
                    for (var j = 0; j < itemsLength; j++) {
                        if ($rootScope.uploadItems[j].status == "waiting") {
                            index = j;
                            break;
                        }
                    }
                    uploadItemAtIndex(index);
                }

                $rootScope.$watch("paused", function (pause) {
                    if (!pause && (!$rootScope.activeUpload || !$rootScope.activeUpload.inProgress()) &&
                        $.grep($rootScope.uploadItems, function (item) {
                            return item.status == "waiting";
                        }).length > 0) {
                        evaluateIndexAndStartUpload($rootScope.uploadItems.length);
                    }
                });

                return uploadHelper;
            }
        )
        .factory("zipAndUploadHelper", function ($rootScope, Upload, ZipItem, UploadItem, clearRoot, $timeout) {

            $rootScope.zipItems = [];

            $rootScope.$watch("zipItems", function () {
                if ($rootScope.zipItems.length == 0) {
                    $rootScope.isZipping = false;
                }
            });

            return function ($scope, uploader) {

                function removeZipped(name) {
                    $rootScope.zipItems = $.grep($rootScope.zipItems, function (item) {
                        return item.name != name;
                    });
                }

                function mapUploadItems(files) {
                    $rootScope.uploadItems = $.map(files, function (file) {
                        var uploadItem = new UploadItem($scope, file.fileItemID, file.name, file.size);
                        uploadItem.unfinishedUpload = file.unfinishedUpload;
                        uploadItem.originalFile = file;
                        return uploadItem;
                    });
                }

                return {
                    processZipping: function processZipping(itemsForZip, zipFunction) {
                        $rootScope.isZipping = true;
                        $rootScope.uploadInProgress = true;
                        $rootScope.paused = false;

                        $rootScope.zipItems = $.map(itemsForZip, function (file) {
                            return new ZipItem($scope, file.uploadName);
                        });

                        function getZipItemByName(name) {
                            return $.grep($rootScope.zipItems, function (item) {
                                return item.name == name;
                            })[0];
                        }

                        var processAtIndex = function (index) {
                            var currentIndex = index;

                            var zipItem = getZipItemByName(itemsForZip[currentIndex].uploadName);
                            deleteRestrictedFolderAttributes(zipItem);
                            CommonLogger.log(itemsForZip, index, zipItem);
                            zipItem.status = "zipping";

                            $rootScope.zipHelper = new ZipHelper({
                                zipFunctions: {
                                    zipProgressCallback: function (entry, progress, getArchivedFileSizeFn) {
                                        var old = $rootScope.zippingProcess.dir.progress;
                                        $rootScope.zippingProcess.dir = entry;
                                        $rootScope.zippingProcess.dir.progress = Math.floor(100 * progress);
                                        if (old != $rootScope.zippingProcess.dir.progress) {
                                            CommonLogger.log("Zipping in progress: " + entry.name + " - " +
                                                $rootScope.zippingProcess.dir.progress);
                                            if (getArchivedFileSizeFn) {
                                                getArchivedFileSizeFn.getMetadata(function (metadata) {
                                                    CommonLogger.log("Current size: " +
                                                        Math.floor(metadata.size / (1024 * 1024)) + "mb");
                                                });
                                            }
                                        }

                                        setTimeout(function () {
                                            $scope.$apply();
                                        });
                                    },
                                    zipCompleteCallback: function (entry) {
                                        removeZipped(entry.name);
                                        if ($rootScope.zipItems.length === 0) {
                                            // $rootScope.filesToZip = [];
                                            $rootScope.multipleFilesItems = [];
                                            $rootScope.zippingProcess.completed = true;
                                            return;
                                        }
                                        processAtIndex(currentIndex + 1);
                                        setTimeout(function () {
                                            $scope.$apply();
                                        });
                                    }
                                },
                                onErrorHandler: function (errorMessage) {
                                    CommonLogger.error(errorMessage);
                                    $scope.onCancelConfirm();
                                    $rootScope.cleanBrowserCachWarning =
                                        new Confirmation("#clean-browser-cache-warning", null,
                                            {
                                                getName: function () {
                                                    return errorMessage;
                                                }
                                            }
                                        );
                                    $timeout(function () {
                                        $rootScope.cleanBrowserCachWarning.showPopup();
                                    }, 1000);


                                }
                            });
                            zipFunction(itemsForZip, currentIndex);
                        };
                        processAtIndex(0);
                    },
                    startUploadZipped: function startUploadZipped(zippedEntry, instrument) {
                        var instrumentId = instrument.id;
                        var labId = instrument.lab;
                        var checkUploadLimitRequest = {
                            instrumentId: instrumentId,
                            bytesToUpload: zippedEntry.size,
                            labId: labId
                        };

                        Upload.checkUploadLimit(checkUploadLimitRequest, function (response) {
                            if (response.value) {
                                uploadFunction();
                            } else {
                                clearRoot();
                                uploadLimitExceptionHandler();
                            }
                        });

                        function uploadFunction() {

                            var uploadRequest = {
                                instrument: instrumentId, files: [
                                    {
                                        name: zippedEntry.name,
                                        labels: zippedEntry.labels,
                                        size: zippedEntry.size,
                                        specie: zippedEntry.specie,
                                        archive: true
                                    }
                                ]
                            };

                            Upload.uploadItems(uploadRequest, function (data) {
                                CommonLogger.log("Upload request posted successfully. Response:" +
                                    JSON.stringify(data));

                                // *** Uploading binary contents after the meta data has been successfully posted *** //
                                var storedItem = $.grep(data.files, function (storedItem) {

                                    // resulting file name shouldn't contain spaces; we're replacing them with underscores.
                                    return storedItem.name == zippedEntry.name.replace(new RegExp(" ", "g"), "_");
                                })[0];

                                zippedEntry.originalFile.fileItemID = storedItem.storedItemId;
                                var files = [zippedEntry.originalFile];

                                if ($scope.activeUploadInProgress) {
                                    var uploadItem = new UploadItem(
                                        $scope,
                                        files[0].fileItemID,
                                        files[0].name,
                                        files[0].size
                                    );
                                    uploadItem.unfinishedUpload = files[0].unfinishedUpload;
                                    uploadItem.originalFile = files[0];
                                    $rootScope.uploadItems.push(uploadItem);
                                } else {
                                    mapUploadItems(files);
                                    removeZipped($rootScope.uploadItems[0].name);
                                    uploader.start();
                                }
                            });

                        }

                    },
                    updateZippedEntry: function updateZippedEntry(zippedEntry, zipped) {
                        zippedEntry.name = zipped.name;
                        zippedEntry.size = zipped.size;
                        zippedEntry.originalFile = zipped;
                        zippedEntry.archive = true;
                        zippedEntry.size = zipped.size;
                    }
                };
            };
        })
        .factory("updateResumeUploadButton", function ($rootScope) {
            return function () {
                $rootScope.$broadcast("refreshResumeUpload");
            };
        })
        .factory("clearRoot", function ($rootScope) {
            return function () {
                $rootScope.$broadcast("clearRootEvent");
            };
        });
})();

function onWindowReload() {

    $("#backgroundUploadReload").dialog({
        title: "Warning",
        draggable: false,
        dialogClass: "message-dialog warning",
        modal: true,
        resizable: false,
        width: 450,
        buttons: {
            "Ok": function () {
                window.location.reload();
            },
            "Cancel": function () {
                $(this).dialog("close");
            }
        },
        close: function () {
            $(this).dialog("close");
        }
    });
}

function remainingTimeFormat(time) {
    if (isNaN(time) || !isFinite(time) || time === 0) {
        return "unknown";
    }
    var hours = Math.floor(time / 3600);
    var number = time - 3600 * hours;
    var minutes = Math.floor(number / 60);
    var seconds = number - minutes * 60;
    return (hours > 0 ? hours + "h " : "") + (minutes > 0 ? minutes + "m " : "") + seconds + "s";
}

function uploadLimitExceptionHandler() {
    $(".modal").modal("hide");

    $("#upload-limit-error-message").dialog({
        title: "Upload Limit Exceeded",
        draggable: false,
        dialogClass: "message-dialog warning",
        modal: true,
        resizable: false,
        width: 450,
        buttons: {
            OK: function () {
                $(this).dialog("close");
            }
        }
    });
}
