"use strict";

(function () {
    var DEFAULT_FACTOR_NAME = "Sample";

    angular.module("experiments-front")
        .directive("experimentWizardSampleToFactorsStep", experimentWizardSampleToFactorsStep);

    function experimentWizardSampleToFactorsStep() {
        return {
            restrict: "E",
            templateUrl: "../pages/component/experiment-wizard-sample-to-factors-step.html",
            replace: true,
            scope: {
                configuration: "="
            },
            controller: function ($scope, $timeout, ExperimentDesignService) {
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
                                setSelected: "experimentWizardSampleToFactorsStep.setSelected"
                            }
                        };
                        $scope.$on($scope.configuration.api.events.setSelected, onSetSelection);
                        activate();
                    }

                    //reverse support
                    function activate() {
                        $scope.vm = {
                            changeSorting: changeSorting,
                            addBtnDisabled: addFactorBtnDisabled,
                            addFactor: addFactor,
                            removeFactor: removeFactor,
                            addAnnotation: addAnnotation,
                            removeAnnotation: removeAnnotation,
                            loadDesignCallback: loadDesignCallback,
                            loadDesignKeys: loadDesignKeys,
                            loadDesignKeyValues: loadDesignKeyValues,
                            isCellPinned: isCellPinned,
                            pinCells: pinCells,
                            factor: {numeric: false},
                            factorId: 0,
                            samples: [],
                            factors: [],
                            annotationId: 0,
                            annotations: []
                        };
                        $scope.data = {experimentId: null};

                        $scope.vm.keyPressedInFactorTable = onFactorTableKeyPressed();

                        function changeSorting(sortField) {
                            if (sortField === "name") {
                                $scope.vm.samples.reverse();
                            }
                        }

                        function addFactorBtnDisabled() {
                            if ($scope.vm.factor.isNumeric == "true") {
                                return !$scope.vm.factor.name || !$scope.vm.factor.units;
                            }
                            return !$scope.vm.factor.name;
                        }

                        function addFactor() {
                            $scope.vm.factor.id = $scope.vm.factorId++;
                            var factor = $scope.vm.factor;
                            factor.experimentId = $scope.data.experimentId;
                            $scope.vm.factors.push(factor);
                            $scope.vm.factor = {numeric: false};
                            angular.forEach($scope.vm.samples, function (sample) {
                                if (!sample.factorValues) {
                                    sample.factorValues = {};
                                }
                                sample.factorValues[factor.id] = "";
                            });
                        }

                        function addAnnotation() {
                            var annotation = $scope.vm.factor;
                            annotation.id = $scope.vm.annotationId++;
                            annotation.experimentId = $scope.data.experimentId;
                            if (!$scope.vm.annotations) {
                                $scope.vm.annotations = [];
                            }
                            $scope.vm.annotations.push(annotation);
                            $scope.vm.factor = {numeric: false};

                            angular.forEach($scope.vm.samples, function (sample) {
                                if (!sample.annotationValues) {
                                    sample.annotationValues = {};
                                }

                                sample.annotationValues[annotation.id] = "";
                            });
                        }

                        function removeFactor(factor) {
                            $scope.vm.factors = removeObjectFromArrayById($scope.vm.factors, factor.id);
                            angular.forEach($scope.vm.samples, function (sample) {
                                delete sample.factorValues[factor.id];
                            });
                        }

                        function removeAnnotation(annotation) {
                            $scope.vm.annotations = removeObjectFromArrayById($scope.vm.annotations, annotation.id);
                            angular.forEach($scope.vm.samples, function (sample) {
                                sample.annotationValues.splice(annotation.id, 1);
                            });
                        }

                        function removeObjectFromArrayById(items, itemId) {
                            return jQuery.grep(items, function (item) {
                                return item.id != itemId;
                            });
                        }

                        function loadDesignCallback(keyName, columns) {
                            var vm = $scope.vm;
                            var expDesign = ExperimentDesignService.wrap(vm.samples, vm.factors, vm.annotations);
                            var keyValuesToIdx = loadDesignKeyValuesToIdx(keyName);
                            var sId;
                            angular.forEach(columns, function (column) {
                                if (column.factor) {
                                    var fId = expDesign.addFactor(column.name);
                                    angular.forEach(column.values, function (value, key) {
                                        sId = keyValuesToIdx[key];
                                        vm.samples[sId].factorValues[fId] = value;
                                    });
                                } else {
                                    var aId = expDesign.addAnnotation(column.name);
                                    angular.forEach(column.values, function (value, key) {
                                        sId = keyValuesToIdx[key];
                                        vm.samples[sId].annotationValues[aId] = value;
                                    });
                                }
                            });
                            //restore ids
                            $scope.vm.factorId = $scope.vm.factors.length;
                            $scope.vm.annotationId = $scope.vm.annotations.length;
                        }

                        function loadDesignKeys() {
                            var keys = ["SAMPLE NAME"];
                            Array.prototype.push.apply(keys, $scope.vm.factors.map(function (item) {
                                return item.name;
                            }));
                            Array.prototype.push.apply(keys, $scope.vm.annotations.map(function (item) {
                                return item.name;
                            }));
                            return keys;
                        }

                        function loadDesignKeyValues(key) {
                            var id;
                            var samples = $scope.vm.samples;
                            var values = [];
                            if (key === "SAMPLE NAME") {
                                values = samples.map(function (sample) {
                                    return sample.name;
                                });
                            } else if ((id = findItemIdByName(key, $scope.vm.annotations)) !== null) {
                                values = samples.map(function (sample) {
                                    return sample.annotationValues[id];
                                });
                            } else if ((id = findItemIdByName(key, $scope.vm.factors)) !== null) {
                                values = samples.map(function (sample) {
                                    return sample.factorValues[id];
                                });
                            }
                            return values.filter(isValueNotEmpty);
                        }

                        function loadDesignKeyValuesToIdx(keyName) {
                            var id;
                            var keyValuesToIdx = {};
                            var samples = $scope.vm.samples;
                            if (keyName === "SAMPLE NAME") {
                                angular.forEach(samples, function (sample, idx) {
                                    if (isValueNotEmpty(sample.name)) {
                                        keyValuesToIdx[sample.name] = idx;
                                    }
                                });
                            } else if ((id = findItemIdByName(keyName, $scope.vm.annotations)) !== null) {
                                angular.forEach(samples, function (sample, idx) {
                                    if (isValueNotEmpty(sample.annotationValues[id])) {
                                        keyValuesToIdx[sample.annotationValues[id]] = idx;
                                    }
                                });
                            } else if ((id = findItemIdByName(keyName, $scope.vm.factors)) !== null) {
                                angular.forEach(samples, function (sample, idx) {
                                    if (isValueNotEmpty(sample.factorValues[id])) {
                                        keyValuesToIdx[sample.factorValues[id]] = idx;
                                    }
                                });
                            }
                            return keyValuesToIdx;
                        }

                        function findItemIdByName(name, items) {
                            var found = items.find(function (item) {
                                return item.name === name;
                            });
                            return found ? found.id : null;
                        }

                        function isValueNotEmpty(value) {
                            return angular.isDefined(value) && value !== null && value !== "";
                        }

                        function pinCells() {
                            return vm.factors.length + vm.annotations.length > 2;
                        }

                        function isCellPinned(cellNum) {
                            return cellNum < 3 && vm.factors.length + vm.annotations.length > 2;
                        }

                        /*** Copy-paste Support ***/

                        var experimentDesignCells = new TableModel(
                            function () {
                                var factors = 0;
                                try {
                                    factors = $scope.vm.factors.length;
                                } catch (e) {
                                    //ignore
                                }
                                var annotations = 0;
                                try {
                                    annotations = $scope.vm.annotations.length;
                                } catch (e) {
                                    //ignore
                                }
                                return factors + annotations;
                            },
                            function () {
                                try {
                                    return $scope.vm.samples.length;
                                } catch (e) {
                                    return 0;
                                }
                            },
                            function (x, y) {//get
                                var fileItem = $scope.vm.samples[y];
                                if (x < $scope.vm.factors.length) {
                                    var factor = $scope.vm.factors[x];
                                    var factorValue = fileItem.factorValues[factor.id];
                                    return factorValue || "";
                                }
                                var a = x - $scope.vm.factors.length;
                                var annotation = $scope.vm.annotations[a];
                                var annotationValue = fileItem.annotationValues[annotation.id];
                                return annotationValue || "";

                            },
                            function (x, y, value) {//set
                                //todo[tymchenko]: think if we could refactor the copy-paste
                                var fileItem = $scope.vm.samples[y];
                                if (x < $scope.vm.factors.length) {
                                    var factor = $scope.vm.factors[x];
                                    fileItem.factorValues[factor.id] = value;
                                } else {
                                    var a = x - $scope.vm.factors.length;
                                    var annotation = $scope.vm.annotations[a];
                                    fileItem.annotationValues[annotation.id] = value;
                                }
                            }
                        );

                        experimentDesignCells.startWatchingModifications($scope);

                        /*** End of Copy-Paste Support ***/
                    }

                }

                //Key pressed support
                var DOM_ELEMENT = {
                    CONTENTEDITABLE: "div"
                };

                function onFactorTableKeyPressed() {
                    return function () {
                        var e = window.event;
                        var target = e.target || e.srcElement;
                        var tdSelector = "td.annotation-value, td.factor-value";
                        if (event.keyCode === 37) {//left
                            return;
                            //TODO: Fix exceptions in $(target).caret() call
                            /* if ($(target).caret().start == 0) {
                             var prevTD = $(target).parent().prev();
                             if (prevTD.length != 0 && prevTD.find(DOM_ELEMENT.CONTENTEDITABLE).length != 0) {
                             var prevArea = prevTD.find(DOM_ELEMENT.CONTENTEDITABLE);
                             prevArea.focus();
                             setTimeout(function () {
                             setCursor(prevArea[0], prevArea.val().length);
                             }, 10);
                             }
                             }*/
                        } else if (event.keyCode === 39) {//right
                            return;
                            //TODO: Fix exceptions in $(target).caret() call
                            /* if ($(target).caret().end == $(target).val().length) {
                             var nextTD = $(target).parent().next();
                             if (nextTD.length != 0 && nextTD.find(DOM_ELEMENT.CONTENTEDITABLE).length != 0) {
                             var nextArea = nextTD.find(DOM_ELEMENT.CONTENTEDITABLE);
                             nextArea.focus();
                             setTimeout(function () {
                             setCursor(nextArea[0], 0);
                             }, 10);
                             }
                             }*/
                        }
                        var td = $(target).parent();
                        var horizontalIndex = td.prevAll(tdSelector).length;
                        if (event.keyCode === 38) {//up
                            var upTR = td.parent().prev();
                            if (upTR.length !== 0) {
                                upTR.find(tdSelector).eq(horizontalIndex).find(DOM_ELEMENT.CONTENTEDITABLE).focus();
                            }
                        } else if (event.keyCode === 40) {//down
                            var downTR = td.parent().next();
                            if (downTR.length !== 0) {
                                downTR.find(tdSelector).eq(horizontalIndex).find(DOM_ELEMENT.CONTENTEDITABLE).focus();
                            }
                        }

                    };
                }

                //End of key pressed support


                function getSelected() {
                    if ($scope.vm.factors.length === 0) {
                        addDefaultFactor();
                    }

                    return {
                        samples: $scope.vm.samples,
                        factors: $scope.vm.factors,
                        annotations: $scope.vm.annotations
                    };
                }

                function addDefaultFactor() {
                    var factor = {
                        id: 1,
                        name: DEFAULT_FACTOR_NAME,
                        numeric: false,
                        experimentId: $scope.data.experimentId
                    };

                    $scope.vm.factors.push(factor);

                    angular.forEach($scope.vm.samples, function (sample) {
                        if (!sample.factorValues) {
                            sample.factorValues = {};
                        }
                        sample.factorValues[factor.id] = sample.name;
                    });
                }


                function validate() {
                    if ($scope.vm.factors.length === 0) {
                        return true;
                    }
                    var allSamplesFilled = true;
                    $($scope.vm.samples).each(function (i, sample) {
                        if (allSamplesFilled) {
                            $($scope.vm.factors).each(function (i, factor) {
                                if (!sample.factorValues[factor.id] || sample.factorValues[factor.id].trim().length ===
                                    0) {
                                    allSamplesFilled = false;
                                }
                                if (factor.isNumeric && !isNumber(sample.factorValues[factor.id])) {
                                    allSamplesFilled = false;
                                }
                            });
                        }

                    });
                    return allSamplesFilled;
                }

                function onSetSelection(e, dataToSpecify) {
                    $scope.vm.factors = dataToSpecify.factors;
                    $scope.vm.factorId = $scope.vm.factors.length;
                    $scope.vm.samples = dataToSpecify.samples;
                    $scope.data.experimentId = dataToSpecify.experimentId;
                    $scope.vm.annotations = dataToSpecify.annotations;
                    $scope.vm.annotationId = dataToSpecify.annotations.length;
                }
            }


        };
    }
})();
