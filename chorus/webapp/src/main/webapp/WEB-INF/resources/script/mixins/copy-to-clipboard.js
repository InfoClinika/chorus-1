(function () {

    "use strict";

    angular.module("mixins").factory("CopyToClipboard", CopyToClipboard);

    function CopyToClipboard() {

        var copyToClipboardDataCache = null;

        /**
         * Used to attach tmp listeners on copy event that will be removed after
         * datacube copy-to-clipboard action will be processed.
         */
        var html5_body =  $("body")[0];

        return {
            copyToClipboard: copyToClipboard
        };

        function copyToClipboard(stringRepresentationOfDataToCopy) {
            /** Cache data to be aware of it when copy event object will be accessible. */
            copyToClipboardDataCache = stringRepresentationOfDataToCopy;
            html5_body.addEventListener("copy", handleCopyToClipboard);
            document.execCommand("copy");
        }

        function handleCopyToClipboard(copyEvent) {
            copyEvent.clipboardData.setData("text/plain", copyToClipboardDataCache);
            copyEvent.preventDefault();
            copyToClipboardDataCache = null;
            html5_body.removeEventListener("copy", handleCopyToClipboard);
        }
    }

})();
