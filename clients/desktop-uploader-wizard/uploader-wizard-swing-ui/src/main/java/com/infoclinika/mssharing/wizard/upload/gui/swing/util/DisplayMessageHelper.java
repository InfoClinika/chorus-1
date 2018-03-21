package com.infoclinika.mssharing.wizard.upload.gui.swing.util;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.*;

/**
 * @author timofey.kasyanov
 *         date:   06.02.14
 */
@Component
public class DisplayMessageHelper {

    @Inject
    private FormLazyFactory formLazyFactory;

    public void showMainWindowMessage(String message, String title, int jOptionPaneMessageCode) {
        JOptionPane.showMessageDialog(
                formLazyFactory.getWizardMainForm(),
                message,
                title,
                jOptionPaneMessageCode
        );
    }

    public void showLoginWindowMessage(String message, String title, int jOptionPaneMessageCode) {
        JOptionPane.showMessageDialog(
                formLazyFactory.getLoginForm(),
                message,
                title,
                jOptionPaneMessageCode
        );
    }

    public boolean showConfirmationDialog(String message, String title) {
        final int result = JOptionPane.showConfirmDialog(
                formLazyFactory.getWizardMainForm(),
                message,
                title,
                JOptionPane.YES_NO_OPTION
        );

        return result == JOptionPane.YES_OPTION;
    }
}
