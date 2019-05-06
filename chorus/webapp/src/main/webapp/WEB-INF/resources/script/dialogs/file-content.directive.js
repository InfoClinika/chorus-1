"use strict";

(function () {

    angular.module("dialogs")

        .directive("fileContent", [function () {
            return {
                scope: {
                    fileContent: "="
                },
                link: function (scope, element) {
                    element.bind("change", function (changeEvent) {
                        scope.$apply(function () {
                            var reader = new FileReader();
                            reader.onload = function (loadEvent) {
                                scope.$apply(function () {
                                    scope.fileContent(changeEvent.target.files[0].name, loadEvent.target.result);
                                });
                            };
                            reader.readAsDataURL(changeEvent.target.files[0]);
                        });
                    });
                }
            };
        }]);

})();
