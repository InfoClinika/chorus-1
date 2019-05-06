"use strict";

(function () {

    angular
        .module(
            "dashboard-common-directives",
            ["modals", "security-back", "front-end", "experiments-back"]
        )
        .directive("advancedFiltering", advancedFiltering)
        .directive("resizableTableColumns", resizableTableColumns)
        .directive("nonClickable", function nonClickable() {
            return {
                restrict: "A",
                link: function (scope, elem, attrs) {
                    var disabled = true;

                    scope.$watch(attrs.nonClickable, function (value) {
                        disabled = !!value;
                    });

                    elem.on("click", function (e) {
                        if (isDisabled()) {
                            e.stopPropagation();
                        }
                    });

                    function isDisabled() {
                        return disabled;
                    }

                }
            };
        })
        .directive("sorting", function () {
            return function ($scope, element, attrs) {

                var state = "none";

                var icons = {
                    "asc": "<i class=\"icon icon-chevron-up\">",
                    "desc": "<i class=\"icon icon-chevron-down\">",
                    "none": ""
                };

                if (attrs.sortByDefault) {
                    setTimeout(function () {
                        applyState(attrs.sortByDefault);
                    }, 0);
                }

                $(element).addClass("sortable");

                function applyState(newState) {
                    state = newState;

                    $(element).find("i").remove();
                    $(element).prepend(icons[state]);
                    if (state === "none") {
                        $(element).removeClass("active");
                    } else {
                        $(element).addClass("active");
                    }

                    if ($scope.sortingElement != element) {
                        if ($scope.sortingElement) {
                            $($scope.sortingElement).trigger("remove-sorting");
                        }
                        $scope.sortingElement = element;
                    }

                    if (state !== "none") {
                        $scope.sorting = {
                            field: attrs.sorting,
                            reverse: state === "desc"
                        };
                    }

                    $scope.$apply();
                }

                element.bind("click", function () {
                    applyState(state === "asc" ? "desc" : "asc");
                });

                element.bind("remove-sorting", function () {
                    applyState("none");
                });
            };
        })
        .directive("chorusStartDatepicker", function () {
            return {
                restrict: "A",
                require: "ngModel",
                link: function (scope, element, attrs, ngModelCtrl) {
                    var startFromToday = true;
                    if (attrs.chorusStartDatepickerFromToday === "false") {
                        startFromToday = false;
                    }
                    var endDate;
                    scope.$watch(attrs.chorusStartDatepicker, function (value) {
                        endDate = value;
                        if (endDate) {
                            endDate = $.datepicker.parseDate("mm/dd/yy", endDate);
                        }
                        element.datepicker("destroy");
                        $(function () {
                            element.datepicker({
                                dateFormat: "mm/dd/yy",
                                minDate: startFromToday ? new Date() : null,
                                maxDate: endDate,
                                onSelect: function (date) {
                                    scope.$apply(function () {
                                        ngModelCtrl.$setViewValue(date);
                                    });
                                }
                            });
                        });
                    });
                }
            };
        })
        .directive("chorusEndDatepicker", function () {
            return {
                restrict: "A",
                require: "ngModel",
                link: function (scope, element, attrs, ngModelCtrl) {
                    var startFromToday = true;
                    if (attrs.chorusStartDatepickerFromToday == "false") {
                        startFromToday = false;
                    }
                    var startDate;
                    scope.$watch(attrs.chorusEndDatepicker, function (value) {
                        startDate = value;
                        if (startDate) {
                            startDate = $.datepicker.parseDate("mm/dd/yy", startDate);
                        } else if (startFromToday) {
                            startDate = new Date();
                        }
                        element.datepicker("destroy");
                        $(function () {
                            element.datepicker({
                                dateFormat: "mm/dd/yy",
                                minDate: startDate,
                                onSelect: function (date) {
                                    scope.$apply(function () {
                                        ngModelCtrl.$setViewValue(date);
                                    });
                                }
                            });
                        });
                    });

                    var watchModel = attrs.ngModel;
                    scope.$watch(watchModel, function (newValue) {
                        if (newValue && element.hasClass("hasDatepicker")) {
                            element.datepicker("setDate", newValue);
                            $("#ui-datepicker-div").hide(); // Fix datepicker shows under dialog window.
                        }
                    });
                }
            };
        })
        .factory("PaginationPropertiesSettingService", paginationPropertiesSetter)
        .factory("filterCommonFunctions", filterCommonFunctions)
        .factory("ResizableTableColumnsCache", ResizableTableColumnsCache)
        .factory("dashboardCommonDirectivesConstants", dashboardCommonDirectivesConstants);

    function dashboardCommonDirectivesConstants() {
        return {
            VIEW_DATA_CUBE_RESULTS: "VIEW_DATA_CUBE_RESULTS",
            HDF5_DATA_CUBE_EXPORT: "HDF5_DATA_CUBE_EXPORT"
        };
    }

    /* Directive allows user to make advanced filtering on list of items(experiments, searches, files, projects). It composes filtering query which can be executed on client or server
     configuration model structure:
     configuration: {
     fields:
     [
     {prop: "name", title:"", type: "string"}
     ]
     }
     directive output is composedFilter model. Its structure:
     composedFilter:{
     conjunction: true,
     predicates:[
     {prop:"name", predicate: "equals", "value": "Vasya"}
     ]
     }
     * */
    function advancedFiltering(applyPaging, filterCommonFunctions) {
        return {
            restrict: "E",
            templateUrl: "../pages/component/advanced-filtering.html",
            replace: true,
            scope: {
                configuration: "=",
                composedFilter: "="
            },
            controller: function ($scope) {
                $scope.tempComposedFilter = {};

                function removeTimeFromDate(date) {
                    date.setMinutes(0);
                    date.setHours(0);
                    date.setMilliseconds(0);
                    date.setSeconds(0);
                    return date;
                }

                var operatorsByType = {
                    "string": [
                        {
                            title: "equals",
                            prop: "EQUAL",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return itemToFilter[prop].toLowerCase() == value.toLowerCase();
                            }
                        },
                        {
                            title: "doesn't equal",
                            prop: "NOT_EQUAL",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return itemToFilter[prop].toLowerCase() != value.toLowerCase();
                            }
                        },
                        {
                            title: "begins with",
                            prop: "BEGINS_WITH",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return itemToFilter[prop].toLowerCase().indexOf(value.toLowerCase()) == 0;
                            }
                        },
                        {
                            title: "ends with",
                            prop: "ENDS_WITH",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                var lowerCased = value.toLowerCase();
                                return lowerCased.indexOf(itemToFilter[prop].toLowerCase()) == value.length -
                                    lowerCased.length;
                            }
                        },
                        {
                            title: "contains",
                            prop: "CONTAINS",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return itemToFilter[prop].toLowerCase().indexOf(value.toLowerCase()) != -1;
                            }
                        },
                        {
                            title: "doesn't contain",
                            prop: "NOT_CONTAINS",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return itemToFilter[prop].toLowerCase().indexOf(value.toLowerCase()) == -1;
                            }
                        },
                        {
                            title: "is empty",
                            prop: "IS_EMPTY",
                            requireValue: false,
                            applyToItem: function (itemToFilter, prop, value) {
                                return value.toLowerCase().trim().length == 0;
                            }
                        },
                        {
                            title: "isn't empty",
                            prop: "IS_NOT_EMPTY",
                            requireValue: false,
                            applyToItem: function (itemToFilter, prop, value) {
                                return value.toLowerCase().trim().length != 0;
                            }
                        },
                        {
                            title: "is in",
                            prop: "IS_IN",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                var values = value.split("\n");
                                return $.inArray(itemToFilter[prop], values) != -1;
                            }
                        },
                        {
                            title: "is not in",
                            prop: "IS_NOT_IN",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                var values = value.split("\n");
                                return $.inArray(itemToFilter[prop], values) == -1;
                            }
                        }
                    ],
                    "number": [
                        {
                            title: "equals",
                            prop: "EQUAL",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return Number(itemToFilter[prop]) == Number(value);
                            }
                        },
                        {
                            title: "doesn't equal",
                            prop: "NOT_EQUAL",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return Number(itemToFilter[prop]) != Number(value);
                            }
                        },
                        {
                            title: "greater than",
                            prop: "GREATER_THAN",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return Number(itemToFilter[prop]) > Number(value);
                            }
                        },
                        {
                            title: "less than",
                            prop: "LESS_THAN",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return Number(itemToFilter[prop]) < Number(value);
                            }
                        },
                        {
                            title: "is in",
                            prop: "IS_IN",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                var numbersInStr = value.split("\n");
                                var numbers = [];
                                $(numbersInStr).each(function (i, item) {
                                    numbers[i] = Number(item);
                                });
                                return $.inArray(Number(itemToFilter[prop]), numbers) != -1;
                            }
                        },
                        {
                            title: "is not in",
                            prop: "IS_NOT_IN",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                var numbersInStr = value.split("\n");
                                var numbers = [];
                                $(numbersInStr).each(function (i, item) {
                                    numbers[i] = Number(item);
                                });
                                return $.inArray(Number(itemToFilter[prop]), numbers) === -1;
                            }
                        }
                    ],
                    "boolean": [
                        {
                            title: "true",
                            prop: "TRUE",
                            requireValue: false,
                            applyToItem: function (itemToFilter, prop) {
                                if (typeof itemToFilter[prop] === "boolean") {
                                    return itemToFilter[prop] === 1 || itemToFilter[prop] === true;
                                }
                                var lowercased = itemToFilter[prop].toLowerCase();
                                return lowercased.indexOf("true") !== -1 || lowercased.indexOf("yes") !== -1;
                            }
                        },
                        {
                            title: "false",
                            prop: "FALSE",
                            requireValue: false,
                            applyToItem: function (itemToFilter, prop) {
                                if (typeof itemToFilter[prop] === "boolean") {
                                    return !(itemToFilter[prop] === 1 || itemToFilter[prop] === true);
                                }
                                var lowercased = itemToFilter[prop].toLowerCase();
                                return !(lowercased.indexOf("true") !== -1 || lowercased.indexOf("yes") !== -1);
                            }
                        }
                    ],
                    "date": [
                        {
                            title: "is on",
                            prop: "IS_ON",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() == removeTimeFromDate(
                                    new Date(value)).getTime();
                            }
                        },
                        {
                            title: "is after",
                            prop: "IS_AFTER",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() > removeTimeFromDate(
                                    new Date(value)).getTime();
                            }
                        },
                        {
                            title: "is on or after",
                            prop: "IS_ON_AND_AFTER",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() >= removeTimeFromDate(
                                    new Date(value)).getTime();
                            }
                        },
                        {
                            title: "is on or before",
                            prop: "IS_ON_OR_BEFORE",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() <= removeTimeFromDate(
                                    new Date(value)).getTime();
                            }
                        },
                        {
                            title: "is before",
                            prop: "IS_BEFORE",
                            requireValue: true,
                            applyToItem: function (itemToFilter, prop, value) {
                                return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() < removeTimeFromDate(
                                    new Date(value)).getTime();
                            }
                        },
                        {
                            title: "is today",
                            prop: "IS_TODAY",
                            requireValue: false,
                            applyToItem: function (itemToFilter, prop) {
                                return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() == removeTimeFromDate(
                                    new Date()).getTime();
                            }
                        },
                        {
                            title: "is yesterday",
                            prop: "IS_YESTERDAY",
                            requireValue: false,
                            applyToItem: function (itemToFilter, prop) {
                                var yesterday = removeTimeFromDate(new Date());
                                yesterday.setDate(yesterday.getDate() - 1);
                                return removeTimeFromDate(new Date(itemToFilter[prop]))
                                    .getTime() == yesterday.getTime();
                            }
                        },
                        {
                            title: "is in this week",
                            prop: "IS_IN_WEEK",
                            requireValue: false,
                            applyToItem: function (itemToFilter, prop) {
                                var inWeek = removeTimeFromDate(new Date());
                                inWeek.setDate(inWeek.getDate() - 7);
                                return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() >= inWeek.getTime();
                            }
                        }
                    ]
                };
                filterCommonFunctions($scope, operatorsByType);

                function cleanTemporaryFilter(restoreFromPersisted) {
                    $scope.tempComposedFilter = angular.copy($scope.composedFilter);
                    if (!$scope.tempComposedFilter.predicates || !restoreFromPersisted) {
                        $scope.tempComposedFilter.predicates = [];
                    }
                    if ($scope.tempComposedFilter.conjunction === undefined) {
                        $scope.tempComposedFilter.conjunction = "true";
                    } else {
                    }
                    $scope.tempComposedFilter.conjunction =
                        $scope.composedFilter.conjunction === true ||
                            $scope.composedFilter.conjunction === "true" ?
                            "true" :
                            "false";
                    if ($scope.tempComposedFilter.predicates.length === 0) {
                        $scope.addEmptyRow();
                    }
                    if (!$scope.tempComposedFilter.applyToItem && !$scope.configuration.pageable) {
                        $scope.tempComposedFilter.applyToItem = function (itemToFilter, predicateItem) {
                            var result = undefined;
                            angular.forEach(
                                $scope.getOperationsListForFieldType($scope.getOperatorsType(predicateItem.prop)),
                                function (operatorItem) {
                                    if (operatorItem.prop == predicateItem.operator) {
                                        result =
                                            operatorItem.applyToItem(
                                                itemToFilter,
                                                predicateItem.prop,
                                                predicateItem.value
                                            );
                                    }
                                }
                            );
                            return result;
                        };
                    }
                }

                $scope.resetChanges = function () {
                    cleanTemporaryFilter();
                };

                $scope.cancelChanges = function () {
                    $scope.tempComposedFilter = null;
                    $scope.confirmation.hidePopup();
                };
                var modalDialog = $("#advancedFilteringDialog");
                $scope.displayAdvancedFilteringDialog = function () {
                    cleanTemporaryFilter(true);

                    $scope.confirmation = new Confirmation("#advancedFilteringDialog", null,
                        {
                            success: function () {
                                $scope.composedFilter = angular.copy($scope.tempComposedFilter);
                                $scope.composedFilter.conjunction =
                                    $scope.composedFilter.conjunction === true ||
                                    $scope.composedFilter.conjunction === "true";
                                if ($scope.getValidationErrorMessage()) {
                                    $scope.composedFilter.predicates = [];
                                }
                                if ($scope.configuration.pageable) {
                                    applyPaging($scope);
                                }

                            }
                        }
                    );
                    $scope.confirmation.showPopup();
                    modalDialog.parent().append($(".modal-backdrop.in"));
                    setTimeout(function () {
                        $("#advancedFilteringDialog .filter-input:first").focus();
                    }, 10);
                };
            }
        };
    }

    function paginationPropertiesSetter() {
        return {
            setPaginationProperties: setPaginationPropsFn
        };

        function setPaginationPropsFn(scope, pagedCollection) {
            scope.total = pagedCollection.itemsCount;
            scope.pageNumber = pagedCollection.pageNumber;
            scope.pageSize = pagedCollection.pageSize;
            scope.page.listItemsCount = pagedCollection.itemsCount;
        }
    }

    function filterCommonFunctions() {
        return function ($scope, operatorsByType) {
            function isValidDate(date) {
                var matches = /^(\d{2})[-\/](\d{2})[-\/](\d{4})$/.exec(date);
                if (matches == null) {
                    return false;
                }
                var d = matches[2];
                var m = matches[1] - 1;
                var y = matches[3];
                var composedDate = new Date(y, m, d);
                return composedDate.getDate() == d &&
                    composedDate.getMonth() == m &&
                    composedDate.getFullYear() == y;
            }

            var validatorsByType = {
                "string": function (val) {
                    if (!val && val.trim().length == 0) {
                        return "String value should not be empty.";
                    }
                },
                "number": function (val, predicate) {
                    if (!val) {
                        return "Number value should not be empty.";
                    }
                    if (predicate === "IS_IN" || predicate === "IS_NOT_IN" || predicate === "is in" ||
                        predicate === "is not in") {
                        var bAllNumbers = true;
                        $(val.split("\n")).each(function (i, item) {
                            if (!isNumber(item)) {
                                bAllNumbers = false;
                            }
                        });
                        if (!bAllNumbers) {
                            return "Not all numbers valid. Please separate values with Enter.";
                        }
                    } else if (!isNumber(val)) {
                        return "Not a valid number.";
                    }

                },
                "boolean": function (val) {
                },
                "date": function (val) {
                    if (!val) {
                        return "Date value should not be empty.";
                    }
                    if (!isValidDate(val)) {
                        return "Date should be in format: MM/DD/YYYY.";
                    }
                }
            };
            var allOperators = [];
            angular.forEach(operatorsByType, function (item) {
                allOperators = $.merge(allOperators, item);
            });
            $scope.getOperationsListForFieldType = function (type) {
                if (type === "java.lang.String" || type === "string") {
                    return operatorsByType.string;
                } else if (type === "java.lang.Date" || type === "date") {
                    return operatorsByType.date;
                } else if (type === "java.lang.Boolean" || type === "boolean") {
                    return operatorsByType.boolean;
                }
                    return operatorsByType.number;

            };
            $scope.getOperatorsType = function (fieldProp) {
                var operatorsType = undefined;
                angular.forEach($scope.configuration.fields, function (fieldItem) {
                    if (fieldItem.prop == fieldProp) {
                        operatorsType = fieldItem.type;
                    }
                });
                if (operatorsType === undefined) {
                    throw "can't find field:" + fieldProp;
                }
                return operatorsType;
            };
            $scope.getOperatorsByField = function (fieldProp) {
                if (!$scope.configuration) {
                    return;
                }
                var operatorsType = $scope.getOperatorsType(fieldProp);
                return $scope.getOperationsListForFieldType(operatorsType);
            };
            $scope.getClassForPredicateValue = function (predicateProp) {
                if ($scope.isValueRequiredForPredicate(predicateProp)) {
                    if (predicateProp == "IS_IN" || predicateProp == "IS_NOT_IN"
                        || predicateProp == "is not in" || predicateProp == "is in") {
                        return "displayTextArea";
                    }
                    return "displayInput";

                }
                return "hiddenInputHolder";

            };
            $scope.isValueRequiredForPredicate = function (predicateProp) {
                var required = undefined;
                angular.forEach(allOperators, function (item) {
                    if (item.prop == predicateProp) {
                        required = item.requireValue;
                    }
                });
                if (required === undefined) {
                    throw "can't find property:" + predicateProp;
                }
                return required;
            };
            $scope.shouldDisplayValidationMessage = function () {
                return $scope.tempComposedFilter.predicates.length > 1
                    || $scope.tempComposedFilter.predicates.length == 1 &&
                    $scope.tempComposedFilter.predicates[0].value.trim().length != 0;
            };
            $scope.getValidationErrorMessage = function () {
                if (!$scope.configuration || !$scope.tempComposedFilter) {
                    return;
                }
                var invalidMessage = undefined;
                angular.forEach($scope.tempComposedFilter.predicates, function (predicateItem) {
                    if (!predicateItem.prop || !predicateItem.operator) {
                        invalidMessage = "Field and/or predicate are empty.";
                    }
                    if ($scope.isValueRequiredForPredicate(predicateItem.operator)) {
                        var operatorsType = $scope.getOperatorsType(predicateItem.prop);
                        var message = validatorsByType[operatorsType](predicateItem.value, predicateItem.operator);
                        if (message) {
                            invalidMessage = message;
                        }
                    }
                });
                return invalidMessage;
            };
            $scope.addEmptyRow = function () {
                $scope.tempComposedFilter.predicates.push({
                    prop: $scope.configuration.fields[0].prop,
                    operator: $scope.getOperatorsByField($scope.configuration.fields[0].prop)[0].prop,
                    value: ""
                });
            };
            $scope.removeRow = function (index) {
                $scope.tempComposedFilter.predicates.splice(index, 1);
            };
        };
    }

    function resizableTableColumns(ResizableTableColumnsCache) {
        return {
            restring: "A",
            link: function (scope, table, attrs) {

                var resizableTableColumnsCache = ResizableTableColumnsCache();
                const COLUMN_MIN_WIDTH = 32;
                const ROW_SELECTOR = ".content > div.row, .content > form div.row-holder";
                const HEADER_COLUMN_SELECTOR = ".heading div.cell:not(.not-resizable)";
                const ROW_COLUMN_SELECTOR = "div.row-holder > div.cell:not(.not-resizable), > div.cell:not(.not-resizable)";
                const TABLE_NAME = attrs.resizableTableColumns;

                if (!TABLE_NAME) {
                    throw "resizable-table-columns attribute must not be empty";
                }

                var columnsCache = resizableTableColumnsCache.retrieve(TABLE_NAME);
                var columns = [];

                init(columnsCache);
                //cache might be reset
                columnsCache = resizableTableColumnsCache.retrieve(TABLE_NAME);

                if (columns.length == 0) {
                    var initialColumnsCount = 0;
                    var minTimeToWaitForColumns = 5000;
                    var intervalColumns = 10;
                    waitUntil(
                        function condition() {
                            minTimeToWaitForColumns -= intervalColumns;
                            return $(table).find(HEADER_COLUMN_SELECTOR).length != initialColumnsCount ||
                                minTimeToWaitForColumns <= 0;
                        },
                        function handler() {
                            init(resizableTableColumnsCache.retrieve(TABLE_NAME));
                        }
                    );
                }

                if (columnsCache) {
                    var initialRowsCount = getRowElements().length;
                    var minTimeToWait = 10000;
                    var interval = 10;
                    waitUntil(
                        function condition() {
                            var toReturn = initialRowsCount != getRowElements().length || minTimeToWait <= 0;
                            minTimeToWait -= interval;
                            return toReturn;
                        },
                        function handler() {
                            columnsCache = resizableTableColumnsCache.retrieve(TABLE_NAME);
                            if (columns.length == 0) {
                                init(columnsCache);
                            } else if (columnsCache) {
                                updateColumnsFromCache(columnsCache, columns, getRowElements());
                            }
                        },
                        interval
                    );
                }

                function waitUntil(conditionFunction, successCallback, timeout) {

                    if (conditionFunction()) {
                        successCallback();
                    } else {
                        setTimeout(function () {
                            waitUntil(conditionFunction, successCallback, timeout);
                        }, timeout);
                    }
                }

                function init(columnsCache) {

                    var headerColumns = $(table).find(HEADER_COLUMN_SELECTOR);

                    function updateResizableColumnMaxWidths(resizableColumns) {
                        $(resizableColumns).each(function (index, column) {
                            if (index != 0) {
                                var previousColumn = resizableColumns[index - 1];
                                var previousColumnMaxWidth = previousColumn[0].getBoundingClientRect().width +
                                    $(column)[0].getBoundingClientRect().width - COLUMN_MIN_WIDTH;
                                previousColumn.resizable("option", "maxWidth", previousColumnMaxWidth);
                            }
                        });
                    }

                    headerColumns.each(function (index, headerColumnDiv) {

                        var column = $(headerColumnDiv);
                        columns.push(column);

                        if (index != 0) {
                            var previousColumn = columns[index - 1];
                            var previousColumnMaxWidth = previousColumn[0].getBoundingClientRect().width +
                                column[0].getBoundingClientRect().width - COLUMN_MIN_WIDTH;
                            previousColumn.resizable("option", "maxWidth", previousColumnMaxWidth);
                        }
                        // do nothing with last column
                        if (headerColumns.length - 1 == index) {
                            return;
                        }

                        var originalWidth = 0;
                        var nextOriginalWidth = 0;
                        var totalTableWidth = 0;
                        column.resizable({
                            minWidth: COLUMN_MIN_WIDTH,
                            handles: "e",
                            helper: "resizable-column-helper",
                            start: function () {
                                originalWidth = column[0].getBoundingClientRect().width;
                                nextOriginalWidth = columns[index + 1][0].getBoundingClientRect().width;
                                totalTableWidth = getHeadingWidth();
                            },
                            stop: function (e, ui) {

                                var nextColumn = columns[index + 1];
                                var currentColumnWidth = ui.size.width;
                                var currentColumnWidthPercents = currentColumnWidth / totalTableWidth * 100;
                                currentColumnWidthPercents =
                                    Math.ceil(currentColumnWidthPercents * 10000) / 10000 + "%";
                                var nextColumnWidth = originalWidth + nextOriginalWidth - currentColumnWidth;
                                var nextColumnWidthPercents = nextColumnWidth / totalTableWidth * 100;
                                nextColumnWidthPercents = Math.floor(nextColumnWidthPercents * 10000) / 10000 + "%";

                                column.css("width", currentColumnWidthPercents);
                                nextColumn.css("width", nextColumnWidthPercents);

                                if (index + 2 < columns.length) {
                                    var nextColumnMaxWidth = nextColumn[0].getBoundingClientRect().width +
                                        columns[index + 2][0].getBoundingClientRect().width - COLUMN_MIN_WIDTH;
                                    nextColumn.resizable("option", "maxWidth", nextColumnMaxWidth);
                                }

                                if (index != 0) {
                                    var previousColumnMaxWidth = columns[index - 1][0].clientWidth +
                                        column[0].clientWidth - COLUMN_MIN_WIDTH;
                                    columns[index - 1].resizable("option", "maxWidth", previousColumnMaxWidth);
                                }

                                var rowElements = getRowElements();

                                rowElements.each(function (rowIndex, rowElem) {

                                    var rowCells = $(rowElem).find(ROW_COLUMN_SELECTOR);
                                    var currentColumnCell = $(rowCells.get(index));
                                    var nextColumnCell = $(rowCells.get(index + 1));

                                    currentColumnCell.css("width", currentColumnWidthPercents);
                                    nextColumnCell.css("width", nextColumnWidthPercents);
                                });

                                updateColumnsCache(
                                    resizableTableColumnsCache,
                                    TABLE_NAME,
                                    index,
                                    currentColumnWidthPercents,
                                    index + 1,
                                    nextColumnWidthPercents,
                                    columns.length
                                );
                            }
                        });
                    });

                    if (columnsCache) {
                        if (columns.length > 0 && columnsCache.count != columns.length) {
                            resizableTableColumnsCache.update(TABLE_NAME, null);
                        } else {
                            updateColumnsFromCache(columnsCache, columns, getRowElements());
                            updateResizableColumnMaxWidths(columns);
                        }
                    }
                }

                function updateColumnsCache(cacheService,
                                            tableName,
                                            currentIndex,
                                            currentPercentage,
                                            nextIndex,
                                            nextPercentage,
                                            columnsCount) {
                    var cacheObj = cacheService.retrieve(tableName) || {};
                    var columnWidthsInPercentage = cacheObj.columnWidths || {};
                    columnWidthsInPercentage[currentIndex] = currentPercentage;
                    columnWidthsInPercentage[nextIndex] = nextPercentage;
                    cacheService.update(tableName, {columnWidths: columnWidthsInPercentage, count: columnsCount});
                }

                function updateColumnsFromCache(cacheObj, headerColumns, rows) {

                    if (!cacheObj || !cacheObj.columnWidths) {
                        return;
                    }

                    var columnsCount = headerColumns.length;
                    var columnWidths = cacheObj.columnWidths;

                    for (var i = 0; i < columnsCount; i++) {

                        var columnWidthValue = columnWidths[i];
                        if (!columnWidthValue) {
                            continue;
                        }

                        headerColumns[i].css("width", columnWidthValue);
                        $.each(rows, function (index, rowElement) {
                            var rowCells = $(rowElement).find(ROW_COLUMN_SELECTOR);
                            $(rowCells.get(i)).css("width", columnWidthValue);
                        });

                    }
                }

                function getRowElements() {
                    return $(table).find(ROW_SELECTOR);
                }

                function getHeadingWidth() {
                    var heading = $(table).find(".heading");
                    var paddingLeft = parseFloat(heading.css("padding-left"));
                    return heading[0].getBoundingClientRect().width - paddingLeft;
                }

            }
        };
    }

    function ResizableTableColumnsCache() {

        var cache = {};

        return function () {
            return {
                "update": update,
                "retrieve": retrieve
            };
        };

        function update(name, columnWidths) {
            cache[name] = columnWidths;
        }

        function retrieve(name) {
            return cache[name];
        }
    }

    persistedDataCubesTableController.$inject = ["$scope", "dashboardCommonDirectivesConstants"];

    function persistedDataCubesTableController($scope, dashboardCommonDirectivesConstants) {
        var knownWithoutZoomDataCubes = [
            "Peptide | File",
            "Protein | File",
            "Feature | File",
            "Isotope Group | File"
        ];
        var ISOTOPE_GROUP_DATACUBE_NAME_PREFIX = "Isotope Group |";

        $scope.datacubeToSelect = null;
        $scope.configuration.api = {
            getSelectedDataCube: getSelectedDataCube,
            setSelectedDataCube: setSelectedDataCube
        };

        $scope.isZoomInAvailable = isZoomInAvailable;
        $scope.displayDCFilesList = displayDCFilesList;
        $scope.downloadCompounds = downloadCompounds;
        $scope.handleDataCubeSelection = handleDataCubeSelection;
        $scope.isDataCubeSelected = isDataCubeSelected;
        $scope.displayOpenBtn = displayOpenBtn;
        $scope.displayHdf5Btn = displayHdf5Btn;
        $scope.viewDataCube = viewDataCube;
        $scope.exportToHdf5 = exportToHdf5;

        function isZoomInAvailable(dataCube) {
            return $.inArray(dataCube.name, knownWithoutZoomDataCubes) === -1 &&
                dataCube.name.indexOf(ISOTOPE_GROUP_DATACUBE_NAME_PREFIX) === -1;
        }

        function hasPersistedDCs() {
            return $scope.configuration.dataCubes && $scope.configuration.dataCubes.length > 0;
        }

        function displayDCFilesList() {
            return $scope.configuration.downloadCompounds && hasPersistedDCs() &&
                $scope.configuration.dataCubes[0].files &&
                $scope.configuration.dataCubes[0].files.length > 0;
        }

        function handleDataCubeSelection(datacubeToSelectName) {
            if ($scope.datacubeToSelect && datacubeToSelectName === $scope.datacubeToSelect.name) {
                $scope.datacubeToSelect = null;
            } else {
                setSelectedDataCube(datacubeToSelectName);
            }
        }

        function isDataCubeSelected(datacubeToSelectID) {
            return $scope.datacubeToSelect && $scope.datacubeToSelect.name === datacubeToSelectID;
        }

        function displayOpenBtn() {
            return $scope.configuration.displayOpenBtn;
        }

        function displayHdf5Btn() {
            return $scope.configuration.displayHdf5Btn;
        }

        function downloadCompounds(file) {
            $.fileDownload("/files/compoundsDownload/raw/" + file.id, {
                failCallback: function (response, url) {
                }
            });
        }

        function getSelectedDataCube() {
            return $scope.datacubeToSelect;
        }

        function setSelectedDataCube(dcName) {
            if (hasPersistedDCs()) {
                $scope.datacubeToSelect = null;
            }

            var selectedDC = $scope.configuration.dataCubes.find(function (dc) {
                return dc.name === dcName;
            });

            $scope.datacubeToSelect = selectedDC || null;
        }

        function viewDataCube(dataCube) {
            $scope.$emit(dashboardCommonDirectivesConstants.VIEW_DATA_CUBE_RESULTS, {
                run: $scope.configuration.run,
                dataCube: dataCube
            });
        }

        function exportToHdf5(dataCube) {
            $scope.$emit(dashboardCommonDirectivesConstants.HDF5_DATA_CUBE_EXPORT, {
                run: $scope.configuration.run,
                dataCube: dataCube
            });
        }
    }

})();
