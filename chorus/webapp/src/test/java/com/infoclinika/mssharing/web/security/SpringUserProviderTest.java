package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.inject.annotation.InjectIntoByType;
import org.unitils.inject.annotation.TestedObject;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Pavel Kaplin
 */
@RunWith(UnitilsJUnit4TestClassRunner.class)
public class SpringUserProviderTest {

    @TestedObject
    private UserDetailsService userProvider = new SpringUserProvider();

    @InjectIntoByType
    private SecurityHelperTemplate securityHelper = mock(SecurityHelperTemplate.class);

    @Test
    public void testLoadUserByUsername() throws Exception {

        SecurityHelper.UserDetails userDetails = new SecurityHelper.UserDetails(17l, "Not", "Verified", "not.verified.email@teamdev.com",
                "pwd", false);
        when(securityHelper.getUserDetailsByEmail("not.verified.email@teamdev.com")).thenReturn(userDetails);

        UserDetails details = userProvider.loadUserByUsername("not.verified.email@teamdev.com");
        assertFalse(details.isEnabled());
    }

    @Test
    public void testLoadUserByUsernameWithCyrillic() throws Exception {
        SecurityHelper.UserDetails userDetails = new SecurityHelper.UserDetails(17l, "Вася", "Пупкин", "vasya.pupkin@teamdev.com",
                "ВасяПупкинПароль", false);
        when(securityHelper.getUserDetailsByEmail("vasya.pupkin@teamdev.com")).thenReturn(userDetails);

        UserDetails details = userProvider.loadUserByUsername("vasya.pupkin@teamdev.com");
        assertFalse(details.isEnabled());
    }
}
