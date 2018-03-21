angular.module("protein-search-back", ["ngResource"])
    .factory("ProteinDatabase", function ($resource) {
        return $resource("../proteindbs/:path/", {}, {
            allAvailable: {method: "GET", isArray: true, params: {path: "allAvailable"}}
        });
    })
    .factory("ProteinDB", function ($resource) {
        return $resource("../proteindbs/:path/:id", {id: "@id"}, {
            "items": {method: "GET", isArray: true, params: {path: "items"}},
            "itemsAccessibleByUser": {method: "GET", isArray: true, params: {path: "itemsAccessibleByUser"}},
            "myItems": {method: "GET", isArray: true, params: {path: "my"}},
            "publicItems": {method: "GET", isArray: true, params: {path: "public"}},
            "postMetadata": {method: "POST", params: {path: "items"}},
            "updateContent": {method: "POST", params: {path: "update"}},
            "updateDetails": {method: "PUT"},
            "remove": {method: "DELETE"},
            "getDestinationPath": {method: "GET", params: {path: "destination"}},
            "maxSizeInBytes": {method: "GET", params: {path: "maxSizeInBytes"}},
            "share": {method: "PUT", params: {path: "share"}},
            "duplicate" : {method: "POST", params: {path: "duplicate"}}
        });
    });

