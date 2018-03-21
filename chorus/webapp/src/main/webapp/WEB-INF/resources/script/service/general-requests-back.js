angular.module("general-requests", ["ngResource"]).
    factory("GeneralRequests", function ($resource) {
        return $resource("../requests/:type/:path/:instrument", {}, {
            "approveInstrument":{method:"POST", params:{type:"instrument", path:"approve"}},
            "approveMembership":{method:"POST", params:{type:"membership",path:"approve"}},
            "refuseInstrument":{method:"POST", params:{type:"instrument", path: "refuse"}},
            "refuseMembership":{method:"POST", params:{type:"membership", path: "refuse"}},
            "inbox":{method:"GET", params:{path:"inbox"}, isArray:true},
            "outbox":{method:"GET", params:{path:"outbox"}, isArray:true},
            "delete":{method:"DELETE", params:{type:"@id"}},
            "memberships":{method:"GET", params:{type:"memberships"}, isArray:true},
            "applyForInstrument":{method:"POST", params:{type:"instrument", path:"new"}},
            "applyForMembership":{method:"POST", params:{type:"membership", path:"new", instrument: "@instrument"}}
        });
    })
    .factory("Inbox", function($resource){
        return $resource("../requests/inbox/:path",{path:"@id"},{
            "approve":{method:"POST", params:{action:"approve"}},
            "refuse":{method:"POST", params:{action:"refuse"}},
            "bulkMarkAsRead":{method:"POST", params: {path: "bulkMarkAsRead"}}
        });
    })
    .factory("Outbox", function($resource){
        return $resource("../requests/outbox/:path",{path:"@id"}, {
            "bulkMarkAsRead":{method:"POST", params: {path: "bulkMarkAsRead"}}
        });
    })
    .factory("GeneralRequestsCounter", function ($resource) {
        return $resource("../requests/:path/count", {}, {
            "inbox":{method:"GET", params:{path:"inbox"}},
            "outbox":{method:"GET", params:{path:"outbox"}}
        });
    })

;
