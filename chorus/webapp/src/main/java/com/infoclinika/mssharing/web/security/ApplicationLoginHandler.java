package com.infoclinika.mssharing.web.security;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.infoclinika.mssharing.model.read.BillingInfoReader;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import com.infoclinika.mssharing.model.write.UserManagement;
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
 * //TODO: merge with AuthenticationSuccessHandlerImpl
 */
@Component
public class ApplicationLoginHandler implements AuthenticationSuccessHandler {

    @Inject
    private LabHeadManagement labHeadManagement;



    @Inject
    private BillingInfoReader billingInfoReader;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        final long id = ((RichUser) authentication.getPrincipal()).getId();

        if (labHeadManagement.isLabHead(id)) {
            //TODO: Disabled due to billing demo testing and redirect to dashboard if head has a debt.
            //checkHeadLabsForDebts(response, id);
            response.sendRedirect("/pages/dashboard.html");
        }  else {
            response.sendRedirect("/pages/dashboard.html");
        }
    }



    private void checkHeadLabsForDebts(HttpServletResponse response, long actor) throws IOException {

        final ImmutableSet<BillingInfoReader.LabDebtDetails> labDebtDetailses = billingInfoReader.readBillingInfo(actor);
        final Optional<BillingInfoReader.LabDebtDetails> labWithDebt = Iterables.tryFind(labDebtDetailses, new Predicate<BillingInfoReader.LabDebtDetails>() {
            @Override
            public boolean apply(BillingInfoReader.LabDebtDetails input) {
                return input.debt >= input.creditLimit;
            }
        });
        if (labWithDebt.isPresent()) {
            response.setStatus(910);
            response.sendRedirect(String.format("/pages/dashboard.html?unpaid=true&lab=%d&labName=%s&debt=%d",
                    labWithDebt.get().lab, labWithDebt.get().labName, labWithDebt.get().debt));

        } else {
            response.sendRedirect("/pages/dashboard.html");
        }

    }

}
