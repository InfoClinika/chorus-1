angular.module("issues-back", ["ngResource"]).
    factory("Issues", function ($resource) {
        return $resource("../issues", {},{});
    });






