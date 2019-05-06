/**
 * @author Adam Filkor <adam.filkor at gmail.com>
 * @author The first version created by Niklas von Hertzen <niklas at hertzen.com> Please visit his cool experiments at hertzen.com! - Adam
 * @created 17.04.2012
 * @website http://filkor.org
 */


var CorsRequestSignUrls = {
    signInitialMultipartRequestUrl: "../cors/sign/initial",
    signPartUploadRequestUrl: "../cors/sign/part",
    signListPartsRequestUrl: "../cors/sign/list",
    signCompleteMultipartRequestUrl: "../cors/sign/complete",
    signAbortMultipartRequestUrl: "../cors/sign/abort",
    signSingleFileUrl: "../cors/sign/singlefile"
};

var UploadDefaults = {
    packetSize: 1024 * 1024 * 6 // bytes, defaults to 6MB packets; Amazon min packet size is 5Mb
};

//If part upload won't be uploaded in selected time uploader will cancel or retry it.
var MAX_PART_UPLOAD_TIME = 600000; // ten minutes


angular.module("js-upload", ["file-hash", "error-catcher"])
    .factory("startNewUpload", function (chunkHash) {
        /**
         * The core upload function. The way it works is very simple: slice the file on client side, sends the slices
         * to the server. When no more slices remain the server merges the slices (or packets). This function continuously stores
         * the current packages' number in the localStorage, so we can pause the upload anytime,
         * then continue upload from the latest package.
         * @param {Object} options
         * @param options.file The current File object
         * @param options.logger Logger element, where we write the logs.
         * @param options.progressHandler Function that updates the progressbar value, at percent 100 it shows the "success image"
         * @param options.pauseButton Reference to the proper pause button element
         * @param options.unfinishedUpload [optional] reference to the unfinishedUpload object you got from UnfinishedUploads resource factory
         */
        return function (options) {
            var self = {};

            self.state = "uploading";

            options.logger = options.logger || function (msg) {
                CommonLogger.log(msg);
            };

            function initTimings() {
                self.packetsUploadedInPreviousSession = [];
                self.startTime = (new Date()).getTime();
            }

            initTimings();

            function init() {

                log("File uploader initialized");
                self.file = options.file;

                self.totalSize = self.file.size;

                self.confirmUrl = options.confirmUrl;
                self.startMultipartUrl = options.startMultipartUrl;

                self.type = self.file.type;
                self.fileName = self.file.name;
                self.fileItemID = self.file.fileItemID;
                self.destinationPath = options.destinationPath;

                self.packetSize = UploadDefaults.packetSize;

                self.totalPackages = Math.ceil(self.totalSize / self.packetSize);

                self.putRetryCount = 0;

                self.chunkUploadWorkers = [];
                self.smallFileUploader = null;

                self.totalWorkers = 10;

                log("Total size: " + self.totalSize / (1024 * 1024) + " mb, total of " + self.totalPackages +
                    " packets");

                if (options.onUploadStart) {
                    options.onUploadStart(self.fileItemID);
                }

                prepareAndStart();
            }

            function prepareAndStart() {
                log("Uploading file " + self.fileName + " to " + self.destinationPath);

                log("Checking whether to resume upload");

                //todo[tymchenko]: fetch list of packets from Amazon by uploadId
                var unfinishedUpload = options.unfinishedUpload;
                if (unfinishedUpload) {
                    var uploadId = unfinishedUpload.uploadId;
                    var destinationPath = unfinishedUpload.destinationPath;
                    log("Resuming upload for the multipart upload with ID " + uploadId + " and destination path " +
                        destinationPath);
                    startWithDetails({
                        fileItemID: unfinishedUpload.id,
                        uploadId: uploadId,
                        destinationPath: destinationPath
                    });
                } else {
                    log("No upload to resume, informing server to initialize a new upload");

                    startWithDetails({
                        fileItemID: self.fileItemID
                    });
                }
            }

            function confirmUploadWithServer(verificationSuccessFn, verificationFailFn) {
                if (self.confirmUrl) {
                    var confirmMultipartUploadRequest = {
                        fileId: self.fileItemID,
                        fileSize: self.totalSize,
                        remoteDestination: self.destinationPath
                    };

                    $.ajax({
                        type: "POST",
                        url: self.confirmUrl,
                        data: JSON.stringify(confirmMultipartUploadRequest),
                        dataType: "json",
                        contentType: "application/json",
                        success: function () {
                            verificationSuccessFn();
                        },
                        error: function () {
                            verificationFailFn();
                        }
                    });
                } else {
                    //makes sense for attachments
                    log("Confirm URL has not been supplied");
                    verificationSuccessFn();
                }
            }

            function startWithDetails(details) {
                self.fileDetails = details;

                self.chunkUploadWorkers = [];
                self.smallFileUploader = null;

                if (self.fileDetails.destinationPath) {
                    self.destinationPath = self.fileDetails.destinationPath;
                }

                var getJobTickets = function (callback) {
                    if (self.fileDetails.uploadId) {

                        log("Resuming upload. Obtaining the list of uploaded packets from Amazon...");
                        AmazonMultipartUploader.listUploadedPackets(self.destinationPath, self.fileDetails.uploadId,
                            function (packets) {
                                var jobTickets = [];
                                for (var t = 0; t < self.totalPackages; t++) {
                                    var packet = (function (ticketNumber) {
                                        return $.grep(packets, function (packet) {
                                            return packet.partNumber == ticketNumber + 1;
                                        });
                                    })(t);
                                    if (packet.length == 0) {
                                        //packet with index t has not been uploaded yet; adding to queue
                                        jobTickets.push(t);
                                    }
                                }
                                log("List of packet indexes to upload during resume: " + jobTickets.join(", "));
                                self.packetsUploadedInPreviousSession = packets;
                                callback(jobTickets);
                            }
                        );
                    } else {
                        log("Starting the upload from scratch.");
                        var jobTickets = [];
                        for (var t = 0; t < self.totalPackages; t++) {
                            jobTickets.push(t);
                        }
                        callback(jobTickets);
                    }
                };


                if (self.totalSize > self.packetSize) {
                    log("Uploading size is greater than packet size. Using multipart upload...");
                    var uploader = new AmazonMultipartUploader(
                        self.destinationPath,
                        self.fileItemID,
                        self.startMultipartUrl
                    );
                    if (self.fileDetails.uploadId) {
                        //supply uploaders with an upload ID if it already exists
                        uploader.uploadId = self.fileDetails.uploadId;
                    }
                    uploadMultipart(uploader, getJobTickets);
                } else {
                    log("Uploading size is less than packet size. Using plain upload directly to S3");
                    self.smallFileUploader = new AmazonSmallFileUploader();
                    self.smallFileUploader.uploadFile(
                        self.destinationPath,
                        getWholeFile(),
                        function (remoteDestination) {
                            log("Notifying server...");
                            confirmUploadWithServer(verificationSuccess, verificationFail);

                            function verificationSuccess() {
                                //set progressbar to 100%, set the serverFileId for the download link
                                options.progressHandler(self.fileDetails.fileItemID, self.totalSize, null);
                                if (options.onUploadFinished) {
                                    options.onUploadFinished(self.fileDetails.fileItemID, self.destinationPath);
                                }
                            }

                            function verificationFail() {
                                options.onUploadVerificationFailed &&
                                options.onUploadVerificationFailed(self.fileDetails.fileItemID);
                            }

                        },
                        function () {
                            //todo[tymchenko]: handle errors
                        },
                        function (loaded) {
                            //update progress bar for a single file
                            var currentMs = (new Date()).getTime();
                            var elapsedSec = (currentMs - self.startTime) / 1000;
                            options.progressHandler(self.fileDetails.fileItemID, loaded, loaded / elapsedSec);
                        }
                    );

                }
            }

            function uploadMultipart(uploader, getJobTicketsFn) {
                uploader.initiateUpload(function () {

                    self.fileDetails.destinationPath = uploader.objectName;
                    self.fileDetails.uploadId = uploader.uploadId;

                    getJobTicketsFn(function (jobTickets) {
                        var completedCallFired = false;
                        var canObtainNextJob = function () {

                            if (self.state == "paused") {
                                return false;
                            }
                            if (self.stopped) {
                                log("Upload has been stopped.");
                                return false;
                            }
                            return true;
                        };

                        var onWholeUploadCompleted = function () {
                            if (completedCallFired) {
                                CommonLogger.log("Complete call has been already fired. Ignoring...");
                                return;
                            }
                            completedCallFired = true;
                            var retriesLeft = 5;
                            CommonLogger.log("Running complete call...");

                            uploader.completeUpload(validateParts, completeCallback, verificationFailFn);

                            function verificationFailFn() {
                                if (retriesLeft > 0) {
                                    --retriesLeft;
                                    setTimeout(function () {
                                        uploader.completeUpload(validateParts, completeCallback, verificationFailFn);
                                    }, 1000);
                                } else {
                                    options.onUploadVerificationFailed() &&
                                    options.onUploadVerificationFailed(self.fileItemID);
                                }
                            }

                            function validateParts(parts) {
                                return parts.length == self.totalPackages
                                    && parts
                                        .map(function (part) {
                                            return parseInt(part.size);
                                        })
                                        .reduce(function (prev, curr) {
                                            return prev + curr;
                                        }) == self.totalSize;
                            }

                            function completeCallback(contentUrl) {
                                log("Finished uploading.");
                                log("Informing server...");

                                confirmUploadWithServer(verificationSuccess, verificationFail);

                                function verificationSuccess() {
                                    //set progressbar to 100%, set the serverFileId for the download link
                                    options.progressHandler(self.fileDetails.fileItemID, self.totalSize, null);

                                    if (options.onUploadFinished) {
                                        options.onUploadFinished(self.fileDetails.fileItemID, self.destinationPath);
                                    }
                                }

                                function verificationFail() {
                                    options.onUploadVerificationFailed &&
                                    options.onUploadVerificationFailed(self.fileDetails.fileItemID);
                                }
                            }

                        };

                        var isEverythingCompleted = function () {
                            var totalProcessedByWorkers = 0;
                            for (var s = 0; s < self.chunkUploadWorkers.length; s++) {
                                totalProcessedByWorkers += self.chunkUploadWorkers[s].jobsProcessed.length;
                            }
                            var totalProcessedForFile = totalProcessedByWorkers +
                                self.packetsUploadedInPreviousSession.length;
                            log("** Total processed: " + totalProcessedForFile + " out of " + self.totalPackages);
                            return totalProcessedForFile >= self.totalPackages;
                        };

                        //run several workers simultaneously:

                        //init workers
                        for (var i = self.chunkUploadWorkers.length; i < self.totalWorkers; i++) {
                            self.chunkUploadWorkers.push(new ChunkUploadWorker(uploader,
                                i,
                                canObtainNextJob,
                                isEverythingCompleted,
                                onWholeUploadCompleted
                            ));
                        }

                        //pass the jobs
                        while (jobTickets.length > 0) {
                            for (var j = 0; j < self.chunkUploadWorkers.length; j++) {
                                var packetIndex = jobTickets.pop();
                                if (packetIndex != null) { //valuable check, since packetIndex has a valid "0" value. Nulls not allowed
                                    self.chunkUploadWorkers[j].indexes.push(packetIndex);
                                }
                            }
                        }

                        //start all of them
                        for (var k = 0; k < self.chunkUploadWorkers.length; k++) {
                            self.chunkUploadWorkers[k].start();
                        }
                    });
                });
            }

            function calculatePacketSize(packetIdx) {
                if (packetIdx < 0 && packetIdx >= self.totalPackages) {
                    return 0;
                }
                if (packetIdx < self.totalPackages - 1) {
                    return self.packetSize;
                }
                return self.totalSize - (self.totalPackages - 1) * self.packetSize;

            }

            function updateMultipartUploadProgress() {
                var currentMs = (new Date()).getTime();
                var elapsedSec = (currentMs - self.startTime) / 1000;

                var uploadedInPreviousSession = 0;

                for (var w = 0; w < self.packetsUploadedInPreviousSession.length; w++) {
                    var packet = self.packetsUploadedInPreviousSession[w];
                    uploadedInPreviousSession += calculatePacketSize(packet.partNumber - 1);
                }

                var uploadedInThisSession = 0;
                for (var j = 0; j < self.chunkUploadWorkers.length; j++) {
                    var worker = self.chunkUploadWorkers[j];
                    for (var p = 0; p < worker.jobsProcessed.length; p++) {
                        var packetIdx = worker.jobsProcessed[p];
                        uploadedInThisSession += calculatePacketSize(packetIdx);
                    }
                    uploadedInThisSession += worker.jobUploadedBytes;
                }

                options.progressHandler(
                    self.fileDetails.fileItemID,
                    uploadedInThisSession + uploadedInPreviousSession,
                    uploadedInThisSession / elapsedSec
                );

            }


            var ChunkUploadWorker = function (uploader,
                                              workerIdx,
                                              canGetNextJob,
                                              isEverythingCompletedFn,
                                              wholeUploadCompletedFn) {
                this.uploader = uploader;
                this.workerIdx = workerIdx;
                this.canGetNextJob = canGetNextJob;
                this.isEverythingCompletedFn = isEverythingCompletedFn;
                this.wholeUploadCompletedFn = wholeUploadCompletedFn;
                this.jobsProcessed = [];
                this.jobUploadedBytes = 0; //the bytes uploaded for current unfinished chunk
                this.indexes = [];
            };

            ChunkUploadWorker.prototype.start = function () {
                log(" -- Worker #" + this.workerIdx + " starting. -- ");
                var worker = this;
                var isEverythingCompleted = this.isEverythingCompletedFn;
                var canGetNextJob = this.canGetNextJob() && !isEverythingCompleted();

                worker.jobUploadedBytes = 0;

                if (canGetNextJob) {
                    var packageIdx = worker.indexes.pop();
                    if (packageIdx == null) {
                        log(" -- Worker #" + this.workerIdx + " has completed all its jobs. -- ");
                        return; // we are out of jobs
                    }
                    log("Uploading packet " + (parseInt(packageIdx) + 1) + " out of " + self.totalPackages);
                    var packet = getPacket(packageIdx);
//                    chunkHash(packet, function (hash) {  //disable hashing for now until we are able to verify the hash on S3 side.

                    //amazon is using 1-based indexes;
                    worker.uploader.uploadPart(packet, packageIdx + 1,
                        function () {
                            log("Finished uploading package " + (1 + parseInt(packageIdx)));
                            updateMultipartUploadProgress();
                            worker.jobsProcessed.push(packageIdx);
                            worker.jobUploadedBytes = 0;
                            worker.start();
                        },
                        function (loaded) {
                            worker.jobUploadedBytes = loaded;
                            updateMultipartUploadProgress();
                        }
                    );

//                    });
                } else if (isEverythingCompleted()) {
                    this.wholeUploadCompletedFn();
                }
            };

            ChunkUploadWorker.prototype.pauseUpload = function () {
                this.uploader.stopRequests();
                this.uploader.jobUploadedBytes = 0;
                updateMultipartUploadProgress();
            };

            CommonLogger.log("Initializing jsUpload...");
            init();


            /***** Helper methods *****/

            function sliceFile(startByte, endByte) {
                var packet;

                if ("mozSlice" in self.file) {
                    // mozilla
                    packet = self.file.mozSlice(startByte, endByte);
                } else if (self.file.slice) {
                    // webkit
                    packet = self.file.slice(startByte, endByte);
                } else {
                    //for safari if slice is undefined
                    packet = self.file.webkitSlice(startByte, endByte);
                }
                return packet;
            }


            /**
             * Return the proper slice (packet)
             * @param {Number} packetId
             * @returns {Blob} Returns a new Blob object containing the data in the specified range of bytes
             */
            function getPacket(packetId) {

                var startByte = packetId * self.packetSize,
                    endByte = startByte + self.packetSize;
                return sliceFile(startByte, endByte);
            }

            function getWholeFile() {
                return sliceFile(0, self.totalSize);
            }

            function stop() {
                if (!self.stopped) {
                    CommonLogger.log("Stopping");
                    self.stopped = true;
                    for (var i = 0; i < self.chunkUploadWorkers.length; i++) {
                        var worker = self.chunkUploadWorkers[i];
                        worker.pauseUpload();
                    }

                    if (self.fileDetails.uploadId) {
                        AmazonMultipartUploader.discardUnfinishedUpload(
                            self.fileDetails.uploadId,
                            self.fileDetails.destinationPath
                        );
                    }

                    if (self.smallFileUploader) {
                        self.smallFileUploader.pauseUpload();
                    }
                }
            }

            function onPauseClick() {
                log("Pause click");
                if (self.state == "uploading") {
                    self.state = "paused";
                    for (var i = 0; i < self.chunkUploadWorkers.length; i++) {
                        var worker = self.chunkUploadWorkers[i];
                        worker.pauseUpload();
                    }
                    if (self.smallFileUploader) {
                        self.smallFileUploader.pauseUpload();
                    }
                } else if (self.state == "paused") {
                    self.state = "uploading";
                    initTimings();
                    startWithDetails(self.fileDetails);
                }
            }


            function log(message) {
                options.logger(message);
            }

            function inProgress() {
                return !self.stopped;
            }

            /***** End of helper methods *****/

            return {
                stop: stop,
                pause: onPauseClick,
                inProgress: inProgress
            };
        };
    });


