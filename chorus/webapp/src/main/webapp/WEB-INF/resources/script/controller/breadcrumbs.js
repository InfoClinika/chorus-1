angular.module("breadcrumbs", ["error-catcher"])
    .run(function($rootScope) {
        $rootScope.breadcrumbHandlers = [];
        $rootScope.returnUrl = {};
        $rootScope.currentUrl = {};
    })
    .factory("registerBreadcrumbHandler", function($rootScope) {
        return function(handler) {
            $rootScope.breadcrumbHandlers.push(handler);
        };
    })
    .controller("breadcrumbs", function($location, $scope, $rootScope) {
        CommonLogger.setTags(["BREADCRUMBS", "BREADCRUMBS-CONTROLLER"]);
        function update() {
            $rootScope.returnUrl = $rootScope.currentUrl;
            $rootScope.currentUrl = $location.path();
            var pathParts = $rootScope.currentUrl.split("/");
            var breadcrumbs = [];

            function handle(path) {
                var handlers = $rootScope.breadcrumbHandlers;
                for (var i = 0; i < handlers.length; i++) {
                    var breadcrumb = handlers[i](path);
                    if (breadcrumb) {
                        breadcrumbs.push(breadcrumb);
                        break;
                    }
                }
            }

            var path = "";

            for (var i = 1; i < pathParts.length; i++) {
                var pathPart = pathParts[i];
                path += "/" + pathPart;
                handle(path);
            }

            $scope.breadcrumbs = breadcrumbs;
        }

        $scope.$on("$locationChangeSuccess", update);
        update();
    });