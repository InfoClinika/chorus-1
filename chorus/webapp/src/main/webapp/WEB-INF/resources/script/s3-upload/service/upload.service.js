"use strict";

(function () {

    const ENDPOINT = "/v2/uploads";
    const CHECK_UPLOAD_LIMIT_ENDPOINT = ENDPOINT + "/checkUploadLimit";

    angular.module("s3UploadModule")
        .factory("UploadService", function ($http, $q) {
            var activeUploads = [];
            listActiveUploads();

            return {
                copy: copyFiles,
                done: uploadDone,
                list: getFileList,
                checkUploadLimit: checkUploadLimit,
                status: getProgress,
                cancelCopy: cancelUploadJob,
                cancelCopyFile: cancelUploadFile,
                validateS3Url: function () {
                    return true;
                },
                types: getTypes,
                user: getUser,
                getWorkflowArn: getWorkflowArn,
                getBucketPolicy: getBucketPolicy,
                getUpload: getUpload,
                listActiveUploads: listActiveUploads
            };

            function copyFiles(uploadId, files) {
                return $http.put(ENDPOINT + "/" + uploadId + "/files", files)
                    .error(function (error) {
                        console.log("Copy files error", error);
                    });
            }

            function checkUploadLimit(request) {
                var def = $q.defer();
                $http.get(CHECK_UPLOAD_LIMIT_ENDPOINT, {params: request})
                    .success(function (response) {
                        def.resolve(response);
                    });
                return def.promise;
            }

            function getFileList(type, instrumentId, url, user, pass, recursive, masks) {
                var def = $q.defer();
                $http.post(ENDPOINT, {
                    url: url,
                    type: type,
                    user: user,
                    pass: pass,
                    masks: masks || ["*.*"],
                    recursive: recursive || false,
                    instrumentId: instrumentId
                })
                    .success(function (data) {
                        def.resolve(data);
                    })
                    .error(function (data, status) {
                        def.reject(data);
                    });
                return def.promise;
            }

            function getProgress(uploadId) {
                return $http
                    .get(ENDPOINT + "/" + uploadId + "/files")
                    .then(function (response) {
                        return response.data;
                    });
            }

            function cancelUploadJob(uploadId) {
                return $http.delete(ENDPOINT + "/" + uploadId);
            }

            function cancelUploadFile(uploadId, fileId) {
                return $http.delete(ENDPOINT + "/" + uploadId + "/files/" + fileId);
            }

            function uploadDone(instrumentId, fileNames, type) {
                var id = SparkMD5.hash(fileNames.join("|"));
                var data = {
                    type: type || "DIRECT",
                    files: fileNames,
                    instrumentId: instrumentId,
                    done: true
                };
                $http.put(ENDPOINT + "/" + id, data);
            }

            function getTypes() {
                return $http.get(ENDPOINT + "/types")
                    .then(function (response) {
                        return response.data;
                    });
            }

            function getUser() {
                return $http.get(ENDPOINT + "/user")
                    .then(function (response) {
                        var user = response.data;
                        return user.substring(1, user.length - 1);
                    });
            }

            function getWorkflowArn() {
                return $http.get(ENDPOINT + "/workflowArn")
                    .then(function (response) {
                        var user = response.data;
                        return user.substring(1, user.length - 1);
                    });
            }

            function getBucketPolicy(s3Url) {
                var config = {
                    params: {
                        "s3Url": s3Url
                    }
                };
                return $http.get(ENDPOINT + "/getBucketPolicy", config)
                    .then(function (response) {
                        return JSON.parse(response.data);
                    });
            }

            function getUpload(uploadId) {
                return $http.get(ENDPOINT + "/" + uploadId)
                    .then(function (response) {
                        return response.data;
                    });
            }

            function listActiveUploads() {
                return $http.get(ENDPOINT)
                    .then(function (response) {
                        activeUploads = response.data;
                        return activeUploads;
                    });
            }
        })

        .factory("TRANSFER_METHODS", function (UploadService) {
            const TRANSFER_METHODS = {};

            UploadService.types()
                .then(function (types) {
                    types.forEach(function (type) {
                        TRANSFER_METHODS[type.id] = type;
                    });
                });

            return TRANSFER_METHODS;
        });

})();
