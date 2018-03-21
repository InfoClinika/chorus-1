package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.internal.repository.NewsRepository;
import com.infoclinika.mssharing.model.read.NewsReader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Comparator;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;


@Service
public class NewsReaderImpl implements NewsReader {

    public static final Function<com.infoclinika.mssharing.model.internal.entity.NewsItem, NewsItem> NEWS_TRANSFORMER = new Function<com.infoclinika.mssharing.model.internal.entity.NewsItem, NewsItem>() {

        @Override
        public NewsItem apply(com.infoclinika.mssharing.model.internal.entity.NewsItem input) {
            return new NewsItem(input.getId(), input.getTitle(), input.getIntroduction(), input.getText(), input.getAuthor(), input.getCreationDate(), input.getLastModification());
        }
    };
    public static final Comparator<NewsItem> NEWS_BY_DATE = new Comparator<NewsItem>() {
        @Override
        public int compare(NewsItem o1, NewsItem o2) {
            if (o1.dateCreated.equals(o2.dateCreated)) {
                return o1.hashCode() - o2.hashCode();
            }
            return o1.dateCreated.compareTo(o2.dateCreated);
        }
    };
    @Inject
    private NewsRepository newsRepository;

    @Override
    public NewsItem readDetails(long id) {
        final com.infoclinika.mssharing.model.internal.entity.NewsItem newsItem = checkPresence(newsRepository.findOne(id), "News with id " + id + " not found");
        return NEWS_TRANSFORMER.apply(newsItem);
    }

    @Override
    public ImmutableSortedSet<NewsItem> readList() {
        return from(newsRepository.findAll())
                .transform(NEWS_TRANSFORMER)
                .toSortedSet(NEWS_BY_DATE);

    }

    @Override
    public ImmutableSortedSet<NewsItem> readLatest(int count) {
        return from(newsRepository.findAll(new Sort(new Sort.Order(Sort.Direction.DESC, "creationDate"))))
                .transform(NEWS_TRANSFORMER).limit(count)
                .toSortedSet(NEWS_BY_DATE);
    }
}
