
angular.module("billing-back", ["ngResource"]).
    factory("Billing", function ($resource) {
        return $resource("../billing/:path/:id", {}, {
            "getInvoice": {method: "GET", params: {path: "invoice"}},
            "register": {method: "GET", params: {path: "register"}},
            "topUp": {method: "GET", params: {path: "topUp"}},
            "payByStore": {method: "GET", params: {path: "payByStore"}},
            "allHistory": {method: "GET", params: {path: "allHistory"}},
            "moreHistory": {method: "GET", params: {path: "moreHistory"}},
            "moreMonthlyHistoryReference": {method: "GET", params: {path: "moreMonthlyHistoryReference"}},
            "list": {method: "GET", isArray: true, params: {path: "list"}},
            "labDetails": {method: "GET", params: {path: "labDetails"}},
            "listAll": {method: "GET", params: {path: "paged"}},
            "enable": {method: "GET", params: {path: "enable"}},
            "disable": {method: "GET", params: {path: "disable"}},
            "subscribe": {method: "GET", params: {path: "subscribe"}},
            "unsubscribe": {method: "GET", params: {path: "unsubscribe"}},
            "featureInfo": {method: "GET", params: {path: "featureInfo"}},
            "listBillingPlans": {method: "GET", params: {path: "billingPlans"}, isArray: true},
            "listFeatures": {method: "GET", params: {path: "features"}, isArray: true},
            "updatePlan": {method: "PUT", params: {path: "updatePlan"}},
            "cancelUpdatePlan": {method: "PUT", params: {path: "cancelUpdatePlan"}},
            "listLabAccountFeatures": {method: "GET", params: {path: "labAccountFeatures"}},
            "updateLabAccountSubscriptionDetails": {method: "PUT", params: {path: "updateLabAccountSubscriptionDetails"}},
            "updateProcessingFeatureState": {method: "PUT", params: {path: "updateProcessingFeatureState"}},
            "adminTopUp": {method: "POST", params: {path: "topup"}},
            "getFeaturesPrices": {method: "GET", params: {path: "featuresPrices"}},
            "makeAccountFree": {method: "PUT", params: {path: "makeAccountFree"}},
            "makeAccountEnterprise": {method: "PUT", params: {path: "makeAccountEnterprise"}},
            "getBillingProperties": {method: "GET", params: {path: "getBillingProperties"}},
            "getPendingCharges": {method: "GET", params: {path: "getPendingCharges"}},
            "getStorageUsage": {method: "GET", params: {path: "getStorageUsage"}},
            "checkCanMakeAccountFree": {method: "GET", params: {path: "checkCanMakeAccountFree"}}
        });
    });
