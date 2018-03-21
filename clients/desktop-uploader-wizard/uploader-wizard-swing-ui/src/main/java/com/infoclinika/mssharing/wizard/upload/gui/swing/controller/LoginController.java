package com.infoclinika.mssharing.wizard.upload.gui.swing.controller;

import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.AuthenticateDTO;
import com.infoclinika.mssharing.upload.common.web.api.WebService;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.bean.WizardSession;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.FormLazyFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
@Component
public class LoginController {

    @Inject
    private WebService webService;

    @Inject
    private WizardSession session;

    @Inject
    private FormLazyFactory formLazyFactory;

    public void authenticate(String username, String password){
        final UserNamePassDTO credentials = new UserNamePassDTO(username, password);
        final AuthenticateDTO authenticate = webService.authenticate(credentials);

        session.setAuthenticate(authenticate);
    }

    public void authenticate(String token){
        final AuthenticateDTO authenticate = webService.authenticate(token);

        session.setAuthenticate(authenticate);
    }

    public void openMainForm(){
        formLazyFactory.getWizardMainForm().clear();
        formLazyFactory.getWizardMainForm().open();
    }
}
