"use strict";

function isBrowserUnsupported() {
    var parsed = detect.parse(navigator.userAgent);
    var unsupported = true;

    if (!parsed.browser.family) { // undetected browser case manually check for IE 11
        var userAgent = navigator.userAgent;
        var regExp = new RegExp("Trident/.*rv:([0-9]{1,}[\.0-9]{0,})");
        if (regExp.exec(userAgent) != null) {
            unsupported = false;
        }
    } else {
        if (parsed.browser.family.toLowerCase() === "chrome" && parseFloat(parsed.browser.version) >= 18) {
            unsupported = false;
        }
        // if (parsed.browser.family.toLowerCase() === "ie" && parseFloat(parsed.browser.version) >= 10) unsupported = false;
        // if (parsed.browser.family.toLowerCase() === "firefox" && parseFloat(parsed.browser.version) >= 12) unsupported = false;
        // if (parsed.browser.family.toLowerCase() === "safari" && parseFloat(parsed.browser.version) >= 5.1) unsupported = false;
        // if (parsed.browser.family.toLowerCase() === "chromium" && parseFloat(parsed.browser.version) >= 28) unsupported = false;
    }

    var cookie = $.cookie("unsupported-browser-not-remind");
    var remind = cookie === "false" || cookie === undefined;
    var cookieWithSession = $.cookie("unsupported-browser-not-remind-in-session");
    var sessionIdEqualsToSaved = cookieWithSession === $.cookie("JSESSIONID");

    return unsupported && (remind || !sessionIdEqualsToSaved);
}

if (isBrowserUnsupported()) {
    location.href = "unsupported-browser.html";
}

