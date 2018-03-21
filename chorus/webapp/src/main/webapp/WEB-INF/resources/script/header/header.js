/*global angular:true*/

(function () {

    "use strict";

    angular.module("header", ["appearance", "issues-back"])
        .directive("profileDialogLink", function($location) {
            return {
                restrict: "A",
                link: function ($scope, elem) {

                    var openProfileDialog = function () {
                        if ($scope.isDashboard) {
                            $location.path("/profile");
                            setTimeout(function () {
                                $scope.$apply();
                            }, 0);
                        } else {
                            window.location ="../pages/dashboard.html#/profile";
                        }
                    };

                    elem.bind("click", openProfileDialog);
                }
            };
        })
        .directive("issueTrackerDialog", function ($rootScope, Issues) {
            return {
                restrict: "E",
                templateUrl: "../pages/user/issue-tracker.html",
                link: function($scope, elem, attrs) {
                    //can be included in header one time, so append it to body as usual modal dialog
                    elem.appendTo("body");

                    // --- Global issue reporter ----
                    //todo[tymchenko]: report steps to reproduce
                    var IssueReporter = function(popupSelector) {
                        this.title = "";
                        this.contents = "";
                        this.dialogTitle = "Report an Issue";
                        this.popupSelector = popupSelector;
                        this.dialogTitleSelector = popupSelector + " .modal-header h3";
                        this.issueTitleSelector = popupSelector + " #issue-title";
                        this.issueContentsSelector = popupSelector + " #issue-contents";
                        this.issueStepsSelector = popupSelector + " #issue-steps";
                        this.confirmationSelector = popupSelector + " #report-successful-container";
                        this.confirmationButtonsSelector = popupSelector + " #report-successful-buttons";
                        this.reportFormSelector = popupSelector + " #report-form";
                        this.reportFormButtonsSelector = popupSelector + " #report-form-buttons";
                    };

                    IssueReporter.prototype.showPopup = function(title, contents) {
                        $(this.issueTitleSelector).val("");
                        $(this.issueContentsSelector).val("");
                        $(this.issueStepsSelector).val("");
                        this.title = (title) ? title : "";
                        this.contents = (contents) ? contents : "";
                        $(this.dialogTitleSelector).text("Report an Issue");

                        $(this.confirmationSelector).hide();
                        $(this.confirmationButtonsSelector).hide();

                        $(this.reportFormSelector).show();
                        $(this.reportFormButtonsSelector).show();

                        $(this.popupSelector).modal("show");
                    };

                    IssueReporter.prototype.hidePopup = function () {
                        $(this.popupSelector).modal("hide");
                    };

                    IssueReporter.prototype.reportIssue = function () {

                        var rawTitle = $(this.issueTitleSelector).val();
                        var rawContents = $(this.issueContentsSelector).val();

                        if(rawTitle.length == 0 || rawContents.length == 0) {
                            return;
                        }

                        this.title = rawTitle;
                        var rawSteps = $(this.issueStepsSelector).val();

                        //todo: process steps and replace newlines with proper newlines
                        this.contents = rawContents + "\n\n\n" + rawSteps;

                        Issues.save({title: this.title, contents: this.contents});
                        $(this.confirmationSelector).show();
                        $(this.confirmationButtonsSelector).show();

                        $(this.dialogTitleSelector).text("Thank You!");

                        $(this.reportFormSelector).hide();
                        $(this.reportFormButtonsSelector).hide();
                    };

                    $rootScope.reporter = new IssueReporter("#issue-reporter");
                }
            };
        });
})();
