"use strict";

(function () {

    angular.module("advert-front", ["advert-back", "error-catcher", "front-end"])
        .controller(
            "ad-controller",
            function ($scope, Advertisement, advertisementExpandMenu, removeAdvertisementConfirmation) {
                $scope.advertisement = Advertisement.query();
                $scope.page.title = "Advertisement";
                $scope.page.showFilter = true;

                $scope.isExpired = function (date) {
                    return moment(new Date()).isAfter(date);
                };

                advertisementExpandMenu($scope);

                $scope.isTableEmpty = function () {
                    return $scope.advertisement.length == 0;
                };

                $scope.getEmptyTableMessage = function () {
                    return "There are no advertisements";
                };

                $scope.showRemoveConfirmation = removeAdvertisementConfirmation($scope);
            }
        )
        .controller("newAd", function ($scope, startNewUpload, Advertisement, dateValidation) {

            $scope.getAllowedAdExtensionsAsString = function () {
                return getAllowedAdExtensionsFn().join(", ");
            };

            $scope.existingAttachments = [];

            AttachmentsHelper.commonSetup($scope, null, "#advertisement-details-dialog", Advertisement,
                startNewUpload, null, true, getAllowedAdExtensionsFn, {
                    attachmentType: "Advertisement Image",
                    fileChooserId: "#advertisementImageChooser",
                    postponedUpload: true,
                    isSingleFileUpload: true,
                    getDataToSendFn: function () {
                        return {
                            title: $scope.advertisement.title,
                            startDate: moment($scope.advertisement.startDate).startOf("day"),
                            endDate: moment($scope.advertisement.endDate).endOf("day"),
                            redirectLink: $scope.advertisement.redirectLink,
                            currentDate: moment().valueOf(),
                            isEnabled: $scope.advertisement.isEnabled
                        };
                    },
                    uploadComplete: function (advertisementId, contentUrl) {
                        if ($scope.existingAttachments.length == 1) {
                            Advertisement.updateImageUrl({
                                advertisementId: advertisementId,
                                contentUrl: contentUrl
                            }, function (data) {
                                CommonLogger.log("Advertisement Image Saved. Data = " + JSON.stringify(data));
                                setTimeout(function () {
                                    $(".modal").modal("hide");
                                }, 0);
                            });
                        }
                    }
                }
            );

            $scope.$watch("existingAttachments", function (newVal) {
                console.log("New value" + newVal);
            }, true);


            $scope.isFormInvalid = function () {
                if (!$scope.advertisement) {
                    return true;
                } else if (
                    !$scope.advertisement.title || !$scope.advertisement.startDate ||
                    !$scope.advertisement.endDate || !$scope.advertisement.redirectLink ||
                    $scope.uploadingAttachments.length == 0 ||
                    !dateValidation.isValidDate($scope.advertisement.startDate) ||
                    !dateValidation.isValidDate($scope.advertisement.endDate)) {
                    return true;
                }
                return false;
            };

            $scope.createAdvertisement = function () {
                AttachmentsHelper.uploadFiles($scope, Advertisement);
            };

            function getAllowedAdExtensionsFn() {
                return [".jpg", ".jpeg", ".gif", ".png"];
            }
        })
        .controller("ad-details", function ($scope, Advertisement, startNewUpload, $routeParams, dateValidation) {
            $scope.details = Advertisement.readDetails({id: $routeParams.id}, function (response) {
                $scope.details.startDate = $.datepicker.formatDate("mm/dd/yy", new Date($scope.details.startDate));
                $scope.details.endDate = $.datepicker.formatDate("mm/dd/yy", new Date($scope.details.endDate));
                CommonLogger.log(response);

                $scope.downloadAdImage = function (attachmentId) {
                    $.fileDownload("../poster/download/" + attachmentId, {});
                };

                $scope.getAllowedAdExtensionsAsString = function () {
                    return getAllowedAdExtensionsFn().join(", ");
                };

                AttachmentsHelper.commonSetup($scope, $scope.details.id, "#advertisement-details-dialog", Advertisement,
                    startNewUpload, null, true, getAllowedAdExtensionsFn, {
                        attachmentType: "Advertisement Image",
                        fileChooserId: "#advertisementImageChooser",
                        postponedUpload: true,
                        isSingleFileUpload: true,
                        getDataToSendFn: function () {
                            return {
                                id: $scope.details.id,
                                title: $scope.details.title,
                                startDate: moment($scope.details.startDate).startOf("day"),
                                endDate: moment($scope.details.endDate).endOf("day"),
                                redirectLink: $scope.details.redirectLink,
                                currentDate: moment().valueOf(),
                                isEnabled: $scope.details.isEnabled
                            };
                        },
                        uploadComplete: function (advertisementId, contentUrl) {
                            if ($scope.existingAttachments.length == 1) {
                                Advertisement.updateImageUrl({
                                    advertisementId: advertisementId,
                                    contentUrl: contentUrl
                                }, function (data) {
                                    CommonLogger.log("Advertisement Image Saved. Data = " + JSON.stringify(data));
                                    setTimeout(function () {
                                        $(".modal").modal("hide");
                                    }, 0);
                                });
                            }
                        }
                    }
                );
            });

            $scope.isFormInvalid = function () {
                if (!$scope.details.title || !$scope.details.startDate || !$scope.details.endDate ||
                    !$scope.details.redirectLink ||
                    ($scope.existingAttachments.length || $scope.uploadingAttachments.length) == 0 ||
                    !dateValidation.isValidDate($scope.details.startDate) ||
                    !dateValidation.isValidDate($scope.details.endDate)) {
                    return true;
                }
                return false;
            };

            $scope.updateAdvertisement = function () {
                if ($scope.removedAttachments && $scope.removedAttachments.length > 0) {
                    AttachmentsHelper.uploadFiles($scope, Advertisement, true);
                } else {
                    Advertisement.updateDetails($scope.attachmentOptions.getDataToSendFn());
                    setTimeout(function () {
                        $(".modal").modal("hide");
                    }, 0);
                }
            };

            function getAllowedAdExtensionsFn() {
                return [".jpg", ".jpeg", ".gif", ".png"];
            }
        })
        .directive("advertisementImageUpload", function () {
            return {
                templateUrl: "../pages/ad/ad-image-upload.html",
                restrict: "E"
            };
        })
        .directive("advertisementDetails", detailsLink({title: "Edit Advertisement"}))
        .factory("advertisementExpandMenu", function (Advertisement) {
            return initExpandMenu(function (ad) {
                Advertisement.get({
                    filter: "additionalDetails",
                    redirectLink: ad.redirectLink
                }, function (additionalDetails) {
                    ad.details = additionalDetails;
                });
            });
        })
        .factory("dateValidation", function () {
            return {
                isValidDate: function (date) {
                    var formats = ["MM-DD-YYYY", "MM/DD/YYYY"];
                    return moment(date, formats, true).isValid();
                }
            };
        })
        .factory("removeAdvertisementConfirmation", function ($route, Advertisement) {
            return function ($scope) {
                return function (advertisement) {
                    $scope.confirmation = new Confirmation("#remove-advertisement-confirmation", advertisement,
                        {
                            success: function () {
                                Advertisement.remove({id: advertisement.id}, function () {
                                    $route.reload();
                                });
                            },
                            getName: function () {
                                return advertisement.title;
                            }
                        }
                    );
                    $scope.confirmation.showPopup();
                };
            };
        });
})();
