"use strict";

(function () {
    angular.module("blog-front", ["security-front", "projects", "blog-back", "modals", "ui.tinymce", "error-catcher",
        "template-components", "header", "footer"])
        .config(function ($routeProvider) {
            $routeProvider
                .when("/", {controller: "dashboard", templateUrl: "blog/dashboard.html"})
                .when("/:blog", {controller: "project-blog", templateUrl: "blog/project-blog.html"})
                .when("/:blog/post", {controller: "create-post", templateUrl: "blog/edit-post.html"})
                .when("/:blog/edit/:post", {controller: "edit-post", templateUrl: "blog/edit-post.html"})
                .when("/:blog/:post", {controller: "view-post", templateUrl: "blog/view-post.html"});
        })
        .controller("main-controller", function () {
            CommonLogger.setTags(["BLOG", "BLOG-CONTROLLER"]);
            // inspired by http://stackoverflow.com/questions/10713708/tracking-google-analytics-page-views-with-angular-js
        })
        .controller("project-blog", function ($scope, $routeParams, Blog, BlogPost) {

            $scope.blog = Blog.get({blog: $routeParams.blog});

            $scope.posts = BlogPost.query({blog: $routeParams.blog});

            $scope.refreshAccess = function () {
                $scope.access = Blog.access({blog: $routeParams.blog});
                $scope.blog = Blog.get({blog: $routeParams.blog});
            };

            $scope.refreshAccess();

        })
        .controller("create-post", function ($scope, $location, $routeParams, Blog, BlogPost) {
            $scope.blog = Blog.get({blog: $routeParams.blog}, function (blog) {
                $scope.post = new BlogPost({blog: blog});
            });

            $scope.returnToBlog = function () {
                $location.path("/" + $routeParams.blog);
            };

            $scope.tinymceOptions = {
                plugins: ["link", "image"],
                menubar: false,
                toolbar: "undo redo | styleselect removeformat | bold italic | justifyleft justifycenter justifyright justifyfull | bullist numlist outdent indent | link image"
            };

        })
        .controller("edit-post", function ($scope, $location, $routeParams, BlogPost, Blog) {
            $scope.blog = Blog.get({blog: $routeParams.blog});

            $scope.post = BlogPost.get({post: $routeParams.post, blog: $routeParams.blog});

            $scope.returnToBlog = function () {
                $location.path("/" + $routeParams.blog);
                tinymce.remove(); //fix for ie - removing the duplicates of tinymce plugin
            };

            $scope.tinymceOptions = {
                plugins: ["link", "image"],
                menubar: false,
                toolbar: "undo redo | styleselect removeformat | bold italic | justifyleft justifycenter justifyright justifyfull | bullist numlist outdent indent | link image"
            };

        })
        .controller("view-post", function ($scope, $routeParams, $location, BlogPost, BlogComment) {
            $scope.updateComments = function () {
                $scope.comments = BlogComment.query({post: $routeParams.post, blog: $routeParams.blog});
                $scope.newComment = new BlogComment({
                    post: $scope.post
                });
                if ($location.search().action == "comment") {
                    $scope.scrollToCommentFormArea();
                }
            };

            $scope.post = BlogPost.get({post: $routeParams.post, blog: $routeParams.blog}, $scope.updateComments);

            function refreshAccess() {
                $scope.access = BlogPost.access({post: $routeParams.post, blog: $routeParams.blog});
                $scope.post = BlogPost.get({post: $routeParams.post, blog: $routeParams.blog});
            }

            $scope.access = BlogPost.access({post: $routeParams.post, blog: $routeParams.blog});

            $scope.subscribe = function () {
                BlogPost.subscribe({post: $routeParams.post, blog: $routeParams.blog}, {}, refreshAccess);
            };

            $scope.unsubscribe = function () {
                BlogPost.unsubscribe({post: $routeParams.post, blog: $routeParams.blog}, {}, refreshAccess);
            };
            $scope.removeConfirmationDialog = {

                selectorConfirmation: "#remove-post-confirmation",
                remove: function () {
                    removeBlogPost();
                    this.hideConfirmation();
                },
                hideConfirmation: function () {
                    $(this.selectorConfirmation).modal("hide");
                },
                showConfirmation: function () {
                    $(this.selectorConfirmation).modal("show");
                }
            };
            $scope.scrollToCommentFormArea = function () {
                setTimeout(function () {
                    $("html, body").scrollTo("#commentForm");
                    $("#commentForm textarea").focus();
                }, 0);
            };

            function removeBlogPost() {
                BlogPost.delete({post: $routeParams.post, blog: $routeParams.blog}, function () {
                    $location.path("/" + $routeParams.blog);
                });
            }
        })
        .controller("dashboard", function ($scope, BlogPost, Blog) {
            $scope.sorting = {};
            $scope.sorting.reverse = true;
            $scope.sorting.field = "date";

            function refreshAccess() {
                $scope.accessBlogs = Blog.access({
                    blogs: $.map($scope.recentBlogs, function (post) {
                        return post.blog.id;
                    })
                });
            }

            function refreshAll() {
                $scope.recentBlogs = Blog.recent(refreshAccess);
                BlogPost.recent({}, function (posts) {
                    if (posts.length < 6) {
                        posts = posts.concat(new Array(6 - posts.length));
                    }
                    $scope.recentPosts = [posts.slice(0, 2), posts.slice(2, 6)];
                });
            }

            refreshAll();

            $scope.subscribeBlog = function (blog) {
                Blog.subscribe({blog: blog.id}, {}, refreshAll);
            };

            $scope.unsubscribeBlog = function (blog) {
                Blog.unsubscribe({blog: blog.id}, {}, refreshAll);
            };


        })
        .directive("unrouteSorting", function () {
            return {
                restrict: "A",
                priority: -1,
                link: function (scope, element, attrs) {
                    if (!scope.sorting) {
                        return;
                    }

                    element.addClass("sortable");

                    scope.$watch("sorting.reverse", function (n) {
                        if (scope.sorting.field === attrs.unrouteSorting) {
                            element.find("i").remove();
                            element.prepend(n === false || n === "false" ?
                                "<i class=\"icon icon-chevron-up\">" :
                                "<i class=\"icon icon-chevron-down\">");
                        }
                    });

                    scope.$watch("sorting.field", function (n) {
                        if (n === attrs.unrouteSorting) {
                            element.addClass("active");
                        } else {
                            element.find("i").remove();
                            element.removeClass("active");
                        }
                    });

                    element.bind("click", function () {
                        var isCurrentStateFalse = scope.sorting.reverse === false || scope.sorting.reverse === "false";
                        scope.sorting.field = attrs.unrouteSorting;
                        scope.sorting.reverse = isCurrentStateFalse;

                        scope.$apply();
                    });
                }
            };
        })
        .controller("project-info", function ($scope, ProjectDetails, $routeParams) {
            $scope.projectShort = ProjectDetails.short({id: $routeParams.blog});
            $scope.project = ProjectDetails.get({id: $routeParams.blog});
        })
        .filter("dehtml", function () {
            // inspired by http://stackoverflow.com/questions/822452/strip-html-from-text-javascript
            return function (input) {
                var tmp = document.createElement("DIV");
                tmp.innerHTML = input;
                return tmp.textContent || tmp.innerText || "";
            };
        })
        .filter("firstImageUrl", function () {
            return function (content) {
                var image = $("<div/>").html(content).find("img")[0];
                return image ? $(image).attr("src") : null;
            };
        })
        .filter("firstParagraph", function () {
            return function (content) {
                var paragraph = $("<div/>").html(content).find("p").filter(function () {
                    return $(this).text().trim().length > 0;
                })[0];
                return paragraph ? $(paragraph).html() : content;
            };
        })
        .filter("charLimit", function () {
            return function (text, length, end) {
                if (!length) {
                    length = 10;
                }
                if (!end) {
                    end = "...";
                }
                if (text) {
                    if (text.length <= length || text.length - end.length <= length) {
                        return text;
                    }
                    return String(text).substring(0, length - end.length) + end;
                }
            };
        })
        .directive("afterCommentsRendering", function ($location) {
            return function (scope) {
                if ($location.search().action == "comment" && scope.$last) {
                    scope.scrollToCommentFormArea();
                }
            };
        });

})();
