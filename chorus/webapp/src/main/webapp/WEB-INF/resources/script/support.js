angular.module("support-page", ["security-front", "error-catcher", "current-year", "features-back", "header"])
    .config(function($routeProvider){
        $routeProvider
            .when("/questions", {redirectTo: "questions/faq"})
            .when("/videos", {redirectTo: "videos/relevant"})
            .when("/documentation", {redirectTo: "documentation/relevant"})
            .when("/message", {controller: "message-controller", templateUrl: "../pages/support/message.html"})
            .when("/questions/:category", {controller: "questions-controller", templateUrl: "../pages/support/questions.html"})
            .when("/videos/:category", {controller: "videos-controller", templateUrl: "../pages/support/videos.html"})
            .when("/documentation/:category", {controller: "documentation-controller", templateUrl: "../pages/support/documentation.html"})
            .when("/search", {controller: "search-controller", templateUrl: "../pages/support/search.html"})
            .otherwise({redirectTo: "questions/faq"});
    })
    .service("Questions", function(){
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
                content: 'The head of each CHORUS account will be required to establish a form\
                of payment for their laboratory.  Initially we will use the PayPal Internet payment service that is linked\
                to a Credit Card or Purchase Order.  Stratus Biosciences will invoice CHORUS lab head accounts on a\
                monthly basis for the services provided to CHORUS lab.\
                ',
                category: "billing",
                availableForCelgene: false,
                isMostRelevant: true
            },
            {
                title: "How can I monitor the cost of my CHORUS account?",
                content: 'Each laboratory head can view real-time billing\
                updates using the accounting tools in CHORUS.  The cost of services is broken down by day, type of\
                services, and the user that uploaded the data.\
                <div>\
                    <div></br>\
                        <span class="center-text bold">Lab Account List</span>\
                        <a href="../img/billing/labs-list.png" target="_blank">\
                        <img src="../img/billing/labs-list.png"/>\
                    </a>\
                </div>\
                <div></br>\
                    <span class="center-text bold">History List</span>\
                    <a href="../img/billing/history-list.png" target="_blank">\
                        <img src="../img/billing/history-list.png"/>\
                    </a>\
                </div>\
                <div></br>\
                    <span class="center-text bold">Daily History Details</span>\
                    <a href="../img/billing/history-details.png" target="_blank">\
                        <img src="../img/billing/history-details.png"/>\
                    </a>\
                 </div>\
                </div>',
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
                without charge or restrictions.  The process is simple, simply contact <a href='mailto:support@chorusproject.org'>support@choruproject.org</a>\
                and indicate that you would like to cancel your account.  If you need time to restore data from CHORUS\
                will be glad to provide time to do that at no cost to you.",
                category: "billing",
                availableForCelgene: false,
                isMostRelevant: true
            }
        ];
    })
    .service("Videos", function(){
        var videoLists = {};
        //relevant playlist
        var playlistId = "PL1pRI26VpljPqC6Bc0jTyD0Uu6vHxy522";
        var APIKey = "AIzaSyB4FWtskLSR7Owq-EkwC9YITQedgen9NpE";//key for Andrii's work email, cause without it exceed limit error is appearing
        var url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&&playlistId=" + playlistId + "&key=" + APIKey;

        //next playlist...
        return {
            load: function (successHandler){
                $.getJSON(url, function (data) {
                    videoLists.relevant = data.items;
                    successHandler(videoLists);
                });

            }
        };
    })
    .service("Documentation", function(){
        return [];
    })
    .controller("questions-controller", function($scope, $routeParams, $timeout){
        var categoryFilter = $routeParams.category;

        $scope.questions = filterByCategory(categoryFilter, $scope.allQuestions);

        function filterByCategory (category, items){
            if(category == "faq"){
                return items.filter(function(item){
                    return item.isMostRelevant;
                })
            }else{
                return items.filter(function(item){
                    return item.category == category;
                })
            }

        }
    })
    .controller("videos-controller",function($scope, $rootScope, $routeParams, SupportConstants){

        var categoryFilter = $routeParams.category;
        updateVideos();

        $scope.$on(SupportConstants.UPDATE_VIDEOS, updateVideos);

        function updateVideos() {
            $scope.videos = $scope.allVideos[categoryFilter] || [];
        }

        $scope.showVideo = function(index) {
            $rootScope.video = $scope.allVideos[categoryFilter][index];
            setTimeout(function(){$scope.$apply();});
            setTimeout(function(){
                $("#preview-video").modal("show");
            });
        };
    })
    .controller("documentation-controller", function($scope){

    })
    .controller("message-controller", function($scope){

    })
    .controller("search-controller", function($scope, $rootScope){
        var items = {
            videos: [],
            faqs: [],
            documentation: []
        };
        $scope.lastSearchQuery = $scope.searchQuery;

        function getVideos(query){
            var videos = [];
            for(var name in $scope.allVideos){
                /*skip it, as it consists of duplicates

                 if(name == "relevant"){
                    continue;
                } */
                $.each($scope.allVideos[name], function(i, item){
                    if(item.video.title.toLowerCase().indexOf(query) >= 0){
                        videos.push(item)
                    }
                })
            }
            return videos;
        }

        function getQuestions(query){
            var questions = [];
            $.each($scope.allQuestions, function(i, item){
                if(item.title.toLowerCase().indexOf(query) >= 0
                    || item.content.toLowerCase().indexOf(query) >= 0){
                    questions.push(item)
                }
            });
            return questions;
        }

        function getDocumentation(query){
            var docs = [];
            $.each($scope.allDocumentation, function(i, item){
                if(item.title.toLowerCase().indexOf(query) >= 0
                    || item.content.toLowerCase().indexOf(query) >= 0){
                    docs.push(item)
                }
            });
            return docs;
        }

        $scope.showVideo = function(index) {
            $rootScope.video = $scope.results.videos[index];
            setTimeout(function(){$scope.$apply();});
            setTimeout(function(){
                $("#preview-video").modal("show");
            });
        };

        var q = $scope.lastSearchQuery.toLowerCase();
        items.videos = getVideos(q);
        items.faqs = getQuestions(q);
        items.documentation = getDocumentation(q);

        $scope.results = items;

        setTimeout(function(){
            $(".search-results").highlight($scope.lastSearchQuery)
        })

    })
    .controller("support-controller", function ($scope, $window, $rootScope, $location, $timeout, Questions, Videos,
                                                Documentation, Features, SupportConstants) {
        $scope.forumProperties = {};
        $scope.allQuestions = {};
        $scope.allVideos = {};
        $scope.allDocumentation = Documentation;
        $scope.searchQuery = "";
        $scope.isSearchResult = false;

        init();

        function init() {

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
        }


        $scope.toggleOpened = function(e){
            var li = $(e.target).closest("li");
            li.toggleClass("opened");
            li.find(".hide").slideToggle(300);
            return false;
        };

        $scope.getActiveTab = function(){
            var path = $location.path();
            var pathParts = path.split("/");
            return pathParts[1];
        };

        $scope.isLinkActive = function(kind, category){
            var pathParts = $location.path().split("/");
            var k = pathParts[1];
            var c = pathParts[2];
            return kind == k && category == c;
        };

        $scope.$on("$routeChangeSuccess", function() {
            setTimeout(function() {
                $(".scroll-area").scrollTop(0);
            },0);

            var pathParts = $location.path().split("/");
            var flag = $scope.isSearchResult;
            $scope.isSearchResult = pathParts[1] == "search";
            if(!$scope.isSearchResult && flag != $scope.isSearchResult){
                $scope.searchQuery = "";
            }
        });

        $scope.search = function(){
            if($scope.searchQuery.trim().length == 0){
                $scope.searchQuery = "";
                return;
            }
            $location.url("search?value=" + $scope.searchQuery);
        };

        $scope.closeVideo = function() {
            delete $rootScope.video;
            setTimeout( function(){$scope.$apply();});
        };

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
            hideOnPrivateInstallationVideoIds: ["UEwxcFJJMjZWcGxqUHFDNkJjMGpUeUQwVXU2dkh4eTUyMi41MzJCQjBCNDIyRkJDN0VD"]
        };
    });
