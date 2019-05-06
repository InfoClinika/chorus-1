/**
 * Provides common directives for modal dialogs
 */

"use strict";

angular.module("modals", ["error-catcher"])
    .directive("modalDialog", function ($location, $rootScope) {
        return function ($scope, $element, attrs) {
            var modalDialogsToIgnoreHeightCorrection = ["create-run-experiment"];

            var elemId = $($element).attr("id");
            $element.addClass("modal");

            function correctModalHeight() {
                var modalDialog = $($element);
                var toExclude = modalDialogsToIgnoreHeightCorrection.find(function (aClass) {
                    return modalDialog.hasClass(aClass);
                });
                if (toExclude) {
                    return;
                }

                var dialogHeight = 0;
                //get dialog content height
                var itemsCount = $(".modal-frame .modal-body:eq(0) > *", modalDialog).length;
                for (var i = 0; i < itemsCount; i++) {
                    dialogHeight += $(".modal-frame .modal-body:eq(0) > *", modalDialog)[i].scrollHeight;
                }
                var headerHeight = $(".modal-header").outerHeight(true);
                var footerHeight = $(".modal-footer")[0].scrollHeight;
                var fullDialogHeight = dialogHeight + headerHeight + footerHeight;
                var screenHeight = $(window).height();

                if (screenHeight < fullDialogHeight + 100) {
                    modalDialog.find(".modal-body")
                        .css("height", screenHeight - headerHeight - footerHeight - 20 + "px");
                } else {
                    modalDialog.find(".modal-body").css("height", "auto");
                }
            }

            $element.on("shown", function () {
                setTimeout(function () {
                    correctModalHeight();
                }, 400); //Timeout while dialog is loading
            });

            $element.on("hide", function (e) {
                var targetId = $(e.target).attr("id");
                if ($scope.dialogNotToReturn || targetId && targetId != elemId) {
                    return;
                }
                removeModalClasses();
                if (attrs.onClose) {
                    $scope.$eval(attrs.onClose);
                }

                //modalReturnUrl is set on ng-click at "Save" button
                var url = $location.url();
                var returnUrl = $scope.modalReturnUrl || url != attrs.modalDialog && attrs.modalDialog ||
                    $rootScope.dialogReturnUrl;

                $location.url(returnUrl);
                setTimeout(function () {
                    $scope.$apply(); // see http://stackoverflow.com/questions/11784656/angularjs-location-not-changing-the-path
                });
            });
            $element.on("keypress", function (event) {
                return event.keyCode !== 13 || event.target.localName == "textarea"; // avoid unexpected dialog behaviour after pressing Enter
            });

            try {
                $element.modal({
                    show: true,
                    keyboard: false,
                    backdrop: "static"
                });
            } catch (error) {
                CommonLogger.log("Error catched: " + error);
            }
        };
    });

function hideModal(afterFn) {
    setTimeout(function () {
        $(".modal").modal("hide");
        if (afterFn) {
            setTimeout(afterFn, 0);
        }
    }, 0);
}

function removeModalClasses() {
    $("body").removeClass("modal-open");
    $(".modal-backdrop").remove();
}
