"use strict";

(function () {

    angular.module("report-issue")
        .controller("issues-list", issuesListController);

    issuesListController.$inject
        = ["$scope", "$rootScope", "$location", "$route", "$routeParams", "$window", "IssueService", "contentRequestParameters"];

    function issuesListController($scope,
                                  $rootScope,
                                  $location,
                                  $route,
                                  $routeParams,
                                  $window,
                                  IssueService,
                                  contentRequestParameters) {
        var ALL_ISSUES_FILTER = "all";
        if ($scope.pathError) {
            return;
        }
        $scope.page.title = "Reported Issues";

        $scope.displayConfirmation = displayConfirmation;
        $scope.showEditDialog = showEditDialog;

        init();

        function init() {
            var filter = $routeParams.filter;
            var request = contentRequestParameters.getParameters("issues");
            $scope.isEmptyTable = false;
            $scope.sorting = {};
            $scope.sorting.reverse = !request.asc;
            $scope.sorting.field = request.sortingField;
            $scope.page.filter = request.filterQuery;
            $scope.total = 0;


            if (filter === ALL_ISSUES_FILTER) {
                IssueService.getAllIssues(request, onIssuesRead);
            } else {
                IssueService.getUserIssues(request, onIssuesRead);
            }
        }

        function showEditDialog(issue) {
            $rootScope.dialogReturnUrl = $location.url();
            $location.url("/report-issue/" + issue.id + "/edit");
        }

        function onIssuesRead(response) {
            $scope.issues = response.items;
            $scope.total = response.itemsCount;
            $scope.isEmptyTable = !response || !response.items || response.items.length <= 0;
        }

        function displayConfirmation(issue) {
            $scope.confirmation = new Confirmation("#remove-issue-confirmation", issue,
                {
                    success: function () {
                        IssueService.deleteIssue({id: issue.id}, $route.reload);
                    },
                    getName: function () {
                        return issue.name;
                    }
                }
            );
            $scope.confirmation.showPopup();
        }

    }

})();
