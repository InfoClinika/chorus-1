"use strict";

(function () {

    var bodyMouseUpListeners = [];

    angular.module("experiments-front")
        .directive("experimentWizardFileToPrepAssignmentStep", experimentWizardFileToPrepAssignmentStep)
        .filter("localeOrderBy", [function () {
            return function (array, sortPredicate, reverseOrder) {
                if (!Array.isArray(array)) {
                    return array;
                }
                if (!sortPredicate) {
                    return array;
                }

                function isString(value) {
                    return typeof value === "string";
                }

                function isNumber(value) {
                    return typeof value === "number";
                }

                function isBoolean(value) {
                    return typeof value === "boolean";
                }

                var arrayCopy = [];
                angular.forEach(array, function (item) {
                    arrayCopy.push(item);
                });

                arrayCopy.sort(function (a, b) {
                    var valueA = a[sortPredicate];
                    var valueB = b[sortPredicate];

                    if (isString(valueA)) {
                        return !reverseOrder ? valueA.localeCompare(valueB) : valueB.localeCompare(valueA);
                    }

                    if (isNumber(valueA) || isBoolean(valueA)) {
                        return !reverseOrder ? valueA - valueB : valueB - valueA;
                    }

                    return 0;
                });

                return arrayCopy;
            };
        }]);

    function experimentWizardFileToPrepAssignmentStep() {
        return {
            restrict: "E",
            templateUrl: "../pages/component/experiment-wizard-file-to-prep-assignment-step.html",
            replace: true,
            scope: {
                configuration: "="
            },
            controller: function ($scope) {

                $scope.settings = {initialized: false};
                $scope.$watch("configuration", setupAPIandActivate); // initialization after first binding

                function setupAPIandActivate() {
                    if (!$scope.settings.initialized && $scope.configuration) {
                        $scope.settings.initialized = false;
                        $scope.configuration.api = {
                            getSelected: getSelected,
                            validate: isStateValid,
                            events: {
                                setSelected: "experimentWizardPrepToBioSampleAssignmentStep.setSelected"
                            }
                        };
                        $scope.$on($scope.configuration.api.events.setSelected, onSetSelection);
                        activate();
                    }

                    function activate() {
                        $scope.vm = {
                            autoFill: {
                                mouseUpHandler: autoFillSamplesAndFractions,
                                removeSamplesAndFractions: removeSamplesAndFractions,
                                selected: {fractionNumber: null, sampleName: null, pairedEnd: null}
                            },
                            getValidationErrorMessage: getValidationErrorMessage,
                            is2dLc: true,
                            pairedEnd: true,
                            multiplexing: false,
                            files: [],
                            keyPressedInTable: enableArrowsUpDownSupport()
                        };
                        var bodyEl = $("body");
                        $(bodyMouseUpListeners).each(function (i, listener) { // clean listeners which have been binded earlier
                            bodyEl.unbind("mouseup", listener);
                        });
                        bodyEl.bind("mouseup", outerMouseUpHandler);
                        bodyMouseUpListeners.push(outerMouseUpHandler);
                        enableCopyPasteForCells();

                        function outerMouseUpHandler() {
                            if ($("#fileNameForAutoFill:visible").length === 1 &&
                                $("#fileNameForAutoFill").is(":focus")) {
                                autoFillSamplesAndFractions();
                            }

                        }

                        function autoFillSamplesAndFractions() { // mouse up handler


                            var selection = getSelection();
                            if (!selection) {
                                return;
                            }
                            if ($scope.vm.autoFill.selected.sampleName) {
                                if (($scope.vm.is2dLc || $scope.vm.pairedEnd) &&
                                    (isNumber(selection.selectedText) || selection.selectedText.length === 1)) {
                                    $scope.vm.autoFill.selected.fractionNumber = selection.selectedText;
                                    $scope.vm.autoFill.selected.fractionNumberRange = {
                                        start: selection.start,
                                        end: selection.end
                                    };
                                }
                            } else {
                                $scope.vm.autoFill.selected.sampleName = selection.selectedText;
                                $scope.vm.autoFill.selected.sampleNameRange = {
                                    start: selection.start,
                                    end: selection.end
                                };
                            }

                            if ($scope.vm.autoFill.selected.sampleName &&
                                ($scope.vm.autoFill.selected.fractionNumber || !$scope.vm.is2dLc &&
                                    !$scope.vm.pairedEnd)) {
                                // do autofilling
                                var autoFillFn = composeAutoFillFunction(
                                    selection.fileNameToProcess,
                                    $scope.vm.autoFill.selected
                                );
                                $($scope.vm.files).each(function (i, file) {
                                    var parseResult = autoFillFn(file.name);
                                    file.preparedSampleName = parseResult.preparedSampleName;
                                    if (parseResult.fractionNumber) {
                                        file.fractionNumber = parseResult.fractionNumber;
                                    }
                                    if (parseResult.pairedEnd) {
                                        file.pairedEnd = parseResult.pairedEnd;
                                    }
                                });
                                $scope.vm.autoFill.selected = {fractionNumber: null, sampleName: null, pairedEnd: null}; //null everything to let user retry

                            }

                            event.stopPropagation();

                            function composeAutoFillFunction(wholeFileName, autoFillSelection) {
                                //F20140305_TS_Ebert_Jan_Rep1_KGG_Light_DMSO_Medium_Lenalidomide1uM_Heavy_Len10uM_bRP_fxn1.raw
                                var sampleSettings = extractSettingsOfOccurrence(
                                    wholeFileName,
                                    autoFillSelection.sampleNameRange
                                );
                                var fractionSettings = null;
                                if ($scope.vm.is2dLc) {
                                    fractionSettings = extractSettingsOfOccurrence(
                                        wholeFileName,
                                        autoFillSelection.fractionNumberRange,
                                        true
                                    );
                                }
                                var pairedEndSettings = null;
                                if ($scope.vm.pairedEnd) {
                                    pairedEndSettings = extractSettingsOfOccurrence(
                                        wholeFileName,
                                        autoFillSelection.fractionNumberRange,
                                        true
                                    );
                                }
                                return autoFillFunction;

                                function autoFillFunction(fileName) {
                                    return {
                                        preparedSampleName: extractValueBasedOnSettings(fileName, sampleSettings),
                                        fractionNumber: extractValueBasedOnSettings(fileName, fractionSettings, true),
                                        pairedEnd: extractValueBasedOnSettings(fileName, pairedEndSettings, true)
                                    };
                                }


                                function extractSettingsOfOccurrence(fullText, ranges, keepOrderFromRight) {
                                    var separatorBefore = ranges.start === 0 ? null : fullText[ranges.start - 1];//_
                                    var separatorAfter = ranges.end === fullText.length ? null : fullText[ranges.end];//_
                                    var orderOfSeparatorBefore = null;
                                    //TCGA-AA-A00R-01A-22_W_VU_20121103_A0218_5G_R_FR01.raw, selected 01
                                    if (keepOrderFromRight) { // for fraction number look from end
                                        orderOfSeparatorBefore = separatorBefore ?
                                            fullText.substr(ranges.start).split(separatorBefore).length - 1 :
                                            0;//[01.raw]
                                    } else {
                                        orderOfSeparatorBefore = separatorBefore ?
                                            fullText.substr(0, ranges.start).split(separatorBefore).length - 1 :
                                            0;
                                    }
                                    var orderOfSeparatorAfter = separatorAfter ?
                                        fullText.substr(ranges.start, ranges.end - ranges.start)
                                            .split(separatorAfter).length - 1 :
                                        0;

                                    return {
                                        separatorBefore: separatorBefore,
                                        separatorAfter: separatorAfter,
                                        orderOfSeparatorBefore: orderOfSeparatorBefore,
                                        orderOfSeparatorAfter: orderOfSeparatorAfter,
                                        keepOrderFromRight: keepOrderFromRight
                                    };
                                }

                                function extractValueBasedOnSettings(fileName, settings, convertValueToNumber) {
                                    if (settings === null) {
                                        return null;
                                    }
                                    var beforeSeparators = settings.separatorBefore === null ?
                                        [fileName] :
                                        fileName.split(settings.separatorBefore);
                                    var orderOfSeparatorBefore = null;
                                    if (settings.keepOrderFromRight) {
                                        orderOfSeparatorBefore = beforeSeparators.length -
                                            settings.orderOfSeparatorBefore - 1;
                                    } else {
                                        orderOfSeparatorBefore = settings.orderOfSeparatorBefore;
                                    }
                                    if (beforeSeparators[orderOfSeparatorBefore]) {
                                        //TCGA-AA-A00R-01A-22_W_VU_20121103_A0218_5G_R_FR01.raw
                                        var valueAsArrayWithCorrectStart = [];
                                        var resultedValue;
                                        if (orderOfSeparatorBefore !== 0) {
                                            for (var beforeSepIndex = orderOfSeparatorBefore;
                                                 beforeSepIndex < beforeSeparators.length; beforeSepIndex++) {
                                                valueAsArrayWithCorrectStart.push(beforeSeparators[beforeSepIndex]);
                                            }
                                            resultedValue = valueAsArrayWithCorrectStart.join(settings.separatorBefore);
                                        } else {
                                            resultedValue = beforeSeparators[0];
                                        }


                                        var afterSeparators = settings.separatorAfter === null ?
                                            [resultedValue] :
                                            resultedValue.split(settings.separatorAfter);
                                        if (afterSeparators[settings.orderOfSeparatorAfter]) {
                                            var valueAsArrayWithCorrectEnd = [];
                                            if (settings.orderOfSeparatorAfter !== 0) {
                                                for (var afterSepIndex = 0;
                                                     afterSepIndex <= settings.orderOfSeparatorAfter; afterSepIndex++) {
                                                    valueAsArrayWithCorrectEnd.push(afterSeparators[afterSepIndex]);
                                                }
                                                resultedValue
                                                    = valueAsArrayWithCorrectEnd.join(settings.separatorAfter);
                                            } else {
                                                resultedValue = afterSeparators[0];
                                            }

                                        }
                                        if (convertValueToNumber) {
                                            if (!isNumber(resultedValue) && resultedValue.length === 1) {
                                                resultedValue = resultedValue.charCodeAt(0);
                                            }
                                        }
                                        return resultedValue;
                                    }
                                    return null;
                                }

                            }

                            function getSelection() {
                                var autoFillJInputEl = $("#fileNameForAutoFill");
                                var autoFillInputEl = autoFillJInputEl.get(0);
                                var selectionStart = autoFillInputEl.selectionStart;
                                var selectionEnd = autoFillInputEl.selectionEnd;
                                if (selectionEnd <= selectionStart) {
                                    return null;
                                }
                                var fileNameToProcess = autoFillJInputEl.val();
                                var selectedText = fileNameToProcess.substr(
                                    selectionStart,
                                    selectionEnd - selectionStart
                                );
                                autoFillJInputEl.blur();
                                autoFillInputEl.selectionStart = -1;
                                autoFillInputEl.selectionEnd = -1;
                                return {
                                    start: selectionStart,
                                    end: selectionEnd,
                                    fileNameToProcess: fileNameToProcess,
                                    selectedText: selectedText
                                };
                            }
                        }

                        function removeSamplesAndFractions() {
                            $scope.vm.autoFill.selected = {fractionNumber: null, sampleName: null, pairedEnd: null};
                            $($scope.vm.files).each(function (i, file) {
                                file.preparedSampleName = "";
                                if ($scope.vm.is2dLc || $scope.vm.pairedEnd) {
                                    file.fractionNumber = "";
                                }
                                if ($scope.vm.pairedEnd) {
                                    file.pairedEnd = "";
                                }
                            });
                        }

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
                                    if (event.keyCode === 37) {//left
                                        return; //TODO:2015-12-28:andrii.loboda: remove duplicated code
                                    } else if (event.keyCode === 39) {//right
                                        return;
                                    }
                                    var td = $(target).parent();
                                    var horizontalIndex = td.prevAll(tdSelector).length;
                                    if (event.keyCode === 38) {//up
                                        var upTR = td.parent().prev();
                                        if (upTR.length !== 0) {
                                            upTR.find(tdSelector)
                                                .eq(horizontalIndex)
                                                .find(DOM_ELEMENT.CONTENTEDITABLE)
                                                .focus();
                                        }
                                    } else if (event.keyCode === 40) {//down
                                        var downTR = td.parent().next();
                                        if (downTR.length !== 0) {
                                            downTR.find(tdSelector)
                                                .eq(horizontalIndex)
                                                .find(DOM_ELEMENT.CONTENTEDITABLE)
                                                .focus();
                                        }
                                    }

                                };
                            }
                        }

                        function getValidationErrorMessage() {
                            var result = validate();
                            if (!result) {
                                return "Files are not specified.";
                            }
                            if (!result.allFilesValid) {
                                return "Absent value(s) for: " + result.invalidFileName;
                            } else if (!result.allFractionsUniqueInPrep) {
                                return "Fractions should be unique per prep sample. Invalid for: " +
                                    result.invalidFileName;
                            } else if (!result.allPrepHaveSameFractions) {
                                return "Each Prep sample should have the same number of fractions. Invalid for: " +
                                    result.invalidFileName;
                            }

                            return "  ";
                        }

                        function enableCopyPasteForCells() {
                            /*** Copy-paste Support ***/

                            var experimentDesignCells = new TableModel(
                                getColumnsCount,
                                getRowsCount,
                                copyValue,
                                pasteValue,
                                getOrder
                            );
                            experimentDesignCells.startWatchingModifications($scope);

                            function getColumnsCount() {
                                var width = 1;
                                if ($scope.vm.is2dLc) {
                                    width += 1;
                                }
                                if ($scope.vm.pairedEnd) {
                                    width += 2;
                                }
                                return width;
                            }

                            function getRowsCount() {
                                return $scope.vm.files.length;
                            }

                            function copyValue(x, y) {
                                var fileItem = $scope.vm.files[y];
                                if (x === 0) {
                                    return fileItem.preparedSampleName || "";
                                } else if ($scope.vm.is2dLc) {
                                    return fileItem.fractionNumber || "";
                                }
                                return x === 1
                                    ? fileItem.pairedEnd
                                    : fileItem.fractionNumber;

                            }

                            function pasteValue(x, y, value) {
                                //todo[tymchenko]: think if we could refactor the copy-paste
                                var fileItem = $scope.vm.files[y];
                                if (x === 0) {
                                    fileItem.preparedSampleName = value;
                                } else if ($scope.vm.is2dLc) {
                                    fileItem.fractionNumber = value;
                                } else /*if ($scope.vm.pairedEnd)*/ if (x === 1) {
                                    fileItem.pairedEnd = value;
                                } else {
                                    fileItem.fractionNumber = value;
                                }
                            }

                            function getOrder() {
                                return $scope.sorting.reverse ? -1 : 1;
                            }

                            /*** End of Copy-Paste Support ***/
                        }

                    }

                    function getSelected() {
                        return $scope.vm.files;
                    }

                    function isStateValid() {
                        var result = validate();
                        if (result) {
                            return result.allFilesValid && result.allFractionsUniqueInPrep &&
                                result.allPrepHaveSameFractions;
                        }
                        return false;
                    }


                    function validate() {
                        if ($scope.vm.files.length === 0) {
                            return null;
                        }
                        var allFilesValid = true;
                        var allFractionsUniqueInPrep = true;
                        var prepToFractionMap = {};
                        var invalidFileName = {};
                        $($scope.vm.files).each(function (i, file) {
                            if (allFilesValid && allFractionsUniqueInPrep) {
                                if (!file.preparedSampleName || file.preparedSampleName.trim().length === 0) {
                                    allFilesValid = false;
                                    invalidFileName = file.name;
                                }
                                if ($scope.vm.is2dLc) {
                                    if (isNaN(parseInt(file.fractionNumber))) {
                                        allFilesValid = false;
                                        invalidFileName = file.name;
                                    } else {
                                        if (!prepToFractionMap[file.preparedSampleName.toString()]) {
                                            prepToFractionMap[file.preparedSampleName.toString()] = {};
                                        }
                                        if (!prepToFractionMap[file.preparedSampleName.toString()][+file.fractionNumber]) {
                                            prepToFractionMap[file.preparedSampleName.toString()][+file.fractionNumber]
                                                = file.name;
                                        } else {
                                            allFractionsUniqueInPrep = false;
                                            invalidFileName = file.name;
                                        }
                                    }
                                }
                                if ($scope.vm.pairedEnd) {
                                    if (file.pairedEnd !== "1" && file.pairedEnd !== "2") {
                                        allFilesValid = false;
                                        invalidFileName = file.name;
                                    }
                                }
                            }

                        });
                        var fractionsCountPerPrep = null;
                        var allPrepHaveSameFractions = true;
                        $.each(prepToFractionMap, function (preparedSampleName, fractionsMap) {
                            if (allPrepHaveSameFractions) {
                                var fractionsCount = 0;
                                var firstFileName = null;
                                $.each(fractionsMap, function (key, fileName) {
                                    fractionsCount++;
                                    if (!firstFileName) {
                                        firstFileName = fileName;
                                    }
                                });
                                if (fractionsCountPerPrep === null) {
                                    fractionsCountPerPrep = fractionsCount;
                                } else if (fractionsCountPerPrep !== fractionsCount) {
                                    allPrepHaveSameFractions = false;
                                    invalidFileName = firstFileName;
                                }
                            }
                        });

                        return {
                            invalidFileName: invalidFileName,
                            allFilesValid: allFilesValid,
                            allFractionsUniqueInPrep: allFractionsUniqueInPrep,
                            allPrepHaveSameFractions: allPrepHaveSameFractions
                        };
                    }

                    function onSetSelection(e, dataToSpecify) {
                        $scope.vm.is2dLc = dataToSpecify.is2dLc;
                        $scope.vm.pairedEnd = dataToSpecify.pairedEnd == 2;
                        $scope.vm.multiplexing = dataToSpecify.multiplexing;
                        $scope.vm.mixedSamplesCount = dataToSpecify.mixedSamplesCount;
                        $scope.vm.files = dataToSpecify.files;
                        $scope.vm.files.sort(function (f1, f2) {
                            return f1.name.localeCompare(f2.name);
                        });

                        if (!$scope.vm.is2dLc) {
                            autoFillPrepNamesIfEmptyWithFileNames($scope.vm.files);
                        }
                    }

                    function autoFillPrepNamesIfEmptyWithFileNames(files) {
                        files.forEach(function (file) {
                            if (!file.preparedSampleName || file.preparedSampleName.trim().length === 0) {
                                file.preparedSampleName = trimFileExtension(file.name);
                            }
                        });
                    }

                    function trimFileExtension(filename) {
                        var indexPoint = filename.indexOf(".");
                        return indexPoint !== -1 ? filename.substring(0, indexPoint) : filename;
                    }
                }
            }
        };
    }
})();
