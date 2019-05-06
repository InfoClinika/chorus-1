/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika,
 * Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use,
 * duplication or disclosure by the
 */
package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.read.RequestsReader;
import com.infoclinika.mssharing.model.write.InstrumentManagement;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.RequestsTemplate;
import com.infoclinika.mssharing.platform.model.read.RequestsReaderTemplate.LabMembershipRequest;
import com.infoclinika.mssharing.web.controller.request.MarkAsReadNotificationRequest;
import com.infoclinika.mssharing.web.controller.response.SuccessErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;
import static com.infoclinika.mssharing.platform.model.RequestsTemplate.InboxItem;
import static com.infoclinika.mssharing.platform.model.RequestsTemplate.OutboxItem;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Oleksii Tymchenko
 */
@Controller
@RequestMapping("/requests")
public class GeneralRequestsController extends ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRequestsController.class);
    private static final Function<LabMembershipRequest, LabRequestItem> LAB_REQUEST_ITEM_FUNCTION =
        input -> new LabRequestItem(input.labId, input.labName, input.requestId);
    @Inject
    private RequestsReader requestsReader;
    @Inject
    private InstrumentManagement instrumentManagement;
    @Inject
    private RequestsTemplate requests;

    @GetMapping("/inbox")
    @ResponseBody
    public ImmutableSortedSet<InboxItem> getInbox(Principal principal) {
        return requests.getInboxItems(getUserId(principal));
    }

    @PostMapping("/inbox/bulkMarkAsRead")
    @ResponseStatus(HttpStatus.OK)
    public void markAllAsReadInInbox(Principal principal, @RequestBody MarkAsReadNotificationRequest request) {
        for (String itemId : request.itemIds) {
            requests.removeInboxItem(getUserId(principal), itemId);
        }
    }

    @PostMapping("/outbox/bulkMarkAsRead")
    @ResponseStatus(HttpStatus.OK)
    public void markAllAsReadInOutbox(Principal principal, @RequestBody MarkAsReadNotificationRequest request) {
        for (String itemId : request.itemIds) {
            requests.removeOutboxItem(getUserId(principal), itemId);
        }
    }

    @GetMapping("/inbox/count")
    @ResponseBody
    public RequestsReader.RequestCounter getInboxCountNumber(Principal principal) {
        return new RequestsReader.RequestCounter(requests.getInboxItems(getUserId(principal)).size());
    }

    @GetMapping("/outbox")
    @ResponseBody
    public ImmutableSortedSet<OutboxItem> getOutbox(Principal principal) {
        return requests.getOutboxItems(getUserId(principal));
    }

    @GetMapping("/outbox/count")
    @ResponseBody
    public RequestsReader.RequestCounter getOutboxCountNumber(Principal principal) {
        return new RequestsReader.RequestCounter(requests.getOutboxItems(getUserId(principal)).size());
    }

    @GetMapping("/memberships")
    @ResponseBody
    public Collection<LabRequestItem> request(Principal principal) {
        final long userId = getUserId(principal);

        return transform(requestsReader.myLabMembershipOutbox(userId), LAB_REQUEST_ITEM_FUNCTION);
    }

    @DeleteMapping("/inbox/{request}")
    @ResponseStatus(HttpStatus.OK)
    public void removeInbox(Principal principal, @PathVariable String request) {
        requests.removeInboxItem(getUserId(principal), request);
    }

    @PostMapping(value = "/inbox/{request}", params = "action=approve")
    @ResponseBody
    public SuccessErrorResponse approve(Principal principal, @PathVariable String request) {
        LOGGER.info("Approve request {}", request);
        try {
            requests.approve(getUserId(principal), request);
            return new SuccessErrorResponse(null, "OK");
        } catch (AccessDenied e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
    }

    @PostMapping(value = "/inbox/{request}", params = "action=refuse")
    @ResponseBody
    public SuccessErrorResponse refuse(
        Principal principal,
        @PathVariable String request,
        @RequestParam(defaultValue = "") String comment
    ) {
        LOGGER.info("Refuse request {}", request);
        try {
            requests.refuse(getUserId(principal), request, comment);
            return new SuccessErrorResponse(null, "OK");
        } catch (Exception e) {
            return new SuccessErrorResponse(e.getMessage(), null);
        }
    }

    @DeleteMapping("/outbox/{request}")
    @ResponseStatus(HttpStatus.OK)
    public void removeOutbox(Principal principal, @PathVariable String request) {
        requests.removeOutboxItem(getUserId(principal), request);
    }

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
