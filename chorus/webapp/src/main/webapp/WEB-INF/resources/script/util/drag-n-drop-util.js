"use strict";

var DRAG_N_DROP_CONSTANTS = {
    SIZE_LIMIT_EXCEED: "SIZE_LIMIT_EXCEED"
};

var DragNDropHelper = function (args) {
    this.fileChooserId = args.fileChooserId;
    this.dropOverlaySelector = args.dropOverlaySelector;
    this.previewAreaSelector = args.previewAreaSelector;
    this.containerSelector = args.containerSelector;
    this.isSingleFileUpload = args.isSingleFileUpload;
    this.displayDragAndDropAreaOnlyIfAreaPresented = args.displayDragAndDropAreaOnlyIfAreaPresented;
    this.onFilesDroppedCallback = args.onFilesDroppedCallback;
    this.allowedExtensions = args.allowedExtensionsFn;
    this.onNotAllowedSizeCallback = args.onNotAllowedSizeCallback;
    this.onNotAllowedExtensionCallback = args.onNotAllowedExtensionCallback;
    this.onEmptyFolderCallback = args.onEmptyFolderCallback;
    this.getDynamicAllowedExtensions = args.getDynamicAllowedExtensionsFn;
    this.droppedFiles = [];
    this.getAllFiles = args.getAllFilesFunction;
    this.setAllFiles = args.setAllFilesFunction;
    this.wrapFile = args.wrapFileFunction;
    this.zipFunctions = args.zipFunctions ? args.zipFunctions : {};
    this.leaveDropSelector = args.leaveDropSelector;
    this.maxAttachmentSize = args.maxAttachmentSize;
    this.allowAllFileTypes = !!args.allowAllFileTypes;
    this.isInit = function () {
        return false;
    };
};

/*File API is dynamically changing - Chrome recently added filesystem attribute to fileItem entry which creates cross references. WE should delete these attributes before serializing the object*/
function deleteRestrictedFolderAttributes(fileEntry) {
    var restrictedAttributes = ["filesystem", "scope"];
    $(restrictedAttributes).each(function (i, attr) {
        delete fileEntry[attr];
    });
}

