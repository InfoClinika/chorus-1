"use strict";

(function () {

    angular.module("client-token-front", ["ngResource", "client-token-back"])
        .controller("client-token", function ($scope, $location, ClientToken, closeTokenWindowConfirmation) {
            var modal = $("#client-token-dialog");
            $scope.page.title = "Client Token";
            $scope.token = null;
            $scope.generateClientToken = generateClientToken;
            $scope.close = close;

            function close() {
                if ($scope.token) {
                    closeTokenWindowConfirmation($scope);
                } else {
                    modal.trigger("hide");
                }
            }

            function generateClientToken() {
                ClientToken.generate(function (response) {
                    $scope.token = response.token;
                });
            }

        })
        .factory("closeTokenWindowConfirmation", function ($location) {
            return function ($scope) {
                $scope.confirmation = new function (element) {
                    return {
                        success: function () {
                            $location.url("/");
                        },
                        ok: function () {
                            this.success();
                        },
                        showPopup: function () {
                            $(element).css({"display": "table"});
                        },
                        hidePopup: function () {
                            $(element).hide();
                        }
                    };
                }("#close-token-window-confirmation");
                $scope.confirmation.showPopup();
            };
        });
})();
