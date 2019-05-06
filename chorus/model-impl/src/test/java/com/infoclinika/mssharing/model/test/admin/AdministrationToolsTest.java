package com.infoclinika.mssharing.model.test.admin;

import com.infoclinika.mssharing.model.AdminNotifier;
import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.write.AdministrationToolsManagement;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Herman Zamula
 */
public class AdministrationToolsTest extends AbstractTest {

    @Inject
    private AdminNotifier adminNotifier;

    @Inject
    private AdministrationToolsManagement administrationToolsManagement;

    @Test
    public void testNotificationSentToAllUsers() {
        uc.createKateAndLab2();

        administrationToolsManagement.broadcastNotification(admin(), "Some Title", "Some notification body");

        //2 admins (Mike, Mike2) and Kate
        verify(adminNotifier, times(3)).sendCommonEmail(anyLong(), eq("Some Title"), eq("Some notification body"));

    }
}
