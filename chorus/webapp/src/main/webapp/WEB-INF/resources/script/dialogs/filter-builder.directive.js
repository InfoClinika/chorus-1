/**
 * Directive allows user to make advanced filtering on list of items(experiments, searches, files, projects).
 * It composes filtering query which can be executed on client or server configuration model structure:
 * fields:{
 *   fields:[
 *     {prop: "name", title:"", type: "string"}
 *   ]
 * }
 * directive output is composedFilter model. Its structure:
 * composedFilter:{
 *   conjunction: true,
 *     predicates:[
 *       {prop:"name", predicate: "equals", "value": "Vasya"}
 *     ]
 * }
 */
"use strict";

(function () {

    const DELAY = 1000;

    var operatorsByType = {
        "string": [
            {
                title: "equals", prop: "EQUAL", requireValue: true, applyToItem: function (itemToFilter, prop, value) {
                    return itemToFilter[prop].toLowerCase() === value.toLowerCase();
                }
            },
            {
                title: "doesn't equal",
                prop: "NOT_EQUAL",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return itemToFilter[prop].toLowerCase() !== value.toLowerCase();
                }
            },
            {
                title: "begins with",
                prop: "BEGINS_WITH",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return itemToFilter[prop].toLowerCase().indexOf(value.toLowerCase()) === 0;
                }
            },
            {
                title: "ends with",
                prop: "ENDS_WITH",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    var lowerCased = value.toLowerCase();
                    return lowerCased.indexOf(itemToFilter[prop].toLowerCase()) === value.length - lowerCased.length;
                }
            },
            {
                title: "contains",
                prop: "CONTAINS",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return itemToFilter[prop].toLowerCase().indexOf(value.toLowerCase()) !== -1;
                }
            },
            {
                title: "doesn't contain",
                prop: "NOT_CONTAINS",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return itemToFilter[prop].toLowerCase().indexOf(value.toLowerCase()) === -1;
                }
            },
            {
                title: "is empty",
                prop: "IS_EMPTY",
                requireValue: false,
                applyToItem: function (itemToFilter, prop, value) {
                    return value.toLowerCase().trim().length === 0;
                }
            },
            {
                title: "isn't empty",
                prop: "IS_NOT_EMPTY",
                requireValue: false,
                applyToItem: function (itemToFilter, prop, value) {
                    return value.toLowerCase().trim().length !== 0;
                }
            },
            {
                title: "is in", prop: "IS_IN", requireValue: true, applyToItem: function (itemToFilter, prop, value) {
                    var values = value.split("\n");
                    return $.inArray(itemToFilter[prop], values) !== -1;
                }
            },
            {
                title: "is not in",
                prop: "IS_NOT_IN",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    var values = value.split("\n");
                    return $.inArray(itemToFilter[prop], values) === -1;
                }
            }
        ],
        "number": [
            {
                title: "equals", prop: "EQUAL", requireValue: true, applyToItem: function (itemToFilter, prop, value) {
                    return Number(itemToFilter[prop]) === Number(value);
                }
            },
            {
                title: "doesn't equal",
                prop: "NOT_EQUAL",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return Number(itemToFilter[prop]) !== Number(value);
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
                title: "is in", prop: "IS_IN", requireValue: true, applyToItem: function (itemToFilter, prop, value) {
                    var numbersInStr = value.split("\n");
                    var numbers = [];
                    $(numbersInStr).each(function (i, item) {
                        numbers[i] = Number(item);
                    });
                    return $.inArray(Number(itemToFilter[prop]), numbers) !== -1;
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
                title: "true", prop: "TRUE", requireValue: false, applyToItem: function (itemToFilter, prop) {
                    if (typeof itemToFilter[prop] === "boolean") {
                        return itemToFilter[prop] === 1 || itemToFilter[prop] === true;
                    }
                    var lowercased = itemToFilter[prop].toLowerCase();
                    return lowercased.indexOf("true") !== -1 || lowercased.indexOf("yes") !== -1;
                }
            },
            {
                title: "false", prop: "FALSE", requireValue: false, applyToItem: function (itemToFilter, prop) {
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
                title: "is on", prop: "IS_ON", requireValue: true, applyToItem: function (itemToFilter, prop, value) {
                    return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() === removeTimeFromDate(new Date(
                        value)).getTime();
                }
            },
            {
                title: "is after",
                prop: "IS_AFTER",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() > removeTimeFromDate(new Date(
                        value)).getTime();
                }
            },
            {
                title: "is on or after",
                prop: "IS_ON_AND_AFTER",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() >= removeTimeFromDate(new Date(
                        value)).getTime();
                }
            },
            {
                title: "is on or before",
                prop: "IS_ON_OR_BEFORE",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() <= removeTimeFromDate(new Date(
                        value)).getTime();
                }
            },
            {
                title: "is before",
                prop: "IS_BEFORE",
                requireValue: true,
                applyToItem: function (itemToFilter, prop, value) {
                    return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() < removeTimeFromDate(new Date(
                        value)).getTime();
                }
            },
            {
                title: "is today",
                prop: "IS_TODAY",
                requireValue: false,
                applyToItem: function (itemToFilter, prop) {
                    return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() === removeTimeFromDate(new Date())
                        .getTime();
                }
            },
            {
                title: "is yesterday",
                prop: "IS_YESTERDAY",
                requireValue: false,
                applyToItem: function (itemToFilter, prop) {
                    var yesterday = removeTimeFromDate(new Date());
                    yesterday.setDate(yesterday.getDate() - 1);
                    return removeTimeFromDate(new Date(itemToFilter[prop])).getTime() === yesterday.getTime();
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

    angular.module("dialogs")

        .directive("filterBuilder", function ($timeout, FilterBuilderService) {
            return {
                restrict: "E",
                templateUrl: "../script/dialogs/filter-builder.template.html",
                replace: true,
                scope: {
                    fields: "=",
                    pageable: "@",
                    advanced: "=",
                    composed: "=",
                    query: "="
                },
                controller: function ($scope) {
                    $scope.pageable = !!$scope.pageable;
                    $scope.composed = FilterBuilderService.emptyFilter;
                    $scope.tempIsValid = false;
                    setupFilterFunctions($scope, $scope.fields, operatorsByType);
                    setupUpdateWatching($scope);
                    cleanTemporaryFilter($scope, true);
                }
            };

            function setupUpdateWatching($scope) {
                var queryPromise;
                $scope.$watch("filterQuery", function (query, old) {
                    if (query !== old) {
                        $timeout.cancel(queryPromise);
                        queryPromise = $timeout(function () {
                            $scope.query = query;
                        }, DELAY);
                    }
                });

                var filterPromise;
                $scope.$watch("tempComposedFilter", function (filter) {
                    var isValid = $scope.getValidationErrorMessage() === undefined;
                    if (isValid && FilterBuilderService.filterChanged($scope.composed, filter)) {
                        $timeout.cancel(filterPromise);
                        filterPromise = $timeout(function () {
                            $scope.composed = filter;
                        }, DELAY);
                    }
                }, true);
            }
        });

    function cleanTemporaryFilter($scope, restoreFromPersisted) {
        if ($scope.composed) {
            $scope.tempComposedFilter = angular.copy($scope.composed);
        }
        var tempFilter = $scope.tempComposedFilter;
        if (!tempFilter.predicates || !restoreFromPersisted) {
            tempFilter.predicates = [];
        }
        if (tempFilter.conjunction === undefined) {
            tempFilter.conjunction = "true";
        } else {
            tempFilter.conjunction =
                tempFilter.conjunction === true || tempFilter.conjunction === "true" ? "true" : "false";
        }
        if (tempFilter.predicates.length === 0) {
            $scope.addEmptyRow();
        }
        if (!tempFilter.applyToItem && !$scope.pageable) {
            tempFilter.applyToItem = function (itemToFilter, predicateItem) {
                var result = undefined;
                angular.forEach(
                    $scope.getOperationsListForFieldType($scope.getOperatorsType(predicateItem.prop)),
                    function (operatorItem) {
                        if (operatorItem.prop === predicateItem.operator) {
                            result = operatorItem.applyToItem(itemToFilter, predicateItem.prop, predicateItem.value);
                        }
                    }
                );
                return result;
            };
        }
    }

    function setupFilterFunctions($scope, fields, operatorsByType) {
        var validatorsByType = {
            "string": function (val) {
                if (!val && val.trim().length === 0) {
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
            angular.forEach(fields, function (fieldItem) {
                if (fieldItem.prop === fieldProp) {
                    operatorsType = fieldItem.type;
                }
            });
            if (operatorsType === undefined) {
                throw "can't find field:" + fieldProp;
            }
            return operatorsType;
        };
        $scope.getOperatorsByField = function (fieldProp) {
            if (!fields) {
                return;
            }
            var operatorsType = $scope.getOperatorsType(fieldProp);
            return $scope.getOperationsListForFieldType(operatorsType);
        };
        $scope.getClassForPredicateValue = function (predicateProp) {
            if ($scope.isValueRequiredForPredicate(predicateProp)) {
                if (predicateProp === "IS_IN" || predicateProp === "IS_NOT_IN"
                    || predicateProp === "is not in" || predicateProp === "is in") {
                    return "displayTextArea";
                }
                return "displayInput";

            }
            return "hiddenInputHolder";

        };
        $scope.isValueRequiredForPredicate = function (predicateProp) {
            var required = undefined;
            angular.forEach(allOperators, function (item) {
                if (item.prop === predicateProp) {
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
                || $scope.tempComposedFilter.predicates.length === 1 &&
                $scope.tempComposedFilter.predicates[0].value.trim().length !== 0;
        };
        $scope.getValidationErrorMessage = function () {
            if (!fields || !$scope.tempComposedFilter) {
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
                prop: fields[0].prop,
                operator: $scope.getOperatorsByField(fields[0].prop)[0].prop,
                value: ""
            });
        };
        $scope.removeRow = function (index) {
            $scope.tempComposedFilter.predicates.splice(index, 1);
        };
    }

    function isValidDate(date) {
        var matches = /^(\d{2})[-\/](\d{2})[-\/](\d{4})$/.exec(date);
        if (matches == null) {
            return false;
        }
        var d = matches[2];
        var m = matches[1] - 1;
        var y = matches[3];
        var composedDate = new Date(y, m, d);
        return composedDate.getDate() === d &&
            composedDate.getMonth() === m &&
            composedDate.getFullYear() === y;
    }

    function removeTimeFromDate(date) {
        date.setMinutes(0);
        date.setHours(0);
        date.setMilliseconds(0);
        date.setSeconds(0);
        return date;
    }

})();