DragNDropHelper.prototype.init = function (overlayDisplayedByDefault) {
    var instance = this;

    this.isInit = function () {
        return overlayDisplayedByDefault;
    };

    function showOverlay() {
        $(instance.dropOverlaySelector).fadeIn(0);
        $(instance.previewAreaSelector).fadeOut(0);
    }

    function hideOverlay() {
        $(instance.dropOverlaySelector).fadeOut(0);
        $(instance.previewAreaSelector).fadeIn(0);
    }

    function change(event) {
        if (!event.originalEvent) {
            return;
        }
        var target = event.originalEvent.target;
        if ($(event.target)[0] == $(instance.fileChooserId)[0] && target.files.length != 0) {
            instance.getFiles(target.files, null, processFiles);
        }
    }

    function filterFiles(fileItemsList) {
        return $.grep(fileItemsList, function (item) {
            return item.kind.toLowerCase() === "file";
        });
    }

    function click(event) {
        var chooser = $(instance.fileChooserId)[0];
        if ($(event.target)[0] == chooser) {
            chooser.value = null;
        }
    }

    function dragEnter(event) {
        CommonLogger.log("Overlay - On Drag Enter fired");
        return false;
    }

    function isDragAreaNotPresented() {
        return instance.displayDragAndDropAreaOnlyIfAreaPresented &&
            !($(instance.dropOverlaySelector).length > 0 || instance.fileChooserId && $(
                instance.fileChooserId).length > 0);
    }

    function dragOver(event) {
        if (isDragAreaNotPresented()) {
            return;
        }
        CommonLogger.log("Overlay - On Drag Over fired");
        var dt = event.originalEvent.dataTransfer;
        if (!dt) {
            return;
        }
        //Verify that only files could be dropped
        //FF
        if (dt.types.contains && !dt.types.contains("Files")) {
            return;
        }
        //Chrome
        if (dt.types.indexOf && dt.types.indexOf("Files") === -1) {
            return;
        }
        //does not work in Chrome without next line
        if ($.browser.webkit) {
            dt.dropEffect = "copy";
        }
        showOverlay();
        return false;
    }

    function drop(event) {
        CommonLogger.log("On Drop fired");
        event.stopPropagation();
        event.preventDefault();
        if (isDragAreaNotPresented()) {
            return;
        }


        $(instance.dropOverlaySelector).fadeOut(0);

        var items = event.originalEvent.dataTransfer.items
            ? event.originalEvent.dataTransfer.items : event.originalEvent.dataTransfer.files;
        if (items) {
            CommonLogger.log("Files are dropped.");
            //Fix for Chrome version 32
            if (event.originalEvent.dataTransfer.items) {
                instance.getFiles(filterFiles(items), event.originalEvent.dataTransfer, processFiles);
            } else {
                instance.getFiles(items, event.originalEvent.dataTransfer, processFiles);
            }
        }
    }

    if (overlayDisplayedByDefault) {
        $(instance.containerSelector).bind("change", change);
        $(instance.containerSelector).bind("click", click);
        $(instance.leaveDropSelector).bind("mouseover", hideOverlay);
        $(instance.containerSelector).bind("dragenter", dragEnter);
        $(instance.containerSelector).bind("dragover", dragOver);
        $(instance.containerSelector).bind("drop", drop);
    } else {
        $(instance.containerSelector).unbind("change");
        $(instance.containerSelector).unbind("click");
        $(instance.leaveDropSelector).unbind("mouseover");
        $(instance.containerSelector).unbind("dragenter");
        $(instance.containerSelector).unbind("dragover");
        $(instance.containerSelector).unbind("drop");
    }

    this.getFiles = function (items, dt, completeFn) {
        var len = instance.isSingleFileUpload ? 1 : items.length;
        var unProcessedItemsLeft = len;
        var allFiles = [];
        var entry;

        function readFileTree(itemEntry, fileCallback) {

            if (itemEntry.isFile) {
                readFile(itemEntry, fileCallback);
            } else if (itemEntry.isDirectory) {
                hasFiles(itemEntry, function (entry, hasAnyFiles) {

                    itemEntry.isEmpty = !hasAnyFiles;

                    if (instance.zipFunctions.isFolderAllowedToZipFn && instance.zipFunctions.isFolderAllowedToZipFn(
                        itemEntry)) {
                        fileCallback(itemEntry);
                    } else {
                        var dirReader = itemEntry.createReader();
                        CommonLogger.log("Reading entries from: " + itemEntry.name);
                        dirReader.readEntries(function (entries) {
                            var idx = entries.length;
                            CommonLogger.log("There are " + idx + " entries inside.");
                            unProcessedItemsLeft += entries.length - 1;
                            CommonLogger.log("Increasing unprocessed items: " + unProcessedItemsLeft);
                            while (idx--) {
                                readFileTree(entries[idx], fileCallback);
                            }

                            //happens if there is an empty folder in folder structure
                            if (unProcessedItemsLeft == 0) {
                                completeFn(allFiles);
                            }

                        });
                    }

                });
            }
        }

        function decUnprocessedItems() {
            unProcessedItemsLeft--;
            CommonLogger.log("Decreasing unprocessed items. Left = " + unProcessedItemsLeft);
            if (unProcessedItemsLeft == 0) {
                completeFn(allFiles);
            }
        }

        function addFile(file) {
            deleteRestrictedFolderAttributes(file);
            allFiles.push(file);
            decUnprocessedItems();
        }


        for (var i = 0; i < len; i++) {
            entry = items[i];
            getEntryFromFile(entry, function handler(en) {
                if (en.isFile) {
                    readFile(en, addFile);
                } else if (en.isDirectory) {
                    readFileTree(en, addFile);
                } else if (en instanceof File) {
                    addFile(en);
                }
            }, decUnprocessedItems);
        }

    };

    function processFiles(files) {

        CommonLogger.log("Processing files");
        CommonLogger.log(files);

        function endsWith(str, suffix) {
            return str.toLowerCase().indexOf(suffix.toLowerCase(), str.length - suffix.length) !== -1;
        }

        var allowedExtensions = instance.allowedExtensions();
        var dynamicAllowedExtensions = instance.getDynamicAllowedExtensions ?
            instance.getDynamicAllowedExtensions() :
            [];
        instance.droppedFiles = $.map(files, function (item) {
            //wrap the data into own object to avoid explicit dependencies on File API structure in UI
            var isDirectory = item.isDirectory;

            var ext = $.grep(allowedExtensions, function (extension) {
                return endsWith(item.name, extension);
            })[0];
            if (ext == undefined) {
                ext = $.grep(dynamicAllowedExtensions, function (extension) {
                    return isDirectory && endsWith(item.name, extension);
                })[0];
            }
            if (ext == undefined) {
                ext = AttachmentsHelper.unknownExt;
            } else {
                //ext = ext.substr(1);
            }

            var wrappedFile = instance.wrapFile(item.name, item.lastModifiedDate, item.size, item.type, item, ext);
            wrappedFile.isEmpty = item.isEmpty;
            return wrappedFile;
        });

        //Filter empty folders
        var showEmptyFolderMessage = false;
        instance.droppedFiles = $.grep(instance.droppedFiles, function (item) {
            if (item.isEmpty) {
                showEmptyFolderMessage = true;
            } else {
                return true;
            }
        });

        if (showEmptyFolderMessage) {
            $(instance.onEmptyFolderCallback);
            showEmptyFolderMessage = false;
        }

        //Filter by size
        var showUnsupportedFilesSizeMessage = false;
        instance.droppedFiles = $.grep(instance.droppedFiles, function (item) {
            if (item.size > instance.maxAttachmentSize) {
                showUnsupportedFilesSizeMessage = true;
            } else {
                return true;
            }
        });

        if (showUnsupportedFilesSizeMessage) {
            $(instance.onNotAllowedSizeCallback);
            showUnsupportedFilesSizeMessage = false;
        }

        //Filter by extension
        var showUnsupportedFilesMessage = false;
        instance.droppedFiles = $.grep(instance.droppedFiles, function (item) {
            if ((item.ext == undefined || item.ext === AttachmentsHelper.unknownExt) && !instance.allowAllFileTypes) {
                showUnsupportedFilesMessage = true;
            } else {
                return true;
            }
        });

        if (showUnsupportedFilesMessage) {
            $(instance.onNotAllowedExtensionCallback);
            showUnsupportedFilesMessage = false;
        }

        var allFiles = instance.getAllFiles();
        if (!instance.isSingleFileUpload) {
            instance.setAllFiles(allFiles.concat(instance.droppedFiles));
        } else {
            instance.setAllFiles(instance.droppedFiles);
        }


        if (instance.onFilesDroppedCallback) {
            instance.onFilesDroppedCallback(instance.droppedFiles);
        }

        if (instance.getAllFiles().length > 0) {
            hideOverlay();
        } else if (overlayDisplayedByDefault) {
            showOverlay();
        } else {
            hideOverlay();
        }

    }

    return true;
};

