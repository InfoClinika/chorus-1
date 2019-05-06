"use strict";

(function () {

    angular.module("instrument-models").controller("instrument-model-create-edit", InstrumentModelCreateEditController);


    function InstrumentModelCreateEditController($q, $scope, $routeParams, $timeout, InstrumentTechnologyTypes,
                                                 InstrumentVendors, InstrumentModels, InstrumentModelValidation) {
        $scope.editMode = !!$routeParams.id;
        $scope.title = $scope.editMode && "Edit an Instrument Model" || "Create an Instrument Model";
        $scope.actionTitle = $scope.editMode && "Save" || "Create";
        $scope.technologyTypes = [];
        $scope.vendors = [];
        $scope.instrumentTypes = [];
        $scope.extensions = [];
        $scope.validationErrors = {
            technologyType: null,
            vendor: null,
            instrumentType: null,
            extensions: null
        };
        $scope.model = {
            id: null,
            name: "",
            technologyType: {},
            vendor: {},
            instrumentType: {},
            extensions: []
        };
        $scope.creation = {
            technologyType: "",
            vendor: "",
            instrumentType: ""
        };

        const DEFAULT_INSTRUMENT_TYPE = {id: null, name: "None"};
        const CREATE_TECHNOLOGY_SELECTOR = "#create-new-technology-type";
        const CREATE_VENDOR_SELECTOR = "#create-new-vendor";
        const CREATE_INSTRUMENT_TYPE_SELECTOR = "#create-new-instrument-type";
        var validate = InstrumentModelValidation;
        var performValidationOnAnyChange = false;
        var saveButtonPressed = false;

        $scope.addVendorExtension = addVendorExtension;
        $scope.removeVendorExtension = removeVendorExtension;
        $scope.showCreateTechnologyTypeDialog = showCreateTechnologyTypeDialog;
        $scope.hideCreateTechnologyTypeDialog = hideCreateTechnologyTypeDialog;
        $scope.createTechnologyType = createTechnologyType;
        $scope.showCreateVendorDialog = showCreateVendorDialog;
        $scope.hideCreateVendorDialog = hideCreateVendorDialog;
        $scope.createVendor = createVendor;
        $scope.showCreateInstrumentTypeDialog = showCreateInstrumentTypeDialog;
        $scope.hideCreateInstrumentTypeDialog = hideCreateInstrumentTypeDialog;
        $scope.createInstrumentType = createInstrumentType;
        $scope.save = save;

        init();

        function init() {

            loadTechnologyTypes();
            loadVendors();
            if ($scope.editMode) {
                loadModel($routeParams.id);
            }
            setupWatchers();

            function loadTechnologyTypes() {
                InstrumentTechnologyTypes.query(function (technologyTypes) {
                    $scope.technologyTypes = technologyTypes;
                    sortByName($scope.technologyTypes);
                    $scope.model.technologyType.name = firstNameOrNull($scope.technologyTypes);
                });
            }

            function loadVendors() {
                InstrumentVendors.all(function (vendors) {
                    $scope.vendors = vendors;
                    sortByName($scope.vendors);
                    $scope.model.vendor.name = firstNameOrNull($scope.vendors);
                });
            }

            function loadModel(id) {
                InstrumentModels.get({id: id}, function (response) {
                    $scope.model = response.value;
                    $scope.model.extensions.sort();
                    $scope.instrumentTypes = [$scope.model.instrumentType];
                });
            }

            function setupWatchers() {
                $scope.$watch("model.technologyType.name", function (newVal) {
                    if (!newVal) {
                        $scope.model.technologyType.id = null;
                    } else {
                        $scope.model.technologyType.id = $.grep($scope.technologyTypes, function (it) {
                            return it.name.trim().toLowerCase() == newVal.trim().toLowerCase();
                        })[0].id;
                    }
                    onTechnologyTypeChange();
                    performValidationIfNeeded();
                });

                $scope.$watch("model.vendor.name", function (newVal) {
                    if (!newVal) {
                        $scope.model.vendor.id = null;
                    } else {
                        $scope.model.vendor.id = $.grep($scope.vendors, function (it) {
                            return it.name.trim().toLowerCase() == newVal.trim().toLowerCase();
                        })[0].id;
                    }
                    onVendorChange();
                    performValidationIfNeeded();
                });

                $scope.$watch("model.instrumentType.name", function (newVal) {
                    if (!newVal) {
                        $scope.model.instrumentType.id = null;
                    } else {
                        $scope.model.instrumentType.id = $.grep($scope.instrumentTypes, function (it) {
                            return it.name.trim().toLowerCase() == newVal.trim().toLowerCase();
                        })[0].id;
                    }
                    performValidationIfNeeded();
                });

                $scope.$watch("model.name", function () {
                    performValidationIfNeeded();
                });

                $scope.$watch("model.extensions.length", function () {
                    performValidationIfNeeded();
                });
            }
        }

        /* $scope functions */

        function addVendorExtension(ext) {

            var extension = ext.split(" ")[0];
            var trimmed = extension.trim();

            if (!trimmed) {
                return;
            }

            var found = $.grep($scope.model.extensions, function (item) {
                return item.trim().toLowerCase() == trimmed.toLowerCase();
            })[0];

            if (found) {
                return;
            }

            $scope.model.extensions.push(trimmed);
            $scope.model.extensions.sort();
        }

        function removeVendorExtension(indexToDelete) {
            if ($scope.editMode) {
                return;
            }
            $scope.model.extensions.splice(indexToDelete, 1);
        }

        function onTechnologyTypeChange() {
            if ($scope.editMode) {
                return;
            }
            reFetchInstrumentTypes();
            reFetchVendorExtensions();

        }

        function onVendorChange() {
            if ($scope.editMode) {
                return;
            }
            reFetchInstrumentTypes();
            reFetchVendorExtensions();
        }

        function save() {

            if (saveButtonPressed) {
                return;
            }

            saveButtonPressed = true;

            validate($scope.model).then(function (errors) {

                $scope.validationErrors = errors;

                if ($.isEmptyObject($scope.validationErrors)) {
                    doSave();
                } else {
                    performValidationOnAnyChange = true;
                    saveButtonPressed = false;
                }
            });

            function doSave() {

                const model = {};
                model.id = $routeParams.id;
                model.vendor = $scope.model.vendor.name.trim();
                model.technologyType = $scope.model.technologyType.name.trim();
                model.instrumentType = $scope.model.instrumentType.name.trim();
                model.extensions = $.map($scope.model.extensions, function (ext) {
                    return ext.trim();
                });
                model.name = $scope.model.name.trim();

                if ($scope.editMode) {
                    InstrumentModels.update(model, onSuccess);
                } else {
                    InstrumentModels.save(model, onSuccess);
                }

                function onSuccess() {
                    saveButtonPressed = false;
                    setTimeout(function () {
                        $(".modal").modal("hide");
                    }, 0);
                }
            }
        }

        function showCreateTechnologyTypeDialog() {
            $scope.creation.technologyType = "";
            $(CREATE_TECHNOLOGY_SELECTOR).modal("show");
        }

        function hideCreateTechnologyTypeDialog() {
            $(CREATE_TECHNOLOGY_SELECTOR).modal("hide");
        }

        function createTechnologyType(name) {

            if (!name) {
                return;
            }
            var trimmedName = name.trim();
            if (!trimmedName) {
                return;
            }

            var existingOne = $.grep($scope.technologyTypes, function (it) {
                return it.name.trim().toLowerCase() == trimmedName.toLowerCase();
            })[0];

            if (existingOne) {
                $scope.model.technologyType.name = existingOne.name;
            } else {
                $scope.technologyTypes.push({id: null, name: trimmedName});
                sortByName($scope.technologyTypes);
                $timeout(function () {
                    $scope.model.technologyType.name = trimmedName;
                }, 0);
            }

            hideCreateTechnologyTypeDialog();
        }

        function showCreateVendorDialog() {
            $scope.creation.vendor = "";
            $(CREATE_VENDOR_SELECTOR).modal("show");
        }

        function hideCreateVendorDialog() {
            $(CREATE_VENDOR_SELECTOR).modal("hide");
        }

        function createVendor(name) {

            if (!name) {
                return;
            }
            var trimmedName = name.trim();
            if (!trimmedName) {
                return;
            }

            var existingOne = $.grep($scope.vendors, function (it) {
                return it.name.trim().toLowerCase() == trimmedName.toLowerCase();
            })[0];

            if (existingOne) {
                $scope.model.vendor.name = existingOne.name;
            } else {
                $scope.vendors.push({id: null, name: trimmedName});
                sortByName($scope.vendors);
                $timeout(function () {
                    $scope.model.vendor.name = trimmedName;
                }, 0);
            }

            hideCreateVendorDialog();
        }

        function showCreateInstrumentTypeDialog() {
            $scope.creation.instrumentType = "";
            $(CREATE_INSTRUMENT_TYPE_SELECTOR).modal("show");
        }

        function hideCreateInstrumentTypeDialog() {
            $(CREATE_INSTRUMENT_TYPE_SELECTOR).modal("hide");
        }

        function createInstrumentType(name) {

            if (!name) {
                return;
            }
            var trimmedName = name.trim();
            if (!trimmedName) {
                return;
            }

            var existingOne = $.grep($scope.instrumentTypes, function (it) {
                return it.name.trim().toLowerCase() == trimmedName.toLowerCase();
            })[0];

            if (existingOne) {
                $scope.model.instrumentType.name = existingOne.name;
            } else {
                $scope.instrumentTypes.push({id: null, name: trimmedName});
                sortByName($scope.instrumentTypes);
                $timeout(function () {
                    $scope.model.instrumentType.name = trimmedName;
                }, 0);
            }

            hideCreateInstrumentTypeDialog();
        }

        /* additional functions */

        function reFetchInstrumentTypes() {

            $scope.model.instrumentType = {};

            if (!$scope.model.technologyType.id || !$scope.model.vendor.id) {
                apply([]);
            } else {
                InstrumentModels.instrumentTypesByTechTypeAndVendor({
                        technologyType: $scope.model.technologyType.id,
                        vendor: $scope.model.vendor.id
                    }, function (response) {

                        apply(response.value);

                        if (response.value.length) {
                            apply($scope.instrumentTypes);
                        } else {
                            apply([DEFAULT_INSTRUMENT_TYPE]);
                        }

                    }
                );
            }

            function apply(types) {
                if (!types || types.length === 0) {
                    $scope.instrumentTypes = [DEFAULT_INSTRUMENT_TYPE];
                } else {
                    $scope.instrumentTypes = types;
                    sortByName($scope.instrumentTypes);
                }
                $scope.model.instrumentType.name = firstNameOrNull($scope.instrumentTypes);
            }
        }

        function reFetchVendorExtensions() {

            if (!$scope.model.technologyType.id || !$scope.model.vendor.id) {
                $scope.model.extensions = [];
                return;
            }

            InstrumentModels.extensionsByTechTypeAndVendor({
                    technologyType: $scope.model.technologyType.id,
                    vendor: $scope.model.vendor.id
                }, function (vendorExtensionsWrapper) {
                    if (vendorExtensionsWrapper.value || vendorExtensionsWrapper.value.length === 0) {
                        $scope.model.extensions = vendorExtensionsWrapper.value;
                        $scope.model.extensions.sort();
                    } else {
                        $scope.model.extensions = [];
                    }
                }
            );
        }

        function firstNameOrNull(items) {
            return items && items.length > 0 && items[0].name || null;
        }

        function performValidationIfNeeded() {
            if (performValidationOnAnyChange) {
                validate($scope.model, {skipUniqueNameValidation: true}).then(function (errors) {
                    $scope.validationErrors = errors;
                });
            }
        }

        function sortByName(items) {
            items.sort(function (a, b) {
                return a.name.localeCompare(b.name);
            });
        }
    }
})();
