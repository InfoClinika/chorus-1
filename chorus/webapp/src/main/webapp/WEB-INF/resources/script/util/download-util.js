"use strict";

(function () {

    angular.module("downloader", ["error-catcher"])
        .factory("downloadFiles", function () {
            return function (files, experiment, lab) {

                if (files && files.length > 0 && typeof files[0] === "object") {
                    files = $.map(files, function (file) {
                        return file.id;
                    });
                }

                var isSingleDownload = !experiment && files && files.length == 1;
                var path = "";

                if (isSingleDownload) {
                    path = "/download/singleFileDownloadUrl?" + $.param({file: files[0], lab: lab});
                } else {
                    path = subStringLastIndexOfFor(window.location.pathname, "/", 2) +
                        "/download/bulk?" + $.param({files: files, experiment: experiment, lab: lab}, true);
                }

                function downloadFunction(url) {
                    $.fileDownload(url, {
                        failCallback: function () {
                            errorHandler();
                        }
                    });
                }

                function getSingleFileUrl(url, successHandler) {
                    $.ajax({
                        type: "GET",
                        url: url,
                        async: true,
                        success: function (response) {
                            successHandler(response.value);
                        }
                    });
                }

                function errorHandler() {
                    fileDownloadErrorHandler("#file-download-message");
                }


                function fileDownloadErrorHandler(selector) {
                    $(".modal").modal("hide");

                    $(selector).dialog({
                        draggable: false,
                        dialogClass: "message-dialog error",
                        modal: true,
                        resizable: false,
                        width: 450,
                        buttons: {
                            OK: function () {
                                $(this).dialog("close");
                            }
                        }
                    });
                }


                function subStringLastIndexOfFor(string, searchString, times) {
                    var result = string;
                    for (var i = 0; i < times; i++) {
                        result = result.substring(0, result.lastIndexOf(searchString));
                    }
                    return result;
                }

                return {
                    url: window.location.origin + path,
                    download: function () {

                        if (isSingleDownload) {
                            getSingleFileUrl(path, function (singleFileUrl) {
                                if (singleFileUrl == null) {
                                    errorHandler();
                                } else {
                                    downloadFunction(singleFileUrl);
                                }
                            });

                        } else {
                            downloadFunction(path);
                        }

                    }
                };
            };
        });

})();
