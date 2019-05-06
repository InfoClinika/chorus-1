"use strict";

var CommonLogging = function (tags) {

    var that = this;
    this.tags = tags ? tags : [];

    CommonLogging.prototype.setTags = function (tags) {
        that.tags = tags ? tags : [];
    };

    CommonLogging.prototype.log = function (message) {
        console.log.apply(console, arguments);
    };

    CommonLogging.prototype.info = function (message) {
        console.info.apply(console, arguments);
    };

    CommonLogging.prototype.warn = function (message) {
        console.warn.apply(console, arguments);
    };

    CommonLogging.prototype.error = function (message) {
        console.error.apply(console, arguments);
    };

    CommonLogging.prototype.exception = function (ex) {
        console.error.apply(console, arguments);
    };

    function getMessage() {

        var message = "";

        for (var i = 0; i < arguments.length; ++i) {

            message += JSON.stringify(arguments[i]);

            if (i + 1 < arguments.length) {
                message += " ";
            }
        }

        return message;
    }

    function getTags() {
        return that.tags ? that.tags : [];
    }

};

var CommonLogger = new CommonLogging();
