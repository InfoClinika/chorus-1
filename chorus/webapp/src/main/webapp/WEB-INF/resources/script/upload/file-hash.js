"use strict";

(function () {

// code from https://github.com/satazor/SparkMD5
    angular.module("file-hash", ["error-catcher"])
        .factory("fileHash", function () {
            return function (file, callback) {
                var blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice,
                    chunkSize = 2097152, // read in chunks of 2MB
                    chunks = Math.ceil(file.size / chunkSize),
                    currentChunk = 0,
                    spark = new SparkMD5.ArrayBuffer(),
                    frOnload = function (e) {
                        CommonLogger.log("read chunk nr", currentChunk + 1, "of", chunks);
                        spark.append(e.target.result); // append array buffer
                        currentChunk++;

                        if (currentChunk < chunks) {
                            loadNext();
                        } else {
                            CommonLogger.log("finished loading");
                            var hash = spark.end(); // compute hash
                            CommonLogger.info("computed hash", hash);
                            callback(hash);
                        }
                    },
                    frOnerror = function () {
                        CommonLogger.warn("oops, something went wrong.");
                    };

                function loadNext() {
                    var fileReader = new FileReader();
                    fileReader.onload = frOnload;
                    fileReader.onerror = frOnerror;

                    var start = currentChunk * chunkSize,
                        end = start + chunkSize >= file.size ? file.size : start + chunkSize;

                    fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
                }

                loadNext();
            };
        })
        .factory("chunkHash", function () {
            var url = substringUrlFrom("pages");
            var hashWorkerUrl = url + "script/upload/hash-worker.js";

            CommonLogger.log("Importing script from: " + hashWorkerUrl);

            var worker = new Worker(hashWorkerUrl);

            return function (chunk, callback) {
                var fileReader = new FileReader();
                fileReader.onload = function (e) {
                    CommonLogger.log("Sending data to hash to the worker...");
                    worker.postMessage({result: e.target.result, url: document.location.href});
                };
                worker.onmessage = function (e) {
                    CommonLogger.log("Received from hashing worker: " + e.data);
                    callback(e.data);
                };
                fileReader.readAsArrayBuffer(chunk);
            };
        });

    function substringUrlFrom(path) {
        var url = location.href;
        var index = url.indexOf(path);
        if (index != -1) {
            url = url.substring(0, index);
        }
        return url;
    }

})();
