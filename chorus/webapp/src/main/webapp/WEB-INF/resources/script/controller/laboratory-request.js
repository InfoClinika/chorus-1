angular.module("requestLaboratory", ["security-front", "security-back", "validators", "error-catcher", "current-year", "header"]).
    controller("requestLaboratory", function ($scope, $location, Security) {
        CommonLogger.setTags(["REQUEST-LABORATORY", "REQUEST-LABORATORY-CONTROLLER"]);
        $scope.laboratory = {};

        $scope.request = {isSent: false, error: false};
        $scope.errorMessage = "";
        $scope.successMessage = "You will be notified by email when your request is processed.";
        $scope.isValid = true;

        $scope.loggedInUser = Security.get({path:""});

        $scope.sendRequest = function(isInvalid) {

            if(isInvalid) {  $scope.isValid = false; return;}
            $scope.request.isSent = false;

            Security.labRequest($scope.laboratory, function (response) {
                $scope.request.error = false;
                if(response.errorMessage) {
                    $scope.request.error = true;
                    $scope.errorMessage =  "This lab name has been already used";
                }
                $scope.request.isSent = true;
            });
        };

        $scope.isRequestSent = function () {
            return $scope.request.isSent;
        };

        $scope.isLabRequestError = function () {
            return  $scope.request.error;
        };

        $scope.onCancel = function() {
            window.history.back();
        };

        $scope.getEmptyTableMessage = function () {
            return "There are no requests";
        };

    })
   ;
