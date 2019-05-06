package com.infoclinika.mssharing.blog;

import com.infoclinika.mssharing.blog.persistence.BlogPost;
import com.infoclinika.mssharing.blog.persistence.Comment;

/**
 * @author Pavel Kaplin
 */
public interface BlogNotifier {
    void postAdded(long subscriber, BlogPost post);

    void commentAdded(long subscriber, Comment comment);

    void postEdited(long subscriber, BlogPost post);

    void postDeleted(long subscriber, BlogPost post, boolean blogSubscriber);
}
