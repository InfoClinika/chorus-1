package com.infoclinika.mssharing.model.write;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Transactional
public interface NewsManagement {

    void updateNews(long actor, long newsId, NewsInfo newsInfo);

    void createNews(long actor, NewsInfo newsInfo);

    void deleteNews(long actor, long newsId);

    class NewsInfo {
        public String title;
        public String creatorEmail;
        public String text;
        public String introduction;
        public Date dateCreated;

        public NewsInfo(String title, String creatorEmail, String introduction, String text) {
            this.title = title;
            this.creatorEmail = creatorEmail;
            this.text = text;
            this.introduction = introduction;
        }

        public NewsInfo(String title, String creatorEmail, String introduction, String text, Date dateCreated) {
            this.title = title;
            this.creatorEmail = creatorEmail;
            this.text = text;
            this.introduction = introduction;
            this.dateCreated = dateCreated;
        }
    }

}
