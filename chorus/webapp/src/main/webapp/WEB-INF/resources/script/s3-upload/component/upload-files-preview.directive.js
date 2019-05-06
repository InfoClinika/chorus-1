"use strict";

(function () {


    angular.module("s3UploadModule")

        .directive("uploadFilesPreview", function ($filter) {
            return {
                restrict: "E",
                scope: {
                    filesLoaded: "=",
                    files: "=",
                    filesSelected: "="
                },
                controller: function ($scope) {
                    var gridOptions = {
                        defaultColDef: {
                            editable: false,
                            sortable: true,
                            suppressFilter: true
                        },
                        columnDefs: [
                            {
                                headerName: "",
                                field: "selected",
                                checkboxSelection: isRowSelectable,
                                sortable: false,
                                width: 30,
                                maxWidth: 30,
                                headerCheckboxSelection: true
                            },
                            {headerName: "NAME", field: "name"},
                            {
                                headerName: "SIZE",
                                field: "size",
                                cellFormatter: fileSizeFormatter,
                                width: 90,
                                maxWidth: 90,
                                cellClass: "text-right"
                            },
                            {
                                headerName: "DATE",
                                field: "date",
                                cellFormatter: mediumDateFormatter,
                                width: 90,
                                maxWidth: 90,
                                cellClass: "text-right"
                            }
                        ],
                        headerHeight: 27,
                        rowHeight: 32,
                        enableColResize: true,
                        enableSorting: true,
                        overlayNoRowsTemplate: "No files found.",
                        suppressRowClickSelection: true,
                        rowSelection: "multiple",
                        onGridReady: function () {
                            gridOptions.api.showLoadingOverlay();
                        },
                        gridSizeChanged: function () {
                            gridOptions.api.sizeColumnsToFit();
                        },
                        viewportChanged: function () {
                            gridOptions.api.sizeColumnsToFit();
                        },
                        onSelectionChanged: function () {
                            var selectedRows = gridOptions.api.getSelectedRows();
                            $scope.selectedFilesCount = countNewFiles(selectedRows);
                            $scope.filesSelected.length = 0;
                            var selectedNewFiles = selectedRows.filter(function (file) {
                                return isNewFile(file);
                            });
                            Array.prototype.push.apply($scope.filesSelected, selectedNewFiles);
                            $scope.$apply();
                        },
                        animateRows: true,
                        enableFilter: false,
                        isExternalFilterPresent: function () {
                            return !$scope.showExistedFiles;
                        },
                        doesExternalFilterPass: function (node) {
                            return isNewFile(node.data);
                        }
                    };

                    $scope.showExistedFiles = false;
                    $scope.countExistedFiles = 0;
                    $scope.selectedFilesCount = 0;
                    $scope.newFilesCount = 0;

                    function isRowSelectable(node) {
                        return isNewFile(node.data);
                    }

                    $scope.gridOptions = gridOptions;

                    $scope.$watch("filesLoaded", function (loaded, oldVal) {
                        if (loaded !== oldVal) {
                            gridOptions.api.sizeColumnsToFit();
                            if (!loaded) {
                                $scope.selectedFilesCount = 0;
                                $scope.newFilesCount = 0;
                                gridOptions.api.setRowData([]);
                                gridOptions.api.showLoadingOverlay();
                            } else if ($scope.files.length === 0) {
                                gridOptions.api.setRowData([]);
                                gridOptions.api.showNoRowsOverlay();
                                $scope.newFilesCount = 0;
                            } else {
                                var data = $scope.files;
                                $scope.newFilesCount = data.length;
                                gridOptions.api.hideOverlay();
                                gridOptions.api.setRowData(data);
                                gridOptions.api.selectAllFiltered();
                                $scope.newFilesCount = gridOptions.api.getSelectedNodes().length;
                                $scope.countExistedFiles = countExistedFiles($scope.files);
                            }
                        }
                    });

                    $scope.$watch("showExistedFiles", function (showExistedFiles) {
                        gridOptions.api.onFilterChanged();
                        $scope.newFilesCount = gridOptions.api.getModel().getVirtualRowCount() + 1;
                    });
                },
                templateUrl: "../script/s3-upload/component/upload-files-preview.template.html",
                replace: true
            };

            function fileSizeFormatter(params) {
                return $filter("fileSize")(params.value);
            }

            function mediumDateFormatter(params) {
                return $filter("date")(params.value, "mediumDate");
            }
        });

    function countExistedFiles(files) {
        if (!files || !files.length) {
            return 0;
        }
        return files.reduce(function (cnt, file) {
            return !isNewFile(file) ? cnt + 1 : cnt;
        }, 0);
    }

    function countNewFiles(files) {
        if (!files || !files.length) {
            return 0;
        }
        return files.reduce(function (cnt, file) {
            return isNewFile(file) ? cnt + 1 : cnt;
        }, 0);
    }

    function isNewFile(file) {
        return file.status !== "REGISTERED";
    }

})();
