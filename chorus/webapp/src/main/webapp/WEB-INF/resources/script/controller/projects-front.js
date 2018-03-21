angular.module("projects-front", ["projects", "users", "groups", "breadcrumbs", "js-upload", "security-front", "security-back", "front-end", "error-catcher", "dashboard-common-directives"])
    .controller("projects", function ($scope, $rootScope, $location, $routeParams, Projects, $route, removeProjectConfirmation,
                                      contentRequestParameters, projectsExpandMenu, PaginationPropertiesSettingService, ProjectColumns, changeableColumnsHelper) {

        if ($scope.pathError) return;
        CommonLogger.setTags(["PROJECTS", "PROJECTS-CONTROLLER"]);

        $scope.page.title = "Projects";
        $scope.page.filterScope = $scope;
        $scope.page.showPageableFilter = true;
        $scope.total = 0;
        $scope.page.changeableProjectsColumns = true;
        var isTableEmpty = false;

        var pageRequest = contentRequestParameters.getParameters("projects");
        pageRequest.labId = 0;
        if($routeParams.labId){
            pageRequest.labId = $routeParams.labId;
        }

        changeableColumnsHelper($scope, ProjectColumns);

        Projects.get(pageRequest, function (response) {
            $scope.projects = response.items;
            PaginationPropertiesSettingService.setPaginationProperties($scope, response);
            isTableEmpty = $scope.projects.length == 0;
        });

        $scope.filter = $routeParams.filter;
        $scope.page.subtitle = $scope.$eval("filter | filterToString");

        projectsExpandMenu($scope);

        $scope.isTableEmpty = function () {
            return isTableEmpty;
        };

        $scope.getEmptyTableMessage = function () {
            return "There are no projects";
        };

        $scope.displayConfirmation = removeProjectConfirmation($scope);
    })
    .run(function (registerBreadcrumbHandler) {
        registerBreadcrumbHandler(function (path) {
            var match = path.match(/\/projects\/(all|my|shared|public)\/([\d]+)\/experiments$/);
            if (!match) {
                return null;
            }
            var filter = match[1];
            return {label: filter[0].toUpperCase() + filter.substring(1) + " Projects", url: "#" + path.replace(/\/([\d]+)\/experiments/, "")};
        });
    })
    .controller("project-details",function ($rootScope, $scope, $location, $routeParams, ProjectDetails, Projects, Users, Groups,
                                            ProjectAttachments, startNewUpload, Security, Laboratories, ProjectShortDetails, savingProject) {
        if ($scope.pathError) return;
        CommonLogger.setTags(["PROJECTS", "PROJECT-DETAILS-CONTROLLER"]);
        $scope.showDetailsDialog = true;
        $scope.groups = Groups.query({includeAllUsers: true});
        $scope.page.title = "Project Details";
        $scope.shared = {};
        $scope.shared.sharedGroups = [];
        $scope.shared.sharedUsers = [];
        $scope.shared.invitedUsers = [];
        $scope.excludeEmails = [];
        $scope.shared.withEmailNotification = false;
        $scope.labs = [];
        $scope.createNewProjectMode = false;
        $scope.details = {};
        $scope.details.lab = undefined;
        $scope.shared.inviteHandler = function (item, callback) {
            Security.invite({email: item.email}, function (user) {
                callback(user);
            });
        };

        var attachmentsInitialized = false;

        $scope.users = Users.query();
        CommonLogger.log($scope.excludeEmails);
        ProjectDetails.get({id: $routeParams.project}, function (response) {
            if (response.errorMessage) {
                $scope.returnUrl = $scope.defaultUrl;
                hideModal();
                return;
            }
            var project = response.details;
            $scope.details = project;
            //since ngSwitch creates a new scope and requires object to propagate field changes
            $scope.details.descriptionObj = {value: ""};
            $scope.details.descriptionObj.value = project.description;
            $scope.returnUrl = $rootScope.returnUrl;
            $scope.shared.sharedGroups = $scope.details.sharedGroups;
            $scope.shared.sharedUsers = $scope.details.sharedPersons;
            $scope.excludeEmails.push($scope.details.ownerEmail);
            CommonLogger.log($scope.excludeEmails);

            $scope.$watch("loggedInUser", function () {
                $scope.editMode = ($scope.getLoggedUserName() == $scope.details.ownerEmail || $scope.getUserId() == project.labHead);
                if (!attachmentsInitialized) {
                    AttachmentsHelper.commonSetup($scope, $scope.details.projectId, "#project-details-dialog", ProjectAttachments,
                        startNewUpload, "../attachments/project/download/", $scope.editMode);
                    attachmentsInitialized = true;
                }

            });

            Security.labs(function (labs) {
                $scope.labs = labs;
                /*$.grep(labs, function (lab) {
                 return $.inArray(lab.id, labs) != -1;
                 });*/
            });


        });


        $scope.projectsShortDetails = ProjectShortDetails.query({filter: "my"});
        $scope.isProjectNameDuplicated = function () {
            var projects = $.grep($scope.projectsShortDetails, function (projectShortDetails) {
                return (projectShortDetails.name == $scope.details.name) && $scope.details.projectId != projectShortDetails.id;
            });
            return projects.length != 0;
        };
        $scope.getLabName = function (labId) {
            if (typeof labId == "undefined") return;
            if (labId == null) {
                return "No Laboratory";
            }
            var labs = $.grep($scope.labs, function (lab) {
                return (lab.id == labId);
            });
            if (labs.length == 0) {
                return;
            }
            return labs[0].name;
        };

        var savingProjectFactory = savingProject($scope);
        $scope.save = savingProjectFactory.save;
        $scope.saveProjectSharedWithUnregisteredUsers = savingProjectFactory.saveWithUnregisteredUsers;
        $scope.saveWithUnregisteredUsersDialogMessages = savingProjectFactory.saveWithUnregisteredUsersDialogMessages;

        $scope.saveProject = function () {
            var project = {};
            project.name = $scope.details.name;
            project.projectId = $scope.details.projectId;
            project.areaOfResearch = $scope.details.areaOfResearch;
            project.description = $scope.details.description;
            project.lab = $scope.details.lab;
            project.areaOfResearch = $scope.details.areaOfResearch;
            project.withEmailNotification = $scope.shared.withEmailNotification;
            project.description = $scope.details.descriptionObj.value;
            project.blogEnabled = $scope.details.blogEnabled;

            setSharingPolicy(project, $scope.shared.sharedUsers, $scope.shared.sharedGroups);

            Projects.update(project, function (data) {
                CommonLogger.log("Project Updated. Response: " + JSON.stringify(data));
                ProjectAttachmentsHelper.completeAttachment($scope, data.projectId, ProjectAttachments, function () {
                    setTimeout(function () {
                        $(".modal").modal("hide");
                    }, 0);
                });
            });
        };

        $scope.$watch(function () {
            if ($scope.shared.sharedUsers.length + $scope.shared.sharedGroups.length == 0) {
                return "PRIVATE";
            }
            if ($.grep($scope.shared.sharedGroups,function (group) {
                return group.name.toLowerCase() == "all";
            }).length > 0) {
                return "PUBLIC"
            }
            return "SHARED";
        }, function (level) {
            $scope.shared.accessLevel = level;
        });

        $scope.getCountOfMembers = function () {
            return getCountOfMembers($scope.shared.sharedUsers.length + $scope.shared.sharedGroups.length);
        };

        $scope.sharedCount = function () {
            return $scope.shared.sharedUsers.length + $scope.shared.sharedGroups.length;
        };

        $scope.isTableWithMembersEmpty = function () {
            return $scope.sharedUsersAndGroups.length == 0;
        };

        $scope.getEmptyTableWithMembersMessage = function () {
            return "There are no members";
        };

    }).
    controller("projectColumnsEditor", function ($scope, ProjectColumns, columnsEditor) {
        columnsEditor($scope, ProjectColumns);

    }).
    controller("project-copy", function ($scope, $routeParams, ProjectCopy, ProjectDetails, Users, Security) {
        CommonLogger.setTags(["PROJECTS", "PROJECT-COPY-CONTROLLER"]);
        $scope.users = Users.query();
        $scope.newOwners = [];
        $scope.invitedOwnerUsers = [];
        $scope.excludeEmails = [];
        ProjectDetails.get({id: $routeParams.project}, function (response) {
            if (response.errorMessage) {
                $scope.returnUrl = $scope.defaultUrl;
                hideModal();
                return;
            }
            $scope.excludeEmails.push(response.details.ownerEmail);
            $scope.projectName = response.details.name;
        });
        $scope.emailNotifications = true;
        $scope.inviteHandler = function (item, callback) {
            Security.invite({email: item.email}, function (user) {
                callback(user);
            });
        };
        $scope.getCountOfMembers = function () {
            getCountOfMembers($scope.newOwners);
        };
        $scope.createCopy = function (isInvalid) {
            if (isInvalid) return;
            if ($scope.invitedOwnerUsers.length > 0) {
                $scope.dialogNotToReturn = true;
                $(".modal").modal("hide");
                $("#confirm-action-for-unregistered").modal({
                    show: true,
                    keyboard: false,
                    backdrop: "static"
                });
            } else {
                $scope.createCopyHandler();
            }
        };
        $scope.createCopyHandler = function () {
            $.each($scope.newOwners, function (index, user) {
              /*  ProjectCopy.copy({newOwner: user.id, id: $routeParams.project,
                    emailNotification: $scope.emailNotifications}, function (res) {
                    CommonLogger.log(res);
                })*/

                ProjectCopy.sendCopyConfirmation({newOwner: user.id, project: $routeParams.project}, function(result) {
                    CommonLogger.log(result);
                })
            });
            $(".modal").modal("hide");
        };
        $scope.createCopyAndForUnregisteredUsers = function () {
            angular.forEach($scope.invitedOwnerUsers, function (user) {
                inviteUser(user);
            });
            var invitedUsersResponse = 0;

            function inviteUser(item) {
                $scope.inviteHandler(item, createCopyIfAllInvited);
                function createCopyIfAllInvited(invited) {
                    invited.name = invited.firstName + " " + invited.lastName;
                    $scope.newOwners.push(invited);
                    invitedUsersResponse++;
                    if (invitedUsersResponse == $scope.invitedOwnerUsers.length) {
                        $scope.createCopy();
                    }
                }
            }
        };
        $scope.createCopyAndForUnregisteredUsersDialogMessages = {
            title: "Confirm Project Copying",
            dialogInformation: "Unregistered users have been added. Would you like to invite them?",
            plainButtonText: "Remove from copying list",
            confirmButtonText: "Invite"
        };
    })
    .directive("projectCopyingSelector", userOrGroupSelection({
        "isEmailNotificationsAvailable": true,
        "groupSelectionAvailable": false,
        "emptyTableMessage": "No members",
        "addActionText": "Specify people, for whom you want to create a copy.",
        "addPlaceHolderText": "Enter person's email",
        "showAllowWrite": false,
        "getInvitedUsers" : true})
    ).
    controller("newProject", function ($scope, $location, Projects, Users, Groups, ProjectAttachments, startNewUpload,
                                       Security, Laboratories, ProjectShortDetails, savingProject) {
        if ($scope.pathError) return;
        $scope.project = {};
        $scope.groups = [];
        $scope.shared = {};
        $scope.labs = [];
        $scope.createNewProjectMode = true;
        $scope.shared.sharedUsers = [];
        $scope.shared.sharedGroups = [];
        $scope.shared.invitedUsers = [];
        $scope.myJsonString = null;
        $scope.shared.withEmailNotification = false;
        $scope.users = [];
        $scope.excludeEmails = [];
        $scope.projectsShortDetails = [];
        $scope.shared.inviteHandler = function (item, callback) {
            Security.invite({email: item.email}, function (user) {
                callback(user);
            });
        };

        var savingProjectFactory = null;

        init();
        function init() {
            CommonLogger.setTags(["PROJECTS", "NEW-PROJECT-CONTROLLER"]);
            $scope.groups = Groups.query({includeAllUsers: true});
            $scope.users = Users.query();
            $scope.projectsShortDetails = ProjectShortDetails.query({filter: "my"});
            Security.get({path: ""}, function (user) {
                $scope.excludeEmails = [user.username];
                Laboratories.labitems(function (labs) {
                    var labs = $.grep(labs, function (lab) {
                        return $.inArray(lab.id, user.labs) != -1;
                    });
                    var noLab = {id: -1, name: "-- No lab --"};
                    labs.unshift(noLab);
                    $scope.labs = labs;
                    $scope.project.lab = getDefaultOptionValue($scope.labs);
                });
            });
            savingProjectFactory = savingProject($scope);
            AttachmentsHelper.commonSetup(
                $scope,
                $scope.project.projectId,
                "#project-create-dialog",
                ProjectAttachments,
                startNewUpload,
                "../attachments/project/download/",
                true,
                null,
                {
                    displayDragAndDropAreaOnlyIfAreaPresented: true,
                    allowAllFileTypes: true
                }
            );
        }

        $scope.isProjectNameDuplicated = function () {
            if (!$scope.project.name) return false;
            var projects = $.grep($scope.projectsShortDetails, function (projectShortDetails) {
                return (projectShortDetails.name.trim().toUpperCase() === $scope.project.name.trim().toUpperCase());
            });
            return projects.length != 0;
        };

        $scope.isFormInvalid = function () {
            return !$scope.project.name || $scope.project.name.trim().length == 0
                || !$scope.project.lab
                || !$scope.project.areaOfResearch || $scope.project.areaOfResearch.trim().length == 0;
        };

        $scope.getCountOfMembers = function () {
            return getCountOfMembers($scope.shared.sharedUsers.length + $scope.shared.sharedGroups.length);
        };

        $scope.$watch(function () {
            if ($scope.shared.sharedUsers.length + $scope.shared.sharedGroups.length == 0) {
                return "PRIVATE";
            }
            if ($.grep($scope.shared.sharedGroups,function (group) {
                return group.name.toLowerCase() == "all";
            }).length > 0) {
                return "PUBLIC"
            }
            return "SHARED";
        }, function (level) {
            $scope.shared.accessLevel = level;
        });


        $scope.save = savingProjectFactory.save;
        $scope.saveProjectSharedWithUnregisteredUsers = savingProjectFactory.saveWithUnregisteredUsers;
        $scope.saveWithUnregisteredUsersDialogMessages = savingProjectFactory.saveWithUnregisteredUsersDialogMessages;

        $scope.saveProject = function () {
            var project = jQuery.extend(true, {}, $scope.project);
            if (project.lab == -1) {
                project.lab = null;
            }
            setSharingPolicy(project, $scope.shared.sharedUsers, $scope.shared.sharedGroups);
            project.withEmailNotification = $scope.shared.withEmailNotification;

            Projects.save(project, function (data) {
                CommonLogger.log("Project saved. Response:" + JSON.stringify(data));
                ProjectAttachmentsHelper.completeAttachment($scope, data.projectId, ProjectAttachments, function () {
                    $scope.returnUrl = "/projects/all" + "?page=1&items=25&sortingField=name&asc=true";
                    setTimeout(function () {
                        $(".modal").modal("hide");
                    }, 0);
                });

            });
        };
    })
    .directive("selectWithAutoComplete", function ($timeout) {
        return function (scope, iElement, iAttrs) {
            scope.$watch(iAttrs, function (values) {
                iElement.autocomplete({
                    source: function (request, response) {
                        var mappedItems = $.map(scope.availableItems(), function (item) {
                            return {
                                "label": scope.showInAutoComplete(item),
                                "value": scope.identify(item),
                                "orig": item,
                                "type": scope.isUser(item) ? "user" : "group"
                            }
                        });

                        var filteredValuesByTerm = $.ui.autocomplete.filter(mappedItems, request.term);
                        if (filteredValuesByTerm.length == 0
                            && !scope.onlyRegisteredUsers
                            && IsEmail(request.term)
                            && $.grep(scope.selectedUsers, function(item){ return item.email == request.term}).length == 0
                            && $.grep(scope.invitedUsers, function(item){ return item.email == request.term}).length == 0
                            && $.inArray(request.term, scope.excludeEmails) == -1) {
                            filteredValuesByTerm.push({
                                "label": "Not registered yet " + "<" + request.term + ">",
                                "value": request.term,
                                "orig": {email: request.term},
                                "type": "invited"
                            })
                        }
                        function IsEmail(email) {
                            var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
                            return regex.test(email);
                        }

                        response(filteredValuesByTerm);
                    },

                    select: function (event, ui) {
                        setTimeout(function () {
                            scope.add(ui.item.orig);
                            iElement.val("");
                            iElement.trigger("input");
                        }, 0);
                    }
                });
            }, true);
        };
    })
    .directive("projectDetails", detailsLink({"title": "Show Project Details", "dataTarget": "#projectDetails"}))
    .directive("projectCopy", detailsLink({"title": "Pass a copy", "dataTarget": "#projectDetails",
        "urlParam": "/copy", icon: "icon pass-copy"}))
    .directive("projectDetailsButton", detailsDirective({"title": "Show Project Details", "dataTarget": "#projectDetails"}))
    .directive("projectName", linkedName({"sub": "experiments"}))
    .directive("sharingSelector", userOrGroupSelection({
        "isEmailNotificationsAvailable": true,
        "groupSelectionAvailable": true,
        "emptyTableMessage": "There are no members",
        "addActionText": "Invite people personally or by groups. Use \"All\" special group to make project public.",
        "addPlaceHolderText": "Enter person's email, group name or \"All\" special group name",
        "showAllowWrite": true,
        "getInvitedUsers" : true})
    )
    .directive("confirmActionForUnregistered", function ($location) {
        return {
            restrict: "E",
            templateUrl: "../pages/projects/confirm-action-for-unregistered.html",
            scope: {
                confirmAction: "=",
                plainAction: "=",
                dialogMessages: "="
            },
            link: function ($scope, elem, attrs) {
                $scope.projectDialogReturn = function () {
                    $scope.$parent.dialogNotToReturn = false;
                };
                $("#confirm-action-for-unregistered").on("hidden", function () {
                    $scope.projectDialogReturn();
                    $(".modal").modal("hide"); //close project dialog and return
                });
            }
        }
    })
    .factory("removeProjectConfirmation", function ($route, Projects) {
        return function ($scope) {
            return function (project) {
                $scope.confirmation = new Confirmation("#remove-project-confirmation", project,
                    {
                        success: function () {
                            Projects.delete({project: project.id, removePermanently: $scope.confirmation.removePermanently}, function () {
                                $route.reload();
                            })
                        },
                        getName: function () {
                            return project.name;
                        }
                    });
                $scope.confirmation.removePermanently = true;
                $scope.confirmation.showPopup();
            }
        }
    })
    .factory("projectsExpandMenu", function (ProjectAttachments, ProjectDetails) {
        return initExpandMenu(function openInlineFashion(project, $scope) {
            ProjectDetails.get({id: project.id}, function (response) {
                project.details = response.details;
            });
            ProjectAttachments.read({path: project.id}, function (attachments) {
                project.attachments = $.map(attachments, function (attachment) {
                    var type = AttachmentsHelper.attachmentTypeFromName(attachment.name);
                    CommonLogger.log(type);
                    var a = new AttachmentsHelper.Attachment(attachment.name, attachment.uploadDate, attachment.sizeInBytes, type, null, null);
                    a.attachmentId = attachment.id;
                    return a;
                });
            });

            $scope.downloadAttachment = function (attachmentId) {
                $.fileDownload("../attachments/project/download/" + attachmentId, {
                });
            };
        })
    })
    .factory("savingProject", function () {
        return function ($scope) {
            return {
                save: function (isInvalid) {
                    $scope.buttonPressed = true;
                    if (isInvalid) return;
                    if ($scope.shared.invitedUsers.length > 0) {
                        $scope.dialogNotToReturn = true;
                        $(".modal").modal("hide");
                        $("#confirm-action-for-unregistered").modal({
                            show: true,
                            keyboard: false,
                            backdrop: "static"
                        });
                    } else {
                        $scope.saveProject();
                    }
                },
                saveWithUnregisteredUsers: function () {
                    angular.forEach($scope.shared.invitedUsers, function (user) {
                        inviteUser(user);
                    });
                    var invitedUsersResponse = 0;

                    function inviteUser(item) {
                        $scope.shared.inviteHandler(item, saveProjectIfAllInvited);
                        function saveProjectIfAllInvited(invited) {
                            invited.name = invited.firstName + " " + invited.lastName;
                            invited.allowWrite = item.allowWrite;
                            $scope.shared.sharedUsers.push(invited);
                            invitedUsersResponse++;
                            if (invitedUsersResponse == $scope.shared.invitedUsers.length) {
                                $scope.saveProject();
                            }
                        }
                    }
                },
                saveWithUnregisteredUsersDialogMessages: {
                    title: "Confirm Project Sharing",
                    dialogInformation: "Unregistered users have been added. Would you like to invite them?",
                    plainButtonText: "Remove from sharing list",
                    confirmButtonText: "Invite"
                }
            }
        }
    });

