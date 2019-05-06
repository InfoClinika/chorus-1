"use strict";

(function () {
    angular.module("news-front", ["news-back", "error-catcher"])
        .controller("news-controller", function ($scope, News, removeNewsConfirmation) {
            CommonLogger.setTags(["NEWS", "NEWS-CONTROLLER"]);
            $scope.news = News.query();
            $scope.page.title = "News";
            $scope.page.showFilter = true;

            $scope.isTableEmpty = function () {
                return $scope.news.length == 0;
            };

            $scope.getEmptyTableMessage = function () {
                return "There are no news";
            };

            $scope.showRemoveConfirmation = removeNewsConfirmation($scope);
        })
        .controller("news-details", function ($scope, News, $routeParams) {
            CommonLogger.setTags(["NEWS", "NEWS-DETAILS-CONTROLLER"]);
            $scope.details = News.get({path: "details", id: $routeParams.id}, function (response) {
                CommonLogger.log(response);
            });

            $scope.save = function () {
                News.update({
                        title: $scope.details.title,
                        author: $scope.details.creator,
                        text: $scope.details.text,
                        id: $scope.details.id,
                        introduction: $scope.details.introduction
                    }
                    , function () {
                        hideModal();
                    });
            };
        })
        .controller("newNews", function ($scope, News) {
            CommonLogger.setTags(["NEWS", "NEW-NEWS-CONTROLLER"]);
            $scope.news = {};

            $scope.save = function () {
                News.save($scope.news, function () {
                    hideModal();
                });
            };
        })
        .directive("newsDetails", detailsLink({title: "Show News Details"}))
        .factory("removeNewsConfirmation", function ($route, News) {
            return function ($scope) {
                return function (news) {
                    $scope.confirmation = new Confirmation("#remove-news-confirmation", news,
                        {
                            success: function () {
                                News.delete({id: news.id}, function () {
                                    $route.reload();
                                });
                            },
                            getName: function () {
                                return news.title;
                            }
                        }
                    );
                    $scope.confirmation.showPopup();
                };
            };
        });

})();
