"use strict";

(function () {

    angular.module("labMembership-front", ["error-catcher", "template-components"])
        .controller("confirmLabMembershipPageCtrl", ["$scope", function ($scope) {
            CommonLogger.setTags(["LAB-MEMBERSHIP", "CONFIRM-LAB-MEMBERSHIP-CONTROLLER"]);
            $scope.message = [];
            $scope.result = "";

            $scope.message.labMemberFirstName = urlUtils.getUrlVars().labMemberFirstName;
            $scope.message.labMemberSecondName = urlUtils.getUrlVars().labMemberSecondName;
            $scope.message.labName = urlUtils.getUrlVars().labName;
            $scope.result = urlUtils.getUrlVars().result; //approve, refuse, deprecated
        }]);

})();
