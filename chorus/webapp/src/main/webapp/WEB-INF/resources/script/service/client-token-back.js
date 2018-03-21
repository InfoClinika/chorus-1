(function () {
    angular.module("client-token-back", ["ngResource"])
        .factory("ClientToken", function ($resource) {
            return $resource("../security/:path", {}, {
                "generate": {method: "GET", params: {path: "generateClientToken"}}
            })
        });
})();
