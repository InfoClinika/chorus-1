"use strict";

angular.module("formatters", ["error-catcher"])
    .filter("yesNo", function () {
        return function (booleanValue) {
            return booleanValue ? "yes" : "no";
        };
    })
    .filter("filterToString", function () {
        return function (filter) {
            return {
                "all": "All",
                "all-available": "All",
                "my": "My",
                "shared": "Shared with Me",
                "public": "Public"
            }[filter];
        };
    })
    .filter("fileSize", function () {
        // taken from http://snipplr.com/view.php?codeview&id=5949
        return function (filesize) {

            function numberFormat(number, decimals, decPoint, thousandsSep) {
                // http://kevin.vanzonneveld.net
                // +   original by: Jonas Raoni Soares Silva (http://www.jsfromhell.com)
                // +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
                // +     bugfix by: Michael White (http://crestidg.com)
                // +     bugfix by: Benjamin Lupton
                // +     bugfix by: Allan Jensen (http://www.winternet.no)
                // +    revised by: Jonas Raoni Soares Silva (http://www.jsfromhell.com)
                // *     example 1: numberFormat(1234.5678, 2, ".", "");
                // *     returns 1: 1234.57

                var n = number, c = isNaN(decimals = Math.abs(decimals)) ? 2 : decimals;
                var d = !angular.isDefined(decPoint) ? "," : decPoint;
                var t = !angular.isDefined(thousandsSep) ? "." : thousandsSep, s = n < 0 ? "-" : "";
                var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;

                return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) +
                    (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
            }

            if (filesize >= 1073741824) {
                filesize = numberFormat(filesize / 1073741824, 2, ".", "") + " GB";
            } else if (filesize >= 1048576) {
                filesize = numberFormat(filesize / 1048576, 2, ".", "") + " MB";
            } else if (filesize >= 1024) {
                filesize = numberFormat(filesize / 1024, 0) + " kB";
            } else {
                filesize = numberFormat(filesize, 0) + " bytes";
            }
            return filesize;
        };
    })
    .filter("uploadSpeed", function ($filter) {
        return function (input) {
            return $filter("fileSize")(input) + "/s";
        };
    })
    .filter("sizeInTB", function () {
        return function (input) {
            var value = input / Math.pow(1024, 4);
            return value.toFixed(2);
        };
    })
    .filter("spaceToUnderscore", function () {
        return function (input) {
            return input && input.replace(/ /g, "_") || "";
        };
    })
    .filter("chargeFormatted", function () {
        return function (input) {
            if (input > 3 || input < -3 || input === 0) {
                return "unknown";
            }
            return "(" + (input >= 0 ? "+" + input : input) + ")";
        };
    });
