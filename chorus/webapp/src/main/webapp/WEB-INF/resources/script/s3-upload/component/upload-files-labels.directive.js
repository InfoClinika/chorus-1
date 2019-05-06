"use strict";

(function () {

    angular.module("s3UploadModule")

        .directive("uploadFilesLabels", function () {
            return {
                restrict: "E",
                scope: {
                    files: "=",
                    specie: "="
                },
                controller: function ($scope, ExperimentSpecies) {
                    var gridOptions = {
                        defaultColDef: {
                            editable: true,
                            sortable: false
                        },
                        columnDefs: [
                            {headerName: "NAME", field: "name", editable: false, sortable: true},
                            {
                                headerName: "SPECIES", field: "specieId", width: 150, maxWidth: 150,
                                cellEditor: "select",
                                cellEditorParams: {
                                    values: []
                                },
                                valueGetter: function (params) {
                                    return $scope.species.find(function (specie) {
                                        return specie.id === params.data.specieId;
                                    }).name;
                                },
                                newValueHandler: function (params) {
                                    var specieName = params.newValue;
                                    var specie = $scope.species.find(function (s) {
                                        return s.name === specieName;
                                    });
                                    if (angular.isDefined(specie)) {
                                        params.data.specieId = specie.id;
                                        return true;
                                    }
                                    return false;

                                }
                            },
                            {headerName: "LABEL", field: "labels", width: 150, maxWidth: 150}
                        ],
                        autoSizePadding: 15,
                        headerHeight: 27,
                        rowHeight: 32,
                        rowData: newFiles($scope.files),
                        enableColResize: true,
                        enableSorting: true,
                        suppressRowClickSelection: true,
                        gridSizeChanged: function () {
                            gridOptions.api.sizeColumnsToFit();
                        },
                        viewportChanged: function () {
                            gridOptions.api.sizeColumnsToFit();
                        }
                    };
                    $scope.gridOptions = gridOptions;

                    ExperimentSpecies.query(function (species) {
                        $scope.species = species;
                        gridOptions.columnDefs[1].cellEditorParams.values = species.map(function (specie) {
                            return specie.name;
                        });
                    });

                    $scope.$on("WIZARD_STEP_ACTIVATE_EVENT", function (event) {
                        gridOptions.api.setRowData(newFiles($scope.files));
                    });
                    $scope.$on("WIZARD_STEP_SET_LABEL_ACTIVATE_EVENT", function (event) {
                        gridOptions.api.setRowData(newFiles($scope.files));
                        gridOptions.api.sizeColumnsToFit();
                    });

                    $scope.addPathToLabels = false;
                    $scope.addPathToLabelsChange = function () {
                        var files = [];
                        gridOptions.api.forEachNode(function (node) {
                            var file = node.data;
                            var path = filePath(file);
                            if ($scope.addPathToLabels) {
                                file.labels = !file.labels ? path : file.labels + ", " + path;
                            } else {
                                file.labels = file.labels.split(/\s*,\s*/)
                                    .filter(function (label) {
                                        return label !== path;
                                    })
                                    .join(", ");
                            }
                            files.push(file);
                        });
                        gridOptions.api.updateRowData({update: files});
                    };
                },
                templateUrl: "../script/s3-upload/component/upload-files-labels.template.html",
                replace: true
            };
        });

    function newFiles(files) {
        return files.filter(function (file) {
            return file.status !== "REGISTERED";
        });
    }

    function filePath(file) {
        var fullName = file.fullName;
        var idx = fullName.lastIndexOf(file.name);
        return idx === -1 ? "" : fullName.substring(0, idx);
    }

})();
