package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.platform.model.helper.SecurityHelperTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;

/**
 * @author Pavel Kaplin
 */
public class SpringUserProviderTest {

    @InjectMocks
    private UserDetailsService userProvider = new SpringUserProvider();

    @Mock
    private SecurityHelperTemplate securityHelper = mock(SecurityHelperTemplate.class);


    @BeforeMethod
    public void before() {
        MockitoAnnotations.initMocks(this);
        SecurityHelper.UserDetails userDetails =
            new SecurityHelper.UserDetails(17L, "Not", "Verified", "not.verified.email@teamdev.com",
                "pwd", false
            );
        when(securityHelper.getUserDetailsByEmail("vasya.pupkin@teamdev.com")).thenReturn(userDetails);

        SecurityHelper.UserDetails cyrillicUserDetails =
            new SecurityHelper.UserDetails(17L, "Вася", "Пупкин", "vasya.pupkin@teamdev.com",
                "ВасяПупкинПароль", false
            );
        when(securityHelper.getUserDetailsByEmail("not.verified.email@teamdev.com")).thenReturn(cyrillicUserDetails);
    }

    @Test
    public void testLoadUserByUsername() throws Exception {
        UserDetails details = userProvider.loadUserByUsername("not.verified.email@teamdev.com");
        assertFalse(details.isEnabled());
    }

    @Test
    public void testLoadUserByUsernameWithCyrillic() throws Exception {

        UserDetails details = userProvider.loadUserByUsername("vasya.pupkin@teamdev.com");
        assertFalse(details.isEnabled());
    }
}
