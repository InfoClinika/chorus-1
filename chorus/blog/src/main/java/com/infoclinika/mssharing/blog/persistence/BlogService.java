package com.infoclinika.mssharing.blog.persistence;

import com.infoclinika.mssharing.platform.security.SecurityChecker;

import java.util.List;
import java.util.Set;

/**
 * @author Pavel Kaplin
 */
public interface BlogService {
    List<BlogPost> getBlogPosts(long blog);

    void addPost(BlogPost blogPost);

    BlogPost findPostById(long id);

    void editPost(BlogPost blogPost);

    List<Comment> getComments(long post);

    void addComment(Comment comment);

    List<BlogPost> getMostRecentPosts(long user, SecurityChecker securityChecker);

    void createOrUpdateBlog(long id, String name);

    Blog getBlog(long blog);

    List<BlogPost> getMostRecentBlogs(long user, SecurityChecker securityChecker);

    void subscribeToBlog(long user, long blog);

    boolean isSubscribedToBlog(long user, long blog);

    void unsubscribeFromBlog(long user, long blog);

    Set<Long> getBlogSubscribers(long blog);

    void subscribeToPost(long user, long post);

    void unsubscribeFromPost(long user, long post);

    boolean isSubscribedToPost(long user, long post);

    Set<Long> getBlogPostSubscribers(long post);

    void deleteBlog(long blog);

    void disableBlog(long blog);

    void deletePost(long user, long blog, long post);
}
