"use strict";

(function () {
    angular.module("blog-back", ["ngResource"])
        .factory("Blog", Blog)
        .factory("BlogPost", BlogPost)
        .factory("BlogComment", function ($resource) {
            return $resource("../blog/comment");
        });

    function BlogPost($resource) {
        return $resource("../blog/post/:post/:action", {},
            {
                access: {method: "GET", params: {action: "access"}},
                recent: {method: "GET", params: {post: "recent"}, isArray: true},
                subscribe: {method: "POST", params: {action: "subscribe"}},
                unsubscribe: {method: "POST", params: {action: "unsubscribe"}}
            }
        );
    }

    function Blog($resource) {
        return $resource("../blog/:blog/:action", {blog: "@id"},
            {
                access: {method: "GET", params: {action: "access"}},
                recent: {method: "GET", params: {action: "recent"}, isArray: true},
                subscribe: {method: "POST", params: {action: "subscribe"}},
                unsubscribe: {method: "POST", params: {action: "unsubscribe"}}
            }
        );
    }
})();
