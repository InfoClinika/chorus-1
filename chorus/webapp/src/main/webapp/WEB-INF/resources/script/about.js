"use strict";

(function () {

    $(document).ready(function () {
        $(".about-page .more a").click(function () {
            $(this).toggleClass("opened");
            $(this).parent().parent().find(".hide").slideToggle(500);
            return false;
        });
        $(".about-page .about-box .more a").click(function () {
            $(this).find("span").text($(this).text() == "More" ? "Less" : "More");
        });
        $(".about-page .twocolumns .more a").click(function () {
            $(this)
                .find("span")
                .text($(this).text() == "Know more details" ? "Know less details" : "Know more details");
        });
    });
})();

