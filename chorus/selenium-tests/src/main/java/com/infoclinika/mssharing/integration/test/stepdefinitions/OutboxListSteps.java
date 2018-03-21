package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.structure.OutboxList;

/**
 * @author Alexander Orlov
 */
public class OutboxListSteps {

    OutboxList outboxList = new OutboxList();

    public boolean isItemPresentInOutbox(String user, String details){
        return outboxList.itemInOutboxList(user, details).isPresent();
    }
}
