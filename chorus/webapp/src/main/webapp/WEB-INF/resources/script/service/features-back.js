angular.module("features-back", ["ngResource"]).
    factory("Features", function ($resource, $q) {
        var forumProperties = null;
        var ssoProperties = null;
        var privateInstallProperties = null;
        var desktopUploaderProperties = null;
        var autoimporterProperties = null;

        function getProperties(properties, path) {
            var deferred = null;

            return function (callback) {
                if (properties !== null) {
                    return callback(properties);
                }

                if (!deferred) { //first call
                    deferred = $q.defer();
                    $resource(path).get({}, function (value) {
                        properties = value;
                        deferred.resolve(value);
                    });
                }
                deferred.promise.then(callback);
            }
        }

        return {
            getForumProperties: getProperties(forumProperties, "../features/forumProperties"),
            getSsoProperties: getProperties(ssoProperties, "../features/sso"),
            getPrivateInstallProperties: getProperties(privateInstallProperties, "../features/privateInstall"),
            getDesktopUploaderProperties: getProperties(desktopUploaderProperties, "../features/desktopUploader"),
            getAutoimporterProperties: getProperties(autoimporterProperties, "../features/autoimporter")
        };
    });
