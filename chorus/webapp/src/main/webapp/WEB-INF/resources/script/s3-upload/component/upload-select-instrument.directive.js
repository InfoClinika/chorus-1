"use strict";

(function () {

    angular.module("s3UploadModule")

        .directive("uploadSelectInstrument", function () {
            return {
                restrict: "E",
                scope: {
                    instrument: "=",
                    specie: "="
                },
                controller: function ($scope, $rootScope,
                                      ExperimentSpecies,
                                      InstrumentModels,
                                      InstrumentTechnologyTypes,
                                      InstrumentVendors,
                                      OperatedInstruments) {
                    var ALL_FILES_MASK = "*.*";
                    var DEFAULT_INSTRUMENT_NAME = "Default";
                    var DEFAULT_INSTRUMENT_MODEL_NAME = "Default";
                    var NOT_SELECTED = null;

                    $scope.vm = {
                        step: 1,
                        selected: {
                            lab: NOT_SELECTED,
                            techType: NOT_SELECTED,
                            vendor: NOT_SELECTED,
                            instrument: NOT_SELECTED,
                            specie: NOT_SELECTED,
                            masks: []
                        },
                        labs: [],
                        techTypes: [],
                        vendors: [],
                        species: [],
                        instruments: [],
                        instrumentModels: [],
                        filteredInstruments: [],
                        importResult: {},
                        addFileMask: addFileMask,
                        showLabSelector: showLabSelector,
                        showNoDefaultModelError: showNoDefaultModelError,
                        showInstrumentSelector: showInstrumentSelector
                    };

                    initController();

                    function initController() {

                        setupWatchers();
                        loadData();

                        function loadData() {
                            $scope.vm.labs = $rootScope.laboratories;
                            $scope.vm.selected.lab = getDefaultOptionValue($scope.vm.labs);

                            InstrumentTechnologyTypes.query(function (techTypes) {
                                $scope.vm.techTypes = techTypes;
                                $scope.vm.selected.techType = getDefaultOptionValue($scope.vm.techTypes);
                            });

                            ExperimentSpecies.query(function (items) {
                                $scope.vm.species = items;
                            });

                            ExperimentSpecies.defaultSpecie(function (defaultSpecie) {
                                $scope.vm.selected.specie = defaultSpecie.id;
                            });

                            getInstruments();
                        }

                        function setupWatchers() {

                            function findItem(id, collection) {
                                return collection.filter(function (item) {
                                    return item.id == id;
                                })[0];
                            }

                            $scope.$watch("vm.selected.instrument", function (id, prevId) {
                                if (!!id && id != prevId) {
                                    $scope.instrument = findItem(id, $scope.vm.instruments);
                                }
                            });
                            $scope.$watch("vm.selected.specie", function (id, prevId) {
                                if (!!id && id !== prevId) {
                                    $scope.specie = findItem(id, $scope.vm.species);
                                }
                            });

                            $scope.$watch("vm.selected.lab", onSelectedLabChanged);
                            $scope.$watch("vm.selected.techType", onSelectedTechTypeChanged);
                            $scope.$watch("vm.selected.vendor", onSelectedVendorChanged);
                            $scope.$watch("vm.selected.instrument", onSelectedInstrumentChanged);

                            function onSelectedInstrumentChanged(instrumentId, previousId) {

                                if (!instrumentId || instrumentId == previousId) {
                                    return;
                                }

                                var instrument = $.grep($scope.vm.instruments, function (item) {
                                    return item.id == instrumentId;
                                })[0];
                                var masks = $scope.vm.selected.masks;
                                masks.splice(0, masks.length);
                                instrument.vendor.fileUploadExtensions.forEach(function (ext) {
                                    addFileMask(prepareMask(ext.name));
                                    for (var additionalExt in ext.additionalExtensions) {
                                        if (ext.additionalExtensions.hasOwnProperty(additionalExt)) {
                                            addFileMask(prepareMask(additionalExt));
                                        }
                                    }
                                });

                                function prepareMask(extension) {
                                    return extension ? "*" + extension : ALL_FILES_MASK;
                                }
                            }

                            function onSelectedVendorChanged(vendorId, previousId) {
                                if (!vendorId || vendorId == previousId || !$scope.vm.selected.techType) {
                                    return;
                                }

                                updateInstrumentModels();
                            }

                            function onSelectedTechTypeChanged(techTypeId, previousId) {
                                if (!techTypeId || techTypeId == previousId) {
                                    return;
                                }

                                updateVendors(techTypeId);
                            }

                            function updateVendors(technologyType) {
                                InstrumentVendors.byTechnologyType({techType: technologyType}, function (vendors) {
                                    $scope.vm.vendors = vendors;
                                    $scope.vm.selected.vendor = getDefaultOptionValue(vendors);
                                });
                            }

                            function onSelectedLabChanged(labId, previousId) {
                                if (!labId || labId == previousId) {
                                    return;
                                }
                                updateAvailableInstruments();
                            }

                            function updateInstrumentModels() {
                                if (!$scope.vm.selected.vendor || !$scope.vm.selected.techType) {
                                    $scope.vm.instrumentModels = [];
                                    return;
                                }
                                InstrumentModels.getByTechnologyTypeAndVendor({
                                    vendor: $scope.vm.selected.vendor,
                                    technologyType: $scope.vm.selected.techType
                                }, function (modelsResponse) {
                                    $scope.vm.instrumentModels = modelsResponse.value;
                                    updateAvailableInstruments();
                                });
                            }

                            function updateAvailableInstruments() {

                                if (!$scope.vm.selected.techType || !$scope.vm.selected.vendor ||
                                    !$scope.vm.selected.lab) {
                                    $scope.vm.filteredInstruments = [];
                                } else {
                                    $scope.vm.filteredInstruments = $.grep($scope.vm.instruments, function (it) {
                                        //noinspection JSUnresolvedVariable
                                        return it.lab == $scope.vm.selected.lab
                                            && it.vendor.id == $scope.vm.selected.vendor
                                            && it.vendor.studyTypeItem.id == $scope.vm.selected.techType;
                                    });
                                }

                                $scope.vm.selected.instrument =
                                    $scope.vm.filteredInstruments.length > 0 &&
                                    getDefaultOptionValue($scope.vm.filteredInstruments) || NOT_SELECTED;
                            }
                        }
                    }

                    function showLabSelector() {
                        return $scope.vm.labs.length > 1;
                    }

                    function showNoDefaultModelError() {
                        return $scope.vm.selected.lab
                            && $scope.vm.selected.techType
                            && $scope.vm.selected.vendor
                            && $scope.vm.filteredInstruments.length == 0
                            && !hasDefaultModel();
                    }

                    function hasDefaultModel() {
                        return $.grep($scope.vm.instrumentModels, function (it) {
                            return it.name == DEFAULT_INSTRUMENT_MODEL_NAME;
                        }).length > 0;
                    }

                    function showInstrumentSelector() {
                        return $scope.vm.filteredInstruments.length > 1
                            || $scope.vm.filteredInstruments.length == 1 &&
                            $scope.vm.filteredInstruments[0].name != DEFAULT_INSTRUMENT_NAME;
                    }

                    function getInstruments(fn) {
                        OperatedInstruments.query(function (items) {
                            $scope.vm.instruments = items;
                            if (fn) {
                                fn();
                            }
                        });
                    }

                    function addFileMask(value) {
                        if (!value || !value.trim() ||
                            $scope.vm.selected.masks.indexOf(value) >= 0) {
                            return;
                        }
                        var mask = value.trim();
                        $scope.vm.selected.masks.push(mask);
                        $scope.vm.selected.masks.sort();
                    }
                },
                templateUrl: "../script/s3-upload/component/upload-select-instrument.template.html",
                replace: true
            };

        });

})();
