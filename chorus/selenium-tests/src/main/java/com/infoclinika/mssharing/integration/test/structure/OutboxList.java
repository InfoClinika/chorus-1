package com.infoclinika.mssharing.integration.test.structure;

import com.infoclinika.mssharing.integration.test.components.Pane;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 *         <p/>
 *         Summary: This class describes elements which are located in Outbox List on Dashboard Page. This list could be opened
 *         by clicking "Outbox" link in the Sidebar Menu (menu in the left side of the page).
 *         Note: All actions which can be performed with the elements of the Outbox List are located in the appropriate
 *         OutboxListSteps class.
 */
public class OutboxList {

    //Dynamic locators
    private static By itemInList(String user, String details) {
        return By.xpath("//div[@class='row clearfix ng-scope'][.//div[@title='" + user + "'] and .//div[contains(text(), '" + details + "')]]");
    }

    public Pane itemInOutboxList(String user, String details) {
        return new Pane(itemInList(user, details));
    }
}
