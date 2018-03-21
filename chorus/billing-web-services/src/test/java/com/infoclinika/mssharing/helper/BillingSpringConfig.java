package com.infoclinika.mssharing.helper;

import com.infoclinika.mssharing.services.billing.persistence.helper.NoS3StorageLogHelperImpl;
import com.infoclinika.mssharing.services.billing.persistence.helper.StorageLogHelper;
import com.infoclinika.mssharing.services.billing.rest.BillingServiceImpl;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.inject.Provider;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author andrii.loboda
 */
@Configuration
@ImportResource({"billing-persistence.cfg.xml", "billing-h2.cfg.xml"})
@ComponentScan(basePackages = {"com.infoclinika.mssharing.services.billing.persistence"})
public class BillingSpringConfig {

    @Bean
    public StorageLogHelper noS3storageLogHelper() {
        return new NoS3StorageLogHelperImpl();
    }

    @Bean
    public BillingRepositories billingRepositories() {
        return new BillingRepositories();
    }

    @Bean
    public TimeZone timeZone() {
        return TimeZone.getDefault();
    }

    @Bean
    public BillingService billingService() {
        return new BillingServiceImpl();
    }

}