DragNDropHelper.prototype.removeFile = function (index) {
    var allFiles = this.getAllFiles();
    allFiles.splice(index, 1);
    this.setAllFiles(allFiles);
};


var ZipHelper = function (args) {
    var kb = 1024;
    var mb = kb * 1024;
    var gb = mb * 1024;
    this.limitSize = 4 * gb;
    this.zipFunctions = args.zipFunctions;
    this.errorHandler = args.onErrorHandler;
    var that = this;

    function getErrorStatusMessage(errorObj) {
        var msg = "";

        switch (errorObj.code) {
            case DOMException.QUOTA_EXCEEDED_ERR:
                msg = "QUOTA_EXCEEDED_ERR";
                break;
            case DOMException.NOT_FOUND_ERR:
                msg = "NOT_FOUND_ERR";
                break;
            case DOMException.SECURITY_ERR:
                msg = "SECURITY_ERR";
                break;
            case DOMException.INVALID_MODIFICATION_ERR:
                msg = "INVALID_MODIFICATION_ERR";
                break;
            case DOMException.INVALID_STATE_ERR:
                msg = "INVALID_STATE_ERR";
                break;
            default:
                msg = "Unknown Error";
                break;
        }
        return msg + ", name: " + errorObj.name + ", message:" + errorObj.message;
    }

    function createTempFile(callback, entryName) {
        var tmpFilename = entryName + ".zip";
        var requestedSizeInMb = 4 * 1024;
        CommonLogger.log("Creating temp file: " + tmpFilename + ", size:" + requestedSizeInMb + " mb");
        requestFileSystem(TEMPORARY, requestedSizeInMb * 1024 * 1024, function (filesystem) {
            CommonLogger.log("Storage for temporary file " + tmpFilename + " is obtained.");

            function create() {
                CommonLogger.log("invoking getFile: " + tmpFilename);
                filesystem.root.getFile(tmpFilename, {
                    create: true
                }, function (zipFile) {
                    CommonLogger.log("Creating Zip File is successfull: " + tmpFilename);
                    callback(zipFile);
                }, function onError(errorObj) {
                    CommonLogger.log("Creating Zip file is failed: " + getErrorStatusMessage(errorObj));
                    CommonLogger.log("Error message object:" + JSON.stringify(errorObj));
                    logUsageQuota(function (message) {
                        that.errorHandler(message);
                    });
                });
            }

            function logUsageQuota(handler) {
                navigator.webkitTemporaryStorage.queryUsageAndQuota(
                    function (usedBytes, grantedBytes) {
                        var errorMessage = "Log usage quota: using " + usedBytes + " of " + grantedBytes + " bytes";
                        CommonLogger.log(errorMessage);
                        if (handler) {
                            handler(errorMessage);
                        }
                    },
                    function (e) {
                        var errorMessage = "Log usage quota failed:" + JSON.stringify(e);
                        CommonLogger.log(errorMessage);
                        if (handler) {
                            handler(errorMessage);
                        }
                    }
                );
            }

            logUsageQuota();

            filesystem.root.getFile(tmpFilename, {create: false}, function (entry) {
                CommonLogger.log("Trying to remove " + entry.name + ", full path: " + entry.fullPath);
                entry.remove(function () {
                    CommonLogger.log("Removed file: " + entry.name + ", full path: " + entry.fullPath);
                    create();
                }, function (removeError) {
                    CommonLogger.log(removeError);
                    CommonLogger.log("Remove is failed:" + entry.name + ", full path: " + entry.fullPath);
                    create();
                });
            }, function onError(errorObj) {

                CommonLogger.log("root.getFile: Error message status:" + getErrorStatusMessage(errorObj));
                CommonLogger.log("Error message object:" + JSON.stringify(errorObj));
                create();
            });
        }, function (errorObj) {

            CommonLogger.log("Failed request to obtain storage for temporary file: " + tmpFilename +
                ". Error message: " + JSON.stringify(
                    errorObj));

            CommonLogger.log("Request file system: error message status:" + getErrorStatusMessage(errorObj));
            that.errorHandler(JSON.stringify(errorObj));
        });
    }

    var obj = window;
    var requestFileSystem = obj.webkitRequestFileSystem || obj.mozRequestFileSystem || obj.requestFileSystem;

    var model = (function () {
        var zipFileEntry, zipWriter, writer;

        return {
            addFiles: function addFiles(files, oninit, onadd, onprogress, onend, entryName) {
                var addIndex = 0;

                CommonLogger.log("Adding to zip file entry with name: " + entryName);

                function nextFile() {
                    var file = files[addIndex];
                    onadd(file);
                    CommonLogger.log("-- Archiving file " + file.name + ". Parent path = " + file.parentPath);
                    if (!file.parentPath) {
                        file.parentPath = "";
                    }
                    zipWriter.add(file.parentPath + file.name, new zip.BlobReader(file), function () {
                        addIndex++;
                        if (addIndex < files.length) {
                            nextFile();
                        } else {
                            onend();
                        }
                    }, function onprogressInterceptor(current, total) {

                        onprogress(current, total, zipFileEntry);
                    });
                }

                function createZipWriter() {
                    zip.createWriter(writer, function (writer) {
                        CommonLogger.log("Created zip writer: " + zipFileEntry.name);
                        zipWriter = writer;
                        oninit();
                        nextFile();
                    }, that.errorHandler);
                }

                if (zipWriter) {
                    nextFile();
                } else {
                    createTempFile(function (fileEntry) {
                        zipFileEntry = fileEntry;
                        CommonLogger.log("Creating zip writer: " + fileEntry.name);
                        writer = new zip.FileWriter(zipFileEntry);
                        CommonLogger.log("Creating zip writer - initializing: " + fileEntry.name);
                        createZipWriter();
                    }, entryName);
                }
            },
            getZipFileEntry: function (callback) {
                zipWriter.close(function (blob) {
                    callback(zipFileEntry);
                    //onAllCompleted(zipFileEntry);
                    zipWriter = null;
                });
            },
            terminate: function (callback) {
                if (!zipWriter) {
                    return;
                }
                zipWriter.terminateWorker(function (blob) {
                    zipWriter = null;
                    if (callback) {
                        callback();
                    }
                });
            }
        };
    })();

    /** Immediately terminate archiving
     *
     * @type {Function}
     */
    this.stop = model.terminate;


    var instance = this;


    function getZipEntry(zippedFileEntry, onAllCompleted) {
        try {
            zippedFileEntry.file(function (file) {
                file.archive = true;
                onAllCompleted(file);
            })();
        } catch (ex) {
            //Uncaught TypeError: undefined is not a function
            CommonLogger.warn(ex);
        }
    }

    this.zipDir = function (entry, fileName, onAllCompleted, zipFailed) {
        zipDirectoryWithFiles(entry, fileName, [], onAllCompleted, zipFailed);
    };

    this.zipDirWithFiles = function (entry, fileName, filesArray, onComplete, zipFailed) {
        zipDirectoryWithFiles(entry, fileName, filesArray, onComplete, zipFailed);
    };

    /**Archive directory
     *
     * @param entry Directory for zipping
     * @param onAllCompleted Ziping directory complete callback. Parameter - zipped file
     */
    function zipDirectoryWithFiles(entry, archiveName, addFiles, onAllCompleted, zipFailed) {

        function zipDir(dirEntry, addZippedDirCallback) {
            CommonLogger.log("Zipping Dir: " + archiveName);


            var allFilesInZipDir = [];
            var unProcessedItemsLeftInZipDir = 1;

            function getAllFilesInZipDir(completeHandlerFn) {
                CommonLogger.log("***** getAllFilesInZipDir called. Dir entry = " + archiveName + " *****");

                function decUnprocessedItemsInZipDir(fileName) {
                    unProcessedItemsLeftInZipDir--;
                    CommonLogger.log("Decreasing zipped items left: " + unProcessedItemsLeftInZipDir + ", fileName:" +
                        fileName);
                    if (unProcessedItemsLeftInZipDir == 0) {
                        completeHandlerFn(allFilesInZipDir);
                    }
                }

                function addFileInZipDir(file) {
                    allFilesInZipDir.push(file);
                    decUnprocessedItemsInZipDir(file.name);
                }

                function readFileTreeInZipDir(itemEntry, fileCallback) {
                    if (itemEntry.isFile) {
                        readFile(itemEntry, fileCallback);
                    } else if (itemEntry.isDirectory) {
                        var dirReader = itemEntry.createReader();
                        dirReader.readEntries(function (entries) {
                            var idx = entries.length;
                            unProcessedItemsLeftInZipDir += entries.length - 1;
                            CommonLogger.log("Increasing zipped items left: " + unProcessedItemsLeftInZipDir);
                            while (idx--) {
                                var parentNamePrefix = "";
                                //set the parent name to preserve folder structure in zip. However, do not supply extra parent path for root contents
                                if (itemEntry.fullPath != dirEntry.fullPath) {
                                    parentNamePrefix = itemEntry.name + "/";
                                } else if (addFiles && addFiles.length != 0) {
                                    parentNamePrefix = itemEntry.name + "/";
                                }
//                                CommonLogger.log(" * Supplying with the parent path. Target = " + entries[idx].name + ". Parent path = " + parentNamePrefix);
                                entries[idx].parentPath =
                                    itemEntry.parentPath ? itemEntry.parentPath + parentNamePrefix : parentNamePrefix;

                                //todo[tymchenko]: changed
                                readFileTreeInZipDir(entries[idx], fileCallback);
                            }
                        });
                    }
                }

                getEntryFromFile(dirEntry, function handler(en) {
                    if (en.isFile) {
                        readFile(en, addFileInZipDir);
                    } else if (en.isDirectory) {
                        readFileTreeInZipDir(en, addFileInZipDir);
                    } else if (en instanceof File) {
                        addFileInZipDir(en);
                    }
                }, decUnprocessedItemsInZipDir);
            }


            getAllFilesInZipDir(function (files) {
                var totalSizeInDir = 0;
                var totalZipped = 0;
                var fullyZippedFilesSize = 0;
                var currentFileInProcess = null;

                var zipFiles = $.merge(files, addFiles);
                angular.forEach(zipFiles, function (file) {
                    totalSizeInDir += file.size;
                });

                if (totalSizeInDir >= instance.limitSize) {
                    var errorMessage = "Folder size exceeds " + instance.limitSize + " bytes.";
                    if (zipFailed) {
                        zipFailed({type: DRAG_N_DROP_CONSTANTS.SIZE_LIMIT_EXCEED, message: errorMessage});
                    }
                    CommonLogger.error(errorMessage);
                    return;
                }
                model.addFiles(zipFiles, function () {
                        CommonLogger.log("onInitCalled: ");
                    },
                    function (file) {
                        if (currentFileInProcess != null) {
                            fullyZippedFilesSize += currentFileInProcess.size;
                        }
                        currentFileInProcess = file;
                    }, function (current, total) {
                        totalZipped = fullyZippedFilesSize + current;
                        //TODO herman.zamula: Think about API for zipping process
                        if (instance.zipFunctions.zipProgressCallback) {
                            instance.zipFunctions.zipProgressCallback(
                                {name: archiveName},
                                totalZipped / totalSizeInDir
                            );
                        }
                    },
                    function () {
                        currentFileInProcess = null;
                        if (instance.zipFunctions.zipCompleteCallback) {
                            instance.zipFunctions.zipCompleteCallback({name: archiveName});
                        }
                        model.getZipFileEntry(function (zippedDirEntry) {
                            addZippedDirCallback(zippedDirEntry);
                        });
                    }, archiveName
                );
            });
        }

        zipDir(entry, function (zippedFileEntry) {
            getZipEntry(zippedFileEntry, onAllCompleted);
        });

    }

    /**Archive list of files
     *
     * @param archiveName Name of new archive
     * @param filesArray Array of files to zip
     * @param onComplete Complete archiving callback. Parameter - zipped file.
     */
    this.zipFiles = function (archiveName, filesArray, onComplete, zipFailed) {
        var totalSize = 0;
        var totalZipped = 0;
        var fullyZippedFilesSize = 0;
        var currentFileInProcess = null;
        angular.forEach(filesArray, function (file) {
            totalSize += file.size;
        });

        if (totalSize >= instance.limitSize) {
            var errorMessage = "Files size exceeds " + instance.limitSize + " bytes.";
            if (zipFailed) {
                zipFailed({type: DRAG_N_DROP_CONSTANTS.SIZE_LIMIT_EXCEED, message: errorMessage});
            }
            CommonLogger.error(errorMessage);
            return;
        }

        // check folder and get all file from folder
        model.addFiles(filesArray, function () {
                CommonLogger.log("onInitCalled: ");
            },
            function (file) {
                if (currentFileInProcess != null) {
                    fullyZippedFilesSize += currentFileInProcess.size;
                }
                currentFileInProcess = file;
            }, function (current, total, getArchivedFileSizeFn) {
                totalZipped = fullyZippedFilesSize + current;

                //TODO herman.zamula: Think about API for zipping process
                if (instance.zipFunctions.zipProgressCallback) {
                    instance.zipFunctions.zipProgressCallback(
                        {name: archiveName},
                        totalZipped / totalSize,
                        getArchivedFileSizeFn
                    );
                }
            },
            function () {
                currentFileInProcess = null;
                if (instance.zipFunctions.zipCompleteCallback) {
                    instance.zipFunctions.zipCompleteCallback({name: archiveName});
                }
                model.getZipFileEntry(function (zippedEntry) {
                    getZipEntry(zippedEntry, onComplete);
                });
            },
            archiveName
        );

    };
};

