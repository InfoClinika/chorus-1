package com.infoclinika.mssharing.web.security;

import com.infoclinika.mssharing.model.read.BillingInfoReader;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Herman Zamula
 *     //TODO: merge with AuthenticationSuccessHandlerImpl
 */
@Component
public class ApplicationLoginHandler implements AuthenticationSuccessHandler {

    @Inject
    private LabHeadManagement labHeadManagement;


    @Inject
    private BillingInfoReader billingInfoReader;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        final long id = ((RichUser) authentication.getPrincipal()).getId();

        if (labHeadManagement.isLabHead(id)) {
            //TODO: Disabled due to billing demo testing and redirect to dashboard if head has a debt.
            //checkHeadLabsForDebts(response, id);
            response.sendRedirect("/pages/dashboard.html");
        } else {
            response.sendRedirect("/pages/dashboard.html");
        }
    }
}
