package com.infoclinika.mssharing.model.read;


import com.google.common.collect.ImmutableSortedSet;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Transactional(readOnly = true)
public interface NewsReader {

    NewsItem readDetails(long id);

    ImmutableSortedSet<NewsItem> readList();

    ImmutableSortedSet<NewsItem> readLatest(int count);

    class NewsItem {
        public final long id;
        public final String title;
        public final String introduction;
        public final String text;
        public final String creator;
        public final Date dateCreated;
        public final Date lastUpdated;

        public NewsItem(long id, String title, String introduction, String text, String creator, Date dateCreated, Date lastUpdated) {
            this.id = id;
            this.title = title;
            this.introduction = introduction;
            this.text = text;
            this.creator = creator;
            this.dateCreated = dateCreated;
            this.lastUpdated = lastUpdated;
        }
    }
}
