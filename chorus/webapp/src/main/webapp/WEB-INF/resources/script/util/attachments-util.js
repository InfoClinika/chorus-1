"use strict";

var AttachmentsHelper = {};

AttachmentsHelper.Attachment = function (name, date, size, type, originalFile, link, ext) {
    this.name = name;
    this.date = date;
    this.size = size;
    this.type = type;
    this.originalFile = originalFile;
    this.link = link;
    this.ext = ext;

    this.isUploading = false;
    this.uploadedSize = 0;
    this.uploadProgress = 0;
    this.uploadSpeed = 0; //in bytes/s
    this.remainingTime = 0; //in seconds
    this.attachmentId = null;
};

AttachmentsHelper.attachmentTypeFromName = function (name) {
    var type = "";
    if (name) {
        var dotIndex = name.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < name.length - 1) {
            type = name.substr(dotIndex + 1);
        }
    }
    return type.toLowerCase();
};

AttachmentsHelper.updateExtensionsLabelSupported = function ($scope, allowedExtensionsFn) {
    var defaultExtensions = [".pdf", ".doc", ".xls", ".ppt", ".jpg", ".png", ".tiff", ".bmp"];
    var xmlOfficeExt = [".docx", ".xlsx", ".pptx"];
    var extensions = allowedExtensionsFn ? $.merge(allowedExtensionsFn(), defaultExtensions) : defaultExtensions;
    $scope.filesExtensionSupported = $.map(extensions, function (item) {
        return item;//item.substring(1);
    }).join(", ");
    return $.merge(extensions, xmlOfficeExt);
};
AttachmentsHelper.unknownExt = ".mir";
AttachmentsHelper.commonSetup =
    function ($scope,
              id,
              dialogSelector,
              AttachmentService,
              startNewUpload,
              attachmentDownloadUri,
              writeMode,
              allowedExtensionsFn,
              options) {
        $scope.attachmentOptions = !options ? {} : options;
        $scope.existingAttachments =
            $scope.attachmentOptions.notRemoveExisted && $scope.existingAttachments ? $scope.existingAttachments : [];
        $scope.attachmentOptions.attachmentType =
            $scope.attachmentOptions.attachmentType ? $scope.attachmentOptions.attachmentType : "Attachment";

        $scope.attachmentsReadOnly = !writeMode;

        function getExtensionClassName($scope, type) {

            var knownExt = AttachmentsHelper.updateExtensionsLabelSupported($scope, null);
            var ext = /*"." +*/ type;
            if ($.inArray(ext, knownExt) == -1) {
                ext = AttachmentsHelper.unknownExt;
            }
            return ext;
        }

        if (id) {
            AttachmentService.read({path: id}, function (data) {
                var items = $.map(data, function (attachment) {
                    var type = AttachmentsHelper.attachmentTypeFromName(attachment.name);
                    CommonLogger.log(type);
                    var a = new AttachmentsHelper.Attachment(attachment.name,
                        attachment.uploadDate,
                        attachment.sizeInBytes,
                        type, null, null, getExtensionClassName(type)
                    );
                    a.attachmentId = attachment.id;
                    return a;
                });
                if (!$scope.attachmentOptions.notRemoveExisted) {
                    $scope.existingAttachments = items;
                } else {
                    var map = {};
                    angular.forEach($.merge($scope.existingAttachments, items), function (item) {
                        map[item.attachmentId] = item;
                    });
                    var results = [];
                    $.each(map, function (key, value) {
                        if ($scope.removedAttachments) {
                            if ($scope.removedAttachments.indexOf(Number(key)) == -1) {
                                results.push(value);
                            }
                        } else {
                            results.push(value);
                        }

                    });
                    $scope.existingAttachments = results;
                }

            });
        }


        $scope.uploadingAttachments = [];

        $scope.isAttachmentsUploading = function () {
            return $scope.uploadingAttachments.length > 0;
        };

        if (!$scope.attachmentsReadOnly) {
            AttachmentService.maxSizeInBytes(function (result) {
                $scope.maxSizeInBytes = result.value;
                var dragNDropHelper = setupAttachments(
                    dialogSelector,
                    $scope,
                    AttachmentsHelper.Attachment,
                    AttachmentsHelper.attachmentTypeFromName,
                    function () {
                        return AttachmentsHelper.updateExtensionsLabelSupported($scope, allowedExtensionsFn);
                    },
                    $scope.maxSizeInBytes,
                    $scope.attachmentOptions.fileChooserId,
                    $scope.attachmentOptions.isSingleFileUpload,
                    $scope.attachmentOptions.displayDragAndDropAreaOnlyIfAreaPresented,
                    options.allowAllFileTypes
                );
                AttachmentsHelper.updateExtensionsLabelSupported($scope, allowedExtensionsFn);

                $scope.removeUploadingAttachment = function (index, item) {
                    if (!item.isUploading) {
                        dragNDropHelper.removeFile(index);
                        return;
                    }
                    $scope.currentUpload.stop();
                    AttachmentService.remove({id: item.attachmentId}, function () {
                        dragNDropHelper.removeFile(index);
                    });
                };
            });
        }


        function removeFromUpload(files) {
            for (var i = 0; i < files.length; i++) {
                var duplicateIndex = DroppedFile.getDuplicateIndex(files[i].name, $scope.existingAttachments);
                if (duplicateIndex >= 0) {
                    $scope.uploadingAttachments.splice(duplicateIndex, 1);
                }
            }

        }

        $scope.$watch(function ($scope) {
            var totalValue = "";
            $($scope.uploadingAttachments).each(function () {
                totalValue = totalValue + this.name + this.size + ";";
            });
            return totalValue;
        }, function (newValue, oldValue, $scope) {
            if (!$scope.attachmentOptions.postponedUpload) {
                AttachmentsHelper.uploadFiles($scope, AttachmentService);
            }

        });

        AttachmentsHelper.uploadFiles = function ($scope, AttachmentService, bUpdateExisting) {
            CommonLogger.log("The uploading attachments have been changed: " +
                JSON.stringify($scope.uploadingAttachments));

            if ($scope.uploadingAttachments.length == 0) {
                //nothing to upload
                return;
            }
            if ($scope.uploadingAttachments[0].isUploading) {
                //the upload of some item is still in progress; let's wait
                return;
            }
            var itemToUpload = $scope.uploadingAttachments[0];

            CommonLogger.log("Saving the metadata for the attachment = " + JSON.stringify(itemToUpload));
            var dataToSend = {
                filename: itemToUpload.name,
                sizeInBytes: itemToUpload.size
            };
            if ($scope.attachmentOptions.getDataToSendFn) {
                dataToSend = $.extend(dataToSend, $scope.attachmentOptions.getDataToSendFn());
            }

            if (bUpdateExisting) {
                $scope.existingAttachments = [];
                AttachmentService.updateDetails(
                    dataToSend,
                    function (data) {
                        upload($scope, data, itemToUpload, AttachmentService);
                    }
                );
            } else {
                AttachmentService.postMetadata(
                    dataToSend,
                    function (data) {
                        upload($scope, data, itemToUpload, AttachmentService);
                    }
                );
            }
        };

        function upload($scope, data, itemToUpload, AttachmentService) {
            CommonLogger.log("The metadata for the attachments have been posted. Response: " + JSON.stringify(data));
            itemToUpload.isUploading = true;

            //to track the file item ID for the upload resume in the future
            itemToUpload.attachmentId = data.attachmentId;

            itemToUpload.originalFile.fileItemID = data.attachmentId;

            AttachmentService.getDestinationPath({id: data.attachmentId}, function (response) {
                $scope.currentUpload = startNewUpload(
                    {
                        file: itemToUpload.originalFile,
                        destinationPath: response.destinationPath,
                        logger: function (message) {
                            CommonLogger.log(message);
                        },
                        onUploadStart: function (fileId) {
                            CommonLogger.log("--- Starting the upload for the file with ID = " + fileId);
                        },
                        progressHandler: function (fileId, uploaded, uploadSpeed) {
                            itemToUpload.uploadProgress = uploaded / itemToUpload.size * 100;
                            itemToUpload.uploadedSize = uploaded;
                            itemToUpload.uploadSpeed = uploadSpeed;
                            itemToUpload.remainingTime =
                                remainingTimeFormat(Math.floor((itemToUpload.size - itemToUpload.uploadedSize) /
                                    itemToUpload.uploadSpeed));
                            $scope.attachmentOptions.uploadProgress && $scope.attachmentOptions.uploadProgress(
                                fileId,
                                uploaded,
                                uploadSpeed
                            );
                        },
                        onUploadFinished: function (fileId, contentUrl) {

                            $scope.attachmentOptions.uploadProgress && $scope.attachmentOptions.uploadProgress(
                                fileId,
                                itemToUpload.size,
                                0
                            );

                            if (AttachmentService.completeUpload) {
                                AttachmentService.completeUpload({id: fileId}, function (response) {
                                    whenComplete();
                                });
                            } else {
                                whenComplete();
                            }

                            function whenComplete() {
                                CommonLogger.log("--- The upload has been completed for the file with ID = " + fileId +
                                    ", contentUrl: " + contentUrl);
                                var completedItem = $scope.uploadingAttachments.splice(0, 1)[0];
                                completedItem.isUploading = false;
                                completedItem.uploadProgress = 100;
                                completedItem.ext = getExtensionClassName(completedItem.type);
                                if ($scope.attachmentOptions.isSingleFileUpload) {
                                    $scope.existingAttachments = [completedItem];
                                } else {
                                    $scope.existingAttachments.push(completedItem);
                                }

                                $scope.$apply();

                                $scope.attachmentOptions.uploadComplete && $scope.attachmentOptions.uploadComplete(
                                    fileId,
                                    contentUrl
                                );
                            }
                        },
                        //pass the reference to pauseButton element
                        pauseButton: document.getElementById("pauseButton")
                    });
            });
        }

        //set the upload indicator updates
        var scheduleIndicatorUpdate = function (indicatorUpdateInterval) {
            setTimeout(function () {
                $scope.$apply();
                scheduleIndicatorUpdate(indicatorUpdateInterval);
            }, indicatorUpdateInterval);
        };
        scheduleIndicatorUpdate(1000);

        $scope.$watch(function ($scope) {
            var totalValue = "";
            $($scope.existingAttachments).each(function () {
                totalValue = totalValue + this.name + this.size + ";";
            });
            return totalValue;
        }, function () {
            CommonLogger.log("The existing attachments have been modified: " +
                JSON.stringify($scope.existingAttachments));
        });

        if (!$scope.attachmentsReadOnly) {
            $scope.removeExistingAttachment = function (attachmentId) {
                $scope.existingAttachments = $.grep($scope.existingAttachments, function (item) {
                    return attachmentId != item.attachmentId;
                });
                if (!$scope.removedAttachments) {
                    $scope.removedAttachments = [];
                }
                $scope.removedAttachments.push(attachmentId);
            };
        }


        $scope.downloadAttachment = function (attachmentId) {
            $.fileDownload(attachmentDownloadUri + attachmentId, {});
        };

        $scope.downloadAll = function () {
            angular.forEach($scope.existingAttachments, function (item) {
                $.fileDownload(attachmentDownloadUri + item.attachmentId, {});
            });
        };
    };
