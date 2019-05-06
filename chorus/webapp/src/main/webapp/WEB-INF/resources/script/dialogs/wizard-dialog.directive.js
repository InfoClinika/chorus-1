"use strict";

angular.module("dialogs")

    .directive("wizardDialog", function () {
        return {
            restrict: "E",
            transclude: true,
            scope: {
                title: "@",
                button: "@",
                onDone: "&"
            },
            controller: function ($scope, $element) {
                var currentStep = $scope.currentStep = 0;
                var steps = $scope.steps = [];

                this.addStep = function (step) {
                    step.current = steps.length === 0;
                    steps.push(step);
                };

                $scope.selectStep = function (step) {
                    angular.forEach(steps, function (s) {
                        s.selected = false;
                    });
                    step.selected = true;
                };
                $scope.hasPrev = function () {
                    return currentStep > 0;
                };
                $scope.hasNext = function () {
                    return currentStep < steps.length - 1;
                };
                $scope.goBack = function () {
                    steps[currentStep].current = false;
                    currentStep--;
                    steps[currentStep].current = true;
                };
                $scope.canNext = function () {
                    return steps[currentStep].validate();
                };
                $scope.goNext = function () {
                    if ($scope.canNext()) {
                        steps[currentStep].current = false;
                        currentStep++;
                        steps[currentStep].current = true;
                        steps[currentStep].init();
                    }
                };
                $scope.cancel = function () {
                    $(".modal").modal("hide");
                };
            },
            templateUrl: "../script/dialogs/wizard-dialog.template.html",
            replace: true
        };
    })

    .directive("wizardStep", function () {
        return {
            require: "^wizardDialog",
            restrict: "E",
            transclude: true,
            scope: {
                init: "&",
                title: "@",
                validate: "&"
            },
            link: function (scope, element, attrs, wizardDialogController) {
                wizardDialogController.addStep(scope);
            },
            template: "<div ng-transclude ng-show='current'></div>",
            replace: true
        };
    });
