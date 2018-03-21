package com.infoclinika.mssharing.model.internal.write;

import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.NewsItem;
import com.infoclinika.mssharing.model.internal.repository.NewsRepository;
import com.infoclinika.mssharing.model.write.NewsManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;

import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;


@Service
public class NewsManagementImpl implements NewsManagement {

    @Inject
    public NewsRepository newsRepository;
    @Inject
    public RuleValidator ruleValidator;

    @Override
    public void updateNews(long actor, long newsId, NewsInfo newsInfo) {
        if(!ruleValidator.canManageNews(actor)) throw new AccessDenied("Only admin can update news");
        final NewsItem newsItem = checkPresence(newsRepository.findOne(newsId));
        newsItem.setAuthor(newsInfo.creatorEmail);
        newsItem.setIntroduction(newsInfo.introduction);
        newsItem.setText(newsInfo.text);
        newsItem.setTitle(newsInfo.title);
        newsItem.setLastModification(new Date());

        newsRepository.save(newsItem);
    }

    @Override
    public void createNews(long actor, NewsInfo newsInfo) {
        if(!ruleValidator.canManageNews(actor)) throw new AccessDenied("Only admin can create news");

        final Date date = newsInfo.dateCreated != null ? newsInfo.dateCreated: new Date();
        NewsItem newsItem = new NewsItem(newsInfo.title, newsInfo.introduction, newsInfo.text, newsInfo.creatorEmail, date);
        newsItem.setLastModification(date);

        newsRepository.save(newsItem);
    }

    @Override
    public void deleteNews(long actor, long newsId) {
        if(!ruleValidator.canManageNews(actor)) throw new AccessDenied("Only admin can delete news");
        final Long news = checkPresence(newsId);
        newsRepository.delete(news);
    }
}
