function detectBrowser(){
    return detect.parse(navigator.userAgent);
}

function isOutdated() {
    var parsed = detectBrowser();
    return !parsed.browser.family || (parsed.browser.family.toLowerCase() === "chrome" || parsed.browser.family.toLowerCase() === "ie" || parsed.browser.family.toLowerCase() === "safari" ||  parsed.browser.family.toLowerCase() === "firefox");
}

function isUploadAvailable(){
    var parsed = detectBrowser();
    return parsed.browser.family && parsed.browser.family.toLowerCase() === "chrome";
}

$(document).ready(function(){
    if(isOutdated()) {
        $(".outdated").show();
        $(".unsupported").hide();
    } else {
        $(".outdated").hide();
        $(".unsupported").show();
    }

    var parsed = detectBrowser();

    $("#" + parsed.browser.family.toLowerCase()).addClass("active");

    $("#notRemind").change(function(event){
        $.cookie("unsupported-browser-not-remind", event.target.checked, { expires: 30 });
    });
    $("#btnKnow").click(function() {
        var value = $("#notRemind")[0].checked;
        if(value == false) {
            $.cookie("unsupported-browser-not-remind-in-session", $.cookie("JSESSIONID"));
        } else {
            $.cookie("unsupported-browser-not-remind", true, { expires: 30 });
        }
        location.href = "dashboard.html";
    })
});