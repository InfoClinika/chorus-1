(function() {
    "use strict";

    angular.module("instrument-models")
        .controller("instrument-models-table", function ($scope, $location, $route,
                                                         contentRequestParameters, PaginationPropertiesSettingService,
                                                         InstrumentModels) {
            if ($scope.pathError) return;
            $scope.total = 0;
            $scope.pageNumber = 0;
            $scope.page.title = "Instrument Models";
            $scope.page.showPageableFilter = true;
            $scope.page.filterScope = $scope;
            
            init();
            
            function init() {
                var pagedRequest = contentRequestParameters.getParameters("instrument-models");
                loadData(pagedRequest);
            }

            function loadData(pagedRequest) {
                InstrumentModels.paged(pagedRequest, function (instrumentModelsPagedItemWrapper) {
                    const instrumentModelsPagedItem = instrumentModelsPagedItemWrapper.value;
                    PaginationPropertiesSettingService.setPaginationProperties($scope, instrumentModelsPagedItem);
                    $scope.instrumentModels = instrumentModelsPagedItem.items;
                });
            }
            
            $scope.showDeleteConfirmation = function (instrumentModel) {

                $scope.confirmation = new Confirmation("#remove-project-confirmation", instrumentModel, {
                    success: removeModel,
                    getName: function () {
                        return instrumentModel.name;
                    }
                });
                $scope.confirmation.removePermanently = true;
                $scope.confirmation.showPopup();
            };
            
            function removeModel(model) {
                InstrumentModels.delete({id: model.id}, function () {
                    $route.reload()
                });
            }
        })
})();

