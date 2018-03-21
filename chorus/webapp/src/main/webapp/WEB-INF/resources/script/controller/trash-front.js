/**
 * Created by elena.philipenko on 18.03.14.
 */
angular.module("trash-front", [ "trash-back", "formatters", "error-catcher"])
    .controller("trash", function ($scope, $route,  $routeParams, Trash, TrashControllerCommon) {
        if ($scope.pathError) return;
        CommonLogger.setTags(["TRASH", "TRASH-CONTROLLER"]);
        $scope.page.title = "Trash";
        var setup = TrashControllerCommon($scope);
        Trash.listAll(function (trash) {
            $scope.trash = trash;
            setup.setup($scope.trash, function(){
                $route.reload();
            });

            $scope.selectItem = function (file, event) {
                file.selected = !file.selected;
            };
            $scope.selectAll = function (files) {
                Selections.selectAll($.grep(files, function (file) {
                    return file != null;
                }));
            };
        });

    })

.factory("TrashControllerCommon", ["$route", "Trash", "DashboardButtonFactory", "DashboardButton", "FilesCharts",
    function ($route, Trash, DashboardButtonFactory, DashboardButton,
               FilesCharts) {
        return function ($scope) {
            return {
                setup: function (selectedFiles, callback) {
                    $scope.$watch(function ($scope) {
                        var selected = $.grep($scope.trash, function (item) {
                            return item.selected
                        });
                        return selected.length;
                    }, function () {
                        CommonLogger.log("The selected trash item list has been changed.");
                        var selectedItems = $.grep($scope.trash, function (item) {
                            return item.selected
                        });
                        switchRestoreButton(selectedItems);
                        $scope.allItemsSelected = $scope.trash.length > 0 && selectedItems.length == $scope.trash.length;
                    });

                    var switchRestoreButton = function (selectedItems) {
                        var button = new DashboardButton(5, "R", "Restore", "restore");
                        button.display = selectedItems.length > 0;
                        var currentUrl = null;

                        var urlRequest = {"fileIds": []};
                        angular.forEach(selectedItems, function (file) {
                            urlRequest.fileIds.push(file.id);
                        });
                        if (urlRequest.fileIds.length > 0) {
                            FilesCharts.getUrl(urlRequest, function (response) {
                                currentUrl = response.url;
                            });
                        }
                        button.onClickHandler = function () {
                            var projectIds = []; var expIds = []; var fileIds = [];
                            var experiments = {}; var files = {}; var projects = {};
                            angular.forEach(selectedItems, function (item) {
                                if (item.type == "experiment") {
                                    expIds.push(item.id);
                                    experiments[item.title] = experiments[item.title] + 1 || 1;
                                }
                                if (item.type == "file") {
                                    fileIds.push(item.id);
                                    files[item.title] = files[item.title] + 1 || 1;
                                }
                                if (item.type == "project") {
                                    projectIds.push(item.id);
                                    projects[item.title] = projects[item.title] + 1 || 1;
                                }
                            });
                            $scope.duplicateExperiments = []; $scope.duplicateFiles = []; $scope.duplicateProjects = [];
                            angular.forEach(selectedItems, function (item) {
                                if (experiments[item.title] > 1 && item.type == "experiment") {
                                    $scope.duplicateExperiments.push(item);
                                    expIds.splice( expIds.indexOf(item.id), 1 );
                                }
                                if (files[item.title] > 1 && item.type == "file") {
                                    $scope.duplicateFiles.push(item);
                                    fileIds.splice( fileIds.indexOf(item.id), 1 );
                                }
                                if (projects[item.title] > 1 && item.type == "project") {
                                    $scope.duplicateProjects.push(item);
                                    projectIds.splice( projectIds.indexOf(item.id), 1 );
                                }
                            });
                            $scope.notRestorableItems = Trash.readNotRestorableItems({projectIds: projectIds, experimentIds: expIds, fileIds: fileIds}, function(response) {
                                angular.forEach(response.projects, function(item) {
                                    projectIds.splice( projectIds.indexOf(item.id), 1 );
                                });
                                angular.forEach(response.experiments, function(item) {
                                    expIds.splice( expIds.indexOf(item.id), 1 );
                                });
                                angular.forEach(response.files, function(item) {
                                    fileIds.splice( fileIds.indexOf(item.id), 1 );
                                });
                                $scope.isReadNotRestorableItems = true;
                            });
                            $scope.restoreSelectedPopup = new FilesSelectedPopup("#restore-selected-confirmation", function () {
                                Trash.restoreFile({itemIds: fileIds}, function(){
                                    callback();
                                    Trash.restoreExperiment({itemIds: expIds}, function(){
                                        callback();
                                        Trash.restoreProject({itemIds: projectIds}, function(){
                                            callback();
                                        });
                                    });
                                });
                            });
                            $scope.restoreSelectedPopup.showPopup();
                        };
                        DashboardButtonFactory.count(selectedItems.length);
                        DashboardButtonFactory.origin("trash");
                        DashboardButtonFactory.put(button);
                    };
                }
            };
        }
    }]);

