// This is an utility script to parse google docs exported HTML for support page.
// It's designed to run in Chrome's console with Jquery included.
// For more details see: CEL-275
"use strict";

(function doParse() {
    var URL_SELECTOR = "a.c3";
    var IMAGES_FOLDER = "user-guide-images";
    var userGuidetopics = getTopics();
    console.log("TOPICS: ");
    console.log(userGuidetopics);

    var parsedSubtopics = parseSubtopics(userGuidetopics);
    parsedSubtopics.forEach(function (value) {
        console.log(JSON.stringify(value));
    });

    function getTopics() {
        var urls = $(URL_SELECTOR);
        var urlRegistry = [];
        var ids = [];

        var topics = [];

        $.each(urls, function (itemIndex, item) {
            var url = $(item);

            var id = url.attr("href");

            if (!urlRegistry[id] && id.startsWith("#")) {
                var elId = id.substring(1);
                var el = document.getElementById(elId);
                if (!el) {
                    return;
                }

                ids.push(id);
                var title = url.html();
                if (title.startsWith("Chapter ")) {
                    topics.push({
                        id: id,
                        title: title,
                        subtopics: []
                    });
                } else {
                    topics[topics.length - 1].subtopics.push({
                        id: id,
                        title: title
                    });
                }
                urlRegistry[id] = {id: id};
            }
        });


        return topics;
    }

    function parseSubtopics(topics) {
        var topicIdsSet = getElementIdsSet(topics);
        var body = $("body").get(0);
        var bodyElements = [];
        var bodyElToIdMap = [];

        $.each(body.children, function (index, be) {
            bodyElements.push(be);
            if (be.id) {
                bodyElToIdMap[be.id] = index;
            }
        });

        return topics.map(processTopic);

        // var parsedTopics = processTopic(topics[1]);
        // console.log(JSON.stringify(parsedTopics));

        function processTopic(tt) {
            var subtopics = tt.subtopics;
            var result = [];
            // parseSubtopic(subtopics[0]);
            subtopics.forEach(parseSubtopic);
            return result;

            function parseSubtopic(s) {
                var title = s.title;
                var elId = getElId(s.id);
                var el = document.getElementById(elId);

                var contentElements = getContentElements(elId);
                var contentHTML = getContentHTML(contentElements);
                contentHTML = replaceImageUrls(contentHTML, IMAGES_FOLDER);
                if (contentHTML) {
                    result.push({
                        title: title,
                        content: contentHTML
                    });
                }
            }
        }

        function getElementIdsSet(topicList) {
            var result = [];
            var ids = [];
            topicList.forEach(function (item) {
                ids.push(getItemId(item));
                ids = ids.concat(item.subtopics.map(getItemId));
            });

            ids.forEach(function (id) {
                var topicId = getElId(id);
                result[topicId] = topicId;
            });
            return result;

            function getItemId(item) {
                return item.id;
            }
        }

        function replaceImageUrls(htmlContent, imagesFolder) {
            return htmlContent.replace(/src="images\//g, "src=\"" + imagesFolder + "/");
        }

        function getContentHTML(elements) {
            var result = "";
            elements.forEach(function (el) {
                result += el.outerHTML;
            });
            return result;
        }

        function getContentElements(id) {
            var index = bodyElToIdMap[id] + 1;
            var elementsInContent = [];

            while (index < body.children.length) {
                if (topicIdsSet[bodyElements[index].id]) {
                    break;
                }

                elementsInContent.push(bodyElements[index]);
                index++;
            }

            return elementsInContent;
        }

        function getElId(id) {
            if (!id) {
                return null;
            }

            return id.substring(1);
        }
    }

})();