angular.module("security-front",
    ["security-back", "features-back", "header", "footer", "error-catcher",
        "enums", "feature-service", "user-details-service"])
    .controller("security", function ($scope, $rootScope, Security, Features, LabFeatures,
                                      BillingFeatures, FeatureProvider, UserDetailsProvider) {
        $scope.ssoProperties = {};
        $scope.showBilling = false;
        $scope.showAdminBilling = false;

        $rootScope.LabFeatures = LabFeatures;
        $rootScope.BillingFeatures = BillingFeatures;
        $rootScope.getSsoProperties = getSsoProperties;

        init();

        function init() {
            Features.getSsoProperties(function (response) {
                $scope.ssoProperties = response;
            });

            $scope.$on("userChanged", function () {
                UserDetailsProvider.updateLoggedInUser($scope);
            });

            $scope.$on("$locationChangeStart", function () {
                UserDetailsProvider.updateLoggedInUser($scope);
            });

            UserDetailsProvider.updateLoggedInUser($scope, function (isAdmin) {
                if (!FeatureProvider.isFeatureAvailable(LabFeatures.BILLING)) {
                    $scope.showBilling = false;
                    $scope.showAdminBilling = false;
                } else {
                    if (UserDetailsProvider.isUserLoggedIn()) {
                        Security.showBilling(function (result) {
                            $scope.showBilling = result.value;
                        });
                    }
                    $scope.showAdminBilling = isAdmin;
                }
                handleUserConsentToPrivacyPolicy();
            });

            function handleUserConsentToPrivacyPolicy() {
                if (UserDetailsProvider.isUserLoggedIn() && $scope.showConsent) {
                    Security.getConsentToPrivacyPolicyDate({}, function (response) {
                        var consentToPrivacyPolicyDate = response.value;
                        if (!consentToPrivacyPolicyDate) {
                            showConsentToPrivacyPolicyDialog(function onOk() {
                                Security.setConsentToPrivacyPolicyDate();
                            });
                        }
                    });
                }

            }
        }

        function getSsoProperties() {
            return $scope.ssoProperties;
        }

    })
    .factory("userChangedNotificationService", function ($rootScope) {
        var userChangedNotificationService = {};

        userChangedNotificationService.broadcastItem = function () {
            $rootScope.$broadcast("userChanged");
        };
        return userChangedNotificationService;
    })
    .factory("requestsUpdatedNotificationService", function ($rootScope) {
        var requestsUpdatedNotificationService = {};

        requestsUpdatedNotificationService.broadcastItem = function () {
            $rootScope.$broadcast("requestsUpdated");
        };
        return requestsUpdatedNotificationService;
    })
    .constant("NotInstitutionalEmailProviders", [
        "gmail.com",
        "outlook.com",
        "yahoo.com",
        "aol.com",
        "mail.com",
        "icloud.com",
        "zoho.com",
        "inbox.com"
    ])
    .directive("forumUrl", ["Features", function (Features) {
        return {
            restrict: "E",
            replace: true,
            template: "<li ng-show=\"forumProperties.enabled\"><a href=\"{{forumProperties.url}}\"></a></li>",
            scope: true,
            controller: function ($scope) {
                $scope.forumProperties = {};
                Features.getForumProperties(function (response) {
                    $scope.forumProperties = response;
                });
            }
        };
    }])
    .factory("DoIfPrivateInstallationDirectiveFactory", function (Features) {
        return function (onInit, onEnabled, onDisabled) {
            return {
                link: function (scope, element, attrs) {
                    onInit(scope, element, attrs);

                    Features.getPrivateInstallProperties(function (properties) {
                        if (properties.enabled) {
                            onEnabled(scope, element, attrs);
                        } else {
                            onDisabled(scope, element, attrs);
                        }
                    });
                }
            };
        };
    })
    .directive("hideIfPrivateInstallation", function (DoIfPrivateInstallationDirectiveFactory) {
        return DoIfPrivateInstallationDirectiveFactory(
            (scope, element) => element.hide(),
            (scope, element) => element.hide(),
            (scope, element) => element.show()
        );
    })
    .directive("showIfPrivateInstallation", function (DoIfPrivateInstallationDirectiveFactory) {
        return DoIfPrivateInstallationDirectiveFactory(
            (scope, element) => element.hide(),
            (scope, element) => element.show(),
            (scope, element) => element.hide()
        );
    })
    .directive("setClassIfPrivateInstallation", function (DoIfPrivateInstallationDirectiveFactory) {
        return DoIfPrivateInstallationDirectiveFactory(
            () => null,
            (scope, element, attrs) => element.addClass(attrs.setClassIfPrivateInstallation),
            (scope, element, attrs) => element.removeClass(attrs.setClassIfPrivateInstallation)
        );
    })
    .directive("removeClassIfPrivateInstallation", function (DoIfPrivateInstallationDirectiveFactory) {
        return DoIfPrivateInstallationDirectiveFactory(
            () => null,
            (scope, element, attrs) => element.removeClass(attrs.removeClassIfPrivateInstallation),
            (scope, element, attrs) => element.addClass(attrs.removeClassIfPrivateInstallation)
        );
    })
    .directive("autoimporterUrl", ["Features", function (Features) {
        return {
            link: function (scope, element) {
                Features.getAutoimporterProperties(function (response) {
                    element.attr("href", response.url);
                });
            }
        };
    }])
    .directive("desktopUploaderUrl", ["Features", function (Features) {
        return {
            restrict: "A",
            scope: {
                desktopUploaderUrl: "@"
            },
            link: function (scope, element) {
                Features.getDesktopUploaderProperties(function (urls) {
                    if (urls[scope.desktopUploaderUrl]) {
                        element.attr("href", urls[scope.desktopUploaderUrl]);
                        console.log("Uploader", scope.desktopUploaderUrl, urls[scope.desktopUploaderUrl]);
                    } else {
                        element.css("display", "none");
                        console.log("Uploader", scope.desktopUploaderUrl, "Not found");
                    }
                });
            }
        };
    }]);


