package com.infoclinika.mssharing.integration.test.data;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.fail;

/**
 * @author Sergii Moroz
 */
public class BlogPostData {
    private String title;
    private String body;
    private List<String> comments = new ArrayList<>();

    private BlogPostData(Builder builder) {
        this.title = builder.title;
        this.body = builder.body;
        this.comments = builder.comments;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public List<String> getComments() {
        return comments;
    }

    public static class Builder {
        private String title;
        private String body;
        private List<String> comments = new ArrayList<>();

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder comments(List<String> comments) {
            this.comments = comments;
            return this;
        }

        public BlogPostData build() {
            return new BlogPostData(this);
        }
    }
}
