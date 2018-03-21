angular.module("laboratories", ["ngResource"]).
    factory("Laboratories", function ($resource) {
        return $resource("../laboratories/:filter/:labitems/:id", {id:"@id"}, {
              "update": { method: "PUT" },
              "labitems": {method: "GET", params: {filter: "my", labitems: "labitems"}, isArray: true},
                "labsFeatures": {method: "GET", params: {labitems: "enabledFeatures"}}
            });
    }).
    factory("LaboratoryDetails", function ($resource) {
        return $resource("../laboratories/", {}, {
            "update":{ method:"PUT" }
        });
    });
