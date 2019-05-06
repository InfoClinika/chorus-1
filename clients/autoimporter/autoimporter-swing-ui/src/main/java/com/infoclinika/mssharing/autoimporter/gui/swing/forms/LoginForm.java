package com.infoclinika.mssharing.autoimporter.gui.swing.forms;

import com.infoclinika.mssharing.autoimporter.gui.swing.api.Frame;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.FormUtils;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.FrameLazyFactory;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.UploadServiceAdapter;
import com.infoclinika.mssharing.autoimporter.service.util.Configuration;
import com.infoclinika.mssharing.clients.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.web.rest.RestExceptionType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.*;
import static com.infoclinika.mssharing.autoimporter.messages.MessagesSource.getMessage;

@Component
@Lazy
public class LoginForm extends JFrame implements Frame {
    private JTextField emailText;
    private JButton signInButton;
    private JPasswordField passText;
    private JPanel contentPane;
    private JLabel emailLabel;
    private JLabel passwordLabel;

    private JPanel tokenContentPane;
    private JLabel tokenLabel;
    private JTextField tokenText;
    private JButton tokenSignInButton;
    @Inject
    private UploadServiceAdapter uploadService;
    @Inject
    private FrameLazyFactory frameLazyFactory;
    @Inject
    private Configuration configuration;

    @PostConstruct
    public void init() {

        setTitle(getMessage(LOGIN_TITLE));
        emailLabel.setText(getMessage(LOGIN_LABEL_EMAIL));
        passwordLabel.setText(getMessage(LOGIN_LABEL_PASSWORD));
        signInButton.setText(getMessage(LOGIN_BUTTON_SIGN_IN));

        tokenLabel.setText(getMessage(LOGIN_LABEL_TOKEN));
        tokenSignInButton.setText(getMessage(LOGIN_BUTTON_SIGN_IN));

        setResizable(false);
        if (configuration.isClientTokenEnabled()) {
            setContentPane(tokenContentPane);
        } else {
            setContentPane(contentPane);
        }

        getRootPane().setDefaultButton(signInButton);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setIconImages(FormUtils.APP_ICONS);

        signInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emailText.getText().isEmpty() || passText.getPassword().length == 0) {
                    LoginForm.this.showErrorMessage("Email and password cannot be empty");
                    return;
                }
                LoginForm.this.onLogin();
            }
        });

        tokenSignInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tokenText.getText().isEmpty()) {
                    LoginForm.this.showErrorMessage("Email and password cannot be empty");
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

    @Override
    public void open() {
        FormUtils.setToScreenCenter(this);
        setVisible(true);
    }

    private void onLogin() {

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final String email = emailText.getText();
        final String password = new String(passText.getPassword());
        final String token = tokenText.getText();

        try {

            if (configuration.isClientTokenEnabled()) {
                uploadService.authorization(token);
            } else {
                uploadService.authorization(email, password);
            }

        } catch (RestServiceException ex) {
            if (ex.getExceptionType() == RestExceptionType.BAD_CREDENTIALS) {
                FormUtils.showError(getMessage(APP_ERROR_BAD_CREDENTIALS));
            } else {
                FormUtils.showError(getMessage(APP_ERROR_SERVER_IS_NOT_RESPONDING));
            }
            return;
        } catch (Exception ex) {
            FormUtils.printError(ex);
            return;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        close();

        frameLazyFactory.getMainForm().clear();
        frameLazyFactory.getMainForm().open();

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
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
