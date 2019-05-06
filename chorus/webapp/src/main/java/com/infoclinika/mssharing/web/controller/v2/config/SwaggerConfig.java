package com.infoclinika.mssharing.web.controller.v2.config;

import com.google.common.base.Predicates;
import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.inject.Inject;

/**
 * @author slava on 6/15/17.
 */
@EnableWebMvc
@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurerAdapter {

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api-endpoints/swagger-ui.html**")
            .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");

        registry.addResourceHandler("/api-endpoints/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/api-endpoints", "/api-endpoints/swagger-ui.html")
            .setKeepQueryParams(true);

        registry.addRedirectViewController(
            "/api-endpoints" + chorusPropertiesProvider.getSwaggerV2ApiDocUrl(),
            chorusPropertiesProvider.getSwaggerV2ApiDocUrl()
        ).setKeepQueryParams(true);

        registry.addRedirectViewController(
            "/api-endpoints/swagger-resources/configuration/ui",
            "/swagger-resources/configuration/ui"
        ).setKeepQueryParams(true);

        registry.addRedirectViewController(
            "/api-endpoints/swagger-resources/configuration/security",
            "/swagger-resources/configuration/security"
        ).setKeepQueryParams(true);

        registry.addRedirectViewController("/api-endpoints/swagger-resources", "/swagger-resources")
            .setKeepQueryParams(true);
    }

    @Bean
    public Docket publicApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("Public API")
            .enable(isSwaggerEnabled())
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
            .paths(Predicates.or(
                PathSelectors.ant("/v2/auth/cookie"),
                PathSelectors.ant("/v2/experiment/**"),
                PathSelectors.ant("/v2/experiments/**"),
                PathSelectors.ant("/export/experiment/**"),
                PathSelectors.ant("/experiment/*/processed-files/**"),
                PathSelectors.ant("/experiment/*/processing-runs/**"),
                PathSelectors.ant("/files/archive"),
                PathSelectors.ant("/files/un-archive"),
                PathSelectors.ant("/files/by-experiment/**"),
                PathSelectors.ant("/files/my/instrument/**"),
                PathSelectors.ant("/files/bylab/**"),
                PathSelectors.ant("/projects**"),
                PathSelectors.ant("/projects/copy/request"),
                PathSelectors.ant("/projects/copy/**"),
                PathSelectors.ant("/projects/details/**"),
                PathSelectors.ant("/projects/details/*/short"),
                PathSelectors.ant("/projects/paged"),
                PathSelectors.ant("/projects/paged/**"),
                PathSelectors.ant("/projects/sharing*"),
                PathSelectors.ant("/projects/allowedForWriting"),
                PathSelectors.ant("/projects/{filter}"),
                PathSelectors.ant("/projects/*/shortDetails")
                   )
            ).build();
    }

    @Bean
    public Docket internalApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("Internal API")
            .enable(isSwaggerEnabled())
            .select()
            .apis(RequestHandlerSelectors.any())
            .build();
    }

    private boolean isSwaggerEnabled() {
        return chorusPropertiesProvider.isSwaggerEnabled();
    }
}