//CORS upload sample is inspired by http://www.ioncannon.net/programming/1539/

var AmazonSmallFileUploader = function () {
    this.signSingleFileUrl = CorsRequestSignUrls.signSingleFileUrl;
    this.currentRequest = null;
    this.progressCallback = null;
};

AmazonSmallFileUploader.prototype.uploadFile = function (objectName,
                                                         file,
                                                         successCallback,
                                                         errorCallback,
                                                         progressCallback) {
    var signRequest = {
        objectName: objectName
    };

    this.progressCallback = progressCallback;

    var uploader = this;

    $.ajax({
        type: "POST",
        url: uploader.signSingleFileUrl,
        data: JSON.stringify(signRequest),
        success: function (reply) {
            CommonLogger.log("Signed single file upload: " + JSON.stringify(reply) + ". Starting upload...");

            uploadToS3(
                file,
                decodeURIComponent(reply.signedUrl),
                reply.serverSideEncryption,
                reply.useAmazonToken,
                reply.amazonToken,
                function () {
                    CommonLogger.log("Single file upload completed");
                    if (successCallback) {
                        successCallback();
                    }
                },
                function () {
                    CommonLogger.log("Error uploading single file");
                    if (errorCallback) {
                        errorCallback();
                    }
                },
                function (uploaded) {
                    if (progressCallback) {
                        progressCallback(uploaded);
                    }
                }
            );
        },
        dataType: "json",
        contentType: "application/json"
    });

    /**
     * Use a CORS call to upload the given file to S3. Assumes the url
     * parameter has been signed and is accessable for upload.
     */
    function uploadToS3(file,
                        url,
                        serverSideEncryption,
                        useAmazonToken,
                        amazonToken,
                        callback,
                        errorCallback,
                        progressCallback) {
        CommonLogger.log("Uploading a single file to Amazon S3 via CORS to URL: " + url);
        var xhr = createCORSRequest("PUT", url);
        if (!xhr) {
            CommonLogger.log("CORS not supported");
            if (errorCallback) {
                errorCallback();
            }
        } else {
            xhr.onload = function () {
                if (xhr.status === 200) {
                    CommonLogger.log("Chunk upload completed");
                    if (callback) {
                        callback();
                    }
                } else {
                    CommonLogger.log("Upload error: " + xhr.status);
                }
            };

            xhr.onerror = function () {
                CommonLogger.log("XHR error");
                if (errorCallback) {
                    errorCallback();
                }
            };

            xhr.upload.onprogress = function (e) {
                if (progressCallback && e.lengthComputable) {
                    progressCallback(e.loaded);
                }
            };

            xhr.setRequestHeader("Content-Type", "application/octet-stream");
            xhr.setRequestHeader("x-amz-acl", "private");
            if (serverSideEncryption) {
                xhr.setRequestHeader("x-amz-server-side-encryption", "AES256");
            }
            if (useAmazonToken) {
                xhr.setRequestHeader("x-amz-security-token", amazonToken);
            }

            uploader.currentRequest = xhr;
            xhr.send(file);
        }
    }
};

