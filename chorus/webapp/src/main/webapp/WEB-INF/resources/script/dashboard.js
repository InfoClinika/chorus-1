"use strict";

(function () {

    angular
        .module(
            "dashboard",
            ["sessionTimeoutHandler", "background-upload", "projects-front", "experiments-back", "instruments-front", "instruments-back",
                "formatters", "users", "security-front", "experiments-front", "ui.directives", "laboratoriesControllers",
                "groups-front", "filesControllers", "front-end", "anonymous-download", "news-front", "news-back",
                "search-front", "search-back", "upload-front", "breadcrumbs", "validators", "user-profile-front", "lab-head-front",
                "general-requests-front", "trash-front", "error-catcher", "admin-front",
                "advert-front", "advert-back", "file-access-log", "scripts", "preferences-front", "client-token-front", "billing-back",
                "template-components", "util", "instrument-models", "enums", "header", "footer",
                "s3UploadModule", "ngsExperiment", "report-issue", "aggregate-statistics-front",
                "feature-service", "user-details-service"]
        )
        .config(function ($routeProvider, LabFeatures) {

            var redirectIfNotAdmin = getRedirectIfNotAdmin();
            var redirectIfNotLabMember = getRedirectIfNotLabMember();

            var DEFAULT_PAGE_PATH = "/projects/all";
            var projectDetails = {controller: "project-details", templateUrl: "../pages/projects/details.html"};
            var projectCopy = {controller: "project-copy", templateUrl: "../pages/projects/copy.html"};
            var experimentCopy = {controller: "copyExperiment", templateUrl: "../pages/experiment/wizard.html"};
            var experimentDetails = {controller: "experiment-details", templateUrl: "../pages/experiment/wizard.html"};
            var experimentFiles = {controller: "experiment-files", templateUrl: "../pages/files/list.html"};
            var fileDetails = {controller: "fileDetails", templateUrl: "../pages/files/details.html"};
            var projectExperiments = {controller: "project-experiments", templateUrl: "../pages/experiment/list.html"};
            var instrumentDetails = {controller: "instrument-details", templateUrl: "../pages/instrument/details.html"};
            var instrumentModelDetails = {
                controller: "instrument-model-details",
                templateUrl: "../pages/instrument-models/details.html"
            };
            var project = {controller: "projects", templateUrl: "../pages/projects/list.html"};

            $routeProvider.when("/projects/new", {controller: "newProject", templateUrl: "../pages/projects/new.html"})
                .when("/projects/:filter", project)
                .when("/projects/:filter/:project", projectDetails)
                .when("/projects/:filter/:project/experiments", projectExperiments)
                .when("/projects/:filter/:project/experiments/:experiment", experimentDetails)
                .when("/projects/:filter/:project/experiments/:experiment/files", experimentFiles)
                .when("/projects/:filter/:project/experiments/:experiment/files/:id", fileDetails)
                .when("/projects/:filter/:project/copy", projectCopy)
                .when(
                    "/projects/:filter/:project/experiments/:experiment/copy",
                    {controller: "copyExperiment", templateUrl: "../pages/experiment/wizard.html"}
                )
                .when(
                    "/experiments/my/new",
                    {controller: "newExperiment", templateUrl: "../pages/experiment/wizard.html"}
                )
                .when(
                    "/experiments/my/ngs/new",
                    {controller: "import-ngs-experiment", templateUrl: "../pages/experiment/import-ngs-experiment.html"}
                )
                .when("/experiments/:filter", {controller: "experiments", templateUrl: "../pages/experiment/list.html"})
                .when("/experiments/:filter/:experiment", experimentDetails)
                .when("/experiments/:filter/:experiment/copy", experimentCopy)
                .when("/experiments/:filter/:experiment/files", experimentFiles)
                .when("/experiments/:filter/:experiment/files/:id", fileDetails)
                .when(
                    "/instruments/new",
                    {
                        controller: "new-instrument",
                        templateUrl: "../pages/instrument/new.html",
                        resolve: redirectIfNotLabMember
                    }
                )
                .when(
                    "/instrument-models",
                    {
                        controller: "instrument-models-table",
                        templateUrl: "../pages/administration-tools/instrument-models/list.html",
                        reloadOnSearch: false
                    }
                )
                .when(
                    "/instrument-models/new",
                    {
                        controller: "instrument-model-create-edit",
                        templateUrl: "../pages/administration-tools/instrument-models/create-edit.html"
                    }
                )
                .when(
                    "/instrument-models/:id",
                    {
                        controller: "instrument-model-create-edit",
                        templateUrl: "../pages/administration-tools/instrument-models/create-edit.html"
                    }
                )
                .when(
                    "/lab/:id/instruments",
                    {controller: "instrumentsByLab", templateUrl: "../pages/instruments.html"}
                )
                .when("/lab/:labId/instruments/:id", instrumentDetails)
                .when("/groups/new", {controller: "new-group", templateUrl: "../pages/groups/new.html"})
                .when("/groups", {controller: "groups", templateUrl: "../pages/groups/list.html"})
                .when("/groups/:id", {controller: "groupDetails", templateUrl: "../pages/groups/details.html"})
                .when(
                    "/upload/new",
                    {controller: "new-upload", templateUrl: "../pages/upload/new.html", resolve: redirectIfNotLabMember}
                )
                .when(
                    "/upload/resume",
                    {
                        controller: "resume-upload",
                        templateUrl: "../pages/upload/resume.html",
                        resolve: redirectIfNotLabMember
                    }
                )
                .when("/upload/s3", {
                    controller: "unifiedUploadController",
                    templateUrl: "../script/s3-upload/controller/unified-upload.template.html",
                    resolve: redirectIfNotLabMember
                })
                .when("/upload/:id/progress", {
                    controller: "uploadTransferProcessDialogController",
                    templateUrl: "../script/s3-upload/controller/upload-transfer-process-dialog.template.html",
                    resolve: redirectIfNotLabMember
                })
                .when("/laboratories/new", {controller: "newLaboratory", templateUrl: "../pages/laboratories/new.html"})
                .when(
                    "/laboratories/:filter",
                    {controller: "laboratories", templateUrl: "../pages/laboratories/list.html"}
                )
                .when(
                    "/laboratories/:id/users",
                    {controller: "laboratoryUsers", templateUrl: "../pages/laboratories/user-list.html"}
                )
                .when(
                    "/laboratories/:filter/:id",
                    {controller: "laboratoryDetails", templateUrl: "../pages/laboratories/details.html"}
                )
                .when("/files/:filter/:id", fileDetails)
                .when("/files/:filter", {controller: "files", templateUrl: "../pages/files/list.html"})
                .when(
                    "/files/:filter/instrument/:id",
                    {controller: "filesByInstrument", templateUrl: "../pages/files/list.html"}
                )
                .when("/files/:filter/instrument/:instrumnentId/:id", fileDetails)
                .when(
                    "/change-columns/files",
                    {controller: "fileColumnsEditor", templateUrl: "../pages/files/column-editor.html"}
                )
                .when(
                    "/change-columns/projects",
                    {controller: "projectColumnsEditor", templateUrl: "../pages/projects/column-editor.html"}
                )
                .when(
                    "/change-columns/experiments",
                    {controller: "experimentColumnsEditor", templateUrl: "../pages/experiment/column-editor.html"}
                )
                .when("/lab/:id/files", {controller: "filesByLab", templateUrl: "../pages/files/list.html"})
                .when("/lab/:labId/files/:id", fileDetails)
                .when(
                    "/lab/:labId/experiments",
                    {controller: "experiments", templateUrl: "../pages/experiment/list.html"}
                )
                .when("/lab/:labId/experiments/:experiment", experimentDetails)
                .when("/lab/:labId/experiments/:experiment/copy", experimentCopy)
                .when("/lab/:labId/experiments/:experiment/files", experimentFiles)
                .when("/lab/:labId/experiments/:experiment/files/:id", fileDetails)
                .when("/lab/:labId/projects", project)
                .when("/lab/:labId/projects/:project", projectDetails)
                .when("/lab/:labId/projects/:project/experiments", projectExperiments)
                .when("/lab/:labId/projects/:project/experiments/:experiment", experimentDetails)
                .when("/lab/:labId/projects/:project/experiments/:experiment/files", experimentFiles)
                .when("/lab/:labId/projects/:project/experiments/:experiment/files/:id", fileDetails)
                .when("/lab/:labId/projects/:project/copy", projectCopy)
                .when(
                    "/lab/:labId/projects/:project/experiments/:experiment/copy",
                    {controller: "copyExperiment", templateUrl: "../pages/experiment/wizard.html"}
                )
                .when(
                    "/requests/inbox/all",
                    {controller: "generalRequests", templateUrl: "../pages/general-requests/inbox.html"}
                )
                .when(
                    "/requests/inbox/lab/:id",
                    {controller: "lab-inbox-details", templateUrl: "../pages/general-requests/lab-request.html"}
                )
                .when(
                    "/requests/inbox/instrument/:instrument/:user/",
                    {
                        controller: "instrument-inbox-details",
                        templateUrl: "../pages/general-requests/instrument-request.html"
                    }
                )
                .when(
                    "/requests/inbox/instrument-creation/:id/",
                    {
                        controller: "instrument-creation-details",
                        templateUrl: "../pages/general-requests/instrument-creation-request.html"
                    }
                )
                .when(
                    "/requests/inbox/all/copy-project/:requestId/",
                    {
                        controller: "copy-project-confirmation",
                        templateUrl: "../pages/general-requests/copy-project.html"
                    }
                )
                .when(
                    "/requests/outbox/all",
                    {controller: "generalRequests", templateUrl: "../pages/general-requests/outbox.html"}
                )
                .when(
                    "/search/:query/projects",
                    {controller: "search-projects", templateUrl: "../pages/projects/list.html"}
                )
                .when("/search/:query/projects/:project", projectDetails)
                .when("/search/:query/projects/:project/experiments", projectExperiments)
                .when("/search/:query/projects/:project/experiments/:experiment", experimentDetails)
                .when("/search/:query/projects/:project/experiments/:experiment/files", experimentFiles)
                .when("/search/:query/projects/:project/experiments/:experiment/files/:id", fileDetails)
                .when(
                    "/search/:query/experiments",
                    {controller: "search-experiments", templateUrl: "../pages/experiment/list.html"}
                )
                .when("/search/:query/experiments/:experiment", experimentDetails)
                .when("/search/:query/experiments/:experiment/files", experimentFiles)
                .when("/search/:query/experiments/:experiment/files/:id", fileDetails)
                .when("/search/:query/files", {controller: "search-files", templateUrl: "../pages/files/list.html"})
                .when("/search/:query/files/:id", fileDetails)
                .when(
                    "/search/:query/instruments",
                    {controller: "search-instruments", templateUrl: "../pages/instruments.html"}
                )
                .when("/search/:query/instruments/:id", instrumentDetails)
                .when("/profile", {controller: "profile", templateUrl: "../pages/user/profile.html"})
                .when(
                    "/profile/:subscriptionResult",
                    {controller: "profile", templateUrl: "../pages/user/profile.html"}
                )
                .//when("/metatranslation", {controller: "metaDataTranslationDetails", templateUrl:"../pages/translation/meta-data-details.html"}).
                when("/news", {controller: "news-controller", templateUrl: "../pages/news/list.html"})
                .when("/news/new", {controller: "newNews", templateUrl: "../pages/news/new.html"})
                .when("/news/:id", {controller: "news-details", templateUrl: "../pages/news/details.html"})
                .when(
                    "/ad",
                    {controller: "ad-controller", templateUrl: "../pages/ad/list.html", resolve: redirectIfNotAdmin}
                )
                .when(
                    "/ad/new",
                    {controller: "newAd", templateUrl: "../pages/ad/new.html", resolve: redirectIfNotAdmin}
                )
                .when(
                    "/ad/:id",
                    {controller: "ad-details", templateUrl: "../pages/ad/details.html", resolve: redirectIfNotAdmin}
                )
                .when("/trash", {controller: "trash", templateUrl: "../pages/trash.html"})
                .when(
                    "/admin/tools/notification/new",
                    {controller: "broadcast-notification", templateUrl: "../pages/admin/new-broadcast-email.html"}
                )
                .when(
                    "/file-access-log",
                    {controller: "file-access-log-controller", templateUrl: "../pages/admin/file-access-log.html"}
                )
                .when("/preferences", {controller: "user-preferences", templateUrl: "../pages/preferences.html"})
                .when(
                    "/report-issue",
                    {controller: "report-issue-dialog", templateUrl: "../script/report-issue/report-issue-dialog.html"}
                )
                .when(
                    "/report-issue/:issueId/edit",
                    {controller: "report-issue-dialog", templateUrl: "../script/report-issue/report-issue-dialog.html"}
                )
                .when("/client-token", {controller: "client-token", templateUrl: "../pages/client-token.html"})
                .when(
                    "/aggregate-statistics",
                    {controller: "aggregate-statistics", templateUrl: "../pages/admin/statistics-list.html"}
                    )
                .when(
                    "/issues/:filter",
                    {controller: "issues-list", templateUrl: "../script/report-issue/issues-list.html"}
                )
                ./* Scripts */
                when(
                    "/scripts/new",
                    {controller: "createScriptController", templateUrl: "../pages/scripts/new-script.html"}
                )
                .when(
                    "/scripts/:scripts",
                    {controller: "scriptsController", templateUrl: "../pages/scripts/scripts-list.html"}
                )
                .when(
                    "/scripts/:scripts/:id",
                    {controller: "editScriptController", templateUrl: "../pages/scripts/edit-script.html"}
                )
                .when(
                    "/scripts/:scripts/:id/view",
                    {controller: "viewScriptController", templateUrl: "../pages/scripts/edit-script.html"}
                )
                .otherwise({redirectTo: "projects/all"});

            function getRedirectIfConditionNotMet(condition) {
                return {
                    redirect: ["$q", "$location", "$rootScope",
                        "CommonUtil", "FeatureProvider",
                        "UserLabsProvider", "UserDetailsProvider", function ($q,
                                                                             $location,
                                                                             $rootScope,
                                                                             CommonUtil,
                                                                             FeatureProvider,
                                                                             UserLabsProvider,
                                                                             UserDetailsProvider) {

                            var deffered = $q.defer();

                            CommonUtil.waitUntil(function () {
                                return UserDetailsProvider.isUserLoggedIn();
                            }, function () {
                                var cond = condition($rootScope, FeatureProvider,
                                    UserLabsProvider, UserDetailsProvider);

                                if (cond) {
                                    deffered.resolve();
                                } else {
                                    deffered.reject();
                                    $location.path(DEFAULT_PAGE_PATH);
                                }
                            });

                            return deffered.promise;
                        }]
                };
            }

            function getRedirectIfNotLabMember() {
                return getRedirectIfConditionNotMet(function ($rootScope, FeatureProvider, UserLabsProvider) {
                    return UserLabsProvider.isLabMember();
                });
            }

            function getRedirectIfNotAdmin() {
                return getRedirectIfConditionNotMet(function ($rootScope, FeatureProvider,
                                                              UserLabsProvider, UserDetailsProvider) {
                    return UserDetailsProvider.isAdmin();
                });
            }
        })
        .controller("sidebar", function ($scope, $location) {
            $scope.isLabHead = function (id) {
                return $scope.loggedInUser && $scope.loggedInUser.id == id;
            };
            $scope.getClass = function (path) {
                return $location.path().substr(0, path.length) == path ? "active" : "";
            };
            $scope.getClassOnlyThis = function (path) {
                return $location.path() == path ? "active" : "";
            };
        })
        .factory("DashboardButton", function () {
            return function (key, text, title, style, btnClass) {
                return {
                    key: key,
                    text: text,
                    title: title,
                    style: style,
                    btnClass: btnClass,
                    showIcon: style ? true : false,
                    display: false,
                    disabledPopupOptions: {},
                    showPopup: false,
                    popupOptions: {},
                    _clicked: function () {
                        if (this._disabled()) {
                            this.disabledPopupOptions.showPopup = true;
                            return this.disabledPopupOptions;
                        }
                        if (this.onClickHandler) {
                            this.onClickHandler();
                        }
                        this.popupOptions.showPopup = this.showPopup;
                        return this.popupOptions;
                    },
                    _disabled: function () {
                        if (this.disabledHandler) {
                            return this.disabledHandler();
                        }
                    }
                };
            };
        })
        .factory("DashboardButtonFactory", function ($rootScope) {
            var buttonFactory = {
                put: function (button) {
                    $rootScope.operationButtonFactory.buttons[button.key] = button;
                },
                origin: function (item) {
                    if (item) {
                        $rootScope.operationButtonFactory.origin = item;
                    } else {
                        return $rootScope.operationButtonFactory.origin;
                    }
                },
                count: function (item) {
                    if (item) {
                        $rootScope.operationButtonFactory.count = item;
                    } else {
                        return $rootScope.operationButtonFactory.count;
                    }
                },
                filesTotalSize: function (item) {
                    if (item) {
                        $rootScope.operationButtonFactory.filesTotalSize = item;
                    } else {
                        return $rootScope.operationButtonFactory.filesTotalSize;
                    }
                },
                clear: function () {
                    $rootScope.operationButtonFactory = {
                        origin: "",
                        count: 0,
                        disabledPopupOptions: {},
                        display: function () {
                            var displayWrap = {};
                            angular.forEach(this.buttons, function (value, key) {
                                this.display = value.display ? true : this.display;
                            }, displayWrap);
                            return displayWrap.display;
                        },
                        filesTotalSize: 0,
                        buttons: {}
                    };
                }
            };
            buttonFactory.clear();
            return buttonFactory;
        })
        .factory("DashboardGlobalButtonFactory", function ($rootScope) {
            var buttonFactory = {
                put: function (button) {
                    $rootScope.globalOperationButtonFactory.buttons[button.key] = button;
                },
                display: function (key, show) {
                    $rootScope.globalOperationButtonFactory.buttons[key].display = show;
                },
                clear: function () {
                    $rootScope.globalOperationButtonFactory = {
                        origin: "",
                        count: 0,
                        display: function () {
                            var displayWrap = {};
                            angular.forEach(this.buttons, function (value, key) {
                                this.display = value.display ? true : this.display;
                            }, displayWrap);
                            return displayWrap.display;
                        },
                        buttons: {}
                    };
                }
            };
            buttonFactory.clear();
            return buttonFactory;
        })
        .factory("experimentByFiles", function () {
            var files = [];
            var labId, specieId, instrumentId, modelId, vendorId, technologyTypeId, instrumentTypeId;

            return {
                setFiles: function (selectedFiles) {
                    files = selectedFiles;
                },
                getFiles: function () {
                    return files;
                },
                setLabId: function (lab) {
                    labId = lab;
                },
                getLabId: function () {
                    return labId;
                },
                setSpecieId: function (specie) {
                    specieId = specie;
                },
                getSpecieId: function () {
                    return specieId;
                },
                setInstrumentId: function (instrument) {
                    instrumentId = instrument;
                },
                getInstrumentId: function () {
                    return instrumentId;
                },
                setModelId: function (model) {
                    modelId = model;
                },
                getModelId: function () {
                    return modelId;
                },
                setVendorId: function (vendor) {
                    vendorId = vendor;
                },
                getVendorId: function () {
                    return vendorId;
                },
                setTechnologyTypeId: function (technologyType) {
                    technologyTypeId = technologyType;
                },
                getTechnologyTypeId: function () {
                    return technologyTypeId;
                },
                setInstrumentTypeId: function (instrumentType) {
                    instrumentTypeId = instrumentType;
                },
                getInstrumentTypeId: function () {
                    return instrumentTypeId;
                },
                clear: function () {
                    files = [];
                    labId = specieId = instrumentId = vendorId = technologyTypeId = instrumentTypeId = undefined;
                }
            };
        })
        .controller(
            "main-controller",
            function ($scope, $rootScope, $location, OperatedInstruments, Laboratories, DashboardButtonFactory,
                      contentRequestParameters, currentContentType, Security, mainViewResizeService,
                      DashboardGlobalButtonFactory, FeatureProvider, UserDetailsProvider) {
                /**
                 * Inspired by http://stackoverflow.com/questions/12506329/how-to-dynamically-change-header-based-on-angularjs-partial-view
                 */

                $scope.defaultUrl = "/projects/all";
                $rootScope.dialogReturnUrl = $scope.defaultUrl;
                DashboardButtonFactory.clear();
                DashboardGlobalButtonFactory.clear();
                $scope.isFeatureAvailable = FeatureProvider.isFeatureAvailable;
                $scope.isAdmin = UserDetailsProvider.isAdmin;

                mainViewResizeService.checkWidth();

                var STRING_PATH = {filter: ["all", "my", "shared", "public", "all-available", "requestId"]};
                var NUMBER_PATH = ["project", "experiment", "id", "user", "labId", "instrumnentId", "page", "items"];
                var forbidden = [/\/laboratories\/\w+\/?/, /\/laboratories\/\w+\/\d+\/?/, /\/news.*/, /\/translation.*/];


                $scope.$watch("page.tempFilter", function (newValue, oldValue) {
                    //if the page was reloaded
                    if (newValue == undefined && oldValue == undefined) {
                        $scope.page.filter = "";
                    }
                    //if we go back from modal dialog
                    if (newValue == undefined && oldValue != newValue) {
                        $scope.page.tempFilter = oldValue;
                    }
                    //if the filter string has become empty
                    if (newValue != undefined && newValue.length == 0) {
                        $scope.page.filter = "";
                    }
                });

                $scope.$watch("page.filter", function (n, o) {
                    if (n != undefined) {
                        contentRequestParameters.setParameters(currentContentType(), {filterQuery: n});
                    }
                });

                //TODO <herman.zamula>: Make proper url validation (e.g, use "resolve" parameter in $routeProvider config)
                function resetPage(event, next, current) {
                    $scope.page = {
                        title: "Dashboard",
                        subtitle: "",
                        showFilter: false,
                        query: "",
                        subHeaderUrl: "sub-header.html",
                        custom: {}
                    };

                    Laboratories.labitems(function (labs) {
                        $rootScope.laboratories = labs;
                        var instruments = [];
                        $.each(labs, function (i, lab) {
                            instruments = instruments.concat(lab.instruments);
                        });
                        $scope.operatedInstruments = instruments;
                    });

                    DashboardButtonFactory.clear();
                    DashboardGlobalButtonFactory.clear();
                }

                $scope.$on("$routeChangeStart", resetPage);
                resetPage();
                $scope.$on("$routeChangeSuccess", function () {
                    setTimeout(function () {
                        $(".scroll-area").scrollTop(0);
                        setScrollAreaHeight();
                    }, 0);
                });

                // ----- Route path checker -----
                function checkPath(event, next, current) {
                    if ($location.url() === $scope.defaultUrl) {
                        $scope.pathError = false;
                        return;
                    }
                    removeModalClasses();

                    function toDefaultLocation() {
                        return $location.path($scope.defaultUrl).replace();
                    }

                    function setPathError(error) {
                        $scope.pathError = error;
                        if (error) {
                            toDefaultLocation();
                        }
                    }

                    for (var name in next.params) {
                        var val = next.params[name];
                        if ($.inArray(name, NUMBER_PATH) != -1 && !isNumber(val)) {
                            CommonLogger.warn("Incorrect number value in path \"" + name + "\"");
                            setPathError(true);
                            return;
                        }
                        if (matchKey(name, STRING_PATH) && typeof val == "string" && $.inArray(
                            val,
                            STRING_PATH[name]
                        ) === -1) {
                            CommonLogger.warn("Incorrect value in path \"" + name + "\"");
                            setPathError(true);
                            return;
                        }
                    }

                    var isAdmin = UserDetailsProvider.isAdmin();

                    if (isAdmin) {
                        Security.get({path: ""}, function (user) {
                            var filteredAdminRoles = $.grep(user.authorities, function (role) {
                                return role.authority == "ROLE_admin";
                            });
                            isAdmin = filteredAdminRoles.length > 0;
                            checkAdmin();
                        });
                    } else {
                        checkAdmin();
                    }

                    function checkAdmin() {
                        if (!isAdmin) {
                            if ($.grep(forbidden, function (pattern) {
                                var path = $location.path().toString();
                                return path.replace(pattern, "").length == 0;
                            }).length > 0) {
                                setPathError(true);
                            }
                        }

                    }
                }


                $scope.$on("$routeChangeStart", checkPath);

                $rootScope.open = function (path) {
                    $rootScope.dialogReturnUrl = $location.url();
                    $location.url(path);
                };

                $scope.openUploadIfAvailable = function (path) {
                    $scope.uploadPathToOpen = path;
                    if ($scope.isUploadAvailable()) {
                        $rootScope.open($scope.uploadPathToOpen);
                    }
                };

                $scope.proceedUploadAnyway = function () {
                    $rootScope.open($scope.uploadPathToOpen);
                };

                var uploadAvailable = isUploadAvailable();

                $scope.isUploadAvailable = function () {
                    if ($scope.laboratories.length == 0) {
                        $("#no-lab-dialog").modal("show");
                        return false;
                    }
                    if ($scope.operatedInstruments.length == 0) {
                        $("#no-instrument-dialog").modal("show");
                        return false;
                    }
                    if (!uploadAvailable) {
                        $("#upload-unavailable-dialog").modal("show");
                    }
                    return uploadAvailable;
                };

                $scope.openModal = function (path) {
                    hideModal(function () {
                        $scope.open(path);
                        $scope.$apply();
                    });
                };

                var params = urlUtils.getUrlVars();
                if (params.unpaid) {
                    $scope.notPayedPopup = new Confirmation("#lab-account-not-payed-dialog");
                    $scope.notPayedPopup.labName = decodeURIComponent(params.labName);
                    $scope.notPayedPopup.lab = params.lab;
                    $scope.notPayedPopup.debt = params.debt;
                    setTimeout(function () {
                        $scope.notPayedPopup.showPopup();
                    });
                }

                function setupBillingNotificationPopup() {
                    Security.shouldShowBillingNotification(function (data) {
                        var remind = data.value;
                        var fromLoginPage = document.referrer.indexOf("authentication.html") != -1;
                        if (remind && (fromLoginPage
                            //TODO: On Production document.referrer is empty after login. Resolve it
                            || document.referrer == "")) {
                            $scope.billingLoginNotification = new Confirmation("#billing-login-notification", null, {
                                success: function () {
                                    if ($scope.billingLoginNotification.doNotShowAgain) {
                                        Security.removeBillingNotification();
                                    }
                                }
                            });
                            $scope.billingLoginNotification.doNotShowAgain = false;
                            setTimeout(function () {
                                $scope.billingLoginNotification.showPopup();
                            });
                        }
                    });

                }

                setupBillingNotificationPopup();

                $rootScope.getUploadDialogUrl = function () {
                    return $rootScope.isNewUpload ? "/upload/new" : "/upload/resume";
                };

                $scope.changeColumns = function (title) {
                    $scope.open("/" + title + "/change-columns");
                };
            }
        )
        .directive("uiSelect2", function () {
            return {
                link: function ($scope, element) {
                    //this is ID of container, which is implicitly created by select2.js
                    var containerIdSelector = "#s2id_" + $(element).attr("id");
                    var container = $(containerIdSelector);

                    $("input.select2-input").live("blur", function () {
                        var choice = $("a.select2-choice", container);
                        choice.removeAttr("tabIndex");
                    });
                }
            };
        })
        .controller(
            "new-upload-button",
            function ($scope, $rootScope, UnfinishedUploads, UploadBackgroundService, backgroundUpload, $location) {
                function refresh() {
                    UnfinishedUploads.query(function (files) {
                        $scope.files = $.grep(files, function (file) {
                            if (file.isArchive) {
                                if (new Date().getTime() - file.lastPingDate > 10000) {
                                    file.$remove();
                                }
                                return false;
                            }
                            return true;
                        });
                    });
                }

                $scope.onClickUploadProgress = function () {
                    if ($rootScope.uploadInProgress) {
                        $location.path($rootScope.isNewUpload ? "/upload/new" : "/upload/resume");
                        $("#uploadDialog").modal("show");
                    } else if (UploadBackgroundService.hasActive()) {
                        var upload = UploadBackgroundService.getActive();
                        UploadBackgroundService.moveToForeground(upload.id);
                        $location.path("/upload/" + upload.id + "/progress");
                    }
                };

                $scope.$on("refreshFileUpload", function () {
                    setTimeout(backgroundUpload());
                });

                $scope.uploadProgressFormatted = function () {
                    if ($rootScope.uploadInProgress) {
                        return $scope.stats.uploadProgressFormatted();
                    } else if (UploadBackgroundService.hasActive()) {
                        return UploadBackgroundService.getActive().progress;
                    }
                    return false;

                };

                $scope.isUploadInProgress = function () {
                    return $rootScope.uploadInProgress || UploadBackgroundService.hasActive();
                };

                $scope.$on("refreshResumeUpload", refresh);
                refresh();
            }
        )
        .factory("mainViewResizeService", function ($rootScope) {
            var mainViewResizeService = {};
            mainViewResizeService.checkWidth = function () {
                var windowWidth = $(window).width();
                var width = windowWidth * 0.85;
                var $center = $(".center-frame");
                $center.css("margin-left", (windowWidth - width) / 2 + "px");
                // margin-right should be 0, however content should be in center
                if (width < parseInt($center.css("max-width")) &&
                    width > parseInt($center.css("min-width"))) {
                    $rootScope.$broadcast("mainViewChanged", width);
                }
            };

            function resizeHandler(e) {
                mainViewResizeService.checkWidth();
            }

            $(window).resize(resizeHandler);
            return mainViewResizeService;
        })
        .directive("operationPopup", function () {
            return {
                restrict: "E",
                templateUrl: "../pages/component/operations/popup.html",
                scope: {options: "="},
                replace: true,
                controller: ["$scope", function ($scope) {
                    $scope.$watch("options", function (opts) {
                        if (opts) {
                            $scope.model = opts.model;
                        }
                    });
                }],
                link: function (scope, element) {
                    scope.$watch("options.showPopup", function (show) {
                        if (show) {
                            $(element).modal("show");
                            scope.options.showPopup = false;
                        }
                    });
                }
            };
        })
        .directive("advertisement", function () {
            return {
                restrict: "E",
                replace: true,
                templateUrl: "../pages/ad/ad.html",
                scope: {},
                controller: function ($scope, $window, AdvertImage) {
                    $scope.advert = AdvertImage.advertToDisplay(function (ad) {
                        if (ad.id) {
                            AdvertImage.incrementsDisplayCount({id: ad.id});
                        }

                    });
                    $scope.advertClick = function advertClickHandler(advert) {
                        $window.open(advert.redirectLink, "_blank");
                        $window.focus();
                        AdvertImage.incrementsClickCount({id: advert.id});
                    };
                }
            };
        });

    function matchKey(key, objArray) {
        for (var name in objArray) {
            if (name === key) {
                return 1;
            }
        }
        return 0;
    }

    function matchValue(val, objArray) {
        for (var name in objArray) {
            if (objArray[name] === val) {
                return 1;
            }
        }
        return 0;
    }

})();