function laboratorySelection(arg) {
    return function () {
        return createSelectionDialog({
            scope: {
                laboratories: "=", selectedLaboratories: "=", undeletableLaboratories: "=", pendingLabs: "=",
                removalLaboratories: "="
            },
            args: arg,
            defineScopeFunctions: function ($scope) {
                $scope.laboratoryName = function (item) {
                    return item.name;
                };
                // check remove can remove new lab
                $scope.canBeRemoved = function (text) {
                    if (!$scope.undeletableLaboratories) {
                        return true;
                    }
                    var labs = jQuery.grep(
                        $scope.undeletableLaboratories.concat($scope.pendingLabs),
                        function (elem, index) {
                            return $scope.identify(elem) == text;
                        }
                    );
                    return labs.length == 0;
                };

                $scope.isPendingMembership = function (item) {
                    var pending = false;
                    angular.forEach($scope.pendingLabs, function (pendingLab) {
                        if (pendingLab == item) {
                            pending = true;
                        }
                    });
                    return pending;
                };
                // add to remove list old lab
                $scope.removeHandler = function (lab) {
                    if (lab.isRemove) {
                        lab.isRemove = false;
                        $scope.removalLaboratories.splice($.inArray(lab, $scope.removalLaboratories), 1);
                    } else {
                        $scope.removalLaboratories.push(lab);
                        lab.isRemove = true;
                    }
                    CommonLogger.log($scope.selectedLaboratories, $scope.removalLaboratories);
                };
            },
            selectedFn: function ($scope) {
                return function () {
                    return $scope.pendingLabs.concat($scope.selectedLaboratories);
                };
            },
            getAllItems: function ($scope) {
                return $scope.laboratories;
            },
            showInAutoCompleteFn: function ($scope) {
                return function (item) {
                    return $scope.laboratoryName(item);
                };
            },
            identifyFn: function ($scope) {
                return function (item) {
                    return item.name;
                };
            },
            addSelectedItem: function ($scope, item) {
                item.isAdded = true;
                $scope.selectedLaboratories.push(item);
            },
            removeSelectedItem: function ($scope, item, text) {

                if (!$scope.canBeRemoved(text)) {
                    return;
                }
                $scope.selectedLaboratories = jQuery.grep($scope.selectedLaboratories, function (elem, index) {
                    return $scope.identify(elem) != text;
                });
                item.isAdded = false;
            },
            templateUrl: "../pages/component/laboratory-selection.html"
        });
    };
}

function selectLabWithAutoCompleteFn() {
    return function (scope, iElement, iAttrs) {
        scope.$watch(iAttrs, function (values) {
            iElement.autocomplete({
                source: function (request, response) {
                    var mappedItems = $.map(scope.availableItems(), function (item) {
                        return {
                            "label": scope.showInAutoComplete(item),
                            "value": scope.identify(item),
                            "orig": item,
                            "type": "laboratory"
                        };
                    });

                    var filteredValuesByTerm = $.ui.autocomplete.filter(mappedItems, request.term);

                    response(filteredValuesByTerm);
                },

                select: function (event, ui) {
                    setTimeout(function () {
                        scope.add(ui.item.orig);
                        iElement.val("");
                        iElement.trigger("input");
                    }, 0);
                }
            });
        }, true);
    };
}

