"use strict";

(function () {

    angular.module("scripts")

        .controller("scriptsController", ScriptsController)
        .controller("createScriptController", CreateScriptController)
        .controller("editScriptController", EditController)
        .controller("viewScriptController", ViewScriptController);


    function ScriptsController($scope,
                               $route,
                               $location,
                               $routeParams,
                               Scripts,
                               removeScriptConfirmation,
                               scriptsExpandMenu,
                               contentRequestParameters) {
        if ($scope.pathError) {
            return;
        }

        $scope.page.title = "Statistical Scripts";
        $scope.page.showFilter = true;
        $scope.page.filter = "";

        var filter = $routeParams.scripts;

        var requestParameters = contentRequestParameters.getParameters("scripts");
        requestParameters.path = filter;
        Scripts.get(requestParameters, function (data) {
            $scope.scripts = data.items;
            $scope.total = data.itemsCount;
        });

        $scope.displayConfirmation = removeScriptConfirmation($scope);
        scriptsExpandMenu($scope);

        $scope.duplicate = function (script) {
            $scope.$broadcast(SHOW_DUPLICATE_SCRIPT_DIALOG, script.id);
        };
        $scope.formatAccessValue = function (isPublic) {
            return isPublic ? "Yes" : "No";
        };
        $scope.share = function (script) {
            Scripts.share({id: script.id, bPublic: !script.bPublic}, function () {
                $route.reload();
            });
        };

    }

    function CreateScriptController($scope, $location, Scripts, LaboratoryDetails) {
        $scope.page.title = "New Script";
        // /laboratories
        $scope.script = {id: null, lab: null, type: "R", details: {}};
        $scope.script.details.parameters = {};
        $scope.userLabs = [];

        LaboratoryDetails.query({}, function (laboratories) {
            $scope.userLabs = laboratories;
        });

        $scope.create = function () {
            Scripts.save($scope.script, function () {
                $location.path("/scripts/my");
            });
        };

    }

    function EditController($scope, $route, $routeParams, Scripts, ScriptDetails) {
        $scope.editMode = true;
        $scope.page.title = "Edit Script";

        $scope.details = ScriptDetails.get({id: $routeParams.id});

        $scope.save = function () {
            var request = {
                id: $scope.details.id,
                type: $scope.details.type,
                details: {
                    text: $scope.details.text,
                    description: $scope.details.description,
                    name: $scope.details.name,
                    parameters: $scope.details.parameters
                }
            };
            Scripts.save(request, function () {
                $route.reload();
            });
        };
    }

    function ViewScriptController($scope, $routeParams, ScriptDetails) {
        $scope.editMode = false;
        $scope.page.title = "Script Details";
        $scope.details = ScriptDetails.get({id: $routeParams.id});
    }

})();

