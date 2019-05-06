"use strict";

(function () {
    angular.module("feature-service", ["security-back"])
        .factory("FeatureProvider", function (Security) {
            let labBillingFeatures = {};
            let labFeatures = {};

            function isBillingFeatureAvailable(feature, labId) {
                if (labId) {
                    return labBillingFeatures[labId] && labBillingFeatures[labId].indexOf(feature) >= 0;
                }
                for (const laboratoryId in labBillingFeatures) {
                    if (labBillingFeatures.hasOwnProperty(laboratoryId)
                        && labBillingFeatures[laboratoryId].indexOf(feature) >= 0) {
                        return true;
                    }
                }
                return false;
            }

            function isFeatureAvailable(feature, labId) {
                if (labId) {
                    return labFeatures[labId] && labFeatures[labId].indexOf(feature) >= 0;
                }
                for (const laboratoryId in labFeatures) {
                    if (labFeatures.hasOwnProperty(laboratoryId)
                        && labFeatures[laboratoryId].indexOf(feature) >= 0) {
                        return true;
                    }
                }
                return false;
            }

            function getEnabledBillingFeatures(labIds, onSuccess) {
                Security.enabledBillingFeatures({labIds}, function (features) {
                    labBillingFeatures = features;
                    onSuccess(features);
                });
            }

            function getEnabledFeatures(labIds, onSuccess) {
                Security.enabledFeatures({labIds}, function (features) {
                    labFeatures = features;
                    onSuccess(features);
                });
            }

            function getLabFeatures() {
                return labFeatures;
            }

            return {
                isBillingFeatureAvailable: isBillingFeatureAvailable,
                isFeatureAvailable: isFeatureAvailable,
                getEnabledBillingFeatures: getEnabledBillingFeatures,
                getEnabledFeatures: getEnabledFeatures,
                getLabFeatures: getLabFeatures
            };
        });
})();
