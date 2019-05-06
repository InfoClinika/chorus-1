"use strict";

(function () {
    angular
        .module(
            "search-front",
            ["search-back", "breadcrumbs", "instruments-front", "projects-front", "filesControllers",
                "instruments-front", "front-end", "error-catcher", "feature-service", "user-details-service"]
        )
        .controller("search-input", function ($scope, $location) {
            $scope.search = function () {
                var split = $location.path().split("/");
                var kind = "projects";
                if (split[1] == "search") {
                    kind = split[3];
                }
                $location.path("/search/" + $scope.page.query + "/" + kind);
            };
        })
        .factory("highlightSearchQuery", function () {
            return function (query) {
                setTimeout(function () {
                    $.each(query.split(" "), function (i, term) {
                        $(".cell-id").highlight(term.trim());
                        $(".cell-name").highlight(term.trim());
                    });
                }, 500);
            };
        })
        .factory("initSearchTabs", function () {
            return function ($scope, kind, query) {
                $scope.page.subHeaderUrl = "search/search-tabs.html";
                $scope.page.custom.searchTabs = [
                    {label: "Projects", href: "#/search/" + query + "/projects", kind: "projects"},
                    {label: "Experiments", href: "#/search/" + query + "/experiments", kind: "experiments"},
                    {label: "Files", href: "#/search/" + query + "/files", kind: "files"},
                    {label: "Instruments", href: "#/search/" + query + "/instruments", kind: "instruments"}
                ];
            };
        })
        .factory("doSearch", function (highlightSearchQuery, initSearchTabs, Search) {
            return function ($scope, $location, kind, query, callback) {
                initSearchTabs($scope, "projects", query);
                $scope.fetched = 0;

                function fetched(fetchingKind) {
                    return function (result) {
                        var tab = jQuery.grep($scope.page.custom.searchTabs, function (searchTab) {
                            return searchTab.kind == fetchingKind;
                        })[0];
                        tab.count = result.length;
                        tab.state = "regular";
                        if (tab.count == 0) {
                            tab.state = "disabled";
                        }
                        if (kind == fetchingKind) {
                            tab.state = "active";
                            highlightSearchQuery(query);
                            $scope.resultCount = tab.count;
                        }
                        $scope.fetched++;
                    };
                }

                function search(query, kind, fetched) {
                    return Search.query({query: query, kind: kind}, function (items) {
                        fetched(items);
                        if (items.length == 0) {
                            if (callback) {
                                callback(kind, items);
                            }
                        }
                    });
                }


                $scope.projects = search(query, "projects", fetched("projects"));
                $scope.experiments = search(query, "experiments", fetched("experiments"));
                $scope.files = search(query, "files", fetched("files"));
                $scope.instruments = search(query, "instruments", fetched("instruments"));
            };
        })
        .factory(
            "doPagedSearch",
            function (highlightSearchQuery,
                      initSearchTabs,
                      Search,
                      contentRequestParameters,
                      PaginationPropertiesSettingService) {
                return function ($scope, $location, kind, query, callback) {
                    initSearchTabs($scope, "projects", query);
                    $scope.fetched = 0;
                    $scope.total = 0;

                    function initTabs(fetchingKind) {
                        return function (count) {
                            var tab = jQuery.grep($scope.page.custom.searchTabs, function (searchTab) {
                                $.each(count, function (k, v) {
                                    if (searchTab.kind == k) {
                                        searchTab.count = v;
                                    }
                                    searchTab.state = "regular";
                                    if (searchTab.count == 0) {
                                        searchTab.state = "disabled";
                                    }
                                });
                                return searchTab.kind == fetchingKind;
                            })[0];
                            tab.count = count[fetchingKind];
                            if (kind == fetchingKind && tab.count != 0) {
                                tab.state = "active";
                            } else if (tab.count == 0) {
                                var tabsToRedirect = jQuery.grep($scope.page.custom.searchTabs, function (searchTab) {
                                    if (count[searchTab.kind] != 0) {
                                        return searchTab;
                                    }
                                });
                                if (tabsToRedirect.length != 0) {
                                    var tabToRedirect = tabsToRedirect[0];
                                    tabToRedirect.count = count[tabToRedirect.kind];
                                    $location.path("/search/" + $scope.page.query + "/" + tabToRedirect.kind);
                                }

                            }
                        };
                    }

                    function fetched(fetchingKind) {
                        return function (result) {
                            $scope[fetchingKind] = result.items;
                            if (kind == fetchingKind) {
                                highlightSearchQuery(query);
                                $scope.resultCount = result.itemsCount;
                                PaginationPropertiesSettingService.setPaginationProperties($scope, result);
                            }
                            $scope.fetched++;
                        };
                    }

                    function search(query, kind, fetched) {
                        var pageRequest = contentRequestParameters.getParameters(kind);
                        pageRequest.query = query;
                        pageRequest.kind = kind;
                        return Search.get(pageRequest, function (items) {
                            fetched(items);
                            if (callback) {
                                callback(kind, items);
                            }
                        });
                    }


                    $scope["paged" + kind] = search(query, kind, fetched(kind));
                    Search.count({query: query}, initTabs(kind));

                };
            }
        )
        .controller(
            "search-projects",
            function ($scope,
                      Search,
                      $routeParams,
                      doSearch,
                      $location,
                      removeProjectConfirmation,
                      doPagedSearch,
                      projectsExpandMenu,
                      UserDetailsProvider) {
                if ($scope.pathError) {
                    return;
                }
                CommonLogger.setTags(["SEARCH", "SEARCH-PROJECTS-CONTROLLER"]);
                var query = $routeParams.query;
                $scope.page.query = query;
                $scope.page.title = "Projects";
                $scope.displayConfirmation = removeProjectConfirmation($scope);
                $scope.getLoggedUserName = UserDetailsProvider.getLoggedUserName;

                projectsExpandMenu($scope);
                $scope.isTableEmpty = function () {
                    return $scope.projects == 0;
                };

                $scope.getEmptyTableMessage = function () {
                    return "There are no matches for projects";
                };
                doPagedSearch($scope, $location, "projects", query);
            }
        )
        .controller("search-experiments", function ($scope, Search, $routeParams, doSearch, $location,
                                                    removeExperimentConfirmation, doPagedSearch, experimentsExpandMenu,
                                                    experimentIconDetails, experimentValidityConfirmation,
                                                    FeatureProvider) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["SEARCH", "SEARCH-EXPERIMENTS-CONTROLLER"]);
            var query = $routeParams.query;
            $scope.page.query = query;
            $scope.page.title = "Experiments";
            $scope.displayConfirmation = removeExperimentConfirmation($scope);
            $scope.showExperimentValidityConfirmation = experimentValidityConfirmation($scope);
            $scope.total = 0;
            $scope.iconDetails = experimentIconDetails;
            experimentsExpandMenu($scope);

            $scope.sort = {disable: {files: true}};
            $scope.isBillingFeatureAvailable = FeatureProvider.isBillingFeatureAvailable;
            $scope.isFeatureAvailable = FeatureProvider.isFeatureAvailable;

            var isTableEmpty = false;

            doPagedSearch($scope, $location, "experiments", query);

            $scope.isTableEmpty = function () {
                return $scope.experiments == 0;
            };

            $scope.getEmptyTableMessage = function () {
                return "There are no matches for experiment";
            };

        })
        .controller("search-files", function ($scope, $route, Search, $routeParams, doSearch, $location,
                                              removeFileConfirmation, changeableColumnsHelper, doPagedSearch,
                                              filesExpandMenu, FilesControllerCommon, FileColumns,
                                              FeatureProvider, UserLabsProvider) {
            if ($scope.pathError) {
                return;
            }
            CommonLogger.setTags(["SEARCH", "SEARCH-FILES-CONTROLLER"]);
            var query = $routeParams.query;
            $scope.page.query = query;
            $scope.page.title = "Files";
            $scope.displayConfirmation = removeFileConfirmation($scope);
            filesExpandMenu($scope);
            $scope.page.changeableColumns = true;
            $scope.isBillingFeatureAvailable = FeatureProvider.isBillingFeatureAvailable;
            $scope.isUserLab = UserLabsProvider.isUserLab;

            changeableColumnsHelper($scope, FileColumns);
            var filesControllerCommon = FilesControllerCommon($scope);


            doPagedSearch($scope, $location, "files", query, function (kind, files) {
                if (kind != "files") {
                    return;
                }
                $scope.pageNumber = files.pageNumber + 1;
                filesControllerCommon.setup(files);
            });

            $scope.isTableEmpty = function () {
                return $scope.files == 0;
            };

            $scope.getEmptyTableMessage = function () {
                return "There are no matches for files";
            };
        })
        .controller(
            "search-instruments",
            function ($scope, Search, $routeParams, instrumentListActions, doSearch, $location,
                      removeInstrumentConfirmation, contentRequestParameters, doPagedSearch,
                      instrumentsExpandMenu, UserDetailsProvider) {
                if ($scope.pathError) {
                    return;
                }
                CommonLogger.setTags(["SEARCH", "SEARCH-INSTRUMENTS-CONTROLLER"]);
                var request = contentRequestParameters.getParameters("instruments");
                $scope.sort = {
                    route: true,
                    disable: {files: true}
                };
                var query = $routeParams.query;
                $scope.page.query = query;
                $scope.page.title = "Instruments";
                instrumentsExpandMenu($scope);
                doPagedSearch($scope, $location, "instruments", query);
                $scope.displayConfirmation = removeInstrumentConfirmation($scope);
                instrumentListActions($scope);

                $scope.isTableEmpty = function () {
                    return $scope.instruments == 0;
                };

                $scope.getEmptyTableMessage = function () {
                    return "There are no matches for instruments";
                };
                $scope.getUserId = UserDetailsProvider.getUserId;
            }
        )
        .directive("onEnter", function () {
            //inspired by https://groups.google.com/forum/#!msg/angular/tv4Nl1HouOw/i0MMmfkMrbUJ
            return function (scope, element, attrs) {
                element.bind("keydown keypress", function (event) {
                    if (event.which === 13) {
                        scope.$apply(function () {
                            scope.$eval(attrs.onEnter);
                        });

                        event.preventDefault();
                    }
                });
            };
        })
        .run(function (registerBreadcrumbHandler) {
            registerBreadcrumbHandler(function (path) {
                var match = path.match(/search\/([^/]+)\/(projects|experiments|files|instruments)$/);
                if (!match) {
                    return null;
                }
                return {label: "Search for " + match[1] + " in " + match[2], url: "#" + path};
            });
        });

})();