AmazonSmallFileUploader.prototype.pauseUpload = function () {
    if (this.currentRequest) {
        CommonLogger.log("Pausing the small file upload");
        this.currentRequest.abort();
        if (this.progressCallback) {
            this.progressCallback(0);
        }
    } else {
        CommonLogger.log("No current small file upload going on... Skipped pausing.");
    }
};


var AmazonMultipartUploader = function (objectName, fileId, startMultipartUrl) {
    this.uploadId = null;
    this.initialSignUrl = CorsRequestSignUrls.signInitialMultipartRequestUrl;
    this.partSignUrl = CorsRequestSignUrls.signPartUploadRequestUrl;
    this.startMultipartUrl = startMultipartUrl;
    this.objectName = objectName;
    this.fileId = fileId;

    this.currentPutRequests = [];
    this.putRetryCount = {};
    this.maxRetryCount = 20;
    this.stopped = false;
};

AmazonMultipartUploader.prototype.stopRequests = function () {
    CommonLogger.log("Stopping current execution for " + this.currentPutRequests.length + " requests...");
    this.stopped = true;
    for (var r = 0; r < this.currentPutRequests.length; r++) {
        this.currentPutRequests[r].abort();
    }
};


AmazonMultipartUploader.prototype.initiateUpload = function (callback) {

    var objectName = this.objectName;
    var uploader = this;

    var signRequest = {
        objectName: objectName
    };

    //we already have uploadID; it's likely a resume upload case
    if (this.uploadId) {
        callback();
    } else {

        $.ajax({
            type: "POST",
            url: this.initialSignUrl,
            data: JSON.stringify(signRequest),
            success: function (reply) {
                CommonLogger.log("Signed initial multipart Upload: " + JSON.stringify(reply));

                $.ajax({
                    type: "POST",
                    url: reply.host,
                    beforeSend: function (request) {
                        request.setRequestHeader("Authorization", reply.authorization);
                        request.setRequestHeader("x-amz-acl", "private");
                        request.setRequestHeader("x-amz-date", reply.date);
                        if (reply.serverSideEncryption) {
                            request.setRequestHeader("x-amz-server-side-encryption", "AES256");
                        }
                        if (reply.useAmazonToken) {
                            request.setRequestHeader("x-amz-security-token", reply.amazonToken);
                        }
                    }
                }).done(function (data, textStatus, jqXHR) {
                    uploader.uploadId = $(data).find("UploadId").text();
                    CommonLogger.log("Initiated multipart upload. UploadID is: " + uploader.uploadId);

                    if (uploader.startMultipartUrl) {
                        CommonLogger.log("Informing server of multipart upload...");
                        var startRequest = {
                            fileId: uploader.fileId,
                            uploadId: uploader.uploadId,
                            destinationPath: objectName
                        };
                        $.ajax({
                            type: "POST",
                            url: uploader.startMultipartUrl,
                            data: JSON.stringify(startRequest),
                            dataType: "json",
                            contentType: "application/json"
                        });
                    } else {
                        //makes sense for attachments
                        CommonLogger.log("No start multipart URL has been submitted");
                    }

                    if (callback) {
                        callback(uploader.uploadId);
                    }
                });

            },
            dataType: "json",
            contentType: "application/json"
        });
    }
};

