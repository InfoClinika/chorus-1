angular.module("lab-head-front", ["security-front", "security-back", "lab-head-back", "laboratories", "error-catcher"]).
    controller("laboratoryUsers",function ($scope, $location, $routeParams, $route, LaboratoryUsers, Laboratories) {
        if($scope.pathError) return;
        CommonLogger.setTags(["LAB-HEAD", "LABORATORY-USERS-CONTROLLER"]);
        var lab = Laboratories.get({id: $routeParams.id, filter: "short"}, function(){
            LaboratoryUsers.query({labId: lab.id}, function (users) {
                $scope.users = users;
                isTableEmpty = $scope.users.length == 0;
            });
            $scope.page.title = "Users of " + lab.name;
        });
        $scope.page.title = "Users of Laboratory";
        var isTableEmpty = false;

        $scope.isTableEmpty = function () {
            return isTableEmpty;
        };

        $scope.getEmptyTableMessage = function () {
            return "There are no users";
        };

        $scope.displayConfirmation = function (user){
            $scope.confirmation = new Confirmation("#remove-user-confirmation", user,
                {
                    success:function(){
                        LaboratoryUsers.delete({labId: lab.id, user:user.id}, function(){
                            $route.reload();
                        })
                    },
                    getName: function(){
                        return user.firstName + " " + user.lastName;
                    }
            });
            $scope.confirmation.showPopup();
        };
    });
