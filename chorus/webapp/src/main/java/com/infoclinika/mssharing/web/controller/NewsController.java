package com.infoclinika.mssharing.web.controller;


import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.read.NewsReader;
import com.infoclinika.mssharing.model.write.NewsManagement;
import com.infoclinika.mssharing.web.controller.request.NewsRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

@Controller
@RequestMapping("/news")
public class NewsController extends ErrorHandler {

    @Inject
    private NewsManagement newsManagement;

    @Inject
    private NewsReader newsReader;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ImmutableSortedSet<NewsReader.NewsItem> getNews(@RequestParam(required = false) Long count) {
        if(count != null) {
           return newsReader.readLatest(count.intValue());
        }
        return newsReader.readList();
    }

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public NewsReader.NewsItem getDetails(@PathVariable long id) {
        return newsReader.readDetails(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void createNews(@RequestBody NewsRequest newsRequest, Principal principal) {
        newsManagement.createNews(getUserId(principal), new NewsManagement.NewsInfo(newsRequest.title, newsRequest.author, newsRequest.introduction, newsRequest.text));
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void updateNews(@RequestBody NewsRequest newsRequest, Principal principal) {
        final NewsManagement.NewsInfo info = new NewsManagement.NewsInfo(newsRequest.title, newsRequest.author, newsRequest.introduction, newsRequest.text);
        newsManagement.updateNews(getUserId(principal),newsRequest.id, info);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteNews(@PathVariable long id, Principal principal){
         newsManagement.deleteNews(getUserId(principal), id);
    }

}
