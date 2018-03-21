package com.infoclinika.mssharing.platform.model.impl.helper.adapters;

import com.infoclinika.mssharing.platform.model.impl.helper.AmazonCorsRequestSignerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Herman Zamula
 */
@Component
public class AmazonCorsRequestSignerAdapter extends AmazonCorsRequestSignerTemplate {

    @Value("${amazon.key}")
    private String amazonKey;

    @Value("${amazon.secret}")
    private String amazonSecret;

    @Value("${amazon.bucket}")
    private String defaultBucket;

    @Override
    protected String getAmazonSecret() {
        return amazonSecret;
    }

    @Override
    protected String getBucket(long userId, String objectName) {
        return defaultBucket;
    }

    @Override
    protected String getAmazonKey() {
        return amazonKey;
    }
}
