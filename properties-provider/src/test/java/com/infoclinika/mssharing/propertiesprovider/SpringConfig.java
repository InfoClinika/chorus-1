package com.infoclinika.mssharing.propertiesprovider;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author : Yevhen Panko
 */
@Configuration
@PropertySource({
    "file:../configs/test-application.properties"
})
@ComponentScan("com.infoclinika.mssharing.propertiesprovider")
public class SpringConfig {

}