angular.module(
    "user-profile-front",
    ["security-front", "security-back", "features-back", "validators", "general-requests", "ui.bootstrap"]
)
    .controller("profile", function ($rootScope, $scope, Security, Features,
                                     userChangedNotificationService, requestsUpdatedNotificationService,
                                     NotInstitutionalEmailProviders,
                                     GeneralRequests, $routeParams, $resource, $location) {
        CommonLogger.setTags(["USER-PROFILE", "PROFILE-CONTROLLER"]);
        $scope.showDetailsDialog = true;
        $scope.page.title = "My Profile";
        $scope.page.form = {
            title: "My Profile",
            editProfileMode: true,
            saveButtonTitle: "Save"
        };
        $scope.allLabs = [];
        $scope.pendingLabs = [];
        $scope.account = {};
        $scope.account.selectedLaboratories = [];
        $scope.account.addedLaboratories = [];
        $scope.account.alreadyAddedLaboratories = [];
        $scope.account.removalLaboratories = [];
        $scope.user = Security.get({path: ""}, function (user) {
            Security.getEmailRequest(function (response) {
                $scope.emailRequest = response;
                $scope.emailRequestSent = $scope.emailRequest.newEmail != null;
            });
            $scope.account.firstName = user.firstName;
            $scope.account.lastName = user.lastName;
            $scope.firstName = $scope.account.firstName;
            $scope.lastName = $scope.account.lastName;
            $scope.account.email = user.username;
            $scope.account.alreadyAddedLaboratories = [];
            Security.labs(function (labs) {
                $scope.allLabs = labs;
                angular.forEach($scope.allLabs, function (lab) {
                    if ($.inArray(lab.id, user.labs) != -1) {
                        lab.isRemove = false;
                        $scope.account.selectedLaboratories.push(lab);
                        $scope.account.alreadyAddedLaboratories.push(lab);
                    }
                });
                GeneralRequests.memberships(function (labRequests) {
                    $scope.pendingLabs = labRequests;
                });
            });
            Security.getAccountRemovalRequestDate({}, function (response) {
                var accountRemovalRequestDate = response.value ?
                    new Date(response.value) :
                    null;
                $scope.accountRemoval = {
                    date: accountRemovalRequestDate
                };
            });
        });

        $scope.buttonPressed = false;
        $scope.changePass = {};
        $scope.changePassButtonPressed = false;
        $scope.newPasswordInvalid = false;
        $scope.changeEmail = {};

        $scope.ssoProperties = {};
        Features.getSsoProperties(function (response) {
            $scope.ssoProperties = response;
        });

        $scope.$watch("changePass.oldPassword", function () {
            if ($scope.changePasswordError) {
                delete $scope.changePasswordError;
            }
        });
        $scope.$watch("changePass.newPassword", validateNewPassword);
        $scope.$watch("confirmPassword", validateNewPassword);

        function validateNewPassword() {
            if ($scope.changePasswordError) {
                delete $scope.changePasswordError;
            }
            $scope.newPasswordInvalid = $scope.confirmPassword !== $scope.changePass.newPassword;
        }

        function isNameChanged() {
            return $scope.firstName != $scope.account.firstName || $scope.lastName != $scope.account.lastName;
        }

        function isOldAndNewPasswordsEquals() {
            return !$scope.newPasswordInvalid && $scope.changePass.oldPassword == $scope.changePass.newPassword;
        }

        $("#firstName").focus();

        $scope.save = function (isInvalid) {
            $scope.buttonPressed = true;
            if (isInvalid) {
                return;
            }
            var account = jQuery.extend(true, {}, $scope.account);
            account.laboratories = [];
            var isRemoveLaboratory = account.removalLaboratories.length !== 0;
            account.addedLaboratories = $.grep(account.selectedLaboratories, function (item) {
                return !item.isRemove && item.isAdded;
            });
            var isAddLaboratory = account.addedLaboratories.length !== 0;

            function preDataForSave() {
                angular.forEach(account.selectedLaboratories, function (lab) {
                    var isRemove = false;
                    var isNew = false;
                    angular.forEach(account.removalLaboratories, function (removalLab) {
                        if (removalLab.id == lab.id) {
                            isRemove = true;
                        }
                    });
                    angular.forEach(account.addedLaboratories, function (removalLab) {
                        if (removalLab.id == lab.id) {
                            isNew = true;
                        }
                    });
                    if (!isRemove && (isNew && isAddLaboratory || !isNew)) {
                        account.laboratories.push(lab.id);
                    }
                });
            }

            function save() {
                preDataForSave();
                delete account.removalLaboratories;
                delete account.selectedLaboratories;
                delete account.alreadyAddedLaboratories;
                delete account.addedLaboratories;
                Security.update(account, function () {
                    setTimeout(function () {
                        if (isNameChanged()) {
                            showUserChangedDialog("Name was changed successfully");
                        }
                        userChangedNotificationService.broadcastItem();
                        requestsUpdatedNotificationService.broadcastItem();
                    }, 0);
                });
            }

            function saveCancelLab() {
                account.removalLaboratories = [];
                isRemoveLaboratory = false;
                isAddLaboratory = false;
                save();
            }

            hideModal(function () {
                if (isRemoveLaboratory || isAddLaboratory) {
                    var message = "";
                    if (isRemoveLaboratory) {
                        message = "<p>You have chosen to leave these labs:</p><ul>";
                        angular.forEach(account.removalLaboratories, function (lab) {
                            message += "<li>" + lab.name + "</li>";
                        });
                        message += "</ul>";
                    }
                    if (isAddLaboratory) {
                        message += "<p>You have chosen to add these labs:</p><ul>";
                        angular.forEach(account.addedLaboratories, function (lab) {
                            message += "<li>" + lab.name + "</li>";
                        });
                        message += "</ul>";
                    }
                    message += "<p>Please note that this action cannot be undone if you proceed.</p>";
                    showUserConfirmDialog(message, save, saveCancelLab);
                } else {
                    saveCancelLab();
                }
            });
        };

        $scope.changePassword = function (isValid) {
            $scope.changePassButtonPressed = true;
            if (!isValid) {
                return;
            }

            Security.changePassword($scope.changePass, function (response) {
                if (response.errorMessage == "New and old password are equals") {
                    $scope.changePasswordError = {"new": true};
                    return;
                } else if (response.errorMessage) {
                    $scope.changePasswordError = {"old": "Old password is not correct"};
                    return;
                }
                delete $scope.changePasswordError;
                setTimeout(function () {
                    $(".modal").modal("hide");
                    showUserChangedDialog("Password was changed successfully");
                }, 0);
            });
        };

        $scope.isEmail = isEmail;

        $scope.isEmailInstitutional = function (email) {
            if (!email) {
                return true;
            }
            return $.grep(NotInstitutionalEmailProviders, function (provider) {
                return email.lastIndexOf("@" + provider) >= 0;
            }).length == 0;
        };

        $scope.isEmailAvailable = true;

        $scope.$watch("changeEmail.email", function () {
            if (isEmail($scope.changeEmail.email)) {
                Security.emailAvailable({email: $scope.changeEmail.email}, function (result) {
                    $scope.isEmailAvailable = result[0] == "t";
                });
            } else {
                $scope.isEmailAvailable = true;
            }
        });

        $scope.isNewEmailValid = function () {
            return $scope.isEmail($scope.changeEmail.email)
                && $scope.changeEmail.email == $scope.changeEmail.confirmEmail
                && $scope.isEmailAvailable;
        };

        var changeEmailButtonPressed = false;
        $scope.onChangeEmail = function () {
            if (changeEmailButtonPressed) {
                return;
            }
            changeEmailButtonPressed = true;
            Security.sendEmailRequest({
                oldEmail: $scope.account.email,
                newEmail: $scope.changeEmail.email
            }, function (response) {
                changeEmailButtonPressed = false;
                if (response.errorMessage == "Such email is already registered") {
                    $scope.isEmailAvailable = false;
                    return;
                }
                setTimeout(function () {
                    $(".modal").modal("hide");
                    showUserChangedDialog("Confirmation email sent to " + $scope.changeEmail.email +
                        ". Please follow the link from the email to complete the email changing procedure.");
                }, 0);
            });
        };

        var sendConfirmationButtonPressed = false;
        $scope.sendConfirmation = function () {
            if (sendConfirmationButtonPressed) {
                return;
            }
            sendConfirmationButtonPressed = true;
            Security.resendEmailRequest(function (response) {
                sendConfirmationButtonPressed = false;
                setTimeout(function () {
                    $(".modal").modal("hide");
                    showUserChangedDialog("Confirmation email sent to " + $scope.emailRequest.newEmail +
                        ". Please follow the link from the email to complete the email changing procedure.");
                }, 0);
            });
        };

        $scope.cancelEmailRequest = function () {
            Security.cancelEmailRequest(function (response) {
                setTimeout(function () {
                    $(".modal").modal("hide");
                    showUserChangedDialog("Email changing procedure has been canceled.");
                }, 0);
            });
        };
        var emailFormDirty = false;
        $scope.initDirtyChecking = function () {
            $(".email-change-form input").bind("blur change", function () {
                emailFormDirty = true;
            });
        };
        $scope.isEmailFormDirty = function () {
            return emailFormDirty;
        };
        $scope.requestAccountRemoval = requestAccountRemoval;
        $scope.revokeAccountRemovalRequest = revokeAccountRemovalRequest;

        var Subscriptions = $resource("../paypal/:action", {}, {
            pending: {method: "POST", params: {action: "pending"}}
        });

        $scope.subscription = Subscriptions.get();

        var result = $routeParams.subscriptionResult;

        if (result) {
            CommonLogger.log("Select subscribe tab with " + result);
            $scope.result = result;
            $scope.showSubscribe = true;
            if ($scope.result == "success") {
                $scope.subscription = Subscriptions.pending();
            }
        }

        function requestAccountRemoval() {
            Security.requestAccountRemoval({}, function () {
                $scope.accountRemoval.date = new Date();
            });
        }

        function revokeAccountRemovalRequest() {
            Security.revokeAccountRemoval({}, function () {
                $scope.accountRemoval.date = null;
            });
        }
    })
    .directive("laboratorySelector", laboratorySelection({
        "emptyTableMessage": "There are no laboratories",
        "addActionText": "Add laboratories",
        "addPlaceHolderText": "Enter laboratory's title"
    }))
    .directive("selectLabWithAutoComplete", function () {
            return selectLabWithAutoCompleteFn();
        }
    );

