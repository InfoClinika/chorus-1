"use strict";

(function () {

    angular.module("aggregate-statistics-front", ["ngResource", "aggregate-statistics-back"])
        .controller("aggregate-statistics", function ($scope, AggregateStatistics) {
            var modal = $("#aggregate-statistics-dialog");
            $scope.page.title = "Storage Usage Statistics";
            $scope.statisticsSummaryList = [];
            $scope.statisticsSearches = [];
            $scope.fromDate = formatDateString(new Date(2010, 0, 6));
            $scope.toDate = formatDateString(new Date());
            $scope.deadline = null;
            $scope.loadingInProgress = false;
            $scope.close = close;
            $scope.aggregateStatistics = aggregateStatistics;
            $scope.getStatistics = getStatistics;
            $scope.formatToGBs = formatToGBs;
            $scope.exportToCSV = exportToCSV;
            $scope.onStatisticsSearchSelected = onStatisticsSearchSelected;
            $scope.getStatisticsSearchItem = getStatisticsSearchItem;
            $scope.isActive = isActive;
            init();

            function init() {
                readStatisticsSearches();
            }

            function isActive(deadline) {
                return $scope.deadline === deadline;
            }

            function getStatisticsSearchItem(dateAsString) {
                var date = new Date(Date.parse(dateAsString));

                return formatDateFullString(date);
            }

            function formatDateString(date) {
                var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
                var month = (date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1);

                return month + "/" + day + "/" + date.getFullYear();
            }

            function formatDateFullString(date) {
                var days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
                var months = ["January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December"];

                var day = days[date.getDay()];
                var month = months[date.getMonth()];

                return day + ", " + month + " " + date.getDate() + ", " + date.getFullYear();
            }

            function close() {
                modal.trigger("hide");
            }

            function readStatisticsSearches() {
                AggregateStatistics.getStatisticsSearches(
                    {},
                    function (response) {
                        $scope.statisticsSearches = response;
                    }
                );
            }

            function aggregateStatistics() {
                $scope.loadingInProgress = true;
                var from = $scope.fromDate;
                var to = $scope.toDate;

                AggregateStatistics.aggregateStatistics(
                    {
                        "fromDate": from,
                        "toDate": to
                    },
                    function (response) {
                        // wait for the push notification from the server
                        var subscribeUrl = "../admin/tools/aggregateStatistics/subscribe/" +
                            response.value;

                        var source = new EventSource(subscribeUrl);
                        source.onmessage = function (event) {
                            $scope.statisticsSummaryList = JSON.parse(event.data);
                            $scope.loadingInProgress = false;

                            readStatisticsSearches();
                            $scope.deadline = getStatisticsSearchItem($scope.toDate);

                            $scope.$apply();
                            source.close();
                        };
                    }
                );
            }

            function getStatistics() {
                $scope.loadingInProgress = true;

                AggregateStatistics.getStatistics(
                    {
                        "deadline": $scope.deadline
                    },
                    function (response) {
                        $scope.statisticsSummaryList = response;
                        $scope.loadingInProgress = false;
                    }
                );
            }

            function onStatisticsSearchSelected(deadline) {
                $scope.deadline = deadline;
                getStatistics();
            }

            function exportToCSV() {
                var csv = "LAB_NAME,RAW_FILES_COUNT,RAW_FILES_SIZE_GB,TRANSLATED_FILES_COUNT,TRANSLATED_FILES_SIZE_GB," +
                    "OTHER_FILES_COUNT,OTHER_FILES_SIZE,TOTAL_FILES_SIZE\n";
                $scope.statisticsSummaryList.forEach(function (row) {
                    csv += "\"" + row.labName + "\"";
                    csv += ",";
                    csv += row.rawFilesCount;
                    csv += ",";
                    csv += formatToGBs(row.rawFilesSize);
                    csv += ",";
                    csv += row.translatedFilesCount;
                    csv += ",";
                    csv += formatToGBs(row.translatedFilesSize);
                    csv += ",";
                    csv += row.otherFilesCount;
                    csv += ",";
                    csv += formatToGBs(row.otherFilesSize);
                    csv += ",";
                    csv += formatToGBs(row.totalFilesSize);
                    csv += "\n";
                });

                console.log(csv);

                var hiddenElement = document.createElement("a");
                hiddenElement.href = "data:text/csv;charset=utf-8," + encodeURI(csv);
                hiddenElement.target = "_blank";
                hiddenElement.download = "storage_usage_per_lab.csv";
                hiddenElement.click();
            }

            function formatToGBs(size) {
                var sizeInGBs = size / (1024 * 1024 * 1024);

                return Math.round(sizeInGBs * 1000) / 1000;
            }
        });
})();
