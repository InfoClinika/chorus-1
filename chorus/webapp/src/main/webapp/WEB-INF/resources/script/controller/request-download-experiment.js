angular.module("request-download-experiment-front", ["security-front", "request-download-experiment-back", 
    "error-catcher", "current-year"])

    .controller("requestExperimentAccess", ["$scope", "$location", "SharingProjectRequest", function ($scope, $location, SharingProjectRequest) {
        CommonLogger.setTags(["REQUEST-DOWNLOAD-EXPERIMENT", "REQUEST-EXPERIMENT-ACCESS-CONTROLLER"]);
        $scope.requestedLink = decodeURIComponent(urlUtils.getUrlVars().downloadLink);
        $scope.downloadExperimentLinks = [];

        var requestedExperimentId = urlUtils.getUrlVars($scope.requestedLink).experiment;

        SharingProjectRequest.get({experimentId: requestedExperimentId}, function (response) {
            //if sharing request on experiment's projects already exists add one more link to request summary
            $scope.downloadExperimentLinks = response.downloadExperimentLinks;
            if ($scope.downloadExperimentLinks.length > 0 && $scope.downloadExperimentLinks.indexOf($scope.requestedLink) == -1) {
                SharingProjectRequest.save({experimentId: requestedExperimentId, downloadExperimentLink: $scope.requestedLink});
                $scope.downloadExperimentLinks.push($scope.requestedLink);
            }
        });

        $scope.requestAccess = function () {
            $("#request-access-dialog").modal("show");
            SharingProjectRequest.save({experimentId: requestedExperimentId, downloadExperimentLink: $scope.requestedLink});
        };

        $scope.goToDashboard = function () {
            window.location = "dashboard.html";
        }
    }]);
