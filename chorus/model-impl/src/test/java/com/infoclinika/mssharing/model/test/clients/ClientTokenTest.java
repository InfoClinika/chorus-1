package com.infoclinika.mssharing.model.test.clients;

import com.infoclinika.mssharing.model.helper.AbstractTest;
import com.infoclinika.mssharing.model.write.ClientTokenService;
import com.infoclinika.mssharing.model.write.ClientTokenService.ClientToken;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * @author Vladislav Kovchug
 */
public class ClientTokenTest extends AbstractTest {

    @Inject
    private ClientTokenService clientTokenService;

    @Test
    public void test_generate_token() {
        final long bob = uc.createLab3AndBob();

        final ClientToken token1 = clientTokenService.generateTokenForUser(bob);
        final ClientToken token2 = clientTokenService.generateTokenForUser(bob);
        Assert.assertNotEquals(token1.token, token2.token, "Error. New client token is the same as old token.");
    }

    @Test
    public void test_authenticate_token() {
        final long bob = uc.createLab3AndBob();

        final ClientToken token = clientTokenService.generateTokenForUser(bob);
        final Long user = clientTokenService.readUserByToken(token);
        Assert.assertNotNull(user, "Error. Can't find user by token.");
        Assert.assertEquals(user, new Long(bob), "Error. Wrong user was read by user token.");
    }

    @Test
    public void test_authenticate_fails_when_new_token_generated() {
        final long bob = uc.createLab3AndBob();

        final ClientToken token = clientTokenService.generateTokenForUser(bob);
        clientTokenService.generateTokenForUser(bob);

        final Long user = clientTokenService.readUserByToken(token);
        Assert.assertNull(user, "Error. User was found by old token.");
    }


}
