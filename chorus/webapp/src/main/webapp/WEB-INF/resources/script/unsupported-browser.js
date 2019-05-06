"use strict";


function detectBrowser() {
    return detect.parse(navigator.userAgent);
}

function validateBrowser() {
    var parsed = detectBrowser();
    var outdated = true;
    var unsupported = true;
    var browserFamily = parsed.browser.family;

    if (browserFamily && browserFamily.toLowerCase() === "chrome") {
        unsupported = false;

        if (parseFloat(parsed.browser.version) >= 18) {
            outdated = false;
        }
    }

    /*if (browserFamily && browserFamily.toLowerCase() === "ie"){
        unsupported = false;

        if (parseFloat(parsed.browser.version) >= 10){
            outdated = false;
        }
    }

    if (browserFamily && browserFamily.toLowerCase() === "firefox"){
        unsupported = false;

        if (parseFloat(parsed.browser.version) >= 12){
            outdated = false;
        }
    }

    if (browserFamily && browserFamily.toLowerCase() === "safari"){
        unsupported = false;

        if (parseFloat(parsed.browser.version) >= 5.1){
            outdated = false;
        }
    }

    if (browserFamily && browserFamily.toLowerCase() === "chromium"){
        unsupported = false;

        if (parseFloat(parsed.browser.version) >= 28){
            outdated = false;
        }
    }*/

    return {
        "outdated": outdated,
        "unsupported": unsupported
    };
}

function isUnsupported() {
    return validateBrowser().unsupported;
}

function isOutdated() {
    return validateBrowser().outdated;
}

function isUploadAvailable() {
    return !validateBrowser().unsupported;
}

$(document).ready(function () {
    if (isUnsupported()) {
        $(".outdated").hide();
        $(".unsupported").show();
    } else if (isOutdated()) {
        $(".outdated").show();
        $(".unsupported").hide();
    }

    var parsed = detectBrowser();

    $("#" + parsed.browser.family.toLowerCase()).addClass("active");

    $("#notRemind").change(function (event) {
        $.cookie("unsupported-browser-not-remind", event.target.checked, {expires: 30});
    });
    $("#btnKnow").click(function () {
        var value = $("#notRemind")[0].checked;
        if (value == false) {
            $.cookie("unsupported-browser-not-remind-in-session", $.cookie("JSESSIONID"));
        } else {
            $.cookie("unsupported-browser-not-remind", true, {expires: 30});
        }
        location.href = "dashboard.html";
    });
});