angular
    .module(
        "login",
        ["security-front", "security-back", "features-back", "validators", "ui", "template-components", "header", "footer"],
        function ($locationProvider) {
//        $locationProvider.html5Mode(true);
        }
    )
    .config(function ($routeProvider) {
        $routeProvider
            .when("/registerInvited/:userId", {controller: "register", templateUrl: "../pages/user/form.html"})
            .when("/forgot-password", {controller: "forgotPassword", templateUrl: "../pages/user/forgot-password.html"})
            .when(
                "/emailVerification",
                {controller: "emailVerification", templateUrl: "../pages/user/email-verification.html"}
            )
            .when("/login/:redirect", {controller: "login", templateUrl: "../pages/user/login.html"})
            .when("/login", {controller: "login", templateUrl: "../pages/user/login.html"})
            .when("/", {controller: "register", templateUrl: "../pages/user/form.html"});
    })
    .controller("login", function ($scope, $routeParams, Security, Features) {
        CommonLogger.setTags(["LOGIN", "LOGIN-CONTROLLER"]);
        $scope.showUnsupported = isBrowserUnsupported();

        $scope.ssoProperties = {};
        Features.getSsoProperties(function (response) {
            $scope.ssoProperties = response;
        });

        $scope.loginResult = Security.loginResult();
        $scope.redirectAfterLogin = "/pages/dashboard.html";
        $("#email").focus();


    })
    .controller("register", function ($scope, $routeParams, Security, Features, NotInstitutionalEmailProviders,
                                      $window) {

            CommonLogger.setTags(["LOGIN", "REGISTER-CONTROLLER"]);
            $scope.account = {};
            $scope.accountHelper = {};
            $scope.account.selectedLaboratories = [];
            $scope.allLabs = [];
            $scope.account.alreadyAddedLaboratories = [];
            Security.labs(function (labs) {
                $scope.allLabs = labs;
            });
            $scope.pendingLabs = [];
            $scope.page = {};
            $scope.page.form = {
                title: "Create Account",
                editProfileMode: false,
                saveButtonTitle: "Create"
            };
            $scope.emailValid = false;
            $scope.buttonPressed = false;
            $scope.accountFormInvalid = false;
            $scope.isEmailAvailable = true;
            $scope.isEmailActivated = true;

            Features.getSsoProperties(function (properties) {
                if (properties.enabled) {
                    $window.location.href = "index.html";
                }
            });

            if ($routeParams.userId != null) {
                Security.findInvited({link: location.pathname + location.hash}, function (user) {
                    $scope.account.email = user.email;
                    $scope.accountHelper.confirmEmail = user.email;
                    $scope.accountHelper.invited = true;
                });
            }

            $("#firstName").focus();

            $scope.save = function (isValid) {
                $scope.buttonPressed = true;
                if (!isValid || $scope.accountFormInvalid) {
                    return;
                }
                var account = jQuery.extend(true, {}, $scope.account);
                account.laboratories = [];
                angular.forEach(account.selectedLaboratories, function (lab) {
                    account.laboratories.push(lab.id);
                });
                delete account.selectedLaboratories;
                delete account.alreadyAddedLaboratories;

                function saveCallback() {
                    $window.location.href = "authentication.html#/emailVerification";
                }

                if (!$scope.accountHelper.invited) {
                    Security.save(account, saveCallback);
                } else {
                    Security.saveInvited(account, saveCallback);
                }
            };

            $scope.isEmail = isEmail;

            $scope.isEmailInstitutional = function (email) {
                if (!email) {
                    return true;
                }
                return $.grep(NotInstitutionalEmailProviders, function (provider) {
                    return email.lastIndexOf("@" + provider) >= 0;
                }).length == 0;
            };

            $scope.$watch("account.email", function () {
                if (isEmail($scope.account.email) && !$scope.accountHelper.invited) {
                    Security.emailAvailable({email: $scope.account.email}, function (result) {
                        $scope.isEmailAvailable = result[0] == "t";
                    });
                } else {
                    $scope.isEmailAvailable = true;
                }
            });

            $scope.$watch("isEmailAvailable", checkIsEmailConfirmed);
            $scope.$watch("accountHelper.confirmEmail", validateForm);
            $scope.$watch("account.password", validateForm);
            $scope.$watch("accountHelper.confirmPassword", validateForm);

            $scope.confirmActivationEmailResend = function () {
                $("#email-not-activated-dialog").modal("show");
            };

            $scope.resendEmail = function () {
                Security.resendActivationEmail({email: $scope.account.email}, function () {
                    $window.location.href = "authentication.html#/emailVerification"; //TODO:2016-05-31:andrii.loboda: duplicated
                });
            };

            function checkIsEmailConfirmed() {
                if (!$scope.isEmailAvailable) {
                    Security.emailActivated({email: $scope.account.email}, function (result) {
                        $scope.isEmailActivated = result[0] == "t";
                    });
                } else {
                    $scope.isEmailActivated = true;
                }
            }

            function validateForm() {
                if ($scope.accountHelper.confirmPassword != $scope.account.password
                    || $scope.accountHelper.confirmEmail != $scope.account.email) {
                    $scope.accountFormInvalid = true;
                } else {
                    $scope.accountFormInvalid = false;
                }
            }
        }
    )
    .controller("unsupported-browser", function ($scope) {

    })
    .controller("forgotPassword", function ($scope, $window, Security) {
        $scope.vm = {
            sendInstructions: function ($event) {
                Security.sendInstructions({email: $("#forgotPasswordEmail").val()}, function (result) {
                    if (result.successMessage) {
                        $scope.instructionsSend = true;
                    }
                    $scope.showError = false;
                    $scope.loginResult = result;
                });
                $event.preventDefault();
                return false;
            },
            redirectToLoginPage: function redirectToLoginPage() { //TODO:2016-05-31:andrii.loboda: duplicated
                $window.location.href = "dashboard.html";
            }

        };
    })
    .controller("emailVerification", function ($scope, $window, Security) {
        $scope.vm = {
            redirectToLoginPage: function redirectToLoginPage() {
                $window.location.href = "dashboard.html";
            },
            confirmActivationEmailResend: function confirmActivationEmailResend() {
                $("#email-not-activated-dialog").modal("show");
            },
            resendEmail: function resendEmail() {
                var email = $("#reSendEmailInput").val();
                Security.resendActivationEmail({email: email}, function () {
                    location.reload();
                });
            },
            emailVerified: false
        };

        Security.isEmailVerified(function (data) {
            $scope.vm.emailVerified = data.verified;
        });

    })
    .directive("emailAvailable", function (Security) {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function (viewValue) {
                    if (ctrl.$error.required || ctrl.$error.email) {
                        ctrl.$setValidity("emailAvailable", true);
                    } else {
                        Security.emailAvailable({email: viewValue}, function (result) {
                            ctrl.$setValidity("emailAvailable", result[0] === "t");
                        });
                    }
                    return viewValue;
                });
            }
        };
    })
    .directive("laboratorySelector", laboratorySelection({
            "emptyTableMessage": "There are no laboratories",
            "addActionText": "Add laboratories",
            "addPlaceHolderText": "Enter laboratory's title"
        })
    )
    .directive("selectLabWithAutoComplete", function ($timeout) {
        return selectLabWithAutoCompleteFn();
    })
    .controller("reset-password", function ($location, $scope, Security, $window) {
        CommonLogger.setTags(["LOGIN", "RESET-PASSWORD-CONTROLLER"]);
        var email = $location.search().email;
        var mac = $location.search().mac;
        CommonLogger.log("Email is " + email);
        CommonLogger.log("Mac is " + mac);
        $scope.errorMacMessage;
        Security.canResetPassword({email: email, mac: mac}, function (res) {
            if (res.errorMessage) {
                $scope.errorMacMessage = res.errorMessage;
            }
        });
        $scope.reset = function () {
            $scope.resettingPassword = true;
            Security.resetPassword({email: email, password: $scope.password}, function () {
                $window.location.href = "dashboard.html";
            });
        };
    })
