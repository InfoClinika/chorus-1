package com.infoclinika.mssharing.desktop.upload.gui.swing.forms;

import com.infoclinika.mssharing.clients.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.LoginController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.DisplayMessageHelper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FormUtils;
import com.infoclinika.mssharing.desktop.upload.model.ConfigurationInfo;
import com.infoclinika.mssharing.web.rest.RestExceptionType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.infoclinika.mssharing.desktop.messages.MessageKey.*;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
@Component
public class LoginForm extends JFrame implements Form {

    private JPanel contentPane;
    private JTextField emailText;
    private JButton signInButton;
    private JPasswordField passText;
    private JLabel labelEmail;
    private JLabel labelPassword;
    private JTextField tokenText;
    private JLabel labelToken;
    private JPanel tokenContentPane;
    private JButton tokenSignInButton;

    @Inject
    private LoginController controller;

    @Inject
    private ConfigurationInfo configurationInfo;

    @Inject
    private DisplayMessageHelper messageHelper;

    @PostConstruct
    private void initialize() {

        setTitle(getMessage(LOGIN_TITLE));
        labelEmail.setText(getMessage(LOGIN_LABEL_EMAIL));
        labelPassword.setText(getMessage(LOGIN_LABEL_PASSWORD));
        signInButton.setText(getMessage(LOGIN_BUTTON_SIGN_IN));

        labelToken.setText(getMessage(LOGIN_LABEL_TOKEN));
        tokenSignInButton.setText(getMessage(LOGIN_BUTTON_SIGN_IN));

        setResizable(false);
        if (configurationInfo.isClientTokenEnabled()) {
            setContentPane(tokenContentPane);
        } else {
            setContentPane(contentPane);
        }

        setIconImages(UiProperties.APP_ICONS);

        getRootPane().setDefaultButton(signInButton);

        tokenSignInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tokenText.getText().isEmpty()) {
                    LoginForm.this.showErrorMessage(getMessage(APP_ERROR_EMPTY_TOKEN));
                    return;
                }
                LoginForm.this.onLogin();
            }
        });

        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emailText.getText().isEmpty() || passText.getPassword().length == 0) {
                    LoginForm.this.showErrorMessage(getMessage(APP_ERROR_EMPTY_CREDENTIALS));
                    return;
                }
                LoginForm.this.onLogin();
            }
        });

        signInButton.requestFocus();
        tokenSignInButton.requestFocus();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
    }

    private void onLogin() {
        final String email = emailText.getText();
        final String password = new String(passText.getPassword());
        final String token = tokenText.getText();

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            if (configurationInfo.isClientTokenEnabled()) {
                controller.authenticate(token);
            } else {
                controller.authenticate(email, password);
            }

        } catch (RestServiceException ex) {
            if (ex.getExceptionType() == RestExceptionType.BAD_CREDENTIALS) {
                showErrorMessage(getMessage(APP_ERROR_BAD_CREDENTIALS));
            } else {
                showErrorMessage(getMessage(APP_ERROR_SERVER_NOT_RESPONDING));
            }
            return;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        clear();
        close();

        controller.openMainForm();

    }

    @Override
    public void open() {
        FormUtils.setToScreenCenter(this);
        setVisible(true);
    }

    @Override
    public void close() {
        setVisible(false);
        dispose();
    }

    @Override
    public void clear() {
        emailText.setText("");
        passText.setText("");
    }

    private void showErrorMessage(String message) {
        messageHelper.showLoginWindowMessage(
            message,
            getMessage(MODALS_ERROR_TITLE),
            JOptionPane.ERROR_MESSAGE
        );
    }

}
