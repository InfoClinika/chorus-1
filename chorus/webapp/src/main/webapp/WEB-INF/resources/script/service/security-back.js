"use strict";

angular.module("security-back", ["ngResource"])
    .factory("Security", function ($resource) {
        return $resource("../security/:path", {}, {
            "getUserDetails": {method: "GET", params: {path: ""}},
            "loginResult": {method: "GET", params: {path: "loginResult"}},
            "isEmailVerified": {method: "GET", params: {path: "isEmailVerified"}},
            "labs": {method: "GET", params: {path: "labs"}, isArray: true},
            "labRequest": {method: "POST", params: {path: "labRequest"}},
            "isLabHead": {method: "GET", params: {path: "isLabHead"}},
            "showBilling": {method: "GET", params: {path: "showBilling"}},
            "isLabHeadOfLab": {method: "GET", params: {path: "isLabHeadOfLab"}},
            "emailAvailable": {method: "GET", params: {path: "emailAvailable"}},
            "sendInstructions": {method: "GET", params: {path: "sendInstructions"}},
            "resetPassword": {method: "GET", params: {path: "resetPassword"}},
            "isMacValid": {method: "GET", params: {path: "isMacValid"}},
            "invite": {method: "GET", params: {path: "inviteUser"}},
            "findInvited": {method: "GET", params: {path: "invited"}},
            "saveInvited": {method: "POST", params: {path: "saveInvited"}},
            "update": {method: "PUT"},
            "changePassword": {method: "PUT", params: {path: "changePassword"}},
            "features": {method: "GET", params: {path: "features"}},
            "userLabsWithEnabledFeature": {method: "GET", params: {path: "userLabsWithEnabledFeature"}, isArray: true},
            "enabledBillingFeatures": {method: "GET", params: {path: "enabledBillingFeatures"}},
            "enabledFeatures": {method: "GET", params: {path: "enabledFeatures"}},
            "getEmailRequest": {method: "GET", params: {path: "getEmailRequest"}},
            "sendEmailRequest": {method: "PUT", params: {path: "emailRequest"}},
            "resendEmailRequest": {method: "GET", params: {path: "resendEmailRequest"}},
            "cancelEmailRequest": {method: "GET", params: {path: "cancelEmailRequest"}},
            "resendActivationEmail": {method: "GET", params: {path: "resendActivationEmail"}},
            "emailActivated": {method: "GET", params: {path: "emailActivated"}},
            "shouldShowBillingNotification": {method: "GET", params: {path: "shouldShowBillingNotification"}},
            "removeBillingNotification": {method: "PUT", params: {path: "removeBillingNotification"}},
            "canResetPassword": {method: "GET", params: {path: "canResetPassword"}},
            "getConsentToPrivacyPolicyDate": {method: "GET", params: {path: "getConsentToPrivacyPolicyDate"}},
            "setConsentToPrivacyPolicyDate": {method: "PUT", params: {path: "setConsentToPrivacyPolicyDate"}},
            "getAccountRemovalRequestDate": {method: "GET", params: {path: "getAccountRemovalRequestDate"}},
            "requestAccountRemoval": {method: "PUT", params: {path: "requestAccountRemoval"}},
            "revokeAccountRemoval": {method: "PUT", params: {path: "revokeAccountRemoval"}}
        });
    });
