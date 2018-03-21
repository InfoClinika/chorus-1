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
            controller: function ($scope, $timeout) {
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
                            addFactor: addFactor,
                            removeFactor: removeFactor,
                            addFactorBtnDisabled: addFactorBtnDisabled,
                            factor: {numeric: false},
                            factorId: 0,
                            samples: [],
                            factors: []
                        };
                        $scope.data = {experimentId: null};

                        $scope.vm.keyPressedInFactorTable = onFactorTableKeyPressed();

                        function changeSorting(sortField) {
                            if (sortField == "name") {
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

                        function removeFactor(factor) {
                            var indexOfRemovedFactor;
                            $scope.vm.factors = jQuery.grep($scope.vm.factors, function (elem, i) {
                                if (elem.id == factor.id) {
                                    indexOfRemovedFactor = i;
                                    return false;
                                } else {
                                    return true;
                                }
                            });
                            angular.forEach($scope.vm.samples, function (sample) {
                                delete sample.factorValues[factor.id];
                            });
                        }

                        /*** Copy-paste Support ***/

                        var experimentDesignCells = new TableModel(
                            function () {
                                var width = 0;
                                return width + $scope.vm.factors.length;
                            },
                            function () {
                                return $scope.vm.samples.length;
                            },
                            function (x, y) {
                                var fileItem = $scope.vm.samples[y];
                                var factor = $scope.vm.factors[x];
                                return fileItem.factorValues[factor.id] || "";
                            },
                            function (x, y, value) {
                                //todo[tymchenko]: think if we could refactor the copy-paste
                                var fileItem = $scope.vm.samples[y];
                                var factor = $scope.vm.factors[x];
                                fileItem.factorValues[factor.id] = value;

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
                        if (event.keyCode == 37) {//left
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
                        } else if (event.keyCode == 39) {//right
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

                //End of key pressed support


                function getSelected() {
                    if($scope.vm.factors.length === 0) {
                        addDefaultFactor();
                    }

                    return {samples: $scope.vm.samples, factors: $scope.vm.factors};
                }

                function addDefaultFactor() {
                    var factor = {
                        id:  1,
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
                    if ($scope.vm.factors.length == 0){
                        return true;
                    }
                    var allSamplesFilled = true;
                    $($scope.vm.samples).each(function(i, sample){
                        if (allSamplesFilled){
                            $($scope.vm.factors).each(function(i, factor){
                                if (!sample.factorValues[factor.id] || sample.factorValues[factor.id].trim().length == 0){
                                    allSamplesFilled = false;
                                }
                                if (factor.isNumeric && !isNumber(sample.factorValues[factor.id])){
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
                }
            }


        }
    }
})();
