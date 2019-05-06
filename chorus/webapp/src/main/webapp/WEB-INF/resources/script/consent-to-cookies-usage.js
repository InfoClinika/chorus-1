"use strict";


function showConsentToCookiesUsageNotificationIfNeeded() {

    $(document).ready(function () {

        var COOKIES_USAGE_COOKIE_NAME = "cookies_usage_consent";

        if (!$.cookie(COOKIES_USAGE_COOKIE_NAME)) {
            var notificationElement = $(
                "<div class='consent-to-cookies-container'>" +
                "<div class='consent-to-cookies-inner-container'>" +
                "<div class='consent-to-cookies-message'>" +
                "This site uses cookies to store information on your computer.&nbsp;" +
                "They are essential to make our site work.&nbsp;" +
                "By using the site, you consent to the placement of these cookies.&nbsp;" +
                "Read our <a href='privacy-policy.html' target='_blank'>Privacy Statement</a> to learn more." +
                "</div>" +
                "<div class='consent-to-cookies-link'><a>I Agree and Dismiss</a></div>" +
                "</div></div>"
            );
            notificationElement.appendTo("body");
            $(".consent-to-cookies-link").click(function () {
                $.cookie(COOKIES_USAGE_COOKIE_NAME, "agreed and dismissed", {expires: 365}); // the cookie expires in 365 days
                notificationElement.remove();
            });
        }
    });
}

showConsentToCookiesUsageNotificationIfNeeded();
