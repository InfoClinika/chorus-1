angular.module("instruments-front", ["modals", "instruments-back", "ui", "general-requests", "security-front", "security-back", "laboratories", "front-end", "error-catcher", "enums"])
    .factory("instrumentListActions", function (GeneralRequests) {
        return function ($scope) {
            $scope.sendRequest = function (instrument) {
                GeneralRequests.applyForInstrument({"instrument": instrument.id}, new function () {
                    instrument.access = "PENDING";
                })
            };
        }
    })
    .controller("instrumentsByLab", function ($scope, $location, $routeParams, $route, Instruments, instrumentListActions,
                                              contentRequestParameters, instrumentsExpandMenu, Security) {
        if ($scope.pathError) return;
        CommonLogger.setTags(["INSTRUMENTS", "INSTRUMENTS-BY-LAB-CONTROLLER"]);
        $scope.page.title = "Instruments";
        $scope.page.showFilter = true;
        CommonLogger.log("instrumentsByLab");
        var request = contentRequestParameters.getParameters("instruments");
        CommonLogger.log(request);
        $scope.sorting = {};
        Security.isLabHead(function(result){
            $scope.isLabHead = result.value;
        });
        $scope.sorting.reverse = !request.asc;
        $scope.sorting.field = request.sortingField;
        $scope.page.filter = request.filterQuery;
        $scope.instruments = Instruments.byLab({id: $routeParams.id});
        instrumentListActions($scope);
        instrumentsExpandMenu($scope);

        $scope.isTableEmpty = function () {
            return $scope.instruments.length == 0;
        };

        $scope.getEmptyTableMessage = function () {
            return "There are no instruments";
        };
        $scope.displayConfirmation = function (instrument) {
            $scope.confirmation = new Confirmation("#remove-instrument-confirmation", instrument,
                {
                    success: function () {
                        Instruments.delete({instrument: instrument.id}, function () {
                            $route.reload();
                        })
                    },
                    getName: function () {
                        return instrument.name;
                    }
                }
            );
            $scope.confirmation.showPopup();
        };
    })
    .controller("new-instrument", function ($scope, InstrumentTechnologyTypes, InstrumentVendors,
                                            InstrumentModels, Instruments, AvailableOperators,
                                            Security, Laboratories, instrumentLockMassesCommons, InstrumentStudyType) {
        CommonLogger.setTags(["INSTRUMENTS", "NEW-INSTRUMENT-CONTROLLER"]);
        $scope.instrument = {details: {}, operators: []};
        $scope.model = {};
        $scope.excludeEmails = [];
        instrumentLockMassesCommons($scope);


        function getUserInfo(callback){
            Security.get({path: ""}, function (user) {
                $scope.userInfo = user;
                if(callback){
                    callback();
                }
            });
        }

        getUserInfo();

        InstrumentTechnologyTypes.query(function (studyTypes) {
            $scope.studyTypes = studyTypes;
            $scope.model.studyType = getDefaultOptionValue($scope.studyTypes);
        });

        Laboratories.query(function (labs) {
            $scope.labs = labs;
            $scope.instrument.lab = getDefaultOptionValue($scope.labs);
        });
        $scope.users = [];

        $scope.$watch("model.studyType", function (techType) {
            if (techType) {
                InstrumentVendors.byTechnologyType({techType: techType}, function (vendors) {
                    $scope.vendors = vendors;
                    $scope.model.vendor = getDefaultOptionValue($scope.vendors)
                });

            } else {
                $scope.vendors = [
                    {name: "Select study first"}
                ];
            }
            $scope.instrument.model = undefined;
        });
        
        $scope.$watch("model.vendor", function (vendor) {
            if (vendor) {
                InstrumentModels.byVendor({vendor: vendor}, function (modelsWrapper) {
                    $scope.models = modelsWrapper.value;
                    $scope.instrument.model = getDefaultOptionValue($scope.models);
                });
            } else {
                $scope.models = [
                    {name: "Select vendor first"}
                ];
            }
            $scope.instrument.model = undefined;
        });

        var instrumentsByLab = [];

        $scope.isNameDuplicated = function () {
            if (!$scope.instrument.details.name) return false;
            var instruments = $.grep(instrumentsByLab, function (item) {
                return item.name.toUpperCase() === $scope.instrument.details.name.toUpperCase().trim();
            });
            return instruments.length != 0;
        };

        $scope.inviteHandler = function (item, callback) {
            Security.invite({email: item.email}, function (invited) {
                callback(invited);
            });
        };

        $scope.isFormInvalid = function () {
            CommonLogger.log("isFormInvalid for instruments");
            return !$scope.instrument.details.name || $scope.instrument.details.name.trim().length == 0
                || !$scope.model.vendor
                || !$scope.instrument.model
                || !$scope.instrument.lab
                || !$scope.instrument.details.serialNumber
                || $scope.instrument.details.serialNumber.trim().length == 0;
        };

        $scope.$watch("instrument.lab", function (val) {
            $scope.instrument.operators = [];
            if ($scope.onLabSelected()) {

                instrumentsByLab = Instruments.byLab({id: val});

                if(!$scope.userInfo){
                    getUserInfo(updateOperators);
                } else {
                    updateOperators();
                }

                function updateOperators(){
                    $scope.users = AvailableOperators.query({lab: $scope.instrument.lab});
                    $scope.excludeEmails = [$scope.userInfo.username];
                }
            }
        });

        $scope.$watch("instrument.details.serialNumber", function () {
            if ($scope.saveInstrumentError) {
                delete $scope.saveInstrumentError;
            }
        });

        $scope.onLabSelected = function () {
            return $scope.instrument.lab;
        };

        $scope.getCreateButtonText = function(){
            if($scope.instrument.lab && !isLabHead($scope.userInfo.id, $scope.instrument.lab)){
                return "Send Request"
            } else {
                return "Create";
            }
        };

        function isLabHead(userId, labId){
            return getLabById(labId).labHead == userId;
        }

        function getLabById(labId){
            return $.grep($scope.labs, function(item){
                return item.id == labId;
            })[0];
        }

        $scope.save = function (isInvalid) {
            $scope.buttonPressed = true;
            if (isInvalid) {
                return
            }
            $scope.instrument.details.hplc = $scope.instrument.details.hplc || "";
            $scope.instrument.details.peripherals = $scope.instrument.details.peripherals || "";
            $scope.instrument.details.lockMasses = $scope.lockMasses;

            $scope.instrument.operators = $.map($scope.instrument.operators, function (el) {
                return parseInt(el.id);
            });
            Instruments.save($scope.instrument, function (response) {
                if (response.errorMessage) {
                    $scope.saveInstrumentError = {message: "Can't create instrument. Please check instrument name or serial number"};
                    $(".alert-modal").fadeIn(500);
                    return;
                }
                $scope.returnUrl = "/lab/" + $scope.instrument.lab + "/instruments";
                setTimeout(function () {
                    $(".modal").modal("hide");
                    if(!isLabHead($scope.userInfo.id, $scope.instrument.lab)){
                        instrumentCreationRequestPopup();
                    }
                }, 0);
            });
        };

        $scope.getModalReturnUrl = function () {
            return "/lab/" + $scope.instrument.lab + "/instruments";
        }
    })
    .controller("instrument-details", function ($scope, $routeParams, Instruments, GeneralRequests, AvailableOperators, Security, instrumentLockMassesCommons, InstrumentStudyType) {
        if ($scope.pathError) return;
        CommonLogger.setTags(["INSTRUMENTS", "INSTRUMENT-DETAILS-CONTROLLER"]);
        $scope.excludeEmails = [];
        $scope.instrument = {};
        $scope.userDescription = {};
        $scope.viewMode = true;
        var instrumentsByLab = [];
        $scope.users = [];

        Instruments.get({id: $routeParams.id}, function (response) {
            if (response.errorMessage) {
                $scope.returnUrl = $scope.defaultUrl;
                hideModal();
                return;
            }
            var instrument = response.details;
            Security.get({path: ""}, function (loggedInUser) {
                var loggedUserIsOperator = false;
                angular.forEach(instrument.operators, function (operator) {
                    if (operator.email == loggedInUser.username) {
                        $scope.excludeEmails = [operator.email, instrument.creator, instrument.lab.headEmail];
                        $scope.userDescription[instrument.lab.headEmail] = "Lab Head";
                        $scope.userDescription[instrument.creator] = "Creator";
                        $scope.userDescription[operator.email] = "Me";
                        loggedUserIsOperator = true;
                        return false;
                    }
                });
                Instruments.byLab({id: instrument.lab.id}, function (items) {
                    instrumentsByLab = items;
                });
                $scope.viewMode = (!loggedUserIsOperator);
                $scope.instrument = instrument;
                instrumentLockMassesCommons($scope, instrument.lockMasses);
                $scope.users = AvailableOperators.query({lab: $scope.instrument.lab.id});
            });
        });

        $scope.isNameDuplicated = function () {
            if (!$scope.instrument.name) return false;
            var instruments = $.grep(instrumentsByLab, function (item) {
                return item.id != $scope.instrument.id && item.name.toUpperCase() === $scope.instrument.name.toUpperCase().trim();
            });
            return instruments.length != 0;
        };

        $scope.$watch("instrument.details.serialNumber", function () {
            if ($scope.saveInstrumentError) {
                delete $scope.saveInstrumentError;
            }
        });

        $scope.instrument.operators = [];

        $scope.save = function (isFormInvalid) {
            $scope.buttonPressed = true;
            if (isFormInvalid) return;

            var instrument = {};
            instrument.id = $routeParams.id;
            instrument.details = {
                name: $scope.instrument.name,
                serialNumber: $scope.instrument.serialNumber,
                hplc: $scope.instrument.hplc || "",
                peripherals: $scope.instrument.peripherals || "",
                lockMasses: $scope.lockMasses
            };
            instrument.operators = $.map($scope.instrument.operators, function (operator) {
                return parseInt([
                    [operator.id]
                ]);
            });
            Instruments.update(instrument, function (response) {
                if (response.errorMessage) {
                    $scope.saveInstrumentError = {message: response.errorMessage};
                    $(".alert-modal").fadeIn(500);
                    return;
                }
                setTimeout(function () {
                    $(".modal").modal("hide");
                }, 0);
            });
        };
        $scope.sendRequest = function () {
            GeneralRequests.applyForInstrument({"instrument": $scope.instrument.id}, new function () {
                $scope.instrument.access = "PENDING";
            })
        };
    })
    .directive("instrumentDetails", detailsLink({"title": "Show Instrument Details", "dataTarget": "#instrumentDetails"}))
    .directive("instrumentModelDetails", detailsLink({"title": "Show Instrument Model Details", "dataTarget": "#instrumentModelDetails"}))
    .directive("operatorSelector", userOrGroupSelection({
        "isEmailNotificationsAvailable": false,
        "groupSelectionAvailable": false,
        "emptyTableMessage": "There are no operators",
        "addActionText": "Invite people personally",
        "addPlaceHolderText": "Enter person's email"}))
    .factory("instrumentLockMassesCommons", function () {

        return function ($scope, initLockMasses) {
            $scope.lockMasses = initLockMasses || [];
            $scope.defaultLockMasses = [
                {
                    checked: false,
                    value: {
                        lockMass: 556.2771,
                        charge: 1
                    }
                },
                {
                    checked: false,
                    value: {
                        lockMass: 785.8426,
                        charge: 2
                    }
                }
            ];

            var checkDefaultsFn = function () {
                $.each($scope.defaultLockMasses, function (i, item) {
                    var selectedDefaults = $.grep($scope.lockMasses, function (value) {
                        return item.value.lockMass == value.lockMass && value.charge == item.value.charge;
                    });
                    item.checked = selectedDefaults.length > 0;
                })
            };
            checkDefaultsFn();

            $scope.$watch("lockMasses.length", function (newVal) {
                checkDefaultsFn();
            });

            $scope.onCheckLockMass = function (item) {
                //added the $scope.lockMasses.indexOf(item.value) === -1 because of IE dblclick issue
                if (item.checked && $scope.lockMasses.indexOf(item.value) === -1) {
                    $scope.lockMasses.push(item.value);
                } else {
                    $scope.lockMasses = $.grep($scope.lockMasses, function(value) {
                        return value.lockMass != item.value.lockMass && value.charge != item.value.charge;
                    });
                }
            };
        }
    })
    .factory("removeInstrumentConfirmation", function ($route, Instruments) {
        return function ($scope) {
            return function (instrument) {
                $scope.confirmation = new Confirmation("#remove-instrument-confirmation", instrument,
                    {
                        success: function () {
                            Instruments.delete({instrument: instrument.id}, function () {
                                $route.reload();
                            })
                        },
                        getName: function () {
                            return instrument.name;
                        }
                    }
                );
                $scope.confirmation.showPopup();
            };
        }
    })
    .factory("instrumentsExpandMenu", function (Instruments) {
        return initExpandMenu(function (instrument, $scope) {
            Instruments.get({id: instrument.id}, function (response) {
                instrument.details = response.details;
                var username = $scope.getLoggedUserName();
                var loggedUserIsOperator = false;
                angular.forEach(instrument.details.operators, function (operator) {
                    if (operator.email == username) {
                        loggedUserIsOperator = true;
                        return false;
                    }
                });
                instrument.showOperators = loggedUserIsOperator;

            });
        })
    });

function instrumentCreationRequestPopup() {
    $(".modal").modal("hide");

    $("#instrument-creation-request-message").dialog({
        title: "Instrument Creation Request Sent",
        draggable: false,
        dialogClass: "message-dialog",
        modal: true,
        resizable: false,
        width: 450,
        buttons: {
            OK: function () {
                $(this).dialog("close")
            }
        }
    });
}



