package com.infoclinika.mssharing.integration.test.data;

/**
 * @author Alexander Orlov
 */
public class NewsData {

    private final String newsTitle;
    private final String creatorEmail;
    private final String introduction;
    private final String text;

    private NewsData(Builder builder) {
        this.newsTitle = builder.newsTitle;
        this.creatorEmail = builder.creatorEmail;
        this.introduction = builder.introduction;
        this.text = builder.text;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getText() {
        return text;
    }

    public static class Builder {
        private String newsTitle;
        private String creatorEmail;
        private String introduction;
        private String text;

        public Builder newsTitle(String newsTitle) {
            this.newsTitle = newsTitle;
            return this;
        }

        public Builder creatorEmail(String creatorEmail) {
            this.creatorEmail = creatorEmail;
            return this;
        }

        public Builder introduction(String introduction) {
            this.introduction = introduction;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public NewsData build() {
            return new NewsData(this);
        }
    }
}
