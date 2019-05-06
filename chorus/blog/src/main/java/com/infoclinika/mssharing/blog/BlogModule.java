package com.infoclinika.mssharing.blog;

import com.infoclinika.mssharing.blog.persistence.BlogService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Pavel Kaplin
 */
@Configuration
public class BlogModule {
    protected ConfigurableApplicationContext context;

    public BlogModule(String databaseConfig) {
        context = new ClassPathXmlApplicationContext("/blog-persistence.cfg.xml", databaseConfig);
    }

    @Bean
    public BlogService getBlogService() {
        return context.getBean(BlogService.class);
    }
}
