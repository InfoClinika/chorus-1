package com.infoclinika.mssharing.web.blog;

import com.infoclinika.mssharing.blog.BlogNotifier;
import com.infoclinika.mssharing.blog.persistence.*;
import com.infoclinika.mssharing.platform.security.SecurityChecker;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Pavel Kaplin
 */
@Controller
@RequestMapping("/blog")
public class BlogController {
    private static final String WRITE = "write";
    private static final String SUBSCRIBE = "subscribe";
    private static final String UNSUBSCRIBE = "unsubscribe";

    @Inject
    private BlogService blogService;

    @Inject
    @Named(value = "validator")
    private SecurityChecker securityChecker;

    @Inject
    private BlogNotifier notifier;

    @RequestMapping("/recent")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public List<BlogPost> getRecentBlogs(Principal principal) {
        long userId = RichUser.getUserId(principal);
        return blogService.getMostRecentBlogs(userId, securityChecker);
    }

    @RequestMapping("/{blog}")
    @ResponseBody
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public Blog getBlog(@PathVariable long blog) {
        return blogService.getBlog(blog);
    }

    @RequestMapping("/access")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public Map<Long, Map<String, Boolean>> accessBlogs(@RequestParam Set<Long> blogs, Principal principal) {
        long user = RichUser.getUserId(principal);
        Map<Long, Map<String, Boolean>> result = new HashMap<Long, Map<String, Boolean>>(blogs.size());
        for (Long blog : blogs) {
            if (!securityChecker.hasReadAccessOnProject(user, blog)) {
                continue;
            }
            result.put(blog, access(blog, principal));
        }
        return result;
    }

    @RequestMapping("/{blog}/access")
    @ResponseBody
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public Map<String, Boolean> access(@PathVariable long blog, Principal principal) {
        long userId = RichUser.getUserId(principal);
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        result.put(WRITE, securityChecker.hasWriteAccessOnProject(userId, blog));
        boolean subscribedToBlog = blogService.isSubscribedToBlog(userId, blog);
        result.put(SUBSCRIBE, !subscribedToBlog);
        result.put(UNSUBSCRIBE, subscribedToBlog);
        return result;
    }

    @RequestMapping(value = "/{blog}/subscribe", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public void subscribeToBlog(@PathVariable long blog, Principal principal) {
        long userId = RichUser.getUserId(principal);
        blogService.subscribeToBlog(userId, blog);
    }

    @RequestMapping(value = "/{blog}/unsubscribe", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public void unsubscribeFromBlog(@PathVariable long blog, Principal principal) {
        long userId = RichUser.getUserId(principal);
        blogService.unsubscribeFromBlog(userId, blog);
    }

    @RequestMapping("/post")
    @ResponseBody
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public List<BlogPost> getBlogPosts(@RequestParam long blog) {
        return blogService.getBlogPosts(blog);
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and hasPermission(#post.blog.id, 'project', 'write')")
    public void savePost(@RequestBody BlogPost post, Principal principal) {
        Author author = getAuthor(principal);
        if (post.getId() == null) {
            post.setAuthor(author);
            blogService.addPost(post);
            for (Long subscriber : blogService.getBlogSubscribers(post.getBlog().getId())) {
                notifier.postAdded(subscriber, post);
            }
        } else {
            blogService.editPost(post);
            for (Long subscriber : blogService.getBlogSubscribers(post.getBlog().getId())) {
                notifier.postEdited(subscriber, post);
            }
        }
    }

    private Author getAuthor(Principal principal) {
        RichUser richUser = RichUser.get(principal);
        Author author = new Author();
        author.setId(richUser.getId());
        author.setName(richUser.getFirstName() + " " + richUser.getLastName());
        return author;
    }

    @RequestMapping(value = "/post/{post}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'write')")
    public void deletePost(@PathVariable long post, @RequestParam long blog, Principal principal) {
        final Set<Long> blogSubscribers = blogService.getBlogSubscribers(blog);
        final Set<Long> blogPostSubscribers = blogService.getBlogPostSubscribers(post);
        final long userId = RichUser.getUserId(principal);
        final BlogPost blogPost = blogService.findPostById(post);
        blogService.deletePost(userId, blog, post);
        for (long subscriber : blogSubscribers) {
            notifier.postDeleted(subscriber, blogPost, true);
        }
        for (long subscriber : blogPostSubscribers) {
            notifier.postDeleted(subscriber, blogPost, false);
        }
    }

    @RequestMapping("/post/recent")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public List<BlogPost> getMostRecentPosts(Principal principal) {
        long userId = RichUser.getUserId(principal);
        return blogService.getMostRecentPosts(userId, securityChecker);
    }

    @RequestMapping("/post/{post}")
    @ResponseBody
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public BlogPost getPost(@PathVariable long post, @RequestParam long blog) {
        BlogPost postById = getBlogPostSecurely(post, blog);
        return postById;
    }

    private BlogPost getBlogPostSecurely(long post, long blog) {
        BlogPost postById = blogService.findPostById(post);
        Assert.isTrue(postById.getBlog().getId() == blog);
        return postById;
    }

    @RequestMapping("/post/{post}/access")
    @ResponseBody
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public Map<String, Boolean> postAccess(@RequestParam long blog, @PathVariable long post, Principal principal) {
        getBlogPostSecurely(post, blog);
        long user = RichUser.getUserId(principal);
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        boolean subscribed = blogService.isSubscribedToPost(user, post);
        result.put(SUBSCRIBE, !subscribed);
        result.put(UNSUBSCRIBE, subscribed);
        result.put(WRITE, securityChecker.hasWriteAccessOnProject(user, blog));
        return result;
    }

    @RequestMapping(value = "/post/{post}/subscribe", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public void subscribeToPost(@RequestParam long blog, @PathVariable long post, Principal principal) {
        getBlogPostSecurely(post, blog);
        long userId = RichUser.getUserId(principal);
        blogService.subscribeToPost(userId, post);
    }

    @RequestMapping(value = "/post/{post}/unsubscribe", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public void unsubscribeFromPost(@RequestParam long blog, @PathVariable long post, Principal principal) {
        getBlogPostSecurely(post, blog);
        long userId = RichUser.getUserId(principal);
        blogService.unsubscribeFromPost(userId, post);
    }

    @RequestMapping("/comment")
    @ResponseBody
    @PreAuthorize("isAuthenticated() and hasPermission(#blog, 'project', 'read')")
    public List<Comment> getComments(@RequestParam long post, @RequestParam long blog) {
        getBlogPostSecurely(post, blog);
        return blogService.getComments(post);
    }


    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated() and hasPermission(#comment.post.blog.id, 'project', 'read')")
    public void addComment(@RequestBody Comment comment, Principal principal) {
        comment.setAuthor(getAuthor(principal));
        blogService.addComment(comment);
        for (Long subscriber : blogService.getBlogPostSubscribers(comment.getPost().getId())) {
            notifier.commentAdded(subscriber, comment);
        }
    }
}
