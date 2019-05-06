"use strict";

(function () {

    angular.module("experiments-front")
        .directive("experimentWizardAnalysisStep", experimentWizardAnalysisStepDirective)
        .constant("ExperimentSpecialLabelType", {
            iTRAQ: "iTRAQ",
            TMT: "TMT",
            iodoTMT: "iodoTMT"
        });

    function experimentWizardAnalysisStepDirective() {
        return {
            restrict: "E",
            templateUrl: "../pages/component/experiment-wizard-analysis-step.html",
            replace: true,
            scope: {
                configuration: "="
            },
            controller: function ($scope,
                                  $timeout,
                                  ExperimentLabels,
                                  NGSExperiments,
                                  ExperimentLabelTypes,
                                  ExperimentTypes,
                                  ExperimentInstrumentModels,
                                  ExperimentInstruments,
                                  InstrumentStudyType,
                                  ExperimentInstrumentTypes,
                                  ExperimentFiles,
                                  ExperimentSpecialLabelType) {
                $scope.initialized = false;
                $scope.$watch("configuration", setupAPIandActivate); // initialization after first binding
                $scope.$watch("configuration.restriction", onExperimentRestrictionChange);
                $scope.$watch("vm.selected.instrumentType", onInstrumentTypeChange);
                $scope.$watch("vm.selected.instrumentModel", reloadInstruments);
                $scope.$watch("vm.selected.instrument", onInstrumentChange);
                $scope.$watch("configuration.restriction.species", onSpeciesChange);

                function setupAPIandActivate() {
                    if ($scope.configuration && !$scope.initialized) {
                        $scope.initialized = true;
                        $scope.configuration.api = {
                            getSelected: getSelected,
                            validate: validate,
                            getInstruments: getInstruments,
                            getInstrumentModels: getInstrumentModels,
                            events: {
                                SPECIFY_SELECTION: "experimentWizardAnalysisStep.specifySelection",
                                INITIALIZED: "analysisStepConfiguration.initialized"
                            }
                        };
                        activate();
                    }

                    function getSelected() {
                        var result = angular.copy($scope.vm.selected);
                        result.is2dLc = result.is2dLc == "1";//Yes
                        result.selectedLabelType = getSelectedLabelType().name;
                        result.groupSpecificParametersType = getSelectedGroupSpecificParametersType().name;
                        if (result.labeledYesNo == "2") { // no
                            result.mixedSampleType = 0;
                            result.selectedLabelType = undefined;
                            result.groupSpecificParametersType = undefined;
                        }
                        result.experimentLabels = {
                            heavyLabels: extractIds(result.heavyLabels),
                            mediumLabels: extractIds(result.mediumLabels),
                            lightLabels: extractIds(result.singleLabels),
                            specialLabels: extractIds(result.specialLabels)
                        };
                        result.experimentTypeDetails = $scope.vm.experimentTypes.find(function (type) {
                            return type.id == result.experimentType;
                        });

                        result.ngsRelatedInfo.multiplexing = !!isMultiplexing();
                        result.ngsRelatedInfo.pairedEnd = getPairedEnd();
                        result.ngsRelatedInfo.rnaSelection = getRnaSelection();
                        result.ngsRelatedInfo.xenograft = isXenograft();
                        result.ngsRelatedInfo.libraryPrep = getLibraryPrep();
                        result.ngsRelatedInfo.experimentType = getNgsExperimentType();
                        result.ngsRelatedInfo.experimentPrepMethod = getExperimentPrepMethod();
                        result.ngsRelatedInfo.ntExtractionMethod = getNtExtractionMethod();

                        return result;
                    }

                    function validate() {
                        return $scope.vm.filesExist;
                    }
                }

                function activate() {

                    requestAllExperimentData(function (data) {
                        var allSamplesMap = composeIdToSampleMap(data.allSamples);
                        var experimentTypeIdToItemMap = composeIdToExperimentType(data.experimentTypes);
                        var uiComponentsInitialized = {
                            experimentTypeCalled: false,
                            labelYesTypeCalled: false,
                            mixSampleTypeCalled: false
                        };
                        $scope.vm = {
                            addLabels: addLabels,
                            removeLabels: removeLabels,
                            selected: getSelectionByDefault(),
                            experimentTypes: data.experimentTypes,
                            changedExperimentType: changedExperimentType,
                            changedLabelYesType: changedLabelYesType,
                            changedGroupSpecificParametersType: changedGroupSpecificParametersType,
                            changedMixedSampleType: changedMixedSampleType,
                            getInstrumentSN: getInstrumentSN,
                            isCreateMode: isCreateMode,
                            isMs: isMs,
                            isMicroArray: isMicroArray,
                            isNGS: isNGS,
                            isMultiplexing: isMultiplexing,
                            allLabeledYesNoTypes: [
                                {id: 2, name: "No"},
                                {id: 1, name: "Yes"}
                            ],
                            allGroupSpecificParametersTypes: [
                                {id: 1, name: "Standard"},
                                {id: 2, name: "Reporter ion MS2"},
                                {id: 3, name: "Reporter ion MS3"}
                            ],
                            allDlcTypes: [
                                {id: 2, name: "No"},
                                {id: 1, name: "Yes"}
                            ],
                            labeledYesNoTypes: [],
                            labelYesTypes: data.labeledTypes,
                            mixedSampleTypes: [
                                {id: 1, name: 1},
                                {id: 2, name: 2},
                                {id: 3, name: 3}
                            ],
                            multiplexingTypes: [
                                {id: 1, name: "Yes"},
                                {id: 2, name: "No"}
                            ],
                            pairedEndTypes: [
                                {id: 1, name: "Single File"},
                                {id: 2, name: "Two Files"}
                            ],
                            pairedEndEnabled: isPairedEndEnabled(),
                            libraryPrepTypes: data.ngsLibraryPrepTypes,
                            experimentPrepMethods: [],
                            ntExtractionMethods: data.ntExtractionMethods,
                            ngsExperimentTypes: [],
                            specialLabelType: false,
                            mixedSampleTypesLabel: "Mixed Samples"
                        };

                        $scope.vm.specialLabelType = isSpecialLabelType();
                        $scope.vm.availableSamples = filterSamples(data.allSamples, $scope.vm.selected.labelYesType);
                        $scope.vm.mixedSampleTypes = getSampleTypes();
                        $scope.vm.mixedSampleTypesLabel = getSampleTypesLabel();

                        $scope.$on($scope.configuration.api.events.SPECIFY_SELECTION, specifySelectionEventHandler);

                        $scope.$watch("vm.selected.ngsRelatedInfo.experimentType", function (type, old) {
                            if (type > 0 && type != old) {

                                NGSExperiments.experimentPrepMethodsByType({typeId: type}, function (response) {

                                    var prepMethods = response.value || [];
                                    $scope.vm.experimentPrepMethods = prepMethods;

                                    var foundCurrentExpPrepMethod = prepMethods.find(function (method) {
                                        return method.id == $scope.vm.selected.ngsRelatedInfo.experimentPrepMethod;
                                    });

                                    if (!foundCurrentExpPrepMethod) {
                                        $scope.vm.selected.ngsRelatedInfo.experimentPrepMethod =
                                            prepMethods.length > 0 ?
                                                prepMethods[0].id :
                                                null;
                                    }
                                });
                            }
                        });

                        NGSExperiments.ngsExperimentTypes(function (response) {
                            $scope.vm.ngsExperimentTypes = response.value || [];
                        });

                        function getSampleTypesLabel() {
                            return $scope.vm.specialLabelType ? "Channels" : "Mixed Samples";
                        }

                        function selectSpecialLabels(filteredLabels, channelsCount) {
                            var filteredSpecialLabels = filterSpecialLabels(filteredLabels, channelsCount);

                            addSpecialLabels($scope.vm.selected.specialLabels, extractIds(filteredSpecialLabels));
                        }

                        function getInstrumentSN(instrument) {
                            return instrument.showSerial ? "S/N: " + instrument.serial : "";
                        }

                        function filterSpecialLabels(filteredLabels, channelsCount) {
                            var filteredSpecialLabels = [];

                            var index = 0;
                            for (var i = 0; i < filteredLabels.length; i++) {
                                var label = filteredLabels[i];
                                if (label.name.indexOf(channelsCount + "plex") != -1) {
                                    filteredSpecialLabels[index++] = label;
                                }
                            }

                            return filteredSpecialLabels;
                        }

                        function filterLabelTypes(labelTypes, groupSpecificParametersType) {
                            var filteredLabelTypes = [];

                            var index = 0;
                            for (var i = 0; i < labelTypes.length; i++) {
                                var labelType = labelTypes[i];
                                if (groupSpecificParametersType != "Standard") {
                                    if (isSpecialLabel(labelType.name)) {
                                        filteredLabelTypes[index++] = labelType;
                                    }
                                } else if (!isSpecialLabel(labelType.name)) {
                                    filteredLabelTypes[index++] = labelType;
                                }
                            }

                            return filteredLabelTypes;
                        }

                        function isSpecialLabel(labelName) {
                            if (labelName != undefined) {
                                return labelName === ExperimentSpecialLabelType.iTRAQ
                                    || labelName === ExperimentSpecialLabelType.TMT
                                    || labelName === ExperimentSpecialLabelType.iodoTMT;
                            }

                            return false;
                        }

                        function isSpecialLabelType() {
                            var selectedLabelType = getSelectedLabelType();
                            return isSpecialLabel(selectedLabelType.name);
                        }

                        function getSampleTypes() {
                            var selectedLabelType = getSelectedLabelType();

                            if (selectedLabelType != undefined) {
                                if (selectedLabelType.name === ExperimentSpecialLabelType.iTRAQ) {
                                    return [
                                        {id: 1, name: 4},
                                        {id: 2, name: 8}
                                    ];
                                } else if (selectedLabelType.name === ExperimentSpecialLabelType.TMT) {
                                    return [
                                        {id: 1, name: 2},
                                        {id: 2, name: 6},
                                        {id: 3, name: 10}
                                    ];
                                } else if (selectedLabelType.name === ExperimentSpecialLabelType.iodoTMT) {
                                    return [
                                        {id: 1, name: 6}
                                    ];
                                }
                            }
                            var sampleTypes = [];
                            for (var i = 1; i <= selectedLabelType.maxSamples; i++) {
                                sampleTypes.push({id: i, name: i});
                            }
                            return sampleTypes;
                        }

                        function getSelectedSampleType() {
                            var selectedSampleTypeId = $scope.vm.selected.mixedSampleType;

                            var mixedSampleTypes = $scope.vm.mixedSampleTypes;
                            for (var i = 0; i < mixedSampleTypes.length; i++) {
                                var mixedSampleType = mixedSampleTypes[i];
                                if (mixedSampleType.id == selectedSampleTypeId) {
                                    return mixedSampleType;
                                }
                            }
                        }

                        function getSampleType(name) {
                            var mixedSampleTypes = $scope.vm.mixedSampleTypes;

                            for (var i = 0; i < mixedSampleTypes.length; i++) {
                                var mixedSampleType = mixedSampleTypes[i];
                                if (mixedSampleType.name === name) {
                                    return mixedSampleType;
                                }
                            }
                        }

                        function notifyComponentInitialized() {
                            if (uiComponentsInitialized.mixSampleTypeCalled &&
                                uiComponentsInitialized.labelYesTypeCalled &&
                                uiComponentsInitialized.experimentTypeCalled) {
                                $scope.$emit($scope.configuration.api.events.INITIALIZED);
                            }
                        }

                        function changedLabelYesType() {
                            $scope.vm.availableSamples = filterSamples(
                                data.allSamples,
                                $scope.vm.selected.labelYesType
                            );

                            var mixedSampleTypes = $scope.vm.mixedSampleTypes;
                            var newMixedSampleTypes = getSampleTypes();

                            if (!angular.equals(newMixedSampleTypes, mixedSampleTypes)) {
                                $scope.vm.mixedSampleTypes = getSampleTypes();
                            }

                            if (!$scope.vm.firstInit &&
                                (isSpecialLabelType() || $scope.vm.specialLabelType != isSpecialLabelType())) {
                                $scope.vm.specialLabelType = isSpecialLabelType();
                                $scope.vm.selected.mixedSampleType = 1;
                                $scope.vm.mixedSampleTypesLabel = getSampleTypesLabel();

                                if (!$scope.vm.specialLabelType) {
                                    $scope.vm.selected.channelsCount = 0;
                                }
                            }

                            if (!uiComponentsInitialized.labelYesTypeCalled) {
                                uiComponentsInitialized.labelYesTypeCalled = true;
                                notifyComponentInitialized();
                            }

                            $scope.vm.firstInit = false;
                        }

                        function changedGroupSpecificParametersType() {
                            var selectedGroupSpecificParametersType = getSelectedGroupSpecificParametersType().name;
                            $scope.vm.labelYesTypes = filterLabelTypes(
                                data.labeledTypes,
                                selectedGroupSpecificParametersType
                            );

                            $scope.vm.selected.labelYesType = $scope.vm.labelYesTypes[0].id;
                        }

                        function changedMixedSampleType() {
                            resetSelection();

                            if ($scope.vm.specialLabelType) {
                                var channelsCount = getSelectedSampleType().name;

                                selectSpecialLabels($scope.vm.availableSamples, channelsCount);
                                $scope.vm.selected.channelsCount = channelsCount;
                            }

                            if (!uiComponentsInitialized.mixSampleTypeCalled) {
                                uiComponentsInitialized.mixSampleTypeCalled = true;
                                notifyComponentInitialized();
                            }

                            function resetSelection() {
                                $scope.vm.selected.mediumLabels = [];
                                $scope.vm.selected.singleLabels = [];
                                $scope.vm.selected.heavyLabels = [];
                                $scope.vm.selected.specialLabels = [];
                                $scope.vm.availableSamples = filterSamples(
                                    data.allSamples,
                                    $scope.vm.selected.labelYesType
                                );
                            }
                        }

                        function changedExperimentType() {
                            $scope.vm.labeledYesNoTypes = getLabeledYesNoTypes();
                            $scope.vm.dlcTypes = get2DLTypes();
                            $scope.vm.selected.labeledYesNo = $scope.vm.labeledYesNoTypes[0].id;
                            $scope.vm.selected.is2dLc = $scope.vm.dlcTypes[0].id;
                            if (!uiComponentsInitialized.experimentTypeCalled) {
                                uiComponentsInitialized.experimentTypeCalled = true;
                                notifyComponentInitialized();
                            }
                        }

                        function composeIdToSampleMap(samples) {
                            var allSamplesMap = {};
                            $.each(samples, function (i, samplesPerType) {
                                $(samplesPerType).each(function (i, item) {
                                    allSamplesMap[item.id] = item;
                                });
                            });
                            return allSamplesMap;
                        }

                        function composeIdToExperimentType(experimentTypes) {
                            var experimentTypeIdToItemMap = {};
                            $(experimentTypes).each(function (i, item) {
                                experimentTypeIdToItemMap[item.id] = item;
                            });
                            return experimentTypeIdToItemMap;
                        }

                        function filterSamples(allSamples, sampleType) {
                            if (!sampleType) {
                                return [];
                            }
                            var samplesOfType = allSamples[sampleType];
                            if (!samplesOfType) {
                                return [];
                            }
                            var alreadySelectedSamplesInLabels = getSelectedSamples();
                            var filtered = $.grep(samplesOfType, function (item) {
                                return $.inArray(item.id, alreadySelectedSamplesInLabels) == -1;
                            });
                            return filtered;
                        }

                        function getSelectedSamples() {
                            var resultMap = {};
                            $($scope.vm.selected.singleLabels).each(function (i, item) {
                                resultMap[item.id] = true;
                            });
                            $($scope.vm.selected.mediumLabels).each(function (i, item) {
                                resultMap[item.id] = true;
                            });
                            $($scope.vm.selected.heavyLabels).each(function (i, item) {
                                resultMap[item.id] = true;
                            });
                            $($scope.vm.selected.specialLabels).each(function (i, item) {
                                resultMap[item.id] = true;
                            });
                            var resultSet = [];
                            $.each(resultMap, function (key) {
                                resultSet.push(+key);
                            });
                            return resultSet;
                        }

                        function getSelectionByDefault() {
                            return {
                                experimentType: 1, //"Bottom Up Proteomics"
                                labeledYesNo: 2, // no
                                mixedSampleType: 1,
                                labelYesType: 1, //SILAC
                                mediumLabels: [],
                                heavyLabels: [],
                                singleLabels: [],
                                specialLabels: [], //labels for TMT, iodoTMT, iTRAQ Label Types
                                is2dLc: 2, // no
                                reporterMassTol: 0.01,
                                filterByPIFEnabled: false,
                                minReporterPIF: 0.75,
                                minBasePeakRatio: 0,
                                minReporterFraction: 0,
                                arrayType: 1,
                                chipDesign: 1,
                                ngsRelatedInfo: {
                                    multiplexing: 2,
                                    samplesCount: 5
                                }
                            };
                        }

                        function is2dLcAllowed() {
                            if (!experimentTypeIdToItemMap) {
                                return false;
                            }
                            var experimentType = experimentTypeIdToItemMap[$scope.vm.selected.experimentType];
                            if (!experimentType) {
                                return false;
                            }
                            return experimentType.allowed2DLC;
                        }

                        function isLabelsAllowed() {
                            if (!experimentTypeIdToItemMap) {
                                return false;
                            }
                            var experimentType = experimentTypeIdToItemMap[$scope.vm.selected.experimentType];
                            if (!experimentType) {
                                return false;
                            }
                            return experimentTypeIdToItemMap[$scope.vm.selected.experimentType].allowLabels;
                        }

                        function getLabeledYesNoTypes() {
                            if (!isLabelsAllowed()) {
                                return [$scope.vm.allLabeledYesNoTypes[0]];
                            }
                            return $scope.vm.allLabeledYesNoTypes;
                        }

                        function get2DLTypes() {
                            if (!is2dLcAllowed()) {
                                return [$scope.vm.allDlcTypes[0]];
                            }
                            return $scope.vm.allDlcTypes;
                        }

                        function addLabels(target, selectedItemIds) {
                            if (!selectedItemIds || selectedItemIds.length === 0) {
                                return;
                            }
                            var idsPresentInTarget = $.map(target, function (item) {
                                return item.id;
                            });
                            var aminoAcidsPresentInTarget = $.map(target, function (item) {
                                return allSamplesMap[+item.id].aminoAcid;
                            });
                            $.grep(selectedItemIds, function (selectedItemId) {
                                var selectedAcid = allSamplesMap[selectedItemId].aminoAcid;
                                if ($.inArray(selectedItemId, idsPresentInTarget) === -1 &&
                                    $.inArray(selectedAcid, aminoAcidsPresentInTarget) === -1) {

                                    target.push({
                                        id: selectedItemId,
                                        name: allSamplesMap[selectedItemId].name
                                    });
                                    idsPresentInTarget.push(idsPresentInTarget);
                                    aminoAcidsPresentInTarget.push(selectedAcid);
                                }
                            });
                            selectedItemIds.splice(0, selectedItemIds.length);
                            $scope.vm.availableSamples = filterSamples(
                                data.allSamples,
                                $scope.vm.selected.labelYesType
                            );
                        }

                        function addSpecialLabels(target, selectedItemIds) {
                            if (!selectedItemIds || selectedItemIds.length === 0) {
                                return;
                            }
                            var idsPresentInTarget = $.map(target, function (item) {
                                return item.id;
                            });
                            $.grep(selectedItemIds, function (selectedItemId) {
                                if ($.inArray(selectedItemId, idsPresentInTarget) === -1) {
                                    target.push({
                                        id: selectedItemId,
                                        name: allSamplesMap[selectedItemId].name
                                    });
                                    idsPresentInTarget.push(idsPresentInTarget);
                                }
                            });

                            selectedItemIds.splice(0, selectedItemIds.length);
                            $scope.vm.availableSamples = filterSamples(
                                data.allSamples,
                                $scope.vm.selected.labelYesType
                            );
                        }

                        function removeLabels(target, itemIdsToRemove) {
                            if (!itemIdsToRemove) {
                                return;
                            }
                            var idsPresentInTarget = $.map(target, function (item) {
                                return +item.id;
                            });
                            $.grep(itemIdsToRemove, function (labelToRemove) {
                                var index = $.inArray(+labelToRemove, idsPresentInTarget);
                                if (index != -1) {
                                    target.splice(index, 1);
                                    idsPresentInTarget.splice(index, 1);
                                }
                            });

                            $scope.vm.availableSamples = filterSamples(
                                data.allSamples,
                                $scope.vm.selected.labelYesType
                            );
                        }

                        function specifySelectionEventHandler(e, selection) {
                            if (selection.experimentType) {
                                $scope.vm.selected.experimentType = selection.experimentType;
                            }
                            $scope.vm.selected.is2dLc = selection.is2dLc == false ? 2 : 1; // transform to yes or no
                            $scope.vm.selected.ngsRelatedInfo = selection.ngsRelatedInfo;
                            $scope.vm.selected.ngsRelatedInfo.multiplexing =
                                selection.ngsRelatedInfo.multiplexing ? 1 : 2;
                            $scope.vm.selected.ngsRelatedInfo.xenograft = selection.ngsRelatedInfo.xenograft ? 1 : 2;
                            var mixedSamples = selection.mixedSamplesCount ? selection.mixedSamplesCount : 0; //no

                            $scope.originalExperimentCopy = selection.originalExperimentCopy;
                            $scope.vm.selected.instrumentModel = selection.instrumentModel;
                            $scope.vm.selected.instrumentType = selection.instrumentType;
                            $scope.vm.selected.instrument = selection.instrument;

                            if ($scope.configuration.restriction) {
                                $scope.configuration.restriction.species = selection.info.specie;
                            }

                            if (selection.labels) {
                                if (mixedSamples > 0) {
                                    $scope.vm.selected.labeledYesNo = 1; //yes
                                    $scope.vm.selected.mixedSampleType = mixedSamples;
                                    $scope.vm.firstInit = true;

                                    if (selection.channelsCount > 0) {
                                        var groupSpecificParametersType = getGroupSpecificParametersTypeByName(selection.groupSpecificParametersType);
                                        $scope.vm.selected.groupSpecificParametersType = groupSpecificParametersType.id;

                                        $scope.vm.selected.channelsCount = selection.channelsCount;
                                        $scope.vm.selected.selectedLabelType = selection.labelType;
                                        $scope.vm.selected.reporterMassTol = selection.reporterMassTol;
                                        $scope.vm.selected.filterByPIFEnabled = selection.filterByPIFEnabled;
                                        $scope.vm.selected.minReporterPIF = selection.minReporterPIF;
                                        $scope.vm.selected.minBasePeakRatio = selection.minBasePeakRatio;
                                        $scope.vm.selected.minReporterFraction = selection.minReporterFraction;

                                        $scope.vm.labelYesTypes =
                                            filterLabelTypes(data.labeledTypes, groupSpecificParametersType);
                                        $scope.vm.selected.labelYesType = getLabelTypeByName(selection.labelType).id;

                                        $scope.vm.mixedSampleTypes = getSampleTypes();
                                        $scope.vm.specialLabelType = isSpecialLabelType();
                                        $scope.vm.mixedSampleTypesLabel = getSampleTypesLabel();

                                        $scope.vm.selected.mixedSampleType = getSampleType(selection.channelsCount).id;

                                    }

                                    if (selection.labels.heavyLabels) {
                                        $scope.vm.addLabels(
                                            $scope.vm.selected.heavyLabels,
                                            selection.labels.heavyLabels
                                        );
                                    }
                                    if (selection.labels.mediumLabels) {
                                        $scope.vm.addLabels(
                                            $scope.vm.selected.mediumLabels,
                                            selection.labels.mediumLabels
                                        );
                                    }
                                    if (selection.labels.lightLabels) {
                                        $scope.vm.addLabels(
                                            $scope.vm.selected.singleLabels,
                                            selection.labels.lightLabels
                                        );
                                    }
                                    if (selection.labels.specialLabels) {
                                        $scope.vm.addLabels(
                                            $scope.vm.selected.specialLabels,
                                            selection.labels.specialLabels
                                        );
                                    }
                                }

                            }

                            $scope.vm.pairedEndEnabled = isPairedEndEnabled();
                        }
                    });

                    function requestAllExperimentData(resolveFn) {

                        new Promise(queryAllData).then(createLabelsMapAndTypes);

                        function createLabelsMapAndTypes(data) {
                            var labelsMap = {};
                            $(data.allLabels).each(function (i, label) {
                                if (!labelsMap[label.type]) {
                                    labelsMap[label.type] = [];
                                }
                                labelsMap[label.type].push(label);
                            });
                            var labeledTypes = [];
                            $(data.allLabelTypes).each(function separateLabeledAndNotLabeledTypes(i, type) {
                                labeledTypes.push(type);
                            });
                            return resolveFn({
                                allSamples: labelsMap,
                                labeledTypes: labeledTypes,
                                experimentTypes: data.experimentTypes,
                                ngsLibraryPrepTypes: data.ngsLibraryPrepTypes,
                                ntExtractionMethods: data.ntExtractionMethods
                            });
                        }

                        function queryAllData(resolve) {

                            var allLabels = null;
                            var allLabelTypes = null;
                            var experimentTypes = null;
                            var ngsLibraryPrepTypes = null;
                            var ntExtractionMethods = null;

                            ExperimentLabels.query(function (data) {
                                allLabels = data;
                                resolveIfAllDataObtained();
                            });
                            ExperimentLabelTypes.query(function (data) {
                                allLabelTypes = data;
                                resolveIfAllDataObtained();
                            });
                            ExperimentTypes.query(function (types) {
                                experimentTypes = types;
                                resolveIfAllDataObtained();
                            });
                            NGSExperiments.libraryPrepTypes(function (response) {
                                ngsLibraryPrepTypes = response.value || [];
                                resolveIfAllDataObtained();
                            });
                            NGSExperiments.ntExtractionMethods(function (response) {
                                ntExtractionMethods = response.value || [];
                                resolveIfAllDataObtained();
                            });

                            function resolveIfAllDataObtained() {
                                if (allLabels != null &&
                                    allLabelTypes != null &&
                                    experimentTypes != null &&
                                    ngsLibraryPrepTypes != null &&
                                    ntExtractionMethods != null) {
                                    resolve({
                                        allLabels: allLabels,
                                        allLabelTypes: allLabelTypes,
                                        experimentTypes: experimentTypes,
                                        ngsLibraryPrepTypes: ngsLibraryPrepTypes,
                                        ntExtractionMethods: ntExtractionMethods
                                    });
                                }
                            }
                        }
                    }
                }

                function getInstrumentModels() {
                    if (!$scope.vm || !$scope.vm.instrumentModels) {
                        return [];
                    }
                    return $scope.vm.instrumentModels;
                }

                function getInstruments() {
                    return $scope.vm.instruments;
                }


                function reloadInstruments(instrumentModel, oldModel) {
                    if (!$scope.configuration || !instrumentModel || instrumentModel === oldModel) {
                        return;
                    }
                    if (isNumber(instrumentModel)) {
                        ExperimentInstruments.query({instrumentModel: instrumentModel}, function (instruments) {
                            $scope.vm.instruments = instruments;
                            $scope.vm.instrumentsByLab = getInstrumentsByLab($scope.configuration.restriction.lab);
                            //preserve the initial instrument if the same model has been chosen
                            if ($scope.originalExperimentCopy &&
                                instrumentModel == $scope.originalExperimentCopy.restriction.instrumentModel) {
                                var instrument = $scope.originalExperimentCopy.restriction.instrument;
                                $scope.vm.selected.instrument = instrument != null ? instrument : -1;
                            } else {
                                $scope.vm.selected.instrument = getDefaultOptionValue($scope.vm.instrumentsByLab);
                            }
                        });

                    } else {
                        $scope.vm.instruments = [];
                    }
                }

                function onInstrumentChange(instrument, oldInstrument) {
                    if (!$scope.vm || !instrument || instrument === oldInstrument) {
                        return;
                    }

                    checkIfFilesExist(instrument);
                }

                function onSpeciesChange(specie, oldSpecie) {

                    if (!$scope.vm || !specie || specie === oldSpecie) {
                        return;
                    }

                    var instrument = $scope.vm.selected.instrument;
                    if (instrument) {
                        checkIfFilesExist(instrument);
                    }
                }

                function onInstrumentTypeChange(newInstrumentType, oldInstrumentType) {
                    if (!$scope.vm) {
                        return;
                    }

                    if (newInstrumentType && newInstrumentType != oldInstrumentType) {
                        var restriction = angular.copy($scope.configuration.restriction);
                        restriction.instrumentType = newInstrumentType;
                        reloadInstrumentModels(restriction);
                    } else if (!newInstrumentType) {
                        $scope.vm.selected.instrumentModel = null;
                    }
                }

                function onExperimentRestrictionChange(restriction) {
                    if (!$scope.configuration || !restriction) {
                        return;
                    }

                    if (isMicroArray()) {
                        ExperimentInstrumentTypes.query(restriction, function (types) {
                            $scope.vm.instrumentTypes = types;
                            $scope.vm.selected.instrumentType =
                                getFirstOrWithIdEqualTo(types, $scope.vm.selected.instrumentType);
                        });
                    } else {
                        reloadInstrumentModels(restriction);
                    }
                }

                function checkIfFilesExist(instrument) {

                    var species = $scope.configuration.restriction.species || -1;
                    var model = $scope.vm.selected.instrumentModel || -1;
                    var instrumentId = instrument || -1;
                    var labId = $scope.configuration.restriction.lab || -1;

                    var requestData = {
                        instrument: instrumentId,
                        model: model,
                        species: species,
                        lab: labId
                    };

                    ExperimentFiles.exist(requestData, function (response) {
                        $scope.vm.filesExist = response.value;
                    });
                }

                function reloadInstrumentModels(restriction) {
                    if (!restriction || !$scope.vm) {
                        return;
                    }

                    var requestData = angular.copy(restriction);
                    if (requestData.lab == -1) {
                        delete requestData.lab;
                    }

                    ExperimentInstrumentModels.query(requestData, function (models) {
                        $scope.vm.instrumentModels = models;
                        $scope.vm.isInstrumentsPresents = $scope.vm.instrumentModels.length > 0;
                        $scope.vm.selected.instrumentModel = getFirstOrWithIdEqualTo(
                            models,
                            $scope.vm.selected.instrumentModel
                        );
                        $scope.vm.instrumentsByLab = getInstrumentsByLab($scope.configuration.restriction.lab);
                        $scope.vm.selected.instrument = getFirstOrWithIdEqualTo(
                            $scope.vm.instrumentsByLab,
                            $scope.vm.selected.instrument
                        );
                    });
                }

                function getInstrumentsByLab(lab) {
                    if (!$scope.vm || !$scope.vm.instruments) {
                        return [];
                    }

                    if (!lab || lab < 0) {
                        checkNamesInInstruments();
                        return $scope.vm.instruments;
                    }

                    return $.grep($scope.vm.instruments, function (item) {
                        item.showSerial = false;
                        return item.lab == lab;
                    });
                }

                function checkNamesInInstruments() {

                    var instruments = $scope.vm.instruments;
                    for (var i = 0; i < instruments.length - 1; i++) {
                        if (instruments[i].name.toUpperCase() === instruments[i + 1].name.toUpperCase()) {
                            instruments[i].showSerial = true;
                            instruments[i + 1].showSerial = true;
                        }
                    }
                }


                function getSelectedGroupSpecificParametersType() {
                    var selectedGroupSpecificParameterTypeId = $scope.vm.selected.groupSpecificParametersType;

                    return getGroupSpecificParametersTypeById(selectedGroupSpecificParameterTypeId);
                }

                function getGroupSpecificParametersTypeById(id) {
                    var allGroupSpecificParametersTypes = $scope.vm.allGroupSpecificParametersTypes;

                    for (var i = 0; i < allGroupSpecificParametersTypes.length; i++) {
                        var groupSpecificParameterType = allGroupSpecificParametersTypes[i];
                        if (groupSpecificParameterType.id == id) {
                            return groupSpecificParameterType;
                        }
                    }
                }

                function getGroupSpecificParametersTypeByName(name) {
                    var allGroupSpecificParametersTypes = $scope.vm.allGroupSpecificParametersTypes;

                    for (var i = 0; i < allGroupSpecificParametersTypes.length; i++) {
                        var groupSpecificParameterType = allGroupSpecificParametersTypes[i];
                        if (groupSpecificParameterType.name === name) {
                            return groupSpecificParameterType;
                        }
                    }
                }

                function getSelectedLabelType() {
                    var selectedLabelTypeId = $scope.vm.selected.labelYesType;

                    return getLabelTypeById(selectedLabelTypeId);
                }

                function getLabelTypeByName(name) {
                    var allLabelTypes = $scope.vm.labelYesTypes;

                    for (var i = 0; i < allLabelTypes.length; i++) {
                        var labelType = allLabelTypes[i];
                        if (labelType.name === name) {
                            return labelType;
                        }
                    }
                }

                function getLabelTypeById(id) {
                    var allLabelTypes = $scope.vm.labelYesTypes;

                    for (var i = 0; i < allLabelTypes.length; i++) {
                        var labelType = allLabelTypes[i];
                        if (labelType.id == id) {
                            return labelType;
                        }
                    }
                }

                function extractIds(items) {
                    return $.map(items, function (item) {
                        return item.id;
                    });
                }

                function isMs() {
                    return techTypeIs(InstrumentStudyType.MS);
                }

                function isMicroArray() {
                    return techTypeIs(InstrumentStudyType.MA);
                }

                function isNGS() {
                    return techTypeIs(InstrumentStudyType.NGS);
                }

                function techTypeIs(type) {
                    if ($scope.configuration.restriction) {
                        return $scope.configuration.restriction.technologyTypeValue == type;
                    }
                }

                function isCreateMode() {
                    if ($scope.configuration) {
                        return $scope.configuration.createMode;
                    }
                }

                function isMultiplexing() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.multiplexing == 1;
                    }
                }

                function getPairedEnd() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.pairedEnd;
                    }
                }

                function isPairedEndEnabled() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.pairedEnd >= 1;
                    }
                }

                $scope.enablePairedEnd = function (enabled) {
                    if (enabled) {
                        $scope.vm.selected.ngsRelatedInfo.pairedEnd = 1;
                        jQuery("#pairedEnd").select2("open");
                    } else {
                        $scope.vm.selected.ngsRelatedInfo.pairedEnd = null;
                    }
                };

                function getRnaSelection() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.rnaSelection;
                    }
                }

                function isXenograft() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.xenograft == 1;
                    }
                }

                function getLibraryPrep() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.libraryPrep;
                    }
                }

                function getNgsExperimentType() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.experimentType;
                    }
                }

                function getExperimentPrepMethod() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.experimentPrepMethod;
                    }
                }

                function getNtExtractionMethod() {
                    if ($scope.vm) {
                        return $scope.vm.selected.ngsRelatedInfo.ntExtractionMethod;
                    }
                }

            }
        };
    }

})();
