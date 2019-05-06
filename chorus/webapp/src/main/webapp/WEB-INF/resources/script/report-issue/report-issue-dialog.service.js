"use strict";

(function () {

    angular.module("report-issue")
        .factory("ReportIssueDialogService", function () {
            var dialogDetails = {};

            return {
                setDialogDetails: function (details) {
                    dialogDetails = details;
                },

                getDialogDetails: function () {
                    return Object.assign({}, dialogDetails);
                },

                clearDialogDetails: function() {
                    this.setDialogDetails(null);
                }
            };
        });
})();
