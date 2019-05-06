package com.infoclinika.mssharing.model.internal.jira;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import com.infoclinika.mssharing.propertiesprovider.JiraPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Vladislav Kovchug
 */
@Service
public class JiraRestClientProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraServiceImpl.class);

    private final JiraPropertiesProvider jiraPropertiesProvider;

    @Inject
    public JiraRestClientProvider(JiraPropertiesProvider jiraPropertiesProvider) {
        this.jiraPropertiesProvider = jiraPropertiesProvider;
    }

    public JiraRestClient get() {
        final URI jiraServer;
        try {
            jiraServer = new URI(jiraPropertiesProvider.getJiraServerUrl());
        } catch (URISyntaxException e) {
            final String errorMessage = "Can't parse jira server URL";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }

        final AuthenticationHandler authenticationHandler = new BasicHttpAuthenticationHandler(
            jiraPropertiesProvider.getJiraUsername(),
            jiraPropertiesProvider.getJiraPassword()
        );

        HttpClientOptions options = new HttpClientOptions();
        options.setIgnoreCookies(true);
        DisposableHttpClient httpClient = createClient(jiraServer, authenticationHandler, options);

        return new AsynchronousJiraRestClient(jiraServer, httpClient);
    }

    private DisposableHttpClient createClient(URI serverUri,
                                              AuthenticationHandler authenticationHandler,
                                              HttpClientOptions options) {
        final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(
            getNoOpEventPublisher(),
            new JiraRestClientApplicationProperties(serverUri), getThreadLocalContextManager()
        );
        final HttpClient httpClient = defaultHttpClientFactory.create(options);
        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            public void destroy() throws Exception {
                defaultHttpClientFactory.dispose(httpClient);
            }
        };
    }

    private EventPublisher getNoOpEventPublisher() {
        return new EventPublisher() {
            @Override
            public void register(Object o) {

            }

            @Override
            public void unregister(Object o) {

            }

            @Override
            public void unregisterAll() {

            }

            @Override
            public void publish(Object o) {

            }
        };
    }

    private ThreadLocalContextManager getThreadLocalContextManager() {
        return new ThreadLocalContextManager() {
            @Override
            public Object getThreadLocalContext() {
                return null;
            }

            @Override
            public void setThreadLocalContext(Object o) {

            }

            @Override
            public void clearThreadLocalContext() {

            }
        };
    }

}
