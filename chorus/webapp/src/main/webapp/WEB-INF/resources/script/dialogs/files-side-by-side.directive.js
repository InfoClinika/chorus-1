"use strict";

angular.module("dialogs")

    .directive("filesSideBySide", function ($filter) {

        return {
            restrict: "E",
            templateUrl: "../script/dialogs/files-side-by-side.template.html",
            replace: true,
            scope: {
                filter: "=",
                files: "="
            },
            controller: function ($scope, FilterFilesService) {

                var DEFAULT_PAGE_SIZE = 20;
                var FIRST_PAGE_NUMBER = 1;
                var filesIds = [];
                var gridFiles = {
                    defaultColDef: {
                        editable: false,
                        sortable: true,
                        suppressFilter: true
                    },
                    columnDefs: [
                        {
                            headerName: "",
                            field: "selected",
                            checkboxSelection: function (node) {
                                try {
                                    return filesIds.indexOf(node.data.id) === -1;
                                } catch (e) {
                                    return false;
                                }
                            },
                            sortable: false,
                            width: 23,
                            maxWidth: 23,
                            minWidth: 23,
                            headerCheckboxSelection: true
                        },
                        {headerName: "NAME", field: "name"},
                        {
                            headerName: "LABELS",
                            field: "labels",
                            width: 90,
                            maxWidth: 90
                        },
                        {
                            headerName: "DATE",
                            field: "uploadDate",
                            cellFormatter: mediumDateFormatter,
                            width: 90,
                            maxWidth: 90,
                            cellClass: "text-right"
                        }
                    ],
                    enableColResize: true,
                    enableSorting: true,
                    headerHeight: 27,
                    rowHeight: 32,
                    overlayNoRowsTemplate: "No files found.",
                    rowSelection: "multiple",
                    suppressRowClickSelection: true,
                    rowDeselection: false,
                    enableServerSideFilter: true,
                    rowModelType: "infinite",
                    onGridReady: function () {
                        gridFiles.api.sizeColumnsToFit();
                    },
                    onModelUpdated: function () {
                        gridFiles.api.sizeColumnsToFit();
                    },
                    onGridSizeChanged: function () {
                        gridFiles.api.sizeColumnsToFit();
                    },
                    onViewportChanged: function () {
                        gridFiles.api.sizeColumnsToFit();
                    },
                    onSortChanged: function () {
                        gridFiles.api.setDatasource(datasource);
                    },
                    paginationPageSize: DEFAULT_PAGE_SIZE,
                    pagination: true
                };
                var gridSelected = {
                    defaultColDef: {
                        editable: false,
                        sortable: true,
                        suppressFilter: true
                    },
                    columnDefs: [
                        {
                            headerName: "",
                            field: "selected",
                            checkboxSelection: true,
                            sortable: false,
                            width: 23,
                            maxWidth: 23,
                            headerCheckboxSelection: true
                        },
                        {headerName: "NAME", field: "name"}
                    ],
                    enableColResize: true,
                    enableSorting: true,
                    headerHeight: 27,
                    rowHeight: 32,
                    overlayNoRowsTemplate: "No files selected.",
                    rowSelection: "multiple",
                    suppressRowClickSelection: true,
                    rowDeselection: false,
                    onGridReady: function () {
                        var newFiles = $scope.files.map(function (f) {
                            delete f.selected;
                            return f;
                        });
                        gridSelected.api.setRowData(newFiles);
                        gridSelected.api.sizeColumnsToFit();
                    },
                    onModelUpdated: function () {
                        gridSelected.api.sizeColumnsToFit();
                    },
                    onGridSizeChanged: function () {
                        gridSelected.api.sizeColumnsToFit();
                    },
                    onViewportChanged: function () {
                        gridSelected.api.sizeColumnsToFit();
                    },
                    paginationPageSize: DEFAULT_PAGE_SIZE,
                    pagination: true
                };

                var datasource = {
                    getRows: function (params) {
                        var startRow = params.startRow;
                        var endRow = params.endRow;
                        var numberOfRows = endRow - startRow;
                        var page = FIRST_PAGE_NUMBER + Math.floor(startRow / numberOfRows);

                        var filter = $scope.filter;
                        var sort = sortBy(gridFiles.api.getSortModel());
                        FilterFilesService.search(filter, sort.field, sort.asc, page, numberOfRows)
                            .then(function (itemBox) {
                                $scope.totalItems = itemBox.totalItems;
                                params.successCallback(itemBox.items, itemBox.totalItems);
                            });
                    }
                };

                $scope.defaultPageSize = DEFAULT_PAGE_SIZE;
                $scope.gridFiltered = gridFiles;
                $scope.gridSelected = gridSelected;
                $scope.addPageToSelected = addPageToSelected;

                $scope.addSelectedItems = function () {
                    addItemsToSelectedTable(gridFiles.api.getSelectedRows());
                };
                $scope.removeFromSelected = function () {
                    var selectedRows = gridSelected.api.getSelectedRows();
                    var exclude = selectedRows.map(function (file) {
                        return file.id;
                    });
                    var filesSelected = $scope.files.filter(function (file) {
                        return exclude.indexOf(file.id) === -1;
                    });
                    $scope.files.length = 0;
                    Array.prototype.push.apply($scope.files, filesSelected);
                    gridSelected.api.updateRowData({
                        remove: selectedRows
                    });
                };
                $scope.clearSelected = function () {
                    $scope.files.length = 0;
                    gridSelected.api.setRowData($scope.files);
                };

                $scope.$watch("filter", updateRowData, true);
                $scope.$watch("files", function () {
                    filesIds = $scope.files.map(function (file) {
                        return file.id;
                    });
                    gridFiles.api.refreshView();
                }, true);

                function addPageToSelected() {
                    var currentPage = gridFiles.api.paginationGetCurrentPage();
                    var startIndex = currentPage * DEFAULT_PAGE_SIZE;
                    var endIndex = startIndex + DEFAULT_PAGE_SIZE - 1;

                    var filesOnPage = [];
                    gridFiles.api.forEachNode(function (node, index) {
                        if (startIndex <= index && index <= endIndex) {
                            filesOnPage.push(node.data);
                        }
                    });

                    addItemsToSelectedTable(filesOnPage);
                }

                function addItemsToSelectedTable(itemsToAdd) {
                    var exclude = $scope.files
                        .map(function (file) {
                            return file.id;
                        });
                    var files = itemsToAdd
                        .filter(function (file) {
                            return exclude.indexOf(file.id) === -1;
                        });
                    Array.prototype.push.apply($scope.files, files);
                    gridSelected.api.updateRowData({
                        add: files
                    });
                }

                function mediumDateFormatter(params) {
                    return $filter("date")(params.value, "mediumDate");
                }

                function updateRowData() {
                    gridFiles.api.setDatasource(datasource);
                }

                function sortBy(sortModel) {
                    return sortModel.length > 0 ?
                        {
                            field: sortModel[0].colId,
                            asc: sortModel[0].sort === "asc"
                        } :
                        {
                            field: "id",
                            asc: true
                        };
                }
            }
        };
    });