function setSharingPolicy(project, sharedUsers, sharedGroups) {
    function transformShared(shared) {
        var result = {};
        $.each(shared, function (idx, item) {
            result[item.id] = item.allowWrite || false;
        });
        return result;
    }

    project.colleagues = transformShared(sharedUsers);
    project.groups = transformShared(sharedGroups);
}

var ProjectAttachmentsHelper = {};
$.extend(ProjectAttachmentsHelper, AttachmentsHelper);

ProjectAttachmentsHelper.completeAttachment = function ($scope, projectId, ProjectAttachments, callback) {

    //update the attachments upon the saved project once it has been successfully saved
    var attachmentIds = $scope.existingAttachments.map(function (attachment) {
        return attachment.attachmentId;
    });

    ProjectAttachments.attachToProject({projectId: projectId, attachmentIds: attachmentIds}, callback);
};

function getDefaultOptionValue(items, currentValue) {
    if (currentValue) {
        return currentValue;
    }
    if(items.length > 0) {
        return  items[0].id;
    }
}

function getFirstOrWithIdEqualTo(items, currentValue) {

    if(items.length == 0) {
        return -1;
    }
    var hasItemWithIdEqualTo = items.find(function (item) {
        return item.id == currentValue;
    }) != undefined;

    if (currentValue && hasItemWithIdEqualTo) {
        return currentValue;
    }

    return  items[0].id;

}