AmazonMultipartUploader.prototype.uploadPart = function (file, partNumber, successCallback, progressCallback) {
    if (!this.uploadId) {
        CommonLogger.log("Upload ID is absent. Cannot upload.");
        return;
    }

    if (this.stopped) {
        CommonLogger.log("Upload has been stopped. Not uploading packet for now.");
        return;
    }

    var uploader = this;
    var objectName = this.objectName;
    var uploadId = this.uploadId;

    var signRequest = {
        objectName: objectName,
        partNumber: partNumber,
        uploadId: uploadId
    };

    var retryUpload = function (number) {
        CommonLogger.log("! --------- XHR error ---");
        if (uploader.stopped) {
            CommonLogger.log("Current upload has been stopped by user. Not retrying the request for now.");
            return;
        }
        var count = uploader.putRetryCount[number];
        if (!count) {
            uploader.putRetryCount[number] = 0;
            count = 0;
        }
        if (count < uploader.maxRetryCount) {
            CommonLogger.log(" ---------------- Retrying PUT for packet # " + number);
            uploader.putRetryCount[number]++;
            setTimeout(function () {
                uploader.uploadPart(file, number, successCallback, progressCallback);
            }, 500);
        } else {
            CommonLogger.log(" ----------- FAILURE: max retry count limit has been reached for packet: " + number +
                ". Total attempts made: " + count);
        }
    };

    $.ajax({
        type: "POST",
        url: this.partSignUrl,
        data: JSON.stringify(signRequest),
        success: function (reply) {
            CommonLogger.log("Signed upload part request: " + JSON.stringify(reply));


            var xhr = createCORSRequest("PUT", reply.host);

            var scheduledTimeout = null;

            var onRequestTimeout = function () {
                CommonLogger.log(" -------- PUT request timed out for part " + partNumber + "! Retrying...");
                xhr.abort();
                retryUpload(partNumber);
            };


            xhr.onload = function () {
                if (xhr.status === 200) {
                    CommonLogger.log("Chunk upload completed");
                    if (scheduledTimeout) {
                        clearTimeout(scheduledTimeout);
                    }
                    if (successCallback) {
                        successCallback();
                    }
                } else {
                    retryUpload(partNumber);
                }
            };

            xhr.onerror = function () {
                retryUpload(partNumber);
            };

            xhr.upload.onprogress = function (e) {
                if (progressCallback && e.lengthComputable) {
                    progressCallback(e.loaded);
                }
            };

            xhr.setRequestHeader("Authorization", reply.authorization);
            xhr.setRequestHeader("x-amz-date", reply.date);
            xhr.setRequestHeader("Content-Type", "application/octet-stream");
            if (reply.useAmazonToken) {
                xhr.setRequestHeader("x-amz-security-token", reply.amazonToken);
            }


            xhr.send(file);
            scheduledTimeout = setTimeout(onRequestTimeout, MAX_PART_UPLOAD_TIME);

            uploader.currentPutRequests.push(xhr);
        },
        dataType: "json",
        contentType: "application/json"
    });
};

