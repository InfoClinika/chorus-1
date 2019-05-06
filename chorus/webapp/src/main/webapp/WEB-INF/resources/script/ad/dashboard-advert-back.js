"use strict";

(function () {

    angular.module("advert-back", ["ngResource"])
        .factory("AdvertImage", function ($resource) {
            return $resource("../poster/:path", {id: "@id"},
                {
                    advertToDisplay: {method: "GET", params: {path: "postToDisplay"}},
                    incrementsClickCount: {method: "PUT", params: {path: "incrementsClickCount"}},
                    incrementsDisplayCount: {method: "PUT", params: {path: "incrementsDisplayCount"}}
                }
            );
        })
        .factory("Advertisement", function ($resource) {
            return $resource("../poster/:path/:id", {id: "@id"}, {
                    "read": {method: "GET", isArray: true},
                    "postMetadata": {method: "POST", params: {path: "new"}},
                    "updateImageUrl": {method: "POST", params: {path: "updateImageUrl"}},
                    "updateDetails": {method: "PUT", params: {path: "updateDetails"}},
                    "readDetails": {method: "GET", params: {path: "details"}},
                    "remove": {method: "DELETE"},
                    "getDestinationPath": {method: "GET", params: {path: "destination"}},
                    "maxSizeInBytes": {method: "GET", params: {path: "maxSizeInBytes"}}
                }
            );
        });
})();
