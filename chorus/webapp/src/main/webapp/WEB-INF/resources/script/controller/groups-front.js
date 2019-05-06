"use strict";
(function () {

    angular
        .module(
            "groups-front",
            ["groups", "users", "projects", "security-front", "security-back", "front-end", "error-catcher"]
        )
        .controller(
            "groups",
            function ($scope, $location, $routeParams, $route, Groups, contentRequestParameters, expandMenuGroup) {

                if ($scope.pathError) {
                    return;
                }
                CommonLogger.setTags(["GROUPS", "GROUPS-CONTROLLER"]);
                $scope.page.title = "Sharing Groups";
                $scope.page.showFilter = true;
                var isEmptyTable = false;

                var request = contentRequestParameters.getParameters("groups");
                $scope.sorting = {};
                $scope.sorting.reverse = !request.asc;
                $scope.sorting.field = request.sortingField;
                $scope.page.filter = request.filterQuery;
                $scope.groups = Groups.query(function () {
                    isEmptyTable = $scope.groups.length == 0;
                });

                expandMenuGroup($scope);

                //TODO: add logic for projects

                $scope.filter = $routeParams.filter;

                $scope.isTableEmpty = function () {
                    return isEmptyTable;
                };

                $scope.getEmptyTableMessage = function () {
                    return "There are no groups";
                };

                $scope.displayConfirmation = function (sharingGroup) {
                    $scope.confirmation = new Confirmation("#remove-sharing-group-confirmation", sharingGroup,
                        {
                            success: function () {
                                Groups.delete({group: sharingGroup.id}, function () {
                                    $route.reload();
                                });
                            },
                            getName: function () {
                                return sharingGroup.name;
                            }
                        }
                    );
                    $scope.confirmation.showPopup();
                };
            }
        )
        .controller("groupDetails", function ($scope, $location, $routeParams, Groups, Users, Security) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["GROUPS", "GROUP-DETAILS--CONTROLLER"]);
            var noMembersMsg = "There are no members";
            var noProjectsMsg = "There are no projects";
            $scope.members = [];
            $scope.sharedProjects = [];
            $scope.excludeEmails = [];
            $scope.users = Users.query();
            Security.get({path: ""}, function (user) {
                $scope.excludeEmails = [user.username];
            });
            Groups.get({id: $routeParams.id}, function (response) {
                if (response.errorMessage) {
                    $scope.returnUrl = $scope.defaultUrl;
                    hideModal();
                    return;
                }
                $scope.details = response.details;
                $scope.members = $scope.details.members;
                $scope.sharedProjects = $scope.details.sharedProjects;
                $scope.isNameDuplicated = function () {
                    if (!$scope.details.name) {
                        return false;
                    }
                    var groups = $.grep(myGroups, function (item) {
                        return item.name.toUpperCase() === $scope.details.name.toUpperCase().trim();
                    });
                    return groups.length != 0;
                };
            });


            var groupDetailsDialog = $("#groupDetails");
            groupDetailsDialog.modal("show");

            var myGroups = [];
            Groups.query(function (items) {
                myGroups = $.grep(items, function (item) {
                    return item.id != $routeParams.id;
                });
            });

            $scope.addMemberToGroup = function () {
                addMemberToGroup($scope.users, $scope.members);
            };

            $scope.removeUserFromMembers = function (member) {
                $scope.members = removeUserFromGroupMembers(member, $scope.members);
            };


            $scope.save = function (isInvalid) {
                $scope.buttonPressed = true;
                if (isInvalid) {
                    return;
                }
                var group = {};
                group.id = $scope.details.id;
                group.name = $scope.details.name;

                group.members = $.map($scope.members, function (el) {
                    return parseInt([
                        [el.id]
                    ]);
                });

                Groups.update(group, function (response) {
                    if (response.errorMessage) {
                        $scope.updateGroupError = {message: "Group with such name already exists"};
                        $(".alert-modal").fadeIn(500);
                        return;
                    }
                    CommonLogger.log("Group Saved");
                    hideModal();
                });
            };

            $scope.getCountOfProjects = function () {
                var projectCount = $scope.sharedProjects.length;
                if (projectCount === 0) {
                    return Messages.NO_PROJECTS;
                } else if (projectCount === 1) {
                    return projectCount + " " + Messages.PROJECT;
                }
                return projectCount + " " + Messages.PROJECTS;

            };

            $scope.inviteHandler = function (item, callback) {
                Security.invite({email: item.email}, function (invited) {
                    callback(invited);
                });
            };

            $scope.getCountOfMembers = function () {
                return getCountOfMembers($scope.members.length);
            };

            $scope.isTableWithMembersEmpty = function () {
                return $scope.members.length == 0;
            };


            $scope.getEmptyTableWithMembersMessage = function () {
                if ($scope.members.length == 0) {
                    return noMembersMsg;
                }
            };

            $scope.isTableWithProjectsEmpty = function () {
                return $scope.sharedProjects.length == 0;
            };

            $scope.getEmptyTableWithProjectsMessage = function () {
                if ($scope.sharedProjects.length == 0) {
                    return noProjectsMsg;
                }
            };

        })
        .controller("new-group", function ($scope, $location, Groups, Users, Security) {
            CommonLogger.setTags(["GROUPS", "NEW-GROUP-CONTROLLER"]);
            $scope.members = [];
            $scope.excludeEmails = [];
            $scope.users = Users.query();
            $scope.group = {};
            Security.get({path: ""}, function (user) {
                $scope.excludeEmails = [user.username];
            });
            var noMembersMsg = "There are no members";

            $scope.addMemberToGroup = function () {
                addMemberToGroup($scope.users, $scope.members);
            };
            var myGroups = Groups.query();

            $scope.inviteHandler = function (item, callback) {
                Security.invite({email: item.email}, function (invited) {
                    callback(invited);
                });
            };

            $scope.isNameDuplicated = function () {
                if (!$scope.group.name) {
                    return false;
                }
                var groups = $.grep(myGroups, function (item) {
                    return item.name.toUpperCase() === $scope.group.name.toUpperCase().trim();
                });
                return groups.length != 0;
            };

            $scope.save = function (isInvalid) {
                $scope.buttonPressed = true;
                if (isInvalid) {
                    return;
                }
                $scope.group.members = $.map($scope.members, function (member) {
                    return parseInt([
                        [member.id]
                    ]);
                });

                Groups.save($scope.group, function (response) {
                    if (response.errorMessage) {
                        $scope.createGroupError = {message: "Group with such name already exists"};
                        $(".alert-modal").fadeIn(500);
                        return;
                    }
                    $scope.returnUrl = undefined;
                    CommonLogger.log("Group Saved");
                    hideModal();
                });
            };

            $scope.getCountOfMembers = function () {
                return getCountOfMembers($scope.members.length);
            };

            $scope.removeUserFromMembers = function (member) {
                $scope.members = removeUserFromGroupMembers(member, $scope.members);
            };

            $scope.isTableWithMembersEmpty = function () {
                return $scope.members.length == 0;
            };

            $scope.getEmptyTableWithMembersMessage = function () {
                if ($scope.members.length == 0) {
                    return noMembersMsg;
                }
            };
        })
        .directive("groupSelector", userOrGroupSelection({
            "isEmailNotificationsAvailable": false,
            "groupSelectionAvailable": false,
            "emptyTableMessage": "There are no members",
            "addActionText": "Invite people personally",
            "addPlaceHolderText": "Enter person's email"
        }))
        .directive("groupDetails", detailsLink({"title": "Show Group Details", "dataTarget": "#groupDetails"}))
        .factory("expandMenuGroup", function (Groups) {
            return initExpandMenu(function (group) {
                Groups.get({id: group.id}, function (response) {
                    group.details = response.details;
                });
            });
        });

    function removeUserFromGroupMembers(user, members) {
        return jQuery.grep(members, function (elem, index) {
            return elem.email != user.email;
        });
    }

    function addMemberToGroup(users, members) {
        var fieldValue = $("#memberFieldInput").val();
        var found = jQuery.grep(users, function (elem, index) {
            return elem.email == fieldValue;
        });

        if (found.length > 0) {
            // check if user is a member
            var foundInMembers = jQuery.grep(members, function (elem, index) {
                return elem.email == found[0].email;
            });
            if (foundInMembers.length == 0) {
                $("#memberFieldInput").val("");
                members.push(found[0]);
            }
        }
    }

})();