AmazonMultipartUploader.prototype.completeUpload = function (validateCallback, successFn, verificationFailFn) {
    if (!this.uploadId) {
        CommonLogger.log("Upload ID is absent. Cannot upload.");
        return;
    }

    var uploader = this;
    var uploadId = this.uploadId;
    var objectName = this.objectName;

    AmazonMultipartUploader.listUploadedPackets(objectName, uploadId, function (parts) {
        if (validateCallback(parts)) {
            AmazonMultipartUploader._executeCompleteUpload(objectName, uploadId, parts, successFn);
        } else {
            verificationFailFn();
        }
    });

};

//static methods

AmazonMultipartUploader._executeCompleteUpload = function (objectName, uploadId, parts, completeCallback) {


    var signRequest = {
        objectName: objectName,
        uploadId: uploadId
        // This fix breaks file upload for Firefox with version 61.0.1 (64-bit). For more details see CEL-284.
        // addCharsetToContentType: $.browser.mozilla
    };

    var body = "<CompleteMultipartUpload>";
    for (var i = 0; i < parts.length; i++) {
        var part = parts[i];
        body += "<Part><PartNumber>" + part.partNumber + "</PartNumber><ETag>" + part.etag + "</ETag></Part>";
    }
    body += "</CompleteMultipartUpload>";

    $.ajax({
        type: "POST",
        url: CorsRequestSignUrls.signCompleteMultipartRequestUrl,
        data: JSON.stringify(signRequest),
        success: function (reply) {
            CommonLogger.log("Signed complete upload request: " + JSON.stringify(reply));

            CommonLogger.log("Running complete request with body: " + body);

            $.ajax({
                type: "POST",
                url: reply.host,
                data: body,
                beforeSend: function (request) {
                    request.setRequestHeader("Authorization", reply.authorization);
                    request.setRequestHeader("x-amz-date", reply.date);
                    if (reply.useAmazonToken) {
                        request.setRequestHeader("x-amz-security-token", reply.amazonToken);
                    }
                },
                contentType: "text/xml"
            }).done(function (data, textStatus, jqXHR) {
                CommonLogger.log("Completed upload request. S3 response is " + data);
                if (completeCallback) {
                    completeCallback(signRequest.objectName);
                }
            });

        },
        dataType: "json",
        contentType: "application/json"
    });
};

