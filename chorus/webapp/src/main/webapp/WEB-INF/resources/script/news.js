angular.module("news-page", ["news-back", "security-front", "error-catcher", "current-year", "header"]).
    controller("news-controller", function ($scope, $window, News) {
        var count = 10;
        $scope.news = News.query({count: count}, function(){
            setTimeout(function () {
                $scope.$apply();
                //don't use $location as it adds "/" after "#" in url
                var elem = $($window.location.hash);
                if (elem.length == 1) {
                    $(document).scrollTop(elem.offset().top);
                }
            }, 0);
        });
    });

$(document).ready(function () {
    $(".twocolumns .more a").click(function () {
        $(this).parent().toggleClass("opened");
        $(this).parent().parent().find(".hide").slideToggle(500);
        return false;
    });
});

