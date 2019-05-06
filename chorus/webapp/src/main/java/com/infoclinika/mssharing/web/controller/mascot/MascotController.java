package com.infoclinika.mssharing.web.controller.mascot;

import com.infoclinika.mssharing.propertiesprovider.ChorusPropertiesProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import java.io.IOException;


/**
 * This class is a controller which is responsible for proxying Mascot search form
 *
 * @author Yevhen Panko
 */
@Controller
@RequestMapping("/mascot")
public class MascotController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MascotController.class);

    @Inject
    private ChorusPropertiesProvider chorusPropertiesProvider;

    @RequestMapping(value = "/proxy", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String proxy() {
        LOGGER.debug("Got request to proxy Mascot search form");

        try {
            final HttpClient client = HttpClientBuilder.create().build();
            final HttpGet checkStatusHttpGet = new HttpGet(chorusPropertiesProvider.getMascotSearchUrl());

            return EntityUtils.toString(client.execute(checkStatusHttpGet).getEntity());
        } catch (RuntimeException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Proxying Mascot search form failed.");
        }
    }
}
