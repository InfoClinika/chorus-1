angular.module("general-requests-front", ["general-requests", "security-front", "requests-details", "front-end", "instruments-front", "error-catcher"])
    .controller("generalRequests", function ($scope, $route, $rootScope, $location, GeneralRequests, requestsUpdatedNotificationService, refuseConfirmationDialog, Inbox, Outbox, GeneralRequestsControllerCommon, GeneralRequestsCounter) {
        CommonLogger.setTags(["GENERAL-REQUESTS", "GENERAL-REQUESTS-CONTROLLER"]);
        $scope.page.showFilter = true;
        if ($location.$$url.match(/\/inbox.*/)) {
            $scope.page.title = "Inbox";
        } else if ($location.$$url.match(/\/outbox.*/)) {
            $scope.page.title = "Outbox";
        }

        $scope.inbox = [];
        $scope.outbox = [];
        $scope.isPressedMap = [];
        var isInboxTableEmpty = false;
        var isOutboxTableEmpty = false;
        var isTableLoading = false;

        $scope.refreshInbox = function () {
            $scope.inbox = Inbox.query(function (items) {
                isInboxTableEmpty = items.length == 0;
                if ($location.$$url.match(/\/inbox.*/)) {
                    var idsToMarkAsRead = [];
                    for (var i = 0; i < items.length; i++) {
                        if (items[i].availableActions === "OK") {
                            idsToMarkAsRead.push(items[i].id);
                        }
                    }
                    var inboxSetup = GeneralRequestsControllerCommon($scope, idsToMarkAsRead, Inbox, "#inbox-mark-as-read-confirmation");
                    inboxSetup.setup(function () {
                        isTableLoading = false;
                        $route.reload();
                    });
                }
            });
        };
        $scope.refreshInbox();

        $scope.refreshOutbox = function () {
            $scope.outbox = Outbox.query(function (items) {
                isOutboxTableEmpty = items.length == 0;
                if ($location.$$url.match(/\/outbox.*/)) {
                    var idsToMarkAsRead = [];
                    for (var i = 0; i < items.length; i++) {
                        idsToMarkAsRead.push(items[i].id);
                    }
                    var outboxSetup = GeneralRequestsControllerCommon($scope, idsToMarkAsRead, Outbox, "#outbox-mark-as-read-confirmation");
                    outboxSetup.setup(function(){
                        isTableLoading = false;
                        $route.reload();
                    });
                }
            });
        };

        $scope.$on("requestsUpdated", $scope.refreshInbox);
        $scope.$on("requestsUpdated", $scope.refreshOutbox);
        $scope.refreshOutbox();

        $scope.onAction = function (response) {
            if (!response.errorMessage) {
                requestsUpdatedNotificationService.broadcastItem();
            }
        };

        function approve(requestId, request) {
            Inbox.approve({id: requestId}, function (response) {
                if (!response.errorMessage) {
                    requestsUpdatedNotificationService.broadcastItem();
                } else {
                    handleFailure(request);
                }
                delete $scope.isPressedMap[requestId];
            });
        }

        $scope.approve = function (request) {
            var requestId = request.id;
            if ($scope.isPressedMap[requestId]) {
                return;
            }
            $scope.isPressedMap[requestId] = true;

            switch (getInboxType(request)) {
                case "CopyProjectStrategy":
                    $location.path("/requests/inbox/all/copy-project/" + requestId);
                    break;
                default :
                    approve(requestId, request);
            }
        };

        function getInboxType(request) {
            return request.id.split(/(\d+)/)[0];
        }

        function handleFailure(request) {
            var ids = request.id.split(/(\d+)/);

            if (ids[0] == "InstrumentCreationStrategy") {
                handleInstrumentCreationFailure(request);
            }
        }

        var currentInstrumentRequest = null;

        function handleInstrumentCreationFailure(request) {
            currentInstrumentRequest = request;
            $(".modal").modal("hide");
            var frame = "#creationInstrumentRequestError";
            $(frame).modal("show");
        }

        $scope.rejectInstrumentCreation = function () {
            if (!currentInstrumentRequest) {
                return;
            }
            $scope.refuse(currentInstrumentRequest);
            currentInstrumentRequest = null;
        };

        $scope.editInstrumentCreationDetails = function () {
            if (!currentInstrumentRequest) {
                return;
            }
            var ids = currentInstrumentRequest.id.split(/(\d+)/);
            currentInstrumentRequest = null;
            $rootScope.dialogReturnUrl = $location.url();
            $location.path("/requests/inbox/instrument-creation/" + parseInt(ids[1]));
        };

        //todo [pavel.kaplin] separate model and DOM elements
        $scope.refuse = function (request) {
            $(".modal").modal("hide");
            var frame = "#confirmRefuseModal";
            $(frame).modal("show");
            $("#confirmRefuseButton").click(function () {
                request.$refuse({comment: $("#confirmRefuseTextArea").val()}, $scope.refreshInbox);
                $(frame).modal("hide");
            });
        };

        $scope.showDetails = function (request) {
            var ids = request.id.split(/(\d+)/);
            var path = ids[0] ==
                "InstrumentStrategy" ?
                "instrument/" + parseInt(ids[1]) + "/" + parseInt(ids[3]) :
                    ids[0] == "LabCreationStrategy" ?
                "lab/" + parseInt(ids[1]) :
                    ids[0] == "InstrumentCreationStrategy" ?
                "instrument-creation/" + parseInt(ids[1]) :
                "";
            $rootScope.dialogReturnUrl = $location.url();
            $location.path("/requests/inbox/" + path);
        };

        $scope.getEmptyTableMessage = function () {
            return "There are no requests";
        };

        $scope.getLoadingTableMessage = function () {
            return "Processing... Please, wait";
        };

        $scope.isInboxTableEmpty = function () {
            return isInboxTableEmpty;
        };

        $scope.isTableLoading = function(){
            return isTableLoading;
        };

        $scope.setTableLoading = function(isLoading){
            isTableLoading = isLoading;
        };

        $scope.isOutboxTableEmpty = function () {
            return isOutboxTableEmpty;
        };

        $scope.showDetailsButton = function (request) {
            return request.availableActions == "APPROVE_REFUSE" && !request.id.match(/(.*LabMembership.*)|(.*ProjectSharing.*)|(.*CopyProject.*)/);
        }
    }).
    factory("GeneralRequestsControllerCommon", ["$route", "DashboardButtonFactory", "DashboardButton",
        function ($route, DashboardButtonFactory, DashboardButton){
            return function ($scope, items, Folder, confirmationDialogHtmlId) {
                return {
                    setup: function (callback) {

                        switchMarkAsReadButton();

                        function switchMarkAsReadButton(){
                            var button = new DashboardButton(5, "R", "Mark all as read", "delete-file");
                            button.display = items.length > 0;
                            button.onClickHandler = function(){
                                $scope.markAsRead = new FilesSelectedPopup(confirmationDialogHtmlId, function () {
                                    $scope.setTableLoading(true);
                                    button.display = false;
                                    Folder.bulkMarkAsRead({"itemIds":items}, function(response){
                                        $scope.onAction(response);
                                        callback();
                                    });
                                });
                                $scope.markAsRead.showPopup();
                            };
                            DashboardButtonFactory.count(items.length);
                            DashboardButtonFactory.origin("trash");
                            DashboardButtonFactory.put(button);
                        }
                    }
                }
            }
        }
    ]).
    controller("generalRequestCounter", function ($scope, GeneralRequestsCounter) {
        function updateRequestCounters() {
            GeneralRequestsCounter.inbox(function (count) {
                $scope.requestsInboxCount = count;
            });
            GeneralRequestsCounter.outbox(function (count) {
                $scope.requestsOutboxCount = count;
            });
        }

        $scope.$on("requestsUpdated", updateRequestCounters);
        updateRequestCounters();
    })
    .controller("copy-project-confirmation", function ($scope, $rootScope, $location, Inbox, $routeParams, requestsUpdatedNotificationService, Laboratories) {
        $scope.labs = Laboratories.query();
        $scope.selectedLab = null;
        $scope.approve = function () {
            Inbox.approve({id: $routeParams.requestId + "," + $scope.selectedLab.id}, onFinish);
        };

        $scope.refuse = function () {
            Inbox.refuse({id: $routeParams.requestId}, onFinish)
        };

        function onFinish() {
            setTimeout(function () {
                $(".modal").modal("hide");
                requestsUpdatedNotificationService.broadcastItem();
            }, 0);
        }
    })
    .controller("lab-inbox-details", function ($scope, $rootScope, $location, RequestsDetails, $routeParams, requestsUpdatedNotificationService, refuseConfirmationDialog) {
        if ($scope.pathError) return;
        CommonLogger.setTags(["GENERAL-REQUESTS", "LAB-INBOX-DETAILS-CONTROLLER"]);
        $scope.returnUrl = $rootScope.returnUrl;
        $scope.showCreateDialog = true;
        $scope.page.title = "Laboratory Operator Request Details";

        RequestsDetails.get({type: "lab", request: $routeParams.id}, function (labRequest) {
            $scope.details = labRequest;
        });


        $scope.approve = function () {
            RequestsDetails.approveLabCreation({request: $routeParams.id}, function () {
                setTimeout(function () {
                    $(".modal").modal("hide");
                    requestsUpdatedNotificationService.broadcastItem();
                }, 0);
            });
        };

        $scope.updateAndApprove = function () {
            var laboratory = getLaboratory();
            RequestsDetails.updateLab(laboratory, function () {
                $scope.approve();
            });
        };

        $scope.refuse = function () {
            refuseConfirmationDialog(RequestsDetails.refuseLabCreation, {requestId: $routeParams.id, comment: ""}, function () {
                requestsUpdatedNotificationService.broadcastItem();
            });
        };

        function getLaboratory() {
            var laboratory = {};
            laboratory.contactEmail = $scope.details.contactEmail;
            laboratory.id = $scope.details.id;
            laboratory.institutionUrl = $scope.details.institutionUrl;
            laboratory.name = $scope.details.name;
            laboratory.headFirstName = $scope.details.headFirstName;
            laboratory.headLastName = $scope.details.headLastName;
            laboratory.headEmail = $scope.details.headEmail;
            return laboratory;
        }

    })
    .controller("instrument-creation-details", function ($scope, $rootScope, RequestsDetails, $routeParams, requestsUpdatedNotificationService, refuseConfirmationDialog, instrumentLockMassesCommons, InstrumentVendors, InstrumentModels, Security, AvailableOperators, Instruments) {

        if ($scope.pathError) return;
        CommonLogger.setTags(["GENERAL-REQUESTS", "INSTRUMENT-CREATION-DETAILS-CONTROLLER"]);
        $scope.returnUrl = $rootScope.returnUrl;
        $scope.showCreateDialog = true;
        $scope.page.title = "Instrument Creation Request Details";

        $scope.instrument = {details: {}, operators: []};
        $scope.model = {};
        $scope.excludeEmails = [];
        $scope.initialized = false;

        function approve() {
            RequestsDetails.approveInstrumentCreation({request: $routeParams.id}, function () {
                setTimeout(function () {
                    $(".modal").modal("hide");
                    requestsUpdatedNotificationService.broadcastItem();
                }, 0);
            });
        }

        $scope.updateAndApprove = function () {
            if ($scope.isFormInvalid()) {
                return;
            }
            var instrumentRequest = getInstrumentRequest();
            RequestsDetails.updateInstrument(instrumentRequest, function () {
                approve();
            });
        };

        $scope.refuse = function () {
            refuseConfirmationDialog(RequestsDetails.refuseInstrumentCreation, {requestId: $routeParams.id, comment: ""}, function () {
                requestsUpdatedNotificationService.broadcastItem();
            });
        };

        function getInstrumentRequest() {

            var request = {};
            var details = {};

            request.details = details;
            request.id = $routeParams.id;
            request.model = $scope.instrument.model;
            request.operators = $.map($scope.instrument.operators, function (item) {
                return item.id;
            });
            details.name = $scope.instrument.details.name;
            details.serialNumber = $scope.instrument.details.serialNumber;
            details.hplc = $scope.instrument.details.hplc;
            details.peripherals = $scope.instrument.details.peripherals;
            details.lockMasses = $scope.lockMasses;

            return request;
        }


        instrumentLockMassesCommons($scope);

        InstrumentVendors.all(function (vendors) {
            $scope.vendors = vendors;
        });

        $scope.users = [];
        $scope.$watch("model.vendor", function (vendor) {
            CommonLogger.log("watch on model.vendor");
            CommonLogger.log(vendor);
            if (vendor) {
                InstrumentModels.byVendor({vendor: vendor}, function (modelsWrapper) {
                    $scope.models = modelsWrapper.value;
                    if ($scope.initialized) {
                        $scope.instrument.model = $scope.models[0].id;
                    } else {
                        $scope.instrument.model = $scope.details.model;
                    }

                });
            } else {
                $scope.models = [
                    {name: "Select vendor first"}
                ];
                $scope.instrument.model = undefined;
            }
        });

        RequestsDetails.get({type: "instrument-creation", request: $routeParams.id}, function (instrumentRequest) {
            $scope.details = instrumentRequest;
            setTimeout(updateInstrumentDetails, 100);
        });

        var instrumentsByLab = [];

        function updateInstrumentDetails() {
            var instr = $scope.instrument;
            var details = $scope.details;

            instr.details.name = details.name;
            instr.details.serialNumber = details.serialNumber;
            instr.details.hplc = details.hplc;
            instr.details.peripherals = details.peripherals;
            instr.lab = details.labId;
            instr.labName = details.labName;
            instr.operators = details.operators;
            instr.model = details.model;

            $scope.model.vendor = details.vendor;
            $scope.lockMasses = details.lockMasses;

            instrumentsByLab = Instruments.byLab({id: instr.lab});
            Security.get({path: ""}, function (user) {
                $scope.users = AvailableOperators.query({lab: $scope.instrument.lab});
                $scope.excludeEmails = [user.username, $scope.details.requester.email];
            });

            setTimeout(function () {
                CommonLogger.log("scope");
                CommonLogger.log($scope);
                $scope.initialized = true;
            }, 100);

        }

        $scope.isNameDuplicated = function () {
            if (!$scope.instrument.details.name) return false;
            var instruments = $.grep(instrumentsByLab, function (item) {
                return item.name.toUpperCase() === $scope.instrument.details.name.toUpperCase().trim();
            });
            return instruments.length != 0;
        };

        $scope.isSerialNumberDuplicated = function () {
            if (!$scope.instrument.details.serialNumber) return false;
            var instruments = $.grep(instrumentsByLab, function (item) {
                return item.serialNumber === $scope.instrument.details.serialNumber.trim();
            });
            return instruments.length != 0;
        };

        $scope.inviteHandler = function (item, callback) {
            Security.invite({email: item.email}, function (invited) {
                callback(invited);
            });
        };

        $scope.isFormInvalid = function () {
            return !$scope.instrument.details.name || $scope.instrument.details.name.trim().length == 0
                || !$scope.model.vendor
                || !$scope.instrument.model
                || !$scope.instrument.details.serialNumber
                || $scope.instrument.details.serialNumber.trim().length == 0;
        };

    })
    .controller("instrument-inbox-details", function ($scope, $rootScope, $location, RequestsDetails, $routeParams, requestsUpdatedNotificationService, refuseConfirmationDialog) {
        if ($scope.pathError) return;
        CommonLogger.setTags(["GENERAL-REQUESTS", "INSTRUMENT-INBOX-DETAILS-CONTROLLER"]);
        $scope.showCreateDialog = true;
        $scope.page.title = "Instrument Operator Request Details";

        RequestsDetails.get({path: "instrument", request: $routeParams.instrument, requester: $routeParams.user}, function (instrument) {
            $scope.details = instrument;
        });


        $scope.approve = function () {
            RequestsDetails.approveInstrument({request: $routeParams.instrument, requester: $routeParams.user}, function () {
                setTimeout(function () {
                    $(".modal").modal("hide");
                }, 0);
            })
        };

        $scope.refuse = function () {
            refuseConfirmationDialog(RequestsDetails.refuseInstrument, {instrumentId: $routeParams.instrument, requesterId: $routeParams.user, comment: ""}, function () {
                requestsUpdatedNotificationService.broadcastItem();
            });
        };

    })
    .factory("refuseConfirmationDialog", function () {
        return function (refuseFn, requestParams, onSuccessFn) {
            setTimeout(function () {
                $(".modal").modal("hide");
                setTimeout(function () {  //avoid unexpected close of Refuse Confirmation dialog
                    var frame = "#confirmRefuseModal";
                    $(frame).modal("show");
                    $("#confirmRefuseButton").click(function () {
                        requestParams.comment = $("#confirmRefuseTextArea").val();
                        refuseFn(requestParams, onSuccessFn);
                        hideModal();
                    })
                }, 0);
            }, 0);
        }
    });
