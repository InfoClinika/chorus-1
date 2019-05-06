package com.infoclinika.mssharing.blog.persistence;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.platform.security.SecurityChecker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Pavel Kaplin
 */
@Component
@Transactional
//todo [pavel.kaplin] extract external (create/delete blog) operations into separate interface and perform access
// check for them
//todo [pavel.kaplin] don't allow to perform any operations on disabled blogs
public class BlogServiceImpl implements BlogService {

    @PersistenceContext(unitName = "chorus-blog")
    private EntityManager em;

    @Override
    public List<BlogPost> getBlogPosts(long blog) {
        return em.createQuery("from BlogPost where blog.id = :blog and blog.enabled = true order by date desc",
            BlogPost.class
        )
            .setParameter("blog", blog)
            .getResultList();
    }

    @Override
    public void addPost(BlogPost blogPost) {
        blogPost.setDate(new Date());
        blogPost.getSubscribers().add(blogPost.getAuthor().getId());
        em.persist(blogPost);
    }

    @Override
    public BlogPost findPostById(long id) {
        return em.find(BlogPost.class, id);
    }

    @Override
    public void editPost(BlogPost blogPost) {
        blogPost.setSubscribers(em.find(BlogPost.class, blogPost.getId()).getSubscribers());
        blogPost.setLastEdited(new Date());
        em.merge(blogPost);
    }

    @Override
    public List<Comment> getComments(long post) {
        return em.createQuery("from Comment where post.id = :post order by date desc", Comment.class)
            .setParameter("post", post)
            .getResultList();
    }

    @Override
    public void addComment(Comment comment) {
        comment.setDate(new Date());
        em.persist(comment);
    }

    @Override
    public List<BlogPost> getMostRecentPosts(long user, SecurityChecker securityChecker) {
        Set<Long> projectsWithReadAccess = securityChecker.getProjectsWithReadAccess(user);
        return em.createQuery("from BlogPost where blog.id in :blogs and blog.enabled = true order by date desc",
            BlogPost.class
        )
            .setParameter("blogs", projectsWithReadAccess)
            .setMaxResults(8).getResultList();
    }

    @Override
    public void createOrUpdateBlog(long id, String name) {
        Blog blog = em.find(Blog.class, id);
        if (blog == null) {
            blog = new Blog(id, name);
            em.persist(blog);
        } else {
            blog.setName(name);
            blog.setEnabled(true);
        }
    }

    @Override
    public Blog getBlog(long blog) {
        return em.find(Blog.class, blog);
    }

    @Override
    public List<BlogPost> getMostRecentBlogs(long user, SecurityChecker securityChecker) {
        Set<Long> projectsWithReadAccess = securityChecker.getProjectsWithReadAccess(user);

        return em.createQuery(
            "from BlogPost post where post.blog.id in :blogs and blog.enabled = true and post.date = " +
                "(select max(another.date) from BlogPost another where another.blog = post.blog) order by date desc",
            BlogPost.class
        )
            .setParameter("blogs", projectsWithReadAccess)
            .getResultList();
    }

    @Override
    public void subscribeToBlog(long user, long blog) {
        em.find(Blog.class, blog).getSubscribers().add(user);
    }

    @Override
    public boolean isSubscribedToBlog(long user, long blog) {
        return em.find(Blog.class, blog).getSubscribers().contains(user);
    }

    @Override
    public void unsubscribeFromBlog(long user, long blog) {
        em.find(Blog.class, blog).getSubscribers().remove(user);
    }

    @Override
    public Set<Long> getBlogSubscribers(long blog) {
        Set<Long> subscribers = em.find(Blog.class, blog).getSubscribers();
        return new HashSet<Long>(subscribers);
    }

    @Override
    public void subscribeToPost(long user, long post) {
        em.find(BlogPost.class, post).getSubscribers().add(user);
    }

    @Override
    public void unsubscribeFromPost(long user, long post) {
        em.find(BlogPost.class, post).getSubscribers().remove(user);
    }

    @Override
    public boolean isSubscribedToPost(long user, long post) {
        return em.find(BlogPost.class, post).getSubscribers().contains(user);
    }

    @Override
    public Set<Long> getBlogPostSubscribers(long post) {
        BlogPost blogPost = em.find(BlogPost.class, post);
        Set<Long> subscribers = blogPost.getSubscribers();
        return new HashSet<Long>(subscribers);
    }

    @Override
    public void deleteBlog(long blog) {
        removeComments(blog);
        removeBlogPosts(blog);
        Optional<Blog> blogEntity = Optional.fromNullable(em.find(Blog.class, blog));
        if (blogEntity.isPresent()) {
            em.remove(blogEntity.get());
        }
    }

    private void removeComments(long blog) {
        // complex queries caused by Hibernate issue. See http://stackoverflow
        // .com/questions/7246563/hibernate-exception-on-mysql-cross-join-query
        em.createQuery("delete Comment where post in (from BlogPost where blog.id = :blog)")
            .setParameter("blog", blog).executeUpdate();
    }

    private void removeBlogPosts(long blog) {
        final List<BlogPost> blogPosts =
            em.createQuery("select bp from BlogPost bp where blog in (from Blog where id = :blog)", BlogPost.class)
                .setParameter("blog", blog).getResultList();

        // Hibernate doesn't cascade delete an element collection records, so remove subscribers directly
        for (BlogPost blogPost : blogPosts) {
            blogPost.getSubscribers().clear();
            em.remove(blogPost);
        }
    }

    @Override
    public void disableBlog(long blog) {
        em.find(Blog.class, blog).setEnabled(false);
    }

    @Override
    public void deletePost(long user, long blog, long post) {
        BlogPost blogPostEntity = em.find(BlogPost.class, post);
        // complex queries caused by Hibernate issue. See http://stackoverflow
        // .com/questions/7246563/hibernate-exception-on-mysql-cross-join-query
        em.createQuery("delete Comment where post=:blogPost")
            .setParameter("blogPost", blogPostEntity).executeUpdate();

        blogPostEntity = em.find(BlogPost.class, post);
        em.remove(blogPostEntity);
    }
}
