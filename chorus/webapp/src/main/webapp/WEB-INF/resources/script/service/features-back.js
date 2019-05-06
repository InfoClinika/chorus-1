"use strict";

angular.module("features-back", ["ngResource"])
    .factory("Features", function ($resource, $q) {
        var forumProperties = null;
        var ssoProperties = null;
        var privateInstallProperties = null;
        var desktopUploaderProperties = null;
        var autoimporterProperties = null;
        var globalSearchProperties = null;
        var alisProperties = null;
        var ltqProperties = null;

        function getProperties(properties, path) {
            var deferred = null;

            return function (callback) {
                if (properties !== null) {
                    callback(properties);
                    return;
                }

                if (!deferred) { //first call
                    deferred = $q.defer();
                    $resource(path).get({}, function (value) {
                        properties = value;
                        deferred.resolve(value);
                    });
                }
                deferred.promise.then(callback);
            };
        }

        return {
            getForumProperties: getProperties(forumProperties, "../features/forumProperties"),
            getSsoProperties: getProperties(ssoProperties, "../features/sso"),
            getPrivateInstallProperties: getProperties(privateInstallProperties, "../features/privateInstall"),
            getDesktopUploaderProperties: getProperties(desktopUploaderProperties, "../features/desktopUploader"),
            getAutoimporterProperties: getProperties(autoimporterProperties, "../features/autoimporter"),
            getALISProperties: getProperties(alisProperties, "../features/alis"),
            getLTQProperties: getProperties(ltqProperties, "../features/ltq")
        };
    });
