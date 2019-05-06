package com.infoclinika.mssharing.model.test;

import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.helper.FailedMailsHelper;
import com.infoclinika.mssharing.model.helper.FailedMailsHelper.FailedEmailItem;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static com.infoclinika.mssharing.model.helper.Data.KATE_EMAIL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Herman Zamula
 */
public class NotificationsTest extends AbstractTest {

    @Inject
    private FailedMailsHelper failedMailsHelper;

    @Test
    public void testEmailsSentToNotifiers() {

        reset(notificator());

        initFailedEmailsNotifiers();

        uc.createKateAndLab2();

        final Set<Long> failedEmails = failedMailsHelper.handleFailedEmails(
            "test",
            "example",
            "timestamp",
            of(new FailedEmailItem(KATE_EMAIL, "some reason")),
            "some raw data"
        );

        assertThat(failedEmails.size(), is(1));

        verify(notificator(), times(2)).sendFailedEmailsNotification(anyString(), eq(of(KATE_EMAIL)), eq(failedEmails));


    }

}
