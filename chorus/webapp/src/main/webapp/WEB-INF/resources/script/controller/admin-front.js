angular.module("admin-front", ["admin-tools-back"])
    .controller("broadcast-notification", function($scope, AdminNotifications) {
        $scope.notification = {};
        $scope.save = function() {
            AdminNotifications.save($scope.notification);
        }
    });