AmazonMultipartUploader.discardUnfinishedUpload = function (uploadId, destinationPath) {
    var signRequest = {
        objectName: destinationPath,
        uploadId: uploadId
    };

    $.ajax({
        type: "POST",
        url: CorsRequestSignUrls.signAbortMultipartRequestUrl,
        data: JSON.stringify(signRequest),
        success: function (reply) {
            CommonLogger.log("Signed abort upload request: " + JSON.stringify(reply));

            CommonLogger.log("Running abort request...");

            $.ajax({
                type: "DELETE",
                url: reply.host,
                beforeSend: function (request) {
                    request.setRequestHeader("Authorization", reply.authorization);
                    request.setRequestHeader("x-amz-date", reply.date);
                    if (reply.useAmazonToken) {
                        request.setRequestHeader("x-amz-security-token", reply.amazonToken);
                    }
                }
            }).done(function (data, textStatus, jqXHR) {
                CommonLogger.log("Aborted upload request. S3 response is " + data);
            });

        },
        dataType: "json",
        contentType: "application/json"
    });
};

AmazonMultipartUploader.listUploadedPackets = function (objectName, uploadId, callback) {

    var signRequest = {
        objectName: objectName,
        uploadId: uploadId
    };

    $.ajax({
        type: "POST",
        url: CorsRequestSignUrls.signListPartsRequestUrl,
        data: JSON.stringify(signRequest),
        success: function (reply) {
            CommonLogger.log("Signed list uploaded chunks request: " + JSON.stringify(reply));

            $.ajax({
                type: "GET",
                url: reply.host,
                beforeSend: function (request) {
                    request.setRequestHeader("Authorization", reply.authorization);
                    request.setRequestHeader("x-amz-date", reply.date);
                    if (reply.useAmazonToken) {
                        request.setRequestHeader("x-amz-security-token", reply.amazonToken);
                    }
                }
            }).done(function (data, textStatus, jqXHR) {
                CommonLogger.log("Listing the uploaded parts from:" + data);

                var parts = [];

                $(data).find("Part").each(function (key, part) {
                    var etag = $(part).find("ETag").text();
                    var partNumber = $(part).find("PartNumber").text();
                    var partSize = $(part).find("Size").text();
                    CommonLogger.log(" *** PartNumber = " + partNumber + ", ETag: " + etag + ", Size: " + partSize);
                    parts.push({partNumber: partNumber, etag: etag, size: partSize});
                });

                if (callback) {
                    callback(parts);
                }
            });

        },
        dataType: "json",
        contentType: "application/json"
    });
};

/*** Common helper methods ***/

function createCORSRequest(method, url) {
    var xhr = new XMLHttpRequest();
    if ("withCredentials" in xhr) {
        xhr.open(method, url, true);
    } else if (typeof XDomainRequest !== "undefined") {
        xhr = new XDomainRequest();
        xhr.open(method, url);
    } else {
        xhr = null;
    }
    return xhr;
}


