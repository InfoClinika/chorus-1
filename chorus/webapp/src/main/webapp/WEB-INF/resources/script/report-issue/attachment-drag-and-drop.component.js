"use strict";

(function () {
    var ACCEPTABLE_EXTENSIONS = ["png", "gif", "bmp", "jpg", "jpeg", "doc", "docx", "xls", "xlsx", "csv", "tsv", "txt"];
    var IMAGE_TYPES = ["image/png", "image/gif", "image/bmp", "image/jpg", "image/jpeg"];
    var DRAG_OVER_CLASS = "drag-over";
    var MAX_FILE_SIZE = 10485760; // 10 MB
    var ALERT_CONTAINER_SELECTOR = ".drag-and-drop .unsupported-files-alert";

    angular.module("report-issue")
        .directive("attachmentDragAndDrop", attachmentDragAndDrop)
        .directive("onFilesSelected", onFilesSelected)
        .directive("onFilesDrop", onFilesDrop);


    function attachmentDragAndDrop() {
        return {
            restrict: "E",
            replace: true,
            require: "ngModel",
            templateUrl: "../script/report-issue/attachment-drag-and-drop.component.html",
            scope: {attachments: "="},
            controller: function ($scope) {
                var vm = {
                    attachments: $scope.attachments,
                    alerts: [],

                    getAcceptableExtensions: getAcceptableExtensions,
                    addFiles: addFiles,
                    removeAttachment: removeAttachment
                };
                $scope.vm = vm;

                function getAcceptableExtensions() {
                    var result = "";
                    ACCEPTABLE_EXTENSIONS.forEach(function (ext) {
                        result += "." + ext + ",";
                    });
                    return result;
                }

                function addFiles(files) {
                    var droppedFile = null;
                    var alerts = [];

                    for (var i = 0; i < files.length; i++) {
                        droppedFile = files[i];
                        var isUnique = !vm.attachments.find(findCallback);
                        console.log("File dropped", droppedFile.type);
                        var isImage = IMAGE_TYPES.indexOf(droppedFile.type) !== -1;

                        if (!isUnique) {
                            alerts.push("File " + droppedFile.name + " already in list.");
                        }

                        if (droppedFile.size > MAX_FILE_SIZE) {
                            alerts.push("File " + droppedFile.name + " size is more then 10MB.");
                        }

                        alerts.push();
                        if (isUnique && droppedFile.size <= MAX_FILE_SIZE) {
                            addFile(droppedFile, isImage);
                        }
                    }

                    if (alerts.length) {
                        showAlerts(alerts);
                    }

                    function findCallback(attachment) {
                        return attachment.name === droppedFile.name;
                    }
                }

                function addFile(file, isImage) {
                    readFile(file, function (content) {
                        vm.attachments.push({
                            content: content,
                            name: file.name,
                            type: file.type,
                            isImage: isImage,
                            fromCloud: false
                        });
                    });
                }

                function removeAttachment(index) {
                    vm.attachments.splice(index, 1);
                }

                function readFile(file, callback) {
                    var reader = new FileReader();
                    reader.onload = function (e) {
                        callback(e.target.result);
                        $scope.$apply();
                    };
                    reader.readAsDataURL(file);
                }

                function showAlerts(alerts) {
                    vm.alerts = alerts;
                    setTimeout(function () {
                        $scope.$apply();
                        $(ALERT_CONTAINER_SELECTOR).fadeIn(500)
                            .delay(1000)
                            .fadeOut(2000, function () {
                                vm.alerts = [];
                            });
                    });
                }
            }
        };
    }

    function onFilesSelected() {
        return {
            restrict: "A",
            link: function (scope, element, attrs) {
                var handler = scope.$eval(attrs.onFilesSelected);

                element.bind("change", function (event) {
                    if (event.target.files && event.target.files.length) {
                        scope.$apply(function () {
                            handler(event.target.files);
                        });
                    }
                });
            }
        };
    }

    function onFilesDrop($document) {
        return {
            restrict: "A",
            link: function (scope, element, attrs) {
                var onFilesDropHandler = scope.$eval(attrs.onFilesDrop);
                var isDragEntered = false;
                var timeout = null;

                element.bind("drop", onFileDrop);
                $document.bind("dragover", onDragOver);

                function onDragOver(e) {
                    e.preventDefault();

                    clearTimeout(timeout);
                    timeout = setTimeout(function () {
                        isDragEntered = false;
                        clearTimeout(timeout);
                        element.removeClass(DRAG_OVER_CLASS);
                    }, 100);

                    if (!isDragEntered) {
                        isDragEntered = true;
                        element.addClass(DRAG_OVER_CLASS);
                    }
                }

                function onFileDrop(e) {
                    e.preventDefault();
                    element.removeClass(DRAG_OVER_CLASS);

                    var files = e.originalEvent.dataTransfer.files;
                    scope.$apply(function () {
                        onFilesDropHandler(files);
                    });

                }
            }
        };
    }

})();
