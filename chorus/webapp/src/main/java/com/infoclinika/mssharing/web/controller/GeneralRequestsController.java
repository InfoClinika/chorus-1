/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.web.controller.request.MarkAsReadNotificationRequest;
import com.infoclinika.mssharing.web.controller.response.SuccessErrorResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;
import static com.infoclinika.mssharing.platform.model.RequestsTemplate.OutboxItem;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/requests")
public class GeneralRequestsController  extends ErrorHandler {
    @Inject
    private RequestsReader requestsReader;

    @Inject
    private InstrumentManagement instrumentManagement;

    @Inject
    private RequestsTemplate requests;

    private static final Logger LOG = Logger.getLogger(GeneralRequestsController.class);

    @RequestMapping(value = "/inbox", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<InboxItem> getInbox(Principal principal) {
        return requests.getInboxItems(getUserId(principal));
    }

    @RequestMapping(value="/inbox/bulkMarkAsRead", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void markAllAsReadInInbox(Principal principal, @RequestBody MarkAsReadNotificationRequest request){
        for (String itemId : request.itemIds) {
            requests.removeInboxItem(getUserId(principal), itemId);
        }
    }

    @RequestMapping(value="/outbox/bulkMarkAsRead", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void markAllAsReadInOutbox(Principal principal, @RequestBody MarkAsReadNotificationRequest request){
        for (String itemId : request.itemIds) {
            requests.removeOutboxItem(getUserId(principal), itemId);
        }
    }

    @RequestMapping(value = "/inbox/count", method = RequestMethod.GET)
    @ResponseBody
    public RequestsReader.RequestCounter getInboxCountNumber(Principal principal) {
        return new RequestsReader.RequestCounter(requests.getInboxItems(getUserId(principal)).size());
    }

    @RequestMapping(value = "/outbox", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<OutboxItem> getOutbox(Principal principal) {
        return requests.getOutboxItems(getUserId(principal));
    }

    @RequestMapping(value = "/outbox/count", method = RequestMethod.GET)
    @ResponseBody
    public RequestsReader.RequestCounter getOutboxCountNumber(Principal principal) {
        return new RequestsReader.RequestCounter(requests.getOutboxItems(getUserId(principal)).size());
    }

    @RequestMapping(value = "/instrument/new/{instrument}", method = RequestMethod.POST)
    @ResponseBody
    public void request(Principal principal, @PathVariable("instrument") long instrument) {
        instrumentManagement.requestAccessToInstrument(getUserId(principal), instrument);
    }

    //todo [pavel.kaplin] remove
    @RequestMapping(value = "/memberships", method = RequestMethod.GET)
    @ResponseBody
    public Collection<LabRequestItem> request(Principal principal) {
        final long userId = getUserId(principal);
        return transform(requestsReader.myLabMembershipOutbox(userId), LAB_REQUEST_ITEM_FUNCTION);
    }

    @RequestMapping(value = "/inbox/{request}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeInbox(Principal principal, @PathVariable String request) {
        requests.removeInboxItem(getUserId(principal), request);
    }

    @RequestMapping(value = "/inbox/{request}", method = RequestMethod.POST, params = "action=approve")
    @ResponseBody
    public SuccessErrorResponse approve(Principal principal, @PathVariable String request) {
        LOG.info("Approve request " + request);
        try {
            requests.approve(getUserId(principal), request);
            return new SuccessErrorResponse(null, "OK");
        } catch (AccessDenied e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/inbox/{request}", method = RequestMethod.POST, params = "action=refuse")
    @ResponseBody
    public SuccessErrorResponse refuse(Principal principal, @PathVariable String request, @RequestParam(defaultValue = "") String comment) {
        LOG.info("Refuse request " + request);
        try {
            requests.refuse(getUserId(principal), request, comment);
            return new SuccessErrorResponse(null, "OK");
        } catch (Exception e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/outbox/{request}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeOutbox(Principal principal, @PathVariable String request) {
        requests.removeOutboxItem(getUserId(principal), request);
    }

    public static final Function<RequestsReader.LabMembershipRequest, LabRequestItem> LAB_REQUEST_ITEM_FUNCTION = new Function<RequestsReader.LabMembershipRequest, LabRequestItem>() {
        @Override
        public LabRequestItem apply(RequestsReader.LabMembershipRequest input) {
            return new LabRequestItem(input.labId, input.labName, input.requestId);
        }
    };

    public static class LabRequestItem {
        public final long id;
        public final String name;
        public final long requestId;

        private LabRequestItem(long id, String name, long requestId) {
            this.id = id;
            this.name = name;
            this.requestId = requestId;
        }
    }

}
