"use strict";

(function () {

    angular.module("report-issue")
        .factory("IssueService", function ($resource) {
            return $resource("../issues/:path/:id", {}, {
                "reportIssue": {method: "POST", params: {path: "report"}},
                "editIssue": {method: "POST", params: {path: "edit"}},
                "deleteIssue": {method: "DELETE", params: {path: "delete"}},
                "getPriorities": {method: "GET", params: {path: "priorities"}, isArray: true},
                "getIssueDetails": {method: "GET", params: {path: "details"}},
                "getUserIssues": {method: "GET", params: {path: "my-issues"}},
                "getAllIssues": {method: "GET", params: {path: "all-issues"}}
            });
        });
})();
