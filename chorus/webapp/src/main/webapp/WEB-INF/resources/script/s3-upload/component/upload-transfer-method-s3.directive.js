"use strict";

(function () {


    angular.module("s3UploadModule")

        .directive("uploadTransferMethodS3", function () {
            return {
                restrict: "E",
                scope: {
                    data: "=",
                    masks: "=",
                    type: "="
                },
                controller: uploadSelectFilesS3Controller,
                templateUrl: "../script/s3-upload/component/upload-transfer-method-s3.template.html",
                replace: true
            };

            function uploadSelectFilesS3Controller($scope, UploadService) {
                var POLICY_INPUT_SELECTOR = "#policy-input";

                $scope.urlPlaceholder = "http://";
                angular.extend($scope.data, {
                    login: "",
                    password: "",
                    url: "",
                    recursive: false
                });

                UploadService.user().then(function (user) {
                    $scope.awsUser = user;
                });

                UploadService.getWorkflowArn().then(function (arn) {
                    $scope.workflowArn = arn;
                });

                UploadService.user().then(function (user) {
                    $scope.awsUser = user;
                });

                $scope.addFileMask = function (mask) {
                    var masks = $scope.masks;
                    if (!!mask && !!mask.trim() && masks.indexOf(mask) === -1) {
                        masks.push(mask.trim());
                        masks.sort();
                    }
                };

                $scope.removeFileMask = function (index) {
                    $scope.masks.splice(index, 1);
                };

                $scope.getPolicyJson = function () {
                    $scope.showEmptyS3UrlError = false;
                    if (!$scope.data.url) {
                        $scope.showEmptyS3UrlError = true;
                        return;
                    }

                    UploadService.getBucketPolicy($scope.data.url).then(function (policy) {
                        var json = JSON.parse(policy);
                        $scope.bucketPolicy = JSON.stringify(json, null, "\t");
                    });
                };

                $scope.copyPolicy = function () {
                    $(POLICY_INPUT_SELECTOR).select();
                    document.execCommand("copy");
                };
            }
        });
})();
