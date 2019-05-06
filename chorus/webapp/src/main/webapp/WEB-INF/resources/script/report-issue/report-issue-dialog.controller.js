"use strict";

(function () {

    angular.module("report-issue")
        .controller("report-issue-dialog", ReportIssueDialogController);


    ReportIssueDialogController.$inject = ["$scope", "$routeParams", "IssueService", "ReportIssueDialogService"];

    function ReportIssueDialogController($scope, $routeParams, IssueService, ReportIssueDialogService) {
        var ISSUE_TYPES = {
            BUG: "BUG",
            IMPROVEMENT: "IMPROVEMENT"
        };

        var dialogDetails = ReportIssueDialogService.getDialogDetails();

        var issueId = $routeParams.issueId;
        var vm = {
            issueTypes: ISSUE_TYPES,
            priorities: [],
            loadingInProgress: false,
            form: {
                title: null,
                issueType: ISSUE_TYPES.BUG,
                priority: null,
                description: null,
                stepsToReproduce: null,
                attachments: [],

                validation: {}
            }
        };
        $scope.vm = vm;

        $scope.onDismiss = function() {
            ReportIssueDialogService.clearDialogDetails();
        };

        vm.isBug = isBug;
        vm.isImprovement = isImprovement;
        vm.submitIssue = submitIssue;

        init();

        function init() {
            CommonLogger.setTags(["REPORT-ISSUE-DIALOG"]);

            IssueService.getPriorities(function (priorities) {
                vm.priorities = priorities;
            });

            if (issueId) {
                vm.loadingInProgress = true;
                IssueService.getIssueDetails({id: issueId}, function (issue) {
                    vm.form.title = issue.name;
                    vm.form.priority = issue.priority;
                    vm.form.issueType = issue.issueType;
                    vm.form.description = issue.description;
                    vm.form.stepsToReproduce = issue.stepsToReproduce;
                    issue.attachments.forEach(function (attachment) {
                        var isImage = false;
                        var content = null;
                        if (attachment.thumbnailBase64Content) {
                            isImage = true;
                            content = "data:" + attachment.type + ";base64," + attachment.thumbnailBase64Content;
                        }

                        vm.form.attachments.push({
                            content: content,
                            name: attachment.name,
                            type: attachment.type,
                            isImage: isImage,
                            fromCloud: true
                        });
                    });

                    vm.loadingInProgress = false;
                });
            } else if (dialogDetails) {
                vm.form.title = dialogDetails.title;
            }
        }

        function isBug() {
            return vm.form.issueType === ISSUE_TYPES.BUG;
        }

        function isImprovement() {
            return vm.form.issueType === ISSUE_TYPES.IMPROVEMENT;
        }

        function validate(form) {
            var isValid = true;
            form.validation = {};

            if (!form.title) {
                form.validation.title = "Title is required";
                isValid = false;
            }

            if (!form.priority) {
                form.validation.priority = "Issue priority is required";
                isValid = false;
            }

            if (!form.description) {
                form.validation.description = "Issue description is required";
                isValid = false;
            }

            return isValid;
        }

        function submitIssue() {
            if (!validate(vm.form)) {
                return;
            }

            ReportIssueDialogService.clearDialogDetails();

            var attachments = [];
            vm.form.attachments.forEach(function (attachment) {
                var base64Content = getAttachmentBase64Data(attachment);
                if (base64Content) {
                    attachments.push({
                        name: attachment.name,
                        base64Content: base64Content,
                        type: attachment.type,
                        thumbnailBase64Content: null
                    });
                }
            });

            var request = {
                title: vm.form.title,
                issueType: vm.form.issueType,
                priority: vm.form.priority,
                description: vm.form.description,
                stepsToReproduce: isImprovement() ? null : vm.form.stepsToReproduce,
                attachments: attachments
            };

            vm.loadingInProgress = true;
            if (issueId) {
                IssueService.editIssue({id: issueId}, request, reportIssueCallback);
            } else {
                IssueService.reportIssue(request, reportIssueCallback);
            }
        }

        function reportIssueCallback() {
            vm.loadingInProgress = false;
            $(".report-issue-dialog").trigger("hide");
        }

        function getAttachmentBase64Data(attachment) {
            if (attachment.fromCloud) {
                return null;
            }
            if (!attachment.content) {
                CommonLogger.warn("No content for attachment: " + JSON.stringify(attachment));
                return null;
            }

            var contentPrefix = "data:" + attachment.type + ";base64,";
            if (!attachment.content.startsWith(contentPrefix)) {
                CommonLogger.warn("Attachment content has wrong format. Content: '" +
                    attachment.content.substr(0, 100) + "'");
                return null;
            }

            return attachment.content.substr(contentPrefix.length);
        }

    }

})();
