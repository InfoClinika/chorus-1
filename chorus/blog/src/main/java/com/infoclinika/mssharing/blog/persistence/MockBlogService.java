package com.infoclinika.mssharing.blog.persistence;

import com.infoclinika.mssharing.platform.security.SecurityChecker;

import java.util.List;
import java.util.Set;

/**
 * @author Pavel Kaplin
 */
public class MockBlogService implements BlogService {
    @Override
    public Set<Long> getBlogPostSubscribers(long post) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteBlog(long blog) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disableBlog(long blog) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<BlogPost> getBlogPosts(long blog) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addPost(BlogPost blogPost) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public BlogPost findPostById(long id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void editPost(BlogPost blogPost) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Comment> getComments(long post) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addComment(Comment comment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<BlogPost> getMostRecentPosts(long user, SecurityChecker securityChecker) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void createOrUpdateBlog(long id, String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Blog getBlog(long blog) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<BlogPost> getMostRecentBlogs(long user, SecurityChecker securityChecker) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void subscribeToBlog(long user, long blog) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSubscribedToBlog(long user, long blog) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unsubscribeFromBlog(long user, long blog) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<Long> getBlogSubscribers(long blog) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void subscribeToPost(long user, long post) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unsubscribeFromPost(long user, long post) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSubscribedToPost(long user, long post) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deletePost(long user, long blog, long post) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
