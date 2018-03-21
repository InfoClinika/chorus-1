(function () {
    angular.module("experiments-front")
        .directive("experimentWizardSampleAssignmentStep", experimentWizardSampleAssignmentStep);

    const CHANNEL_1 = "CHANNEL_1";
    const CHANNEL_2 = "CHANNEL_2";
    const CHANNEL_3 = "CHANNEL_3";
    const CHANNEL_4 = "CHANNEL_4";
    const CHANNEL_5 = "CHANNEL_5";
    const CHANNEL_6 = "CHANNEL_6";
    const CHANNEL_7 = "CHANNEL_7";
    const CHANNEL_8 = "CHANNEL_8";
    const CHANNEL_9 = "CHANNEL_9";
    const CHANNEL_10 = "CHANNEL_10";

    function experimentWizardSampleAssignmentStep() {
        return {
            restrict: "E",
            templateUrl: "../pages/component/experiment-wizard-sample-assignment-step.html",
            replace: true,
            scope: {
                configuration: "="
            },
            controller: function ($scope, ExperimentSpecialLabelType) {

                var samplesToWeightsMapping = getSamplesToWeightsMapping();

                //TODO:2015-12-01:andrii.loboda: add sorting
                $scope.settings = {initialized: false};
                $scope.$watch("configuration", setupAPIandActivate); // initialization after first binding

                function setupAPIandActivate() {
                    if (!$scope.settings.initialized && $scope.configuration) {
                        $scope.settings.initialized = false;
                        $scope.configuration.api = {
                            getSelected: getSelected,
                            validate: validate,
                            events: {
                                setSelected: "experimentWizardSampleAssignmentStep.experimentWizardSetSelected"
                            }
                        };
                        $scope.$on($scope.configuration.api.events.setSelected, onSetSelection);
                        activate();
                    }
                    //reverse support
                    function activate() {
                        $scope.vm = {
                            changeSorting: changeSorting,
                            allSampleTypeNames: {LIGHT: "LIGHT", MEDIUM: "MEDIUM", HEAVY: "HEAVY"},
                            allSampleTypes: ["LIGHT", "MEDIUM", "HEAVY"],
                            channelSampleTypes: [
                                CHANNEL_1, CHANNEL_2, CHANNEL_3, CHANNEL_4, CHANNEL_5,
                                CHANNEL_6, CHANNEL_7, CHANNEL_8, CHANNEL_9, CHANNEL_10
                            ],
                            selectedSampleTypes: [],
                            is2dLc: true,
                            preparedSamples: [],
                            keyPressedInTable: enableArrowsUpDownSupport(),
                            getLabelForSampleType: getLabelForSampleType
                        };
                        enableCopyPasteForCells();

                        function enableArrowsUpDownSupport() {
                            //Key pressed support
                            var DOM_ELEMENT = {
                                CONTENTEDITABLE: "div"
                            };
                            return arrowsUpDownSupport();

                            function arrowsUpDownSupport() {
                                return function () {
                                    var e = window.event;
                                    var target = e.target || e.srcElement;
                                    var tdSelector = "td.annotation-value, td.factor-value";
                                    if (event.keyCode == 37) {//left
                                        return; //TODO:2015-12-28:andrii.loboda: remove duplicated code
                                    } else if (event.keyCode == 39) {//right
                                        return;
                                    } else {
                                        var td = $(target).parent();
                                        var horizontalIndex = td.prevAll(tdSelector).length;
                                        if (event.keyCode == 38) {//up
                                            var upTR = td.parent().prev();
                                            if (upTR.length != 0) {
                                                upTR.find(tdSelector).eq(horizontalIndex).find(DOM_ELEMENT.CONTENTEDITABLE).focus();
                                            }
                                        } else if (event.keyCode == 40) {//down
                                            var downTR = td.parent().next();
                                            if (downTR.length != 0) {
                                                downTR.find(tdSelector).eq(horizontalIndex).find(DOM_ELEMENT.CONTENTEDITABLE).focus();
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        function getLabelForSampleType(sampleType, typesCount) {
                            if(!$scope.vm.labelType) {
                                return;
                            }

                            var weight = sampleType;
                            if ($scope.vm.labelType === ExperimentSpecialLabelType.TMT || $scope.vm.labelType === ExperimentSpecialLabelType.iodoTMT) {
                                weight = samplesToWeightsMapping[sampleType].weights.tmt[typesCount];
                            } else if ($scope.vm.labelType === ExperimentSpecialLabelType.iTRAQ){
                                weight = samplesToWeightsMapping[sampleType].weights.itraq[typesCount];
                            }
                            return weight;
                        }

                        //End of key pressed support
                        function changeSorting(sortField) {
                            if (sortField == "name") {
                                $scope.vm.preparedSamples.reverse();
                            }
                        }

                        function enableCopyPasteForCells() {
                            /*** Copy-paste Support ***/

                            var experimentDesignCells = new TableModel(getColumnsCount, getRowsCount, copyValue, pasteValue);
                            experimentDesignCells.startWatchingModifications($scope);

                            function getColumnsCount() {
                                return $scope.vm.selectedSampleTypes.length;
                            }

                            function getRowsCount() {
                                return $scope.vm.preparedSamples.length;
                            }

                            function copyValue(x, y) {
                                var prepSample = $scope.vm.preparedSamples[y];
                                if ($scope.vm.specialLabelType){
                                    return prepSample.samples[asChannelSampleType(x)];
                                } else {
                                    return prepSample.samples[asSampleType(x)];
                                }
                            }

                            function pasteValue(x, y, value) {
                                //todo[tymchenko]: think if we could refactor the copy-paste
                                var prepSample = $scope.vm.preparedSamples[y];
                                if ($scope.vm.specialLabelType){
                                    prepSample.samples[asChannelSampleType(x)] = value;
                                } else {
                                    prepSample.samples[asSampleType(x)] = value;
                                }
                            }

                            function asSampleType(typeInNumericFormat) {
                                if (typeInNumericFormat === 0) {
                                    return $scope.vm.allSampleTypeNames.LIGHT
                                } else if (typeInNumericFormat == 1) {
                                    if ($scope.vm.selectedSampleTypes.length == 2) {
                                        return $scope.vm.allSampleTypeNames.HEAVY;
                                    } else {
                                        return $scope.vm.allSampleTypeNames.MEDIUM;
                                    }
                                } else if (typeInNumericFormat == 2) {
                                    return $scope.vm.allSampleTypeNames.HEAVY;
                                }
                            }
                            
                            function asChannelSampleType(index) {
                                return $scope.vm.channelSampleTypes[index];
                            }

                            /*** End of Copy-Paste Support ***/
                        }

                    }

                    function getSelected() {
                        var samplesMap = {};
                        $($scope.vm.preparedSamples).each(function (i, preparedSample) {

                            $($scope.vm.selectedSampleTypes).each(function (i, sampleType) {
                                var sampleName = preparedSample.samples[sampleType];
                                if (!samplesMap[sampleName + sampleType]) {
                                    samplesMap[sampleName + sampleType] = {
                                        name: sampleName,
                                        type: sampleType,
                                        preparedSamples: []
                                    };
                                }
                                samplesMap[sampleName + sampleType].preparedSamples.push(preparedSample.id);
                            });

                        });
                        var samplesList = [];
                        $.each(samplesMap, function (key, value) {
                            samplesList.push(value);
                        });
                        return samplesList;
                    }

                    function validate() {
                        if ($scope.vm.selectedSampleTypes.length == 0) {
                            return false;
                        }
                        var allPrepSamplesValid = true;
                        $($scope.vm.preparedSamples).each(function (i, preparedSample) {
                            if (allPrepSamplesValid) {
                                $($scope.vm.selectedSampleTypes).each(function (i, sampleType) {
                                    if (!preparedSample.samples[sampleType] || $.trim(preparedSample.samples[sampleType]).length == 0) { // if no samples at all
                                        allPrepSamplesValid = false;
                                    }
                                });
                            }

                        });
                        return allPrepSamplesValid;
                    }

                    function onSetSelection(e, dataToSpecify) {
                        $scope.vm.preparedSamples = dataToSpecify.preparedSamples;
                        $scope.vm.mixedSamplesCount = dataToSpecify.mixedSamplesCount;
                        $scope.vm.channelsCount = dataToSpecify.channelsCount;
                        $scope.vm.specialLabelType = $scope.vm.channelsCount > 0;
                        $scope.vm.labelType = dataToSpecify.labelType;

                        var selectedSampleTypes = [];
                        if ($scope.vm.specialLabelType) {
                            for (var i = 0; i < $scope.vm.channelsCount; i++) {
                                selectedSampleTypes[i] = "CHANNEL_" + (i + 1);
                            }
                        } else {
                            if (dataToSpecify.mixedSamplesCount == 0 || dataToSpecify.mixedSamplesCount == 1) {
                                selectedSampleTypes = [$scope.vm.allSampleTypes[0]]; // light sample
                                if (dataToSpecify.mixedSamplesCount == 0) {
                                    autoFillLightSamplesIfAbsent($scope.vm.preparedSamples);
                                }

                            } else if (dataToSpecify.mixedSamplesCount == 2) {
                                selectedSampleTypes = [$scope.vm.allSampleTypes[0], $scope.vm.allSampleTypes[2]]
                            } else {
                                selectedSampleTypes = $scope.vm.allSampleTypes.slice(); // copy whole array
                            }
                        }

                        $scope.vm.selectedSampleTypes = selectedSampleTypes;

                        function autoFillLightSamplesIfAbsent() {
                            $($scope.vm.preparedSamples).each(function (i, prepSample) {
                                if ($.trim(prepSample.samples[$scope.vm.allSampleTypes[0]]).length == 0) {
                                    prepSample.samples[$scope.vm.allSampleTypes[0]] = prepSample.name; // specify light sample to be the same as prepared sample
                                }

                            });
                        }

                    }
                }

                function getSamplesToWeightsMapping() {
                    var mapping = {
                    };

                    mapping[CHANNEL_1] = {
                        name: CHANNEL_1,
                        weights: {
                            tmt: {
                                2: "126.127726",
                                6: "126.127726",
                                10: "126.127725"
                            },
                            itraq: {
                                4: "114",
                                8: "113"
                            }
                        }
                    };

                    mapping[CHANNEL_2] = {
                        name: CHANNEL_2,
                        weights: {
                            tmt: {
                                2: "127.131081",
                                6: "127.124761",
                                10: "127.124760"
                            },
                            itraq: {
                                4: "115",
                                8: "114"
                            }
                        }
                    };

                    mapping[CHANNEL_3] = {
                        name: CHANNEL_3,
                        weights: {
                            tmt: {
                                6: "128.134436",
                                10: "127.131079"
                            },
                            itraq: {
                                4: "116",
                                8: "115"
                            }
                        }
                    };

                    mapping[CHANNEL_4] = {
                        name: CHANNEL_4,
                        weights: {
                            tmt: {
                                6: "129.131471",
                                10: "128.128114"
                            },
                            itraq: {
                                4: "117",
                                8: "116"
                            }
                        }
                    };

                    mapping[CHANNEL_5] = {
                        name: CHANNEL_5,
                        weights: {
                            tmt: {
                                6: "130.141145",
                                10: "128.134433"
                            },
                            itraq: {
                                8: "117"
                            }
                        }
                    };

                    mapping[CHANNEL_6] = {
                        name: CHANNEL_6,
                        weights: {
                            tmt: {
                                6: "131.138180",
                                10: "129.131468"
                            },
                            itraq: {
                                8: "118"
                            }
                        }
                    };

                    mapping[CHANNEL_7] = {
                        name: CHANNEL_7,
                        weights: {
                            tmt: {
                                10: "129.137787"
                            },
                            itraq: {
                                8: "119"
                            }
                        }
                    };

                    mapping[CHANNEL_8] = {
                        name: CHANNEL_8,
                        weights: {
                            tmt: {
                                10: "130.134822"
                            },
                            itraq: {
                                8: "120"
                            }
                        }
                    };

                    mapping[CHANNEL_9] = {
                        name: CHANNEL_9,
                        weights: {
                            tmt: {
                                10: "130.141141"
                            }
                        }
                    };

                    mapping[CHANNEL_10] = {
                        name: CHANNEL_10,
                        weights: {
                            tmt: {
                                10: "131.138176"
                            }
                        }
                    };

                    return mapping;
                }
            }
        }
    }

})
();
