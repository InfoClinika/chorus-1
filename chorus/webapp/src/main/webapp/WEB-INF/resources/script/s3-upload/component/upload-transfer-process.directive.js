"use strict";

(function () {

    angular.module("s3UploadModule")

        .directive("uploadTransferProcess", function () {
            return {
                restrict: "E",
                scope: {
                    uploadItems: "="
                },
                controller: function ($scope) {
                    $scope.removeUploadingItem = function (item) {
                        $scope.$emit("REMOVE_UPLOADING_ITEM_EVENT", item.fileId);
                    };
                },
                templateUrl: "../script/s3-upload/component/upload-transfer-process.template.html",
                replace: true
            };
        });
})();
