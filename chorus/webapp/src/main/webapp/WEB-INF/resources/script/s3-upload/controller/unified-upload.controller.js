"use strict";

(function () {

    angular.module("s3UploadModule")
        .controller("unifiedUploadController", unifiedUploadController);

    function unifiedUploadController($scope, $location, TRANSFER_METHODS, UploadService) {

        angular.extend($scope, {
            vm: {
                uploadId: 0,
                errors: [],
                files: [],
                filesLoaded: false,
                filesSelected: [],
                instrument: null,
                masks: [],
                methodId: null,
                s3Data: {},
                specie: null,
                uploadItems: []
            },
            transferMethods: TRANSFER_METHODS
        });

        // Step 2
        $scope.initTransferInfo = function () {
            var vm = $scope.vm;
            var hasInstrument = !!vm.instrument;
            vm.methodId = TRANSFER_METHODS.S3_COPY.id;
            if (hasInstrument) {
                var masks = vm.masks;
                masks.length = 0;
                vm.instrument.vendor.fileUploadExtensions.forEach(function (ext) {
                    masks.push(prepareMask(ext.name));
                    for (var additionalExt in ext.additionalExtensions) {
                        if (ext.additionalExtensions.hasOwnProperty(additionalExt)) {
                            masks.push(prepareMask(additionalExt));
                        }
                    }
                });
                masks.sort();
            }

            function prepareMask(extension) {
                var hasExtension = !!extension;
                return hasExtension ? "*" + extension : "*.*";
            }
        };

        $scope.validateTransferInfo = function (method, transferInfo) {
            clearErrors();

            switch (method) {
                case TRANSFER_METHODS.S3_COPY.id:
                    return UploadService.validateS3Url(transferInfo.url) && !!transferInfo.login &&
                        !!transferInfo.password;
                case TRANSFER_METHODS.S3_LINK.id:
                    return UploadService.validateS3Url(transferInfo.url);
                default:
                    return false;
            }
        };

        var lastRequestId = 0;
        var currentRequestId = 0;

        $scope.prepareFiles = function () {
            clearErrors();

            var vm = $scope.vm;
            if (vm.methodId === TRANSFER_METHODS.S3_COPY.id || vm.methodId === TRANSFER_METHODS.S3_LINK.id) {
                vm.files.length = 0;
                var s3 = vm.s3Data;
                vm.filesLoaded = false;
                var requestId = ++lastRequestId;
                currentRequestId = requestId;
                UploadService.list(vm.methodId, vm.instrument.id, s3.url, s3.login, s3.password, s3.recursive, vm.masks)
                    .then(
                        function (uploadDetails) {
                            if (requestId === currentRequestId) {
                                vm.uploadId = uploadDetails.id;
                                Array.prototype.push.apply(vm.files, uploadDetails.files);
                                vm.filesLoaded = true;
                            }
                        },
                        function (error) {
                            if (requestId === currentRequestId) {
                                $scope.vm.errors.push(error.message);
                                vm.filesLoaded = true;
                            }
                        }
                    );
            }
        };

        $scope.prepareLabels = function () {
            clearErrors();

            var specie = $scope.vm.specie;
            var files = $scope.vm.filesSelected
                .filter(function (file) {
                    return file.status !== "REGISTERED";
                })
                .map(function (file) {
                    file.specieId = specie.id;
                    return file;
                });
            $scope.vm.files.length = 0;
            Array.prototype.push.apply($scope.vm.files, files);
            $scope.$broadcast("WIZARD_STEP_SET_LABEL_ACTIVATE_EVENT");
        };

        $scope.uploadFiles = function () {

            var vm = $scope.vm;

            clearErrors();
            checkUploadLimit(doUploadFiles, showUploadLimitExceededNotification);

            function checkUploadLimit(continueUploadFn, limitExceededFn) {
                var checkLimitRequest = {
                    labId: vm.instrument.lab,
                    bytesToUpload: 0
                };
                vm.filesSelected.forEach(function (file) {
                    checkLimitRequest.bytesToUpload += file.size;
                });
                UploadService.checkUploadLimit(checkLimitRequest)
                    .then(function (response) {
                        var uploadAvailable = response.value;
                        if (uploadAvailable) {
                            continueUploadFn();
                        } else {
                            limitExceededFn();
                        }
                    });
            }

            function doUploadFiles() {

                var files = vm.filesSelected.map(function (file) {
                    return {
                        date: file.date,
                        fullName: file.fullName,
                        labels: file.labels ? file.labels.split(/\s*,\s*/) : [],
                        name: file.name,
                        size: file.size,
                        specieId: file.specieId
                    };
                });

                UploadService.copy(vm.uploadId, files)
                    .then(function () {
                        $location.path("/upload/" + vm.uploadId + "/progress");
                    });
            }

            function showUploadLimitExceededNotification() {
                uploadLimitExceptionHandler();
            }

        };

        function clearErrors() {
            $scope.vm.errors.length = 0;
        }
    }

})();
