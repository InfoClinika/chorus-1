"use strict";

(function () {

    angular
        .module(
            "support-page",
            ["security-front", "error-catcher", "template-components", "features-back", "header", "footer"]
        )
        .config(function ($routeProvider) {
            $routeProvider
                .when("/questions", {redirectTo: "questions/faq"})
                .when("/videos", {redirectTo: "videos/relevant"})
                .when("/releases", {redirectTo: "releases/latest"})
                .when("/documentation", {redirectTo: "documentation/relevant"})
                .when("/message", {controller: "message-controller", templateUrl: "../pages/support/message.html"})
                .when(
                    "/questions/:category",
                    {controller: "questions-controller", templateUrl: "../pages/support/questions.html"}
                )
                .when(
                    "/videos/:category",
                    {controller: "videos-controller", templateUrl: "../pages/support/videos.html"}
                )
                .when(
                    "/algorithm/:version",
                    {controller: "algorithm-controller", templateUrl: "../pages/support/algorithm.html"}
                )
                .when("/algorithm", {redirectTo: "algorithm/v1.0.0"})
                .when("/releases/:version", {
                    controller: "releases-controller",
                    templateUrl: "../pages/support/releases.html"
                })
                .when(
                    "/documentation",
                    {controller: "documentation-controller", templateUrl: "../pages/support/documentation.html"}
                )
                .when("/search", {controller: "search-controller", templateUrl: "../pages/support/search.html"})
                .when("/user-guide", {redirectTo: "user-guide/introduction"})
                .when(
                    "/user-guide/:category",
                    {controller: "user-guide-controller", templateUrl: "../pages/support/user-guide.html"}
                )
                .otherwise({redirectTo: "questions/faq"});
        })
        .service("Questions", function () {
            return [
                {
                    title: "I am a new user who wants to upload data and create projects and experiments.  What should I do first?",
                    content: "First, you should request laboratory membership or creation if you have not already done so. Next, you can either create a project or an instrument.  These steps have no prerequisites other than belonging to a laboratory with mass spectrometers.   Registering an instrument will let you upload .raw files from that instrument.  Once you have created a project and have uploaded .raw data to the Chorus cloud, you can create experiments and organize them into your created projects.  You can't create an experiment without specifying an existing project where you want the experiment to belong.",
                    category: "projects",
                    availableForCelgene: true,
                    isMostRelevant: true
                },
                {
                    title: "Do I need to join a laboratory to use Chorus?",
                    content: "No, but the options are somewhat limited. You will be able to visualize and download files without belonging to a lab on Chorus.  You will also be able to create and join sharing groups.   Joining a laboratory is, however, required to add instruments, which is required to upload data. Project creation requires a laboratory membership.",
                    category: "accounts",
                    availableForCelgene: true,
                    isMostRelevant: true
                },
                {
                    title: "Can I share my data files with another person who does not have an account on Chorus?",
                    content: "Yes. You can create a permanent download link by clicking the “public link” button to the right of an experiment's name.  This link can be shared on a public website or sent via email.  The files will be downloaded as a .zip file.",
                    category: "groups",
                    availableForCelgene: true,
                    isMostRelevant: true
                },
                {
                    title: "What kind of files can I upload to Chorus?",
                    content: "Chorus accepts files with the .raw extension.  These files come straight from the spectrometers and contain intensity and retention time data for each detected peak.",
                    category: "upload",
                    availableForCelgene: true,
                    isMostRelevant: true
                },
                {
                    title: "I uploaded some data files to Chorus and I can't find them on the Create Experiment screens.  Am I doing something wrong?",
                    content: "Most likely, the model organism or the instrument used to generate the data were incorrectly filled on either the Create Experiment or the Upload Files pages.  These fields must match to add data files to your experiment.",
                    category: "experiments",
                    availableForCelgene: true,
                    isMostRelevant: true
                },
                {
                    title: "What are the fees for using CHORUS?",
                    content: "Users are charged only for the services they use beyond the $100\
                per month lab credit.  Initially, two fee-based services will be offered, Active Storage and Archive\
                Storage.  Active Storage provides high-speed access to data that is needed for viewers and other tools\
                that perform real time data analysis or visualization.  Active Storage is designed for data that is being\
                used or analyzed and needs to be available on-demand.  The cost for Active Storage is $90/TB/month.\
                Archive Storage provides a low cost option for storing data that does not require fast on-demand access.\
                Data stored with the Archive Storage service can take several hours to access compared to Active\
                Storage that is near instantaneous.  Archive Storage is charged at $30/TB/month.</br>\
                It is important to note that we offer an extremely cost effective cloud storage solution.  Most \
                laboratories currently using CHORUS will take a long time to use up their $100 credit.  If 100% of a lab’s\
                data is stored in the Archive Storage, they can maintain up to 200 GB of data in CHORUS for >16 months\
                using their onetime $100 credit.",
                    category: "billing",
                    availableForCelgene: false,
                    isMostRelevant: true
                },
                {
                    title: "How will my account be billed?",
                    content: "The head of each CHORUS account will be required to establish a form\
                of payment for their laboratory.  Initially we will use the PayPal Internet payment service that is linked\
                to a Credit Card or Purchase Order.  Stratus Biosciences will invoice CHORUS lab head accounts on a\
                monthly basis for the services provided to CHORUS lab.\
                ",
                    category: "billing",
                    availableForCelgene: false,
                    isMostRelevant: true
                },
                {
                    title: "How can I monitor the cost of my CHORUS account?",
                    content: "Each laboratory head can view real-time billing\
                updates using the accounting tools in CHORUS.  The cost of services is broken down by day, type of\
                services, and the user that uploaded the data.\
                <div>\
                    <div></br>\
                        <span class=\"center-text bold\">Lab Account List</span>\
                        <a href=\"../img/billing/labs-list.png\" target=\"_blank\">\
                        <img src=\"../img/billing/labs-list.png\"/>\
                    </a>\
                </div>\
                <div></br>\
                    <span class=\"center-text bold\">History List</span>\
                    <a href=\"../img/billing/history-list.png\" target=\"_blank\">\
                        <img src=\"../img/billing/history-list.png\"/>\
                    </a>\
                </div>\
                <div></br>\
                    <span class=\"center-text bold\">Daily History Details</span>\
                    <a href=\"../img/billing/history-details.png\" target=\"_blank\">\
                        <img src=\"../img/billing/history-details.png\"/>\
                    </a>\
                 </div>\
                </div>",
                    category: "billing",
                    availableForCelgene: false,
                    isMostRelevant: true
                },
                {
                    title: "Is there a free, no cost trial period?",
                    content: "Yes!  First, all labs will receive a onetime $100 credit.  We will\
                upgrade the system shortly after December 3rd but no fees will be charged until after March 1st 2015.\
                However, users can monitor their CHORUS use to see what the actual charges would be for their lab.\
                After March 1st, a credit will be issued to cover all user fees incurred since December 3rd.",
                    category: "billing",
                    availableForCelgene: false,
                    isMostRelevant: true
                },
                {
                    title: "What if I want to cancel my CHORUS account?",
                    content: "You can cancel your CHORUS account at any time\
                without charge or restrictions.  The process is simple, simply contact <a href='mailto:support@infoclinika.com'>support@infoclinika.com</a>\
                and indicate that you would like to cancel your account.  If you need time to restore data from CHORUS\
                will be glad to provide time to do that at no cost to you.",
                    category: "billing",
                    availableForCelgene: false,
                    isMostRelevant: true
                }
            ];
        })
        .service("Videos", function () {
            var videoLists = {};
            //relevant playlist
            var playlistId = "PL1pRI26VpljPqC6Bc0jTyD0Uu6vHxy522";
            var APIKey = "AIzaSyB4FWtskLSR7Owq-EkwC9YITQedgen9NpE";//key for Andrii's work email, cause without it exceed limit error is appearing
            var url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&&playlistId=" + playlistId +
                "&key=" + APIKey;

            videoLists.experiments = [getVideoItem("PPTm Experiment creation", "BW4UxMU5Wns")];
            //next playlist...
            return {
                load: function (successHandler) {
                    $.getJSON(url, function (data) {
                        videoLists.relevant = data.items;
                        successHandler(videoLists);
                    });

                }
            };

            function getVideoItem(title, videoId) {
                return {
                    id: videoId,
                    snippet: {
                        title: title,
                        resourceId: {
                            videoId: videoId
                        },
                        thumbnails: {
                            high: {url: "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg"}
                        }
                    }
                };
            }
        })
        .service("Documentation", function () {
            return [];
        })
        .controller("questions-controller", function ($scope, $routeParams) {
            var categoryFilter = $routeParams.category;

            $scope.questions = filterByCategory(categoryFilter, $scope.allQuestions);

            function filterByCategory(category, items) {
                if (category == "faq") {
                    return items.filter(function (item) {
                        return item.isMostRelevant;
                    });
                }
                return items.filter(function (item) {
                    return item.category == category;
                });


            }
        })
        .controller("videos-controller", function ($scope, $rootScope, $routeParams, SupportConstants) {

            var categoryFilter = $routeParams.category;
            updateVideos();

            $scope.$on(SupportConstants.UPDATE_VIDEOS, updateVideos);

            function updateVideos() {
                $scope.videos = $scope.allVideos[categoryFilter] || [];
            }

            $scope.showVideo = function (index) {
                $rootScope.video = $scope.allVideos[categoryFilter][index];
                setTimeout(function () {
                    $scope.$apply();
                });
                setTimeout(function () {
                    $("#preview-video").modal("show");
                });
            };
        })
        .controller("user-guide-controller", function ($scope, $routeParams, UserGuide) {
            var categoryFilter = $routeParams.category;
            var selectedTopic = UserGuide.topicsMap[categoryFilter];

            $scope.topics = selectedTopic.subtopics;
        })
        .controller("documentation-controller", function ($scope, $http, SupportConstants) {

            $scope.togglePanelBody = function (e) {
                var li = $(e.target).closest("li");
                li.toggleClass("opened");
                li.find(".panel-body").slideToggle(300);
                return false;
            };

            $http.get(SupportConstants.SWAGGER_PUBLIC_API).success(function (response) {

                $scope.descriptionLoadFailed = false;

                var endpointItems = [];
                var tagsItems = [];

                Object.keys(response.tags).forEach(function (item) {
                    tagsItems.push({
                        key: response.tags[item].name,
                        description: response.tags[item].description
                    });
                });

                Object.keys(response.paths).forEach(function (item) {
                    endpointItems.push({
                        endpointName: item,
                        path: response.paths[item]
                    });
                });

                $scope.controllers = sortEndpointsByTag(tagsItems, endpointItems, response.definitions);

            }).error(function () {
                $scope.descriptionLoadFailed = true;
            });

            function sortEndpointsByTag(tags, endpoints, definitions) {

                var sortedPathsByTag = [];

                tags.forEach(function (tag) {
                    var paths = {tag: "", description: "", endpoints: []};

                    Object.keys(endpoints).forEach(function (value) {
                        var endpointName = endpoints[value].endpointName;

                        var endpointData = getDefaultEndpointData();

                        Object.keys(endpoints[value].path).forEach(function (httpMethod) {
                            if (endpoints[value].path[httpMethod].tags.indexOf(tag.key) >= 0) {

                                if (sortedPathsByTag.length > 0) {
                                    sortedPathsByTag.forEach(function (data) {
                                        if (data.tag === tag.key) {
                                            endpointData.name = endpointName;
                                            endpointData.http_method_name = httpMethod.toUpperCase();
                                            endpointData.endpoint_param = endpoints[value].path[httpMethod];
                                            data.endpoints.push(endpointData);
                                            endpointData = getDefaultEndpointData();
                                        }
                                    });
                                }

                                if (paths.tag === "") {

                                    paths.tag = tag.key;
                                    paths.description = tag.description;
                                    endpointData.name = endpointName;
                                    endpointData.http_method_name = httpMethod.toUpperCase();
                                    endpointData.endpoint_param = endpoints[value].path[httpMethod];
                                    paths.endpoints.push(endpointData);
                                    sortedPathsByTag.push(paths);
                                    endpointData = getDefaultEndpointData();
                                }
                            }
                        });
                    });
                });

                return searchDefinitions(definitions, sortedPathsByTag);
            }

            function getDefaultEndpointData() {
                return {
                    http_method_name: "",
                    name: "",
                    endpoint_param: "",
                    schema_request_parameters: [],
                    schema_response_parameters: []
                };
            }

            function getDefinitions(reference) {
                var splitted = reference.split("/");
                return splitted[splitted.length - 1];
            }

            function searchDefinitions(definitions, paths) {

                var STATUS_CODE = 200;
                var dto;

                paths.forEach(function (path) {

                    path.endpoints.forEach(function (item) {

                        var response = {name: "", value: {}};
                        var request = {name: "", value: {}};

                        if (item.endpoint_param.parameters) {

                            item.endpoint_param.parameters.forEach(function (param) {
                                if (param.schema) {
                                    dto = getDefinitions(param.schema.$ref);
                                    request = {name: dto, value: definitions[dto]};
                                    item.schema_request_parameters.push(request);
                                }
                            });
                        }

                        if (item.endpoint_param.responses[STATUS_CODE].schema) {
                            if (item.endpoint_param.responses[STATUS_CODE].schema.$ref) {
                                dto = getDefinitions(item.endpoint_param.responses[STATUS_CODE].schema.$ref);
                                response = {name: dto, value: definitions[dto]};
                                item.schema_response_parameters.push(response);

                            } else if (item.endpoint_param.responses[STATUS_CODE].schema.items &&
                                item.endpoint_param.responses[STATUS_CODE].schema.items.$ref) {
                                dto = getDefinitions(item.endpoint_param.responses[STATUS_CODE].schema.items.$ref);
                                response = {name: dto, value: definitions[dto]};
                                item.schema_response_parameters.push(response);

                            }
                        }
                    });
                });

                return paths;
            }
        })
        .controller("message-controller", function ($scope) {

        })
        .controller("releases-controller", function ($scope, $routeParams, $location, ReleaseNotes) {
            var releaseVersion = $routeParams.version;
            if (releaseVersion === "latest") {
                // If latest version is requested, redirect to it.
                // It's needed to highlight latest version as selected using isLinkActive('releases', version).
                var latestVersionPath = getLatestReleaseVersionPath();
                $location.path(latestVersionPath);
            }

            $scope.releaseNotes = ReleaseNotes.details[releaseVersion] || [];

            function getLatestReleaseVersionPath() {
                var activePath = $location.path();
                var releasesPathEndIndex = activePath.lastIndexOf("/");
                var releasesPath = activePath.substring(0, releasesPathEndIndex + 1);
                var latestVersion = ReleaseNotes.versions[ReleaseNotes.versions.length - 1];

                return releasesPath + latestVersion;
            }
        })
        .controller("search-controller", function ($scope, $rootScope, $location, UserGuide) {
            var items = {
                videos: [],
                faqs: [],
                documentation: []
            };
            $scope.lastSearchQuery = $scope.searchQuery;

            function getVideos(query) {
                var videos = [];
                for (var name in $scope.allVideos) {

                    $.each($scope.allVideos[name], function (i, item) {
                        if (item.snippet.title.toLowerCase().indexOf(query) >= 0) {
                            videos.push(item);
                        }
                    });
                }
                return videos;
            }

            function getQuestions(query) {
                var questions = [];
                $.each($scope.allQuestions, function (i, item) {
                    if (item.title.toLowerCase().indexOf(query) >= 0
                        || item.content.toLowerCase().indexOf(query) >= 0) {
                        questions.push(item);
                    }
                });
                return questions;
            }

            function getDocumentation(query) {
                var docs = [];
                $.each($scope.allDocumentation, function (i, item) {
                    if (item.title.toLowerCase().indexOf(query) >= 0
                        || item.content.toLowerCase().indexOf(query) >= 0) {
                        docs.push(item);
                    }
                });
                return docs;
            }

            function getUserGuide(query) {
                var userGuideItems = [];

                $.each(UserGuide.allSubtopics, function (i, item) {
                    if (item.title.toLowerCase().indexOf(query) >= 0
                        || item.content.toLowerCase().indexOf(query) >= 0) {
                        userGuideItems.push(item);
                    }
                });
                return userGuideItems;
            }

            $scope.showVideo = function (index) {
                $rootScope.video = $scope.results.videos[index];
                setTimeout(function () {
                    $scope.$apply();
                });
                setTimeout(function () {
                    $("#preview-video").modal("show");
                });
            };

            var q = $scope.lastSearchQuery.toLowerCase();
            items.videos = getVideos(q);
            items.faqs = getQuestions(q);
            items.documentation = getDocumentation(q);
            items.userGuideItems = getUserGuide(q);

            $scope.results = items;

            setTimeout(function () {
                $(".search-results").highlight($scope.lastSearchQuery);
            });

        })
        .controller("support-controller", function ($scope,
                                                    $window,
                                                    $rootScope,
                                                    $location,
                                                    $timeout,
                                                    Questions,
                                                    Videos,
                                                    Documentation,
                                                    Features,
                                                    SupportConstants,
                                                    UserGuide,
                                                    ReleaseNotes,
                                                    Algorithms) {
            $scope.forumProperties = {};
            $scope.allQuestions = {};
            $scope.allVideos = {};
            $scope.allDocumentation = Documentation;
            $scope.searchQuery = "";
            $scope.isSearchResult = false;
            $scope.userGuideTopics = [];
            $scope.releaseVersions = [];
            $scope.allAlgorithms = {};

            init();

            function init() {
                if (!$scope.searchQuery) {
                    $scope.searchQuery = $location.search().value;
                }

                Features.getForumProperties(function (response) {
                    $scope.forumProperties = response;
                });

                Features.getPrivateInstallProperties(function (privateInstallProperties) {
                    $scope.allQuestions = $.grep(Questions, function (question) {
                        return !privateInstallProperties.enabled || question.availableForCelgene;
                    });

                    Videos.load(function (videos) {
                        $.each(videos, function (category, list) {
                            $scope.allVideos[category] = $.grep(list, function (video) {
                                return !privateInstallProperties.enabled ||
                                    SupportConstants.hideOnPrivateInstallationVideoIds.indexOf(video.id) === -1;
                            });
                        });
                        $scope.$broadcast(SupportConstants.UPDATE_VIDEOS);
                        $timeout(function () {
                            $scope.$apply();
                        }, 0);
                    });
                });

                $scope.userGuideTopics = UserGuide.topics;
                $scope.releaseVersions = ReleaseNotes.versions;
                $scope.allAlgorithms = Algorithms.algorithm;
            }


            $scope.toggleOpened = function (e) {
                var li = $(e.target).closest("li");
                li.toggleClass("opened");
                li.find(".hide").slideToggle(300);
                return false;
            };

            $scope.getActiveTab = function () {
                var path = $location.path();
                var pathParts = path.split("/");
                return pathParts[1];
            };

            $scope.isLinkActive = function (kind, category) {
                var pathParts = $location.path().split("/");
                var k = pathParts[1];
                var c = pathParts[2];
                return kind == k && category == c;
            };

            $scope.$on("$routeChangeSuccess", function () {
                setTimeout(function () {
                    $(".scroll-area").scrollTop(0);
                }, 0);

                var pathParts = $location.path().split("/");
                var flag = $scope.isSearchResult;
                $scope.isSearchResult = pathParts[1] == "search";
                if (!$scope.isSearchResult && flag != $scope.isSearchResult) {
                    $scope.searchQuery = "";
                }
            });

            $scope.search = function () {
                if ($scope.searchQuery.trim().length == 0) {
                    $scope.searchQuery = "";
                    return;
                }
                $location.url("search?value=" + $scope.searchQuery);
            };

            $scope.closeVideo = function () {
                delete $rootScope.video;
                setTimeout(function () {
                    $scope.$apply();
                });
            };

        })
        .controller("algorithm-controller", function ($scope) {
            $scope.algorithms = $scope.allAlgorithms;

        })
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
        .factory("SupportConstants", function () {
            return {
                UPDATE_VIDEOS: "UPDATE_VIDEOS",
                hideOnPrivateInstallationVideoIds: ["UEwxcFJJMjZWcGxqUHFDNkJjMGpUeUQwVXU2dkh4eTUyMi41MzJCQjBCNDIyRkJDN0VD"],
                SWAGGER_PUBLIC_API: "/swag/v2/api-docs?group=Public API"

            };
        })
        .factory("ReleaseNotes", function () {
            var versionReleaseNotesMap = {
                "1.1.0": [
                    "NGS experiment type addition",
                    "API to support NGS pipeline",
                    "External processing runs UI",
                    "S3 to S3 copy functionality",
                    "S3 by reference functionality",
                    "Direct upload improvement to work directly against AWS SDK",
                    "Parametrized processing run functionality",
                    "Peptides detection algorithm",
                    "Additional Maxquant features handling",
                    "Update Processing wizard to allow select multiple databases",
                    "Post processing of datacubes to allow users to merge identified isotope groups to peptides and proteins",
                    "Automatic import and translation of the low resolution LTQ files",
                    "LTQ peak identification algorithm",
                    "Fingerprint search functionality"
                ],
                "1.1.1": [
                    "S3 copy and by reference to add generation of AWS Policies",
                    "Auto importer API requests improvements",
                    "Desktop Uploader wrapping for MacOS and Windows",
                    "UI fixes: logos, news, blog, choosing of files on experiment wizard",
                    "Ability to set system to search in-spray adducts as a global parameter",
                    "Fingerprint Search UI changes",
                    "Relative intensities of all identified proteins per database",
                    "Heatmap viewer addition to Viewers app",
                    "Issue Report functionality for users"
                ],
                "1.1.2": [
                    "UI fixes and changes(file handling, links naming, better error handling)",
                    "API reference and Algorithm description addition to Knowledge Center",
                    "Autoimporter security , UI improvement and failed file upload retry",
                    "Data Cube viewers improvements(heat-map hide toggle, better, better scrolling)",
                    "GDPR compliance features(cookies alerts, request to delete account, billing info)",
                    "Billing functionality UI report page fixes"
                ]
            };

            return {
                versions: Object.keys(versionReleaseNotesMap),
                details: versionReleaseNotesMap
            };
        });

})();
