
angular.module("request-download-experiment-back", ["ngResource"])
    .factory("SharingProjectRequest", function ($resource) {
        return $resource("../projects/sharing");
    });
