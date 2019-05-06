"use strict";

(function () {

    angular
        .module("scripts")
        .directive("ipScriptDetails", detailsLink({
            "title": "Show Script Details"
        }))
        .directive("scriptName", linkedName({}))
        .directive("ipPopover", ipPopover)
        .directive("ipScriptParameters", ipScriptParameters)
        .factory("removeScriptConfirmation", removeScriptConfirmation)
        .factory("scriptsExpandMenu", scriptsExpandMenu)
        .filter("scriptTypeFormatter", scriptTypeFormatter)
        .directive("parameterValueValidator", parameterValueValidator)
        .directive("duplicateScript", duplicateScript);

    function ipPopover() {
        return function (scope, elem) {
            $(elem).popover();
            $(elem).on("hide", function (e) {
                e.stopPropagation();
            });
        };
    }

    function ipScriptParameters() {
        return {
            restrict: "E",
            templateUrl: "../../pages/scripts/script-parameters.html",
            scope: {data: "=", edit: "="},
            controller: function ($scope) {
                $scope.editMode = angular.isDefined($scope.edit) ? $scope.edit : true;

                function paramNameAlreadyPresent(param) {
                    return $scope.data[param.name];
                }

                $scope.addParameter = function (param) {

                    if (paramNameAlreadyPresent(param)) {
                        console.warn("Parameter with name " + param.name + " has already added");
                        return;
                    }

                    if (!param.type) {
                        console.warn("Param type is not selected");
                        return;
                    }

                    $scope.data[param.name] = {type: param.type.toString(), defaultValue: param.defaultValue};
                };

                $scope.removeParameter = function (paramName) {
                    delete $scope.data[paramName];
                };

                $scope.isDataEmpty = function () {
                    return $scope.data && Object.keys($scope.data).length == 0;
                };
            }
        };
    }

    function removeScriptConfirmation($route, Scripts) {
        return function ($scope) {
            return function (script) {
                $scope.confirmation = new Confirmation("#remove-script-confirmation", script,
                    {
                        success: function () {
                            Scripts.delete({id: script.id}, $route.reload);
                        },
                        getName: function () {
                            return script.name;
                        }
                    }
                );
                $scope.confirmation.removePermanently = true;
                $scope.confirmation.showPopup();
            };
        };
    }

    function scriptTypeFormatter() {
        return function (scriptType) {
            switch (scriptType) {
                case "R":
                    return "Statistical Script (R)";
                case "PYTHON":
                    return "Python Script";
                default :
                    return scriptType;
            }
        };
    }

    function scriptsExpandMenu(Scripts) {
        return initExpandMenu(function openInlineFashion(script) {
        });
    }

    function parameterValueValidator() {
        return {
            require: "ngModel",
            link: function (scope, elem, attr, ngModel) {
                var selectedType = null;
                scope.$watch(attr.parameterValueValidator, function (value) {
                    selectedType = value;
                });

                ngModel.$parsers.unshift(function (value) {
                    var valid = validate(value);
                    ngModel.$setValidity("parameterValue", validate(value));
                    return valid ? value : undefined;
                });

                function validate(value) {
                    if (!value) { // empty value is always valid
                        return true;
                    } else if (selectedType == "STRING") {
                        return true;
                    } else if (selectedType == "REAL_NUMBER") {
                        return value.match(/^[+-]?\d+(\.\d+)?$/);
                    } else if (selectedType == "INTEGER") {
                        return value.match(/^[+-]?\d+$/);
                    }
                    return true;

                }
            }
        };
    }

    function duplicateScript() {
        return {
            restrict: "E",
            templateUrl: "../../pages/scripts/duplicate-script.html",
            scope: {},
            link: function (scope, elem) {
                scope.showDialog = function () {
                    elem.find(".modal").modal("show");
                };
            },
            controller: function ($scope, $location, LaboratoryDetails, Scripts) {
                $scope.scriptId = null;
                $scope.userLabs = [];
                $scope.lab = null;

                $scope.$on(SHOW_DUPLICATE_SCRIPT_DIALOG, function (event, scriptId) {
                    $scope.showDialog();
                    $scope.scriptId = scriptId;
                });

                $scope.isFormInvalid = function () {
                    return !$scope.lab || !$scope.scriptId;
                };

                $scope.duplicate = function () {
                    Scripts.duplicate({id: $scope.scriptId, lab: $scope.lab}, function (response) {
                        $location.path("/scripts/my/" + response.id);
                    });
                };

                init();

                function init() {
                    LaboratoryDetails.query({}, function (laboratories) {
                        $scope.userLabs = laboratories;
                    });

                }
            }
        };
    }

})();

var SHOW_DUPLICATE_SCRIPT_DIALOG = "SHOW_DUPLICATE_SCRIPT_DIALOG";
