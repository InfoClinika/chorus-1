package com.infoclinika.mssharing.web.helper;

import com.infoclinika.auth.ChorusAuthenticationService;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.web.rest.UploaderRestServiceImpl;
import com.infoclinika.mssharing.web.rest.auth.ChorusAuthenticationServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import static org.mockito.Mockito.mock;

/**
 * @author Pavel Kaplin
 */
//@ContextConfiguration(locations = "classpath:testApplicationContext.cfg.xml")
@Configuration
@ImportResource({"testApplicationContext.cfg.xml", "h2.cfg.xml"})
@ComponentScan(basePackages = {"com.infoclinika.mssharing.model", "com.infoclinika.mssharing.web"})
public class SpringSupportTest {

    private static final Logger LOG = Logger.getLogger(SpringSupportTest.class);

    @Bean
    public ChorusAuthenticationService chorusAuthenticationService(){
        ChorusAuthenticationService chorusAuthenticationService = new ChorusAuthenticationServiceImpl();
        return chorusAuthenticationService;
    }

    @Bean
    public UploaderRestServiceImpl uploaderRestService(){
        return new UploaderRestServiceImpl();
    }

}
