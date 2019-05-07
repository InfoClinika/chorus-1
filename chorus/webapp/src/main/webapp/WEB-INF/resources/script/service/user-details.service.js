"use strict";

(function () {
    angular.module("user-details-service", ["security-back", "feature-service", "enums"])
        .factory("UserDetailsProvider", function ($rootScope, Security, FeatureProvider) {
            const ADMIN_ROLE = "ROLE_admin";
            let admin;

            function updateLoggedInUser($scope, onSuccess) {
                Security.get({path: ""}, function (user) {
                    if (user.id || user.id === 0) {
                        $rootScope.loggedInUser = user;

                        admin = $.grep(user.authorities, function (role) {
                            return role.authority === ADMIN_ROLE;
                        }).length > 0;
                        let billingFeaturesLoaded = false;
                        let featuresLoaded = false;

                        FeatureProvider.getEnabledBillingFeatures(user.labs, function () {
                            billingFeaturesLoaded = true;
                            if (featuresLoaded && onSuccess) {
                                onSuccess(admin);
                            }
                        });

                        FeatureProvider.getEnabledFeatures(user.labs, function () {
                            featuresLoaded = true;
                            if (billingFeaturesLoaded && onSuccess) {
                                onSuccess(admin);
                            }
                        });

                    } else {
                        $rootScope.loggedInUser = null;
                        admin = false;
                    }
                    $scope.$emit("security.userInfoUpdated");
                });
            }

            function isAdmin() {
                return admin;
            }

            function getUserId() {
                return $rootScope.loggedInUser.id;
            }

            function getLoggedUserName() {
                return $rootScope.loggedInUser.username;
            }

            function isUserLoggedIn() {
                return Boolean($rootScope.loggedInUser);
            }

            return {
                updateLoggedInUser: updateLoggedInUser,
                isAdmin: isAdmin,
                getUserId: getUserId,
                getLoggedUserName: getLoggedUserName,
                isUserLoggedIn: isUserLoggedIn
            };
        })
        .factory("UserLabsProvider", function ($rootScope, FeatureProvider, BillingFeatures, LabFeatures) {
            function isUserLab(labId) {
                return FeatureProvider.getLabFeatures().hasOwnProperty(labId);
            }

            function isLabMember() {
                return getLoggedUserLabs().length > 0;
            }

            function getLoggedUserLabs() {
                return $rootScope.loggedInUser && $rootScope.loggedInUser.labs || [];
            }

            return {
                isUserLab: isUserLab,
                isLabMember: isLabMember
            };
        });
})();
