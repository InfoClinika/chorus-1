"use strict";

(function () {

    angular.module("popups")
        .directive("deleteFilesPopup", function (Files) {
            return {
                restrict: "E",
                templateUrl: "../pages/popups/delete-files-confirmation.html",
                scope: {
                    configuration: "="
                },
                controller: function ($scope, $route) {
                    $scope.configuration.api = {
                        showPopup: showPopup,
                        hidePopup: hidePopup
                    };
                    $scope.vm = {
                        removeButtonTitle: "",
                        removePermanently: true,
                        filteredFiles: {},
                        ok: onOk,
                        cancel: hidePopup
                    };
                    var vm = $scope.vm;

                    function onOk() {
                        removeFiles(vm.filteredFiles.available, vm.removePermanently);
                        hidePopup();
                    }

                    function removeFiles(items, removePermanently) {
                        CommonLogger.log("Removes Selected Files start = ", items);

                        var fileIds = items.map(function (item) {
                            return item.id;
                        });

                        var request = {"files": fileIds};
                        if (removePermanently) {
                            Files.deletePermanently(request, $route.reload);
                        } else {
                            Files.delete(request, $route.reload);
                        }
                    }

                    function filterFilesForDelete(files) {
                        var available = [];
                        var notAvailable = [];
                        files.forEach(function (file) {
                            if (file.usedInExperiments) {
                                notAvailable.push(file);
                            } else {
                                available.push(file);
                            }
                        });

                        return {
                            available: available,
                            notAvailable: notAvailable
                        };
                    }

                    function getRemoveButtonTitle() {
                        return vm.filteredFiles.notAvailable.length > 0 && vm.filteredFiles.available.length > 0 ?
                            "Remove Available" : "Remove";
                    }

                    function showPopup(files) {
                        vm.filteredFiles = filterFilesForDelete(files);
                        vm.removeButtonTitle = getRemoveButtonTitle();

                        if ($scope.popupElement) {
                            $scope.popupElement.modal("show");
                        }
                    }

                    function hidePopup() {
                        if ($scope.popupElement) {
                            $scope.popupElement.modal("hide");
                        }
                    }
                },
                link: function (scope, element, attrs) {
                    scope.popupElement = $(element).find(".modal");
                }
            };
        });

})();
