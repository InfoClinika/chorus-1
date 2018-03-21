angular.module("index-page", ["security-front", "statistics-back", "formatters", "news-back", "error-catcher", 
    "current-year", "features-back", "header"]).
    controller("home-controller", function ($scope, $http, Statistics, News, Features) {
        $scope.forumProperties = {};
        $scope.ssoProperties = {};

        Features.getForumProperties(function (response) {
            $scope.forumProperties = response;
        });

        Features.getSsoProperties(function (response) {
            $scope.ssoProperties = response;
        });
        $scope.usage = Statistics.usage();

        setupYouTubeItems($scope);

        $scope.showVideo = function(index) {
            $scope.video = $scope.items[index];
            setTimeout(function(){$scope.$apply();});
            setTimeout(function(){
                $("#preview-video").modal("show");
            });
        };

        $scope.closeVideo = function() {
            delete $scope.video;
            setTimeout( function(){$scope.$apply();});
        };

        var count = 2;
        $scope.news = News.query({count: count});
    })
    //http://stackoverflow.com/questions/13964735/angularjs-newline-filter-with-no-other-html
    .filter("newlines", function () {
        return function(text) {
            return text.replace(/\n/g, "<br/>");
        }
    });

function setupYouTubeItems($scope) {
    var playlistId = "PL1pRI26VpljPqC6Bc0jTyD0Uu6vHxy522";
    var APIKey = "AIzaSyB4FWtskLSR7Owq-EkwC9YITQedgen9NpE";//key for Andrii"s work email, cause without it exceed limit error is appearing
    var url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&&playlistId=" + playlistId + "&key=" + APIKey;
    $.getJSON(url, function (data, status, xhr) {
        $scope.items = data.items;
        $scope.$apply();
    });
}

$(document).ready(function () {
    $(".carousel").carousel({
        interval: 10000000 // in milliseconds
    });
});
