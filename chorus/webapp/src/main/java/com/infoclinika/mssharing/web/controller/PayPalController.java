package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.Subscriptions;
import com.infoclinika.mssharing.services.billing.rest.api.BillingService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Pavel Kaplin
 */
@Controller
@RequestMapping("/paypal")
public class PayPalController {

    private static final Logger LOG = Logger.getLogger(PayPalController.class);
    private static final String URI = "https://www.paypal.com/cgi-bin/webscr";

    @Resource(name = "billingRestService")
    private BillingService billingService;

    @Inject
    private Subscriptions subscriptions;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping("/ipn")
    public void onIpnMessage(HttpServletRequest request) throws IOException {

        // see http://javaskeleton.blogspot.com/2010/07/paypal-instant-payment-notification-ipn.html
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("cmd", "_notify-validate")); //You need to add this parameter to tell PayPal to verify
        Map<String, String> paramsMap = extractAllParams(request, params);

        HttpClient client = new DefaultHttpClient();
        HttpPost confirmationRequest = new HttpPost(URI);
        confirmationRequest.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse confirmationResponse = client.execute(confirmationRequest);

        HttpEntity entity = confirmationResponse.getEntity();
        String confirmationResponseText = EntityUtils.toString(entity);
        LOG.debug("Received confirmation response from PayPal: " + confirmationResponseText);

        if ("VERIFIED".equalsIgnoreCase(confirmationResponseText)) {
            if(paramsMap.get("item_name").equals("Top Up")) {
                billingService.depositStoreCredit(new BillingService.DepositStoreCreditRequest(paramsMap));
            }
            // see https://developer.paypal.com/webapps/developer/docs/classic/ipn/integration-guide/IPNandPDTVariables/#id08CTB0S055Z
        } else {
            LOG.warn("Unknown response from PayPal: " + confirmationResponseText);
        }
    }


    private Map<String, String> extractAllParams(HttpServletRequest request, List<NameValuePair> params) {
        Map<String, String> paramsMap = new HashMap<>();
        for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
            String name = e.nextElement();
            String value = request.getParameter(name);
            params.add(new BasicNameValuePair(name, value));
            paramsMap.put(name, value);
        }
        return paramsMap;
    }

    @RequestMapping
    @ResponseBody
    public Subscriptions.Subscription getStatus(Principal principal) {
        return subscriptions.get(getUserId(principal));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/pending")
    @ResponseBody
    // todo [pavel.kaplin] use paypal api and check subscription status right here, without waiting for IPN
    public Subscriptions.Subscription pending(Principal principal) {
        long userId = getUserId(principal);
        Subscriptions.Subscription subscription = subscriptions.get(userId);
        if (subscription.status == Subscriptions.Subscription.Status.SUBSCRIBED) {
            LOG.warn("Will not change subscribed status to pending for " + userId);
            return subscription;
        }
        subscriptions.update(userId, Subscriptions.Subscription.Status.PENDING);
        return subscriptions.get(userId);
    }

}