ZipHelper.isZippingSupport = function () {
    return !!(window.webkitRequestFileSystem || window.requestFileSystem);
};


var DroppedFile = {};

DroppedFile.hasDuplicate = function (fileName, files) {
    var filesWithSameName = $.grep(files, function (item) {
        return item.name == fileName;
    });
    return filesWithSameName.length > 0;
};

DroppedFile.getDuplicateIndex = function (fileName, files) {
    for (var i = 0; i < files.length; i++) {
        if (files[i].name == fileName) {
            return i;
        }
    }
    return -1;
};

function getEntryFromFile(entry, entryHandler, decUnprocessedItemsFn) {
    if (entry.getAsEntry) { //Standard HTML5 API
        entry = entry.getAsEntry();
        entryHandler(entry);
    } else if (entry.webkitGetAsEntry) { //WebKit implementation of HTML5 API.
        entry = entry.webkitGetAsEntry();
        entryHandler(entry);
    } else if (FileReader) { // ignoring folders for other browsers
        if (!entry.type && entry.size % 4096 == 0 && entry.size <= 102400) {
            try {
                var reader = new FileReader();

                reader.onerror = function (value) {
                    CommonLogger.log("!! Error reading file: " + JSON.stringify(value));
                    decUnprocessedItemsFn(entry.name);
                };
                reader.onload = function (event) {
                    entryHandler(event.target);
                };
                reader.readAsBinaryString(entry);

            } catch (e) {
                CommonLogger.log("!! Error reading file: " + JSON.stringify(e));
                decUnprocessedItemsFn(entry.name);
            }
        } else {
            entryHandler(entry);
        }
    }
}

function readFile(fileEntry, callback, bZippedDir) {
    //Get File object from FileEntry
    fileEntry.file(function (callback, file) {
        CommonLogger.log("Reading file: " + file.name + ". File entry parent path is: " + fileEntry.parentPath);
        file.parentPath = fileEntry.parentPath;
        if (callback) {
            file.archive = bZippedDir == true;
            callback(file);
        }
    }.bind(this, callback));
}

function hasFiles(dirEntry, callback) {
    var hasFiles = false;
    var callbackInvoked = false;
    var unprocessed = 1;

    function readFileTree(itemEntry) {

        if (itemEntry.isFile) {
            hasFiles = true;
            callbackInvoked = true;
            callback(dirEntry, true);
        } else if (itemEntry.isDirectory) {
            var dirReader = itemEntry.createReader();
            dirReader.readEntries(function (entries) {
                unprocessed += entries.length - 1;
                var idx = entries.length;
                while (idx--) {
                    if (hasFiles) {
                        break;
                    }
                    readFileTree(entries[idx], false);
                }

                if (unprocessed == 0 && !callbackInvoked) {
                    callback(dirEntry, hasFiles);
                }

            });
        }
    }

    readFileTree(dirEntry);

}

