/**
 * Provides common functions for selection dialogs such as user&group, laboratories selection from list.
 */
function createSelectionDialog(options) {
        return {
            scope:options.scope,
            restrict: "E",
            templateUrl: options.templateUrl,
            controller: function($scope){
                options.defineScopeFunctions($scope);
                $scope.toAdd = "";
                $scope.selected = options.selectedFn($scope);
                $scope.availableItems = function () {
                    return jQuery.grep(options.getAllItems($scope), function (elem) {
                        return jQuery.map($scope.selected(), $scope.identify).indexOf($scope.identify(elem)) < 0;
                    });
                };
                $scope.showInAutoComplete = options.showInAutoCompleteFn($scope);
                $scope.identify = options.identifyFn($scope);
                $scope.addPlaceHolderText = function () {
                    return options.args.addPlaceHolderText;
                };
                $scope.emptyTableMessage = function () {
                    return options.args.emptyTableMessage;
                };
                $scope.isTableEmpty = function () {
                    if (options.selectedToCreate){
                        var selectedToCreateResultFn = options.selectedToCreate($scope);
                        if (selectedToCreateResultFn() && selectedToCreateResultFn().length != 0){
                            return false;
                        }
                    }
                    return !$scope.selected() || $scope.selected().length == 0 ;
                };
                $scope.addActionText = function () {
                    return options.args.addActionText;
                };
                $scope._findByIdentity = function (text) {
                    var found = jQuery.grep(options.getAllItems($scope), function (selectedElem, index) {
                        return $scope.identify(selectedElem) == text;
                    });
                    if (found.length == 1) return found[0];
                };
                $scope._alreadyAdded = function (item) {
                    var uniqueKey = $scope.identify(item);
                    return jQuery.grep($scope.selected,function (selectedElem, index) {
                        return $scope.identify(selectedElem) == uniqueKey;
                    }).length != 0;
                };
                $scope.add = function (item) {
                    if (item == null || $scope._alreadyAdded(item))return;
                    $scope.toAdd = "";
                    options.addSelectedItem($scope, item);
                };
                $scope.removeItem = function (item) {
                    var id = $scope.identify(item);
                    options.removeSelectedItem($scope, item, id);
                };
            }
        }
}