angular.module("blog-back", ["ngResource"])
    .factory("Blog", function ($resource) {
        return $resource("../blog/:blog/:action", {blog: "@id"},
            {
                access: {method: "GET", params: {action: "access"}},
                recent: {method: "GET", params: {action: "recent"}, isArray: true},
                subscribe: {method: "POST", params: {action: "subscribe"}},
                unsubscribe: {method: "POST", params: {action: "unsubscribe"}}
            }
        );
    })
    .factory("BlogPost", function ($resource) {
        return $resource("../blog/post/:post/:action", {},
            {
                access: {method: "GET", params: {action: "access"}},
                recent: {method: "GET", params: {post: "recent"}, isArray: true},
                subscribe: {method: "POST", params: {action: "subscribe"}},
                unsubscribe: {method: "POST", params: {action: "unsubscribe"}}
            }
        );
    })
    .factory("BlogComment", function ($resource) {
        return $resource("../blog/comment")
    });