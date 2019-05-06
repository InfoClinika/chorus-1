"use strict";

angular.module("filesControllers",
    ["files-back", "instruments-back", "formatters", "breadcrumbs", "security-front", "security-back",
        "watchFighters", "downloader", "front-end", "error-catcher", "enums", "popups",
        "feature-service", "user-details-service"])
    .controller("files", function ($scope,
                                   contentRequestParameters,
                                   $routeParams,
                                   Files,
                                   removeFileConfirmation,
                                   FilesControllerCommon,
                                   getFilesColumnsForAdvancedFilter,
                                   PaginationPropertiesSettingService,
                                   FeatureProvider,
                                   UserLabsProvider) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["FILES", "FILES-CONTROLLER"]);
            $scope.page.title = "Files";
            $scope.page.showPageableFilter = true;
            $scope.page.filterScope = $scope;
            $scope.total = 0;
            $scope.pageNumber = 0;
            $scope.filter = $routeParams.filter;
            $scope.page.subtitle = $scope.$eval("filter | filterToString");
            $scope.page.changeableColumns = true;
            $scope.files = [];
            $scope.isBillingFeatureAvailable = FeatureProvider.isBillingFeatureAvailable;
            $scope.isUserLab = UserLabsProvider.isUserLab;

            var pagedRequest = new contentRequestParameters.getParameters("files");

            $scope.page.advancedFilter = {
                composedFilter: pagedRequest.advancedFilter ? angular.copy(pagedRequest.advancedFilter) : {},
                configuration: {
                    pageable: true,
                    fields: getFilesColumnsForAdvancedFilter()
                }
            };

            var filesController = FilesControllerCommon($scope);


            Files.get(pagedRequest, function (filesCollection) {
                filesController.setup(filesCollection);
                PaginationPropertiesSettingService.setPaginationProperties($scope, filesCollection);
            });

            $scope.displayConfirmation = removeFileConfirmation($scope);
        }
    )
    .controller("filesByInstrument", function ($scope,
                                               contentRequestParameters,
                                               $routeParams,
                                               Files,
                                               removeFileConfirmation,
                                               FilesControllerCommon,
                                               getFilesColumnsForAdvancedFilter,
                                               PaginationPropertiesSettingService,
                                               FeatureProvider,
                                               UserLabsProvider) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["FILES", "FILES-BY-INSTRUMENT-CONTROLLER"]);
            $scope.page.title = "Files";
            $scope.page.showPageableFilter = true;
            $scope.page.filterScope = $scope;
            $scope.page.changeableColumns = true;
            $scope.total = 0;
            $scope.pageNumber = 0;
            $scope.isBillingFeatureAvailable = FeatureProvider.isBillingFeatureAvailable;
            $scope.isUserLab = UserLabsProvider.isUserLab;

            var filesController = FilesControllerCommon($scope);

            var pagedRequest = contentRequestParameters.getParameters("files");
            $scope.page.advancedFilter = {
                composedFilter: pagedRequest.advancedFilter ? angular.copy(pagedRequest.advancedFilter) : {},
                configuration: {
                    pageable: true,
                    fields: getFilesColumnsForAdvancedFilter()
                }
            };

            pagedRequest.id = $routeParams.id;
            pagedRequest.filter = "my";
            pagedRequest.type = "instrument";
            Files.get(
                pagedRequest,
                function (filesCollection) {
                    filesController.setup(filesCollection);
                    PaginationPropertiesSettingService.setPaginationProperties($scope, filesCollection);
                }
            );

            $scope.displayConfirmation = removeFileConfirmation($scope);
        }
    )
    .controller("filesByLab", function ($scope,
                                        contentRequestParameters,
                                        $routeParams,
                                        removeFileConfirmation,
                                        FilesByLab,
                                        FilesControllerCommon,
                                        getFilesColumnsForAdvancedFilter,
                                        PaginationPropertiesSettingService,
                                        FeatureProvider,
                                        UserLabsProvider) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["FILES", "FILES-BY-LAB-CONTROLLER"]);
            $scope.page.title = "Files";
            $scope.page.showPageableFilter = true;
            $scope.page.filterScope = $scope;
            $scope.page.changeableColumns = true;
            $scope.total = 0;
            $scope.pageNumber = 0;
            $scope.isBillingFeatureAvailable = FeatureProvider.isBillingFeatureAvailable;
            $scope.isUserLab = UserLabsProvider.isUserLab;

            var filesController = FilesControllerCommon($scope);

            var pagedRequest = contentRequestParameters.getParameters("files");
            $scope.page.advancedFilter = {
                composedFilter: pagedRequest.advancedFilter ? angular.copy(pagedRequest.advancedFilter) : {},
                configuration: {
                    pageable: true,
                    fields: getFilesColumnsForAdvancedFilter()
                }
            };
            pagedRequest.id = $routeParams.id;
            FilesByLab.get(pagedRequest, function (filesCollection) {
                CommonLogger.log("query to files in lab");
                filesController.setup(filesCollection);
                PaginationPropertiesSettingService.setPaginationProperties($scope, filesCollection);
            });

            $scope.displayConfirmation = removeFileConfirmation($scope);
        }
    )
    .controller("experiment-files", function ($scope,
                                              $routeParams,
                                              $location,
                                              contentRequestParameters,
                                              Files,
                                              ExperimentDetails,
                                              FilesByExperiment,
                                              FilesControllerCommon,
                                              getFilesColumnsForAdvancedFilter,
                                              PaginationPropertiesSettingService,
                                              FeatureProvider,
                                              UserLabsProvider) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["FILES", "EXPERIMENT-FILES-CONTROLLER"]);
            $scope.total = 0;
            $scope.pageNumber = 0;
            $scope.page.showPageableFilter = true;
            $scope.page.filterScope = $scope;
            $scope.page.changeableColumns = true;
            $scope.isBillingFeatureAvailable = FeatureProvider.isBillingFeatureAvailable;
            $scope.isUserLab = UserLabsProvider.isUserLab;

            var filesController = FilesControllerCommon($scope);
            ExperimentDetails.get({id: $routeParams.experiment}, function (experiment) {
                $scope.page.title = $routeParams.experiment + ": " + experiment.details.info.name;
                $scope.page.res = {
                    type: "EXPERIMENT",
                    id: $routeParams.experiment,
                    path: $location.$$url.substring(
                        0,
                        $location.$$url.lastIndexOf("/" + $routeParams.experiment + "/files")
                    ),
                    returnUrl: $location.$$url,
                    accessLevel: experiment.details.accessLevel,
                    canEdit: $scope.loggedInUser.username == experiment.details.ownerEmail || experiment.details.labHead ==
                        $scope.loggedInUser.id
                };
            });

            var pagedRequest = contentRequestParameters.getParameters("files");
            $scope.page.advancedFilter = {
                composedFilter: pagedRequest.advancedFilter ? angular.copy(pagedRequest.advancedFilter) : {},
                configuration: {
                    pageable: true,
                    fields: getFilesColumnsForAdvancedFilter()
                }
            };
            pagedRequest.id = $routeParams.experiment;
            FilesByExperiment.get(pagedRequest, function (filesCollection) {
                filesController.setup(filesCollection);

                PaginationPropertiesSettingService.setPaginationProperties($scope, filesCollection);
            });
        }
    )
    .controller("fileDetails", function ($scope, $location, $routeParams, Files, FileDetails,
                                         FileDetailsWithConditions, Security) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["FILES", "FILE-DETAILS-CONTROLLER"]);
            $scope.showDetailsDialog = true;
            $scope.page.title = "File Details";
            $scope.viewMode = true;

            var throughExperiment = $routeParams.experiment != undefined;

            if (!throughExperiment) {
                FileDetails.get({filter: $routeParams.filter, id: $routeParams.id}, getFileSuccess);
            } else {
                FileDetailsWithConditions.get(
                    {experimentId: $routeParams.experiment, id: $routeParams.id},
                    getFileSuccess
                );
            }

            function getFileSuccess(file) {
                $scope.showConditions = throughExperiment;
                if (throughExperiment) {
                    file.conditions = $.map(file.conditions, function (item) {
                        return item.name;
                    });
                }
                $scope.details = file;

                Security.get({path: ""}, function (user) {
                    $scope.loggedInUser = user;
                    $scope.viewMode = $scope.loggedInUser.username != $scope.details.ownerEmail;
                });
            }


            $scope.save = function () {
                var file = {};
                file.labels = $scope.details.labels;
                file.fileId = $scope.details.id;
                Files.update(file, function () {
                    CommonLogger.log("File Saved");
                    setTimeout(function () {
                        $(".modal").modal("hide");
                    }, 0);
                });
            };
        }
    )
    .controller("fileColumnsEditor", function ($scope, FileColumns, columnsEditor) {
        columnsEditor($scope, FileColumns);
    })
    .directive("fileDetails", detailsLink({"title": "Show File Details", "dataTarget": "#fileDetails"}))
    .factory("removeFileConfirmation", function ($route, Files) {
        return function ($scope) {
            return function (file, $event) {
                $scope.deleteFilesPopupConfiguration.api.showPopup([file]);
                $event.stopPropagation();
            };
        };
    })
    .factory("getFilesColumnsForAdvancedFilter", function () {
        return function () {
            return [
                {prop: "id", title: "ID", type: "number"},
                {prop: "name", title: "Name", type: "string"},
                {prop: "instrument", title: "Instrument", type: "string"},
                {prop: "laboratory", title: "Laboratory", type: "string"},
                {prop: "uploadDate", title: "Upload Date", type: "date"},
                {prop: "labels", title: "Labels", type: "string"},
                {prop: "annotationInstrument", title: "Annotation Instrument", type: "string"},
                {prop: "sizeInBytes", title: "Size(in bytes)", type: "number"},
                {prop: "userName", title: "User Name", type: "string"},
                {prop: "userLabels", title: "User Labels", type: "string"},
                {prop: "fileCondition", title: "File Condition", type: "string"},
                {prop: "instrumentMethod", title: "Instrument Method", type: "string"},
                //{prop: "endRt", title:"End Time", type: "string"},
                //{prop: "startRt", title:"Start Time", type: "string"},
                {prop: "creationDate", title: "Creation Date", type: "date"},
                {prop: "comment", title: "Comment", type: "string"},
                //{prop: "startMz", title:"Start mz", type: "string"},
                //{prop: "endMz", title:"End mz", type: "string"},
                {prop: "fileName", title: "File Name", type: "string"},
                {prop: "seqRowPosition", title: "Position", type: "string"},
                {prop: "sampleName", title: "Sample Name", type: "string"},
                {prop: "instrumentSerialNumber", title: "Instrument Serial", type: "string"},
                {prop: "phone", title: "Phone", type: "string"},
                {prop: "instrumentName", title: "Instrument Name", type: "string"}
            ];
        };
    })
    .factory("filesExpandMenu", function (FileDetails, FileDetailsWithConditions, $routeParams) {
        return initExpandMenu(function openInlineFashion(file) {
            var throughExperiment = $routeParams.experiment != undefined;
            if (!throughExperiment) {
                FileDetails.get({id: file.id}, getFileSuccess(file));
            } else {
                FileDetailsWithConditions.get({
                    experimentId: $routeParams.experiment,
                    id: file.id
                }, getFileSuccess(file));
            }

            function getFileSuccess(file) {
                return function (response) {
                    file.showConditions = throughExperiment;
                    if (throughExperiment) {
                        response.conditions = $.map(response.conditions, function (item) {
                            return item.name;
                        });
                    }
                    file.details = response;
                };
            }
        });
    })
    .factory("FilesControllerCommon", function ($rootScope,
                                                $route,
                                                $location,
                                                $routeParams,
                                                $timeout,
                                                $window,
                                                Instruments,
                                                FileDownloads,
                                                DashboardButtonFactory,
                                                DashboardButton,
                                                animatedScroll,
                                                downloadFiles,
                                                $filter,
                                                applyPaging,
                                                Files,
                                                experimentByFiles,
                                                Laboratories,
                                                OperatedInstruments,
                                                ExperimentSpecies,
                                                $q,
                                                filesExpandMenu,
                                                changeableColumnsHelper,
                                                Security,
                                                FileColumns,
                                                InstrumentStudyType,
                                                LabFeatures,
                                                BillingFeatures,
                                                ExperimentRestriction,
                                                FeatureProvider,
                                                UserLabsProvider,
                                                UserDetailsProvider) {
            return function ($scope) {
                filesExpandMenu($scope);
                changeableColumnsHelper($scope, FileColumns);

                $scope.deleteFilesPopupConfiguration = {};
                $scope.haveNGStudyType = haveNGStudyType;

                return {
                    setup: function (filesCollection) {

                        $scope.page.onFilterEnter = function () {
                            applyPaging($scope);
                        };

                        $scope.labs = Laboratories.query();

                        var validators = {};

                        validators.isFileUploadedToUsersLab = function (file, labItem) {
                            return file.labId == labItem.id;
                        };

                        validators.isUserOperatorOfAllFiles = function (files) {
                            var userId = UserDetailsProvider.getUserId();
                            for (var i = 0; i < files.length; i++) {
                                var isOperator = $.inArray(userId, files[i].operators) > -1;
                                if (isOperator == false) {
                                    return false;
                                }
                            }
                            return true;
                        };

                        validators.isAvailableForArchive = function (file) {
                            var userId = UserDetailsProvider.getUserId();
                            var labId = file.labId;
                            return UserLabsProvider.isUserLab(labId)
                                && isFileUploaded(file)
                                && !file.corrupted
                                && file.storageStatus == "UNARCHIVED"
                                && (file.labHead == userId || file.owner == userId);
                        };

                        validators.isAvailableForUnarchive = function (file) {
                            var userId = UserDetailsProvider.getUserId();
                            var labId = file.labId;
                            return UserLabsProvider.isUserLab(labId)
                                && isFileUploaded(file)
                                && file.storageStatus != "UNARCHIVED"
                                && (file.labHead == userId || file.owner == userId)
                                && FeatureProvider.isBillingFeatureAvailable(BillingFeatures.ANALYSE_STORAGE, labId);
                        };

                        validators.archiveFeatureIsAvailable = function (file) {
                            return UserLabsProvider.isUserLab(file.labId)
                                && FeatureProvider.isBillingFeatureAvailable(
                                    BillingFeatures.ARCHIVE_STORAGE,
                                    file.labId
                                );
                        };

                        validators.canEdit = function (file) {
                            if (file.corrupted) {
                                return false;
                            }
                            var userId = UserDetailsProvider.getUserId();
                            var isOperator = $.inArray(userId, file.operators) > -1;
                            return file.labHead == userId
                                || file.owner == userId
                                || isOperator;
                        };

                        validators.isFileInUserLab = function (file) {
                            return $.grep($scope.labs, function (item) {
                                return item.id == file.labId;
                            }).length > 0;
                        };

                        validators.isFilePublic = function (file) {
                            return file.accessLevel == "PUBLIC";
                        };

                        function isFileUploaded(file) {
                            return Boolean(file.archiveId) || Boolean(file.contentId);
                        }

                        validators.canDownload = function (file) {
                            return isFileUploaded(file)
                                && !file.corrupted
                                && (file.accessLevel == "PUBLIC" ||
                                    FeatureProvider.isBillingFeatureAvailable(BillingFeatures.DOWNLOAD));
                        };

                        validators.canArchiveFile = function (file) {
                            return validators.archiveFeatureIsAvailable(file)
                                && validators.isAvailableForArchive(file);
                        };

                        validators.haveCompounds = function (file) {
                            return !file.corrupted && file.fileCompounds && file.fileCompounds.length > 0;
                        };

                        validators.fileCannotBeSelected = function (file) {
                            if (file.corrupted) {
                                return false;
                            }
                            return !file.contentId && !file.archiveId;
                        };

                        validators.displayDetailsButton = function (file) {
                            if (!file) {
                                return false;
                            }
                            if (file.corrupted) {
                                return file.accessLevel === "PRIVATE";
                            }
                            return Boolean(file.contentId) || Boolean(file.archiveId);
                        };

                        validators.canDeleteFile = function (file) {
                            if (file.accessLevel !== "PRIVATE") {
                                return false;
                            }
                            return file.corrupted || !file.usedInExperiments;
                        };

                        var popups = {};

                        popups.showArchiveFilePopup = function (file) {
                            popups.archiveFilePopup = new Confirmation("#archive-file-confirmation", file, {
                                success: function (item) {
                                    onArchiveFiles([item]);
                                }
                            });
                            popups.archiveFilePopup.fileName = file.name;
                            popups.archiveFilePopup.showPopup();
                        };

                        popups.showUnarchiveFilePopup = function (file) {
                            popups.unarchiveFilePopup = new Confirmation("#unarchive-file-confirmation", file, {
                                success: function (item) {
                                    onUnarchiveFiles([item]);
                                }
                            });
                            popups.unarchiveFilePopup.fileName = file.name;
                            popups.unarchiveFilePopup.showPopup();
                        };

                        popups.showEditPopup = function (file) {
                            popups.editFilePopup = new Confirmation("#edit-file-popup", file, {
                                success: function (item) {
                                    editFiles([item], popups.editFilePopup);
                                }
                            });

                            var currentUser = UserDetailsProvider.getUserId();
                            var isOwner = currentUser == file.owner;
                            var isLabHead = currentUser == file.labHead;
                            var isOperator = $.inArray(currentUser, file.operators) > -1;

                            var canEditSpecies = (isOwner || isOperator || isLabHead) && !file.usedInExperiments;
                            var defaultModel = {
                                species: species,
                                appendLabels: "true",
                                newSpecie: file.specieId,
                                disableEditSpecies: !canEditSpecies,
                                canEditSpecies: canEditSpecies,
                                canEditLabels: isLabHead || isOwner || isOperator
                            };

                            angular.extend(popups.editFilePopup, defaultModel);

                            popups.editFilePopup.fileName = file.name;
                            popups.editFilePopup.showPopup();

                        };

                        popups.showDownloadSharedFilesPopup = function (files) {
                            $scope.downloadSharedFilesPopup = new Confirmation("#download-shared-files-popup", files, {
                                success: function (files) {
                                    downloadFiles(files, undefined, $scope.downloadSharedFilesPopup.selectedLab)
                                        .download();
                                }
                            });
                            $scope.downloadSharedFilesPopup.selectedLab = null;
                            $scope.downloadSharedFilesPopup.showPopup();
                        };

                        function reloadWithTimeoutFn(wait) {
                            return function () {
                                setTimeout(function () {
                                    $route.reload();
                                }, wait);
                            };
                        }

                        $scope.validators = validators;
                        $scope.popups = popups;

                        var availableInstruments = [];
                        var isTableEmpty = false;

                        OperatedInstruments.query(function (data) {
                            availableInstruments = $.map(data, function (item) {
                                return item.id;
                            });
                            $scope.files = $.map(filesCollection.items || filesCollection, function (item) {
                                item.selected = false;
                                //Hot fix: If there are files in experimentByFiles, then select it
                                $.each(experimentByFiles.getFiles(), function () {
                                    if (this.id == item.id) {
                                        item.selected = this.selected;
                                    }
                                });
                                CommonLogger.log(item);
                                return item;
                            });
                            //and then clean files in experimentByFiles
                            experimentByFiles.clear();

                            console.groupEnd();
                            isTableEmpty = $scope.files.length == 0;
                        });
                        $scope.bulkLabelEditDialog = new OperationDialog("#bulk-update-labels-dialog", {});
                        $scope.bulkLabelEditDialog.model.appendLabels = "true";


                        $scope.download = function (file) {

                            if (validators.isFileInUserLab(file) || validators.isFilePublic(file)) {
                                downloadFiles([file]).download();
                            } else if ($scope.labs.length == 1) {
                                downloadFiles([file], undefined, $scope.labs[0].id).download();
                            } else {
                                $scope.popups.showDownloadSharedFilesPopup([file]);
                            }

                        };

                        $scope.requestDownload = function (file) {
                            showFileDownloadConfirm(
                                "Preparing job for downloading file was started. You will receive notification," +
                                " when all files will be ready.",
                                function () {
                                    FileDownloads.moveToStorage({files: [file.id]});
                                }
                            );
                        };
                        $scope.getDownloadTitle = function (status) {
                            if (status === "UNARCHIVED") {
                                return "File is ready to download";
                            } else if (status === "UN_ARCHIVING_IN_PROCESS") {
                                return "File unArchiving request is already in progress";
                            } else if (status === "UN_ARCHIVING_FOR_DOWNLOAD_IN_PROCESS") {
                                return "File download request is already in progress";
                            }
                            return "Request the file download";

                        };

                        $scope.getDownloadIcon = function (status) {
                            if (status === "UNARCHIVED") {
                                return "quickDownload";
                            } else if (status === "UN_ARCHIVING_IN_PROCESS") {
                                return "download-in-time";
                            } else if (status === "UN_ARCHIVING_FOR_DOWNLOAD_IN_PROCESS") {
                                return "download-in-progress";
                            }
                            return "slowDownload";

                        };

                        $scope.downloadCompounds = function (file) {

                            $.fileDownload("/files/compoundsDownload/" + file.id, {
                                failCallback: function (response, url) {
                                }
                            });
                        };

                        var species = ExperimentSpecies.query();

                        var switchDownloadButton = function (selectedItems, featuresByLab) {
                            var button = new DashboardButton(0, "download", "Download selected files", "download");
                            var enabledDownloadLabsFeatures = $.grep(featuresByLab, function (enabledInLab) {
                                return $.inArray(BillingFeatures.DOWNLOAD, enabledInLab.features) >= 0;
                            });
                            var enabledLabsWithDownloadFeature = $.map(enabledDownloadLabsFeatures, function (item) {
                                return item.lab;
                            });
                            var allowedToDownload = $.grep(selectedItems, function (item) {
                                return $.inArray(item.labId, enabledLabsWithDownloadFeature) > -1 && !item.corrupted;
                            });
                            button.display = allowedToDownload.length > 0 && enabledDownloadLabsFeatures.length > 0;
                            button.disabledHandler = function () {
                                const hasCorruptedFiles = selectedItems.some(function (file) {
                                    return file.corrupted;
                                });
                                return hasCorruptedFiles || enabledDownloadLabsFeatures.length !== featuresByLab.length;
                            };

                            function download() {
                                CommonLogger.log("Download Selected button clicked.");
                                if ($.grep(allowedToDownload, function (item) {
                                    return item.storageStatus !== "UNARCHIVED";
                                }).length > 0) {
                                    showFileDownloadConfirm(
                                        "Preparing job for downloading files was started. You will receive notification," +
                                        " when all files will be ready.",
                                        function () {
                                            FileDownloads.moveToStorage({
                                                files: $.map(allowedToDownload, function (file) {
                                                    return file.id;
                                                })
                                            });
                                        }
                                    );
                                } else if (FeatureProvider.isFeatureAvailable(LabFeatures.BILLING)) {

                                    var canDownloadAllFilesThroughUserLabs = $.grep(
                                        allowedToDownload,
                                        function (file) {
                                            return validators.isFileInUserLab(file) || validators.isFilePublic(file);
                                        }
                                    ).length === allowedToDownload.length;

                                    if (canDownloadAllFilesThroughUserLabs) {
                                        downloadFiles(allowedToDownload).download();
                                    } else if ($scope.labs.length === 1) {
                                        downloadFiles(allowedToDownload, undefined, $scope.labs[0].id).download();
                                    } else {
                                        $scope.popups.showDownloadSharedFilesPopup(allowedToDownload);
                                    }

                                } else {

                                    downloadFiles(allowedToDownload).download();

                                }
                            }

                            button.onClickHandler = function () {
                                download();
                            };

                            button.disabledPopupOptions = {
                                title: "Can Not Download All Selected Files",
                                type: "dialog",
                                success: {
                                    handler: function () {
                                        download();
                                    },
                                    caption: "Download"
                                },
                                bodyMessageUrl: "../pages/component/operations/unsupported-file-download.html"
                            };

                            var totalSize = 0;
                            $(selectedItems).each(function () {
                                totalSize = totalSize + this.columns.sizeInBytes;
                            });

                            DashboardButtonFactory.count(selectedItems.length);
                            DashboardButtonFactory.origin("files");
                            DashboardButtonFactory.filesTotalSize(totalSize);
                            DashboardButtonFactory.put(button);
                        };

                        var switchRemovesButton = function (selectedItems) {
                            var button = new DashboardButton(
                                91,
                                "remove-files",
                                "Remove selected files",
                                "delete-file"
                            );
                            button.display = selectedItems.length > 0;

                            button.onClickHandler = function () {
                                $scope.deleteFilesPopupConfiguration.api.showPopup(selectedItems);
                            };

                            DashboardButtonFactory.put(button);
                        };

                        function editFiles(selectedItems, model) {
                            var query = {
                                fileIds: []
                            };
                            angular.forEach(selectedItems, function (item) {
                                query.fileIds.push(item.id);
                            }, query);
                            var deferred = $q.defer();
                            deferred.resolve();
                            var promise = deferred.promise;
                            if (model.editSpecies) {
                                promise = promise.then(function () {
                                    var deferred = $q.defer();
                                    Files.bulkUpdateSpecies($.extend({newValue: model.newSpecie}, query), function () {
                                        deferred.resolve();
                                    });
                                    return deferred.promise;
                                });
                            }
                            if (model.editLabels) {
                                promise = promise.then(function () {
                                    var deferred = $q.defer();
                                    Files.bulkUpdateLabels($.extend({
                                            newValue: model.newLabels,
                                            appendLabels: model.appendLabels
                                        }, query)
                                        , function () {
                                            deferred.resolve();
                                        });
                                    return deferred.promise;
                                });
                            }
                            promise.then($route.reload);
                        }

                        var switchEditButton = function (selectedItems) {
                            var button = new DashboardButton(3, "edit-labels", "Edit selected files", "edit-labels");

                            function isFileAllowedForEdit(file) {
                                var userId = UserDetailsProvider.getUserId();
                                var isOperator = $.inArray(userId, file.operators) > -1;
                                return (isOperator || userId === file.labHead || userId === file.owner)
                                    && !file.corrupted;
                            }

                            var allowedFilesForEditSpecies = $.grep(selectedItems, function (file) {
                                return isFileAllowedForEdit(file) && !file.usedInExperiments;
                            });
                            var allowedFilesForEditLabels = $.grep(selectedItems, isFileAllowedForEdit);

                            var defaultModel = {
                                species: species,
                                appendLabels: "true",
                                disableEditSpecies: allowedFilesForEditSpecies.length == 0,
                                canEditAllSpecies: allowedFilesForEditSpecies.length == selectedItems.length,
                                canEditAllLabels: allowedFilesForEditLabels.length == selectedItems.length
                            };
                            button.showPopup = true;
                            var dialogTitle = selectedItems.length > 1 ? "Edit Files" : "Edit File";
                            var popupOptions = {
                                model: defaultModel,
                                title: dialogTitle,
                                type: "dialog",
                                bodyMessageUrl: "../pages/component/operations/edit-files.html",
                                success: {
                                    caption: "Save",
                                    handler: function (model) {
                                        const itemsToEdit = selectedItems.filter(item => !item.corrupted);
                                        editFiles(itemsToEdit, model);
                                    }
                                },
                                cancel: {
                                    handler: function () {
                                        switchEditButton(selectedItems);
                                    }
                                }
                            };
                            button.popupOptions = popupOptions;
                            button.disabledPopupOptions = popupOptions;
                            button.display = allowedFilesForEditLabels.length + allowedFilesForEditSpecies.length > 0;

                            button.disabledHandler = function () {
                                return allowedFilesForEditLabels.length != selectedItems.length
                                    || allowedFilesForEditSpecies.length != selectedItems.length;
                            };

                            DashboardButtonFactory.put(button);
                        };

                        function runExperiment(selectedFiles, haveSameInstrument) {
                            experimentByFiles.setFiles(selectedFiles);
                            var sampleFile = experimentByFiles.getFiles()[0];
                            experimentByFiles.setSpecieId(sampleFile.specieId);
                            var instrumentId = sampleFile.instrumentId;
                            if (haveSameInstrument) {
                                experimentByFiles.setInstrumentId(instrumentId);
                            }

                            ExperimentRestriction.get({instrumentId: instrumentId}, function (restriction) {
                                experimentByFiles.setLabId(sampleFile.labId);
                                experimentByFiles.setModelId(restriction.instrumentModel);
                                experimentByFiles.setVendorId(restriction.vendor);
                                experimentByFiles.setTechnologyTypeId(restriction.technologyType);
                                experimentByFiles.setInstrumentTypeId(restriction.instrumentType);
                                $location.path("/experiments/my/new");
                            });
                        }

                        var switchExperimentButton = function (selectedFiles) {
                            var button = new DashboardButton(
                                4,
                                "E",
                                "Run new experiment with selected files",
                                "add-to-experiment"
                            );
                            const allowedToUseInExperiment = selectedFiles.filter(file => !file.corrupted);
                            button.display = allowedToUseInExperiment.length > 0;
                            var haveSameInstrument = true;
                            button.disabledHandler = function () {
                                var allFilesHaveSimilarInstrumentModelAndSpecie = true;
                                if (selectedFiles.length > 0) {
                                    var commonInstrumentModelId = selectedFiles[0].modelId;
                                    var commonSpecieId = selectedFiles[0].specieId;
                                    var commonInstrumentId = selectedFiles[0].instrumentId;
                                    let hasCorruptedFiles = false;
                                    angular.forEach(selectedFiles, function (file) {
                                        if (file.modelId != commonInstrumentModelId || file.specieId != commonSpecieId) {
                                            allFilesHaveSimilarInstrumentModelAndSpecie = false;
                                        }
                                        if (file.instrumentId != commonInstrumentId) {
                                            haveSameInstrument = false;
                                        }
                                        if (file.corrupted) {
                                            hasCorruptedFiles = true;
                                        }
                                    });
                                    return !allFilesHaveSimilarInstrumentModelAndSpecie || hasCorruptedFiles;
                                }
                                return allFilesHaveSimilarInstrumentModelAndSpecie;

                            };
                            button.disabledPopupOptions = {
                                title: "Create Experiment Operation Is Not Available",
                                bodyMessageUrl: "../pages/component/operations/unsupported-run-experiment.html"
                            };

                            button.onClickHandler = function () {
                                runExperiment(selectedFiles, haveSameInstrument);
                            };
                            DashboardButtonFactory.put(button);
                        };

                        function onArchiveFiles(allowedToArchiving) {
                            Files.archive({
                                files: $.map(allowedToArchiving, function (item) {
                                    return item.id;
                                })
                            }, function () {
                                $route.reload();
                            });
                        }

                        var switchArchiveButton = function (selectedItems, enabledFeaturesByLab) {
                            var button = new DashboardButton(
                                7,
                                "Archive",
                                "Archive Selected Files",
                                "archive-selected"
                            );
                            var userId = UserDetailsProvider.getUserId();
                            var allowedToArchiving = $.grep(selectedItems, function (item) {
                                return item.storageStatus === "UNARCHIVED"
                                    && !item.corrupted
                                    && (item.owner == userId || item.labHead == userId)
                                    && validators.canArchiveFile(item);
                            });
                            var enabledAnalysableStorageLabFeatures = $.grep(
                                enabledFeaturesByLab,
                                function (enabledInLab) {
                                    return $.inArray(BillingFeatures.ANALYSE_STORAGE, enabledInLab.features) >= 0;
                                }
                            );
                            var allFilesIsAllowedToArchiving = allowedToArchiving.length == selectedItems.length;
                            var allFilesLabsProvideAnalysableFeature = enabledAnalysableStorageLabFeatures.length ==
                                enabledFeaturesByLab.length;

                            button.display =
                                allowedToArchiving.length > 0 && enabledAnalysableStorageLabFeatures.length > 0;
                            button.disabledHandler = function () {
                                return !allFilesIsAllowedToArchiving || !allFilesLabsProvideAnalysableFeature;
                            };
                            button.showPopup = true;
                            var isAllAvailableToArchive = allFilesIsAllowedToArchiving &&
                                allFilesLabsProvideAnalysableFeature;
                            var popupOptions = {
                                model: {allAvailableToArchive: isAllAvailableToArchive},
                                title: "Archive Files",
                                type: "dialog",
                                bodyMessageUrl: "../pages/component/operations/archive-files.html",
                                success: {
                                    caption: isAllAvailableToArchive && "Archive" || "Archive Allowed",
                                    handler: function () {
                                        onArchiveFiles(allowedToArchiving);
                                    }
                                }
                            };
                            button.popupOptions = popupOptions;
                            button.disabledPopupOptions = popupOptions;

                            if (button.display && $scope.haveNGStudyType(selectedItems)) {
                                button.display = false;
                            }

                            DashboardButtonFactory.put(button);
                        };

                        function onUnarchiveFiles(archivedFiles) {
                            Files.unarchive({
                                files: $.map(archivedFiles, function (item) {
                                    return item.id;
                                })
                            }, function () {
                                $route.reload();
                            });
                        }

                        var switchUnarchiveButton = function (selectedItems, enabledFeaturesByLab) {
                            var button = new DashboardButton(
                                8,
                                "Unarchive",
                                "Unarchive Selected Files",
                                "unarchive-selected"
                            );

                            var userId = UserDetailsProvider.getUserId();
                            var archivedFiles = $.grep(selectedItems, function (item) {
                                return item.storageStatus !== "UNARCHIVED"
                                    && (item.owner == userId || item.labHead == userId);
                            });

                            var enabledAnalysableStorageLabFeatures = $.grep(
                                enabledFeaturesByLab,
                                function (enabledInLab) {
                                    return $.inArray(BillingFeatures.ANALYSE_STORAGE, enabledInLab.features) >= 0;
                                }
                            );
                            var allFilesLabsProvideAnalysableFeature = enabledAnalysableStorageLabFeatures.length ==
                                enabledFeaturesByLab.length;
                            var allFilesIsArchived = archivedFiles.length == selectedItems.length;

                            button.display = archivedFiles.length > 0 && enabledAnalysableStorageLabFeatures.length > 0;
                            button.disabledHandler = function () {
                                return !allFilesIsArchived || !allFilesLabsProvideAnalysableFeature;
                            };
                            button.showPopup = true;
                            var allAvailableToUnarchive = allFilesIsArchived && allFilesLabsProvideAnalysableFeature;
                            var popupOptions = {
                                model: {allAvailableToUnarchive: allAvailableToUnarchive},
                                title: "Unarchive Files",
                                type: "dialog",
                                bodyMessageUrl: "../pages/component/operations/unarchive-files.html",
                                success: {
                                    caption: allAvailableToUnarchive && "Unarchive" || "Unarchive Allowed",
                                    handler: function () {
                                        onUnarchiveFiles(archivedFiles);
                                    }
                                }
                            };
                            button.popupOptions = popupOptions;
                            button.disabledPopupOptions = popupOptions;
                            DashboardButtonFactory.put(button);
                        };


                        function openChartWindow(currentUrl) {
                            if (currentUrl != null) {
                                window.open(
                                    currentUrl,
                                    "_blank" // <- This is what makes it open in a new window.
                                );
                                window.focus();
                            }
                        }

                        $scope.openChartWindow = openChartWindow;

                        $scope.isTableEmpty = function () {
                            return isTableEmpty;
                        };

                        $scope.getEmptyTableMessage = function () {
                            return "There are no items";
                        };

                        $scope.$watch(function ($scope) {
                            //watch the number of "selected" items
                            var selected = $.grep($scope.files || [], function (item) {
                                return item.selected;
                            });
                            return selected.length;
                        }, function () {
                            CommonLogger.log("The selected file list has been changed.");
                            //examine those of the items which are selected and update the download button accordingly
                            var selectedItems = $.grep($scope.files || [], function (item) {
                                return item.selected;
                            });

                            var withFeaturesByLabs = function withFeaturesByLabs(selectedItems, callback) {
                                var retrieveFileLabs = function (selectedItems) {
                                    var labs = [];
                                    $.each(selectedItems, function (i, item) {
                                        if ($.inArray(item.labId, labs) < 0) {
                                            labs.push(item.labId);
                                        }
                                    });
                                    return labs;
                                };
                                var userLabs = retrieveFileLabs(selectedItems);
                                var labFeaturesLoaded = false;
                                var billingLabFeaturesLoaded = false;
                                var labFeatures = [];
                                var billingLabFeatures = [];

                                Security.enabledBillingFeatures({labIds: userLabs}, function (features) {
                                    billingLabFeatures = $.map(userLabs, function (item) {
                                        return {
                                            lab: item,
                                            features: features[item]
                                        };
                                    });
                                    billingLabFeaturesLoaded = true;
                                    tryToResolve();
                                });
                                Security.enabledFeatures({labIds: userLabs}, function (features) {
                                    labFeatures = $.map(userLabs, function (item) {
                                        return {
                                            lab: item,
                                            features: features[item]
                                        };
                                    });
                                    labFeaturesLoaded = true;
                                    tryToResolve();
                                });

                                function tryToResolve() {
                                    if (labFeaturesLoaded && billingLabFeaturesLoaded) {
                                        callback(selectedItems, billingLabFeatures, labFeatures);
                                    }
                                }

                            };

                            switchRemovesButton(selectedItems);
                            switchEditButton(selectedItems);
                            switchExperimentButton(selectedItems);
                            withFeaturesByLabs(selectedItems, function (items, billingFeatures, labFeatures) {
                                switchDownloadButton(items, billingFeatures);
                                switchArchiveButton(items, billingFeatures);
                                switchUnarchiveButton(items, billingFeatures);
                            });

                            $scope.allItemsSelected =
                                $scope.files && $scope.files.length > 0 && selectedItems.length == $scope.files.length;
                        });

                        $scope.selectItem = function (file, event) {
                            if (file.contentId == null && file.archiveId == null) {
                                return;
                            }
                            file.selected = !file.selected;
                        };

                        $scope.selectAll = function (files) {
                            Selections.selectAll($.grep(files, function (file) {
                                if (file.corrupted) {
                                    return true;
                                }
                                return (file.contentId != null || file.archiveId != null) && !file.invalid;
                            }));
                        };

                        $scope.sort = function (files, field, reverse) {
                            return $filter("orderBy")(files, field, reverse);
                        };

                        $scope.allItemsSelected = false;

                    }
                };

                function haveNGStudyType(selectedItems) {
                    return selectedItems.some(function (item) {
                        return item.instrumentStudyType == InstrumentStudyType.NGS;
                    });
                }
            };
        }
    )
    .directive("cellColumn", function () {
        return {
            restrict: "E",
            replace: true,
            scope: {
                type: "=", value: "=", cellClass: "=", show: "=", cellStyle: "=", cellTitle: "="
            },
            templateUrl: "../pages/component/cell-column.html"
        };
    });


var FilesSelectedPopup = function (selector, okCallback) {
    this.popupSelector = selector;
    this.okCallback = okCallback;
};

FilesSelectedPopup.prototype.showPopup = function () {
    $(this.popupSelector).modal("show");
};
FilesSelectedPopup.prototype.hidePopup = function () {
    $(this.popupSelector).modal("hide");
};

FilesSelectedPopup.prototype.ok = function () {
    this.okCallback();
    this.hidePopup();
};

FilesSelectedPopup.prototype.cancel = function () {
    this.hidePopup();
};

function showFileDownloadConfirm(message, onOk) {
    $("#fileDownloadConfirm").html(message).dialog({
        title: "Files Download",
        draggable: false,
        dialogClass: "message-dialog warning",
        modal: true,
        resizable: false,
        width: 450,
        buttons: {
            "OK": function () {
                $(this).dialog("close");
                if (onOk) {
                    onOk();
                }
            }
        }
    });
}
