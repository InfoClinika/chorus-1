"use strict";

(function () {

    const ANNOTATION = "Annotation";
    const FACTOR = "Factor";
    const IGNORE = "Ignore";
    const KEY = "Mapping Key";
    const TYPES = [IGNORE, ANNOTATION, FACTOR, KEY];
    const ERROR_COLS = "No " + ANNOTATION + "s and/or " + FACTOR + "s are selected";
    const ERROR_KEY = KEY + " have to be chosen";
    const ERROR_DATA = "No data found mapped by this " + KEY;

    angular.module("ngsExperiment")

        .directive("experimentDesignFromCsv", function () {
            return {
                restrict: "E", //../script/dialogs/wizard-dialog.template.html
                templateUrl: "../script/ngs/experiment-design-from-csv.template.html",
                replace: true,
                scope: {
                    keys: "&",
                    values: "&",
                    callback: "&"
                },
                controller: function ($scope) {
                    var csvContent = "";

                    $scope.vm = {
                        columns: [],
                        keys: [],
                        types: TYPES,
                        filename: "",
                        errors: []
                    };
                    $scope.showDialog = function () {
                        $("#experiment-design-from-csv-popup").modal("show");
                    };
                    $scope.closeDialog = function () {
                        cleanVM();
                        $("#exp-design-csv-file-input").val("");
                        $("#experiment-design-from-csv-popup").modal("hide");
                    };
                    $scope.isMappingKeyOptionDisabled = function (type, column) {
                        return type === KEY && column.type !== KEY && keyIsChosen();
                    };
                    $scope.readFile = function (name, dataURI) {
                        var base64 = dataURI.replace(/^data:.*?;base64,/, "");
                        csvContent = atob(base64);
                        $scope.vm.filename = name;

                        var results = Papa.parse(csvContent, {
                            header: true
                        });
                        if (results.data.length > 1) {
                            updateVM(results.meta.fields);
                            $scope.rows = results.data;
                        }
                        results = null;
                    };
                    $scope.finishMapping = function () {
                        try {
                            $scope.vm.errors.length = 0;
                            var mapping = buildMapping($scope.vm.columns, $scope.rows, $scope.values());
                            $scope.callback()(mapping.key, mapping.data);
                            $scope.closeDialog();
                        } catch (errorMessage) {
                            $scope.vm.errors.push(errorMessage);
                        }
                    };

                    function updateVM(columnNames) {
                        var columns = columnNames.map(function (name) {
                            return {
                                name: name,
                                type: IGNORE,
                                isAnnotation: function () {
                                    return this.type === ANNOTATION;
                                },
                                isFactor: function () {
                                    return this.type === FACTOR;
                                },
                                isKey: function () {
                                    return this.type === KEY;
                                },
                                isIgnored: function () {
                                    return this.type === IGNORE;
                                }
                            };
                        });
                        $scope.vm.columns.length = 0;
                        Array.prototype.push.apply($scope.vm.columns, columns);
                        $scope.vm.keys.length = 0;
                        var keys = $scope.keys()();
                        Array.prototype.push.apply($scope.vm.keys, keys);
                    }

                    function cleanVM() {
                        $scope.rows = [];
                        $scope.vm.columns.length = 0;
                        $scope.vm.errors.length = 0;
                        $scope.vm.filename = "";
                    }

                    function keyIsChosen() {
                        var key = $scope.vm.columns.find(function (column) {
                            return column.type === KEY;
                        });
                        return key !== undefined;
                    }
                }
            };
        });

    function buildMapping(columns, rows, fnValuesByKeyName) {
        var key;
        var cols = [];
        for (var i = 0, l = columns.length; i < l; i++) {
            var column = columns[i];
            switch (column.type) {
                case KEY:
                    key = column;
                    break;
                case FACTOR:
                    column.factor = true;
                    break;
                case ANNOTATION:
                    column.values = {};
                    cols.push(column);
                    break;
            }
        }

        if (!key) {
            throw ERROR_KEY;
        }
        if (cols.length === 0) {
            throw ERROR_COLS;
        }
        var keyValues = fnValuesByKeyName(key.mappedKey);
        if (keyValues.length === 0) {
            throw ERROR_DATA;
        }

        var noData = true;
        for (i = 0, l = rows.length; i < l; i++) {
            var row = rows[i];
            var keyValue = row[key.name];
            var idx;
            if ((idx = keyValues.indexOf(keyValue)) !== -1) {
                noData = false;
                delete keyValues[idx];
                angular.forEach(cols, function (col) {
                    var value = row[col.name];
                    if (value !== undefined && value !== null && value !== "") {
                        col.values[keyValue] = value;
                    }
                });
            }
        }
        if (noData) {
            throw ERROR_DATA;
        }

        return {
            key: key.mappedKey,
            data: cols.map(function (col) {
                return {
                    name: col.name,
                    factor: !!col.factor,
                    values: col.values
                };
            })
        };
    }

})();
