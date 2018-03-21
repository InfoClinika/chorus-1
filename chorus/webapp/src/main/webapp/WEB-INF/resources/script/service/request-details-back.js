angular.module("requests-details", ["ngResource"]).
    factory("RequestsDetails", function ($resource) {
        return $resource("../requests/details/:type/:path/:request/:requester", {request: "@request", requester: "@requester"}, {
            "approveInstrument":{method: "POST", params:{type: "instrument", path:"approve"}},
            "refuseInstrument":{method:"POST", params:{type: "instrument", path: "refuse"}},
            "approveLabCreation":{method:"POST", params:{type:"lab", path: "approve"}},
            "refuseLabCreation": {method:"POST", params:{type: "lab", path: "refuse"}},
            "details":{method:"GET", params:{}},
            "updateLab": {method:"POST", params:{type: "lab", path:"update"} },
            "updateInstrument": {method:"POST", params: {type: "instrument-creation", path:"update"}},
            "refuseInstrumentCreation":{method:"POST", params:{type: "instrument-creation", path:"refuse"}},
            "approveInstrumentCreation":{method:"POST", params:{type: "instrument-creation", path:"approve"}}
        });
    })
;