;

function showConsentToPrivacyPolicyDialog(onOk) {

    var message = "<p>Users of Chorus must acknowledge consent to the <a class=\"consent-to-privacy-policy-link\" href=\"privacy-policy.html\" target=\"_blank\">Chorus Privacy Policy</a> so that we may continue to communicate with you and to provide services to you.</p>";
    var elementId = "userConsentToPrivacyPolicyDialog";
    var elementIdSelector = "#" + elementId;

    if (!$(elementIdSelector).length) {
        var container = $("<div>", {class: "hidden"}).appendTo("body");
        $("<div>", {class: "hidden", id: elementId}).appendTo(container);
    }

    $(elementIdSelector).html(message).dialog({
        title: "Consent to Privacy Policy",
        draggable: false,
        dialogClass: "message-dialog",
        modal: true,
        resizable: false,
        width: 650,
        buttons: {
            "I Consent To The Chorus Privacy Policy": function () {
                $(this).dialog("close");
                onOk && onOk();
            }
        }
    });
}

function showUserChangedDialog(message, onOk) {
    $("#userChangedDialog").html(message).dialog({
        title: "Notification",
        draggable: false,
        dialogClass: "message-dialog warning",
        modal: true,
        resizable: false,
        width: 450,
        buttons: {
            "OK": function () {
                $(this).dialog("close");
                if (onOk) {
                    onOk();
                }
            }
        }
    });
}

function showUserConfirmDialog(message, onOk, onCancel) {
    $("#userChangedDialog").html(message).dialog({
        title: "Confirm",
        draggable: false,
        dialogClass: "message-dialog warning",
        modal: true,
        resizable: false,
        width: 450,
        buttons: {
            "OK": function () {
                $(this).dialog("close");
                if (onOk) {
                    onOk();
                }
            },
            "Cancel": function () {
                $(this).dialog("close");
                if (onCancel) {
                    onCancel();
                }
            }
        }
    });
}

function isEmail(text) {
    if (!text) {
        return false;
    }
    return /^.+@.+\..{2,4}/.test(text.trim());
}

