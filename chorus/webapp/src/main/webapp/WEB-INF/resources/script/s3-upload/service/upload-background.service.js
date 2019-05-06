"use strict";

(function () {

    angular.module("s3UploadModule")
        .factory("UploadBackgroundService", function (UploadService, $timeout) {
            var uploadInBackground = null;
            checkActiveUpload();

            return {
                getActive: function () {
                    return uploadInBackground;
                },
                hasActive: function () {
                    return uploadInBackground !== null;
                },
                moveToBackground: moveToBackground,
                moveToForeground: function (uploadId) {
                    if (uploadInBackground.id === uploadId) {
                        uploadInBackground = null;
                    }
                }
            };

            function moveToBackground(upload) {
                UploadService.listActiveUploads()
                    .then(function (activeUploads) {
                        uploadInBackground = activeUploads.find(function (item) {
                            return item.id === upload.id;
                        });
                        watchProgress(uploadInBackground);
                    });
            }

            function watchProgress(upload) {
                if (!upload) {
                    return;
                }
                console.log("watchProgress", upload);
                UploadService.getUpload(upload.id)
                    .then(function (data) {
                        upload.progress = data.progress;
                        if (isInProgress(data)) {
                            var delay = data.progress > 0 ?
                                Math.max(1000, data.durationInMillis / data.progress) :
                                5000;
                            $timeout(function () {
                                watchProgress(upload);
                            }, delay);
                        } else {
                            uploadInBackground = null;
                        }
                    });
            }

            function isInProgress(upload) {
                return upload.status !== "CANCELED" && upload.status !== "FINISHED" && upload.progress < 100;
            }

            function checkActiveUpload() {
                UploadService.listActiveUploads()
                    .then(function (activeUploads) {
                        if (activeUploads && activeUploads.length) {
                            moveToBackground(activeUploads[0]);
                        }
                    });
            }
        });

})();
