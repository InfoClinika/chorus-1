"use strict";

(function () {

    const ENDPOINT = "/v2/filesFilter";

    angular.module("dialogs")

        .factory("FilterFilesService", function ($http) {
            return {
                search: search
            };

            function search(filter, field, asc, page, pageSize) {
                return $http
                    .post(ENDPOINT, {
                        filter: filter.advanced ? filter.composed : null,
                        query: filter.query,
                        sortBy: field,
                        sortAsc: asc,
                        page: page > 0 ? page : 1,
                        pageSize: pageSize > 0 ? pageSize : 100000
                    })
                    .then(function (response) {
                        return response.data;
                    });
            }
        });

})();
