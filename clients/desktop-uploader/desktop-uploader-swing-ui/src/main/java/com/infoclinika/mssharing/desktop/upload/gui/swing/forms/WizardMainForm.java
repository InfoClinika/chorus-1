package com.infoclinika.mssharing.desktop.upload.gui.swing.forms;

import com.infoclinika.mssharing.clients.common.web.api.exception.RestServiceException;
import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.MainController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.DisplayMessageHelper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FormUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.MatteBorder;
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
@Lazy
public class WizardMainForm extends JFrame implements Form {

    public static enum NextButtonTitle {
        NEXT(getMessage(MAIN_BUTTON_NEXT)),
        UPLOAD(getMessage(MAIN_BUTTON_UPLOAD)),
        CANCEL_UPLOAD(getMessage(MAIN_BUTTON_CANCEL)),
        START_NEW_UPLOAD(getMessage(MAIN_BUTTON_START_NEW));

        private final String value;

        NextButtonTitle(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private JPanel contentPanel;
    private JButton nextButton;
    private JButton resetButton;
    private JButton backButton;
    private JPanel stepContent;
    private JPanel buttonsPanel;

    @Inject
    private MainController mainController;

    @Inject
    private DisplayMessageHelper messageHelper;

    @PostConstruct
    private void initialize() {
        setTitle(getMessage(MAIN_TITLE));
        backButton.setText(getMessage(MAIN_BUTTON_BACK));
        resetButton.setText(getMessage(MAIN_BUTTON_RESET));

        setContentPane(contentPanel);

        setSize(800, 600);

        final MatteBorder matteBorder =
            BorderFactory.createMatteBorder(
                UiProperties.BORDER_THICKNESS,
                0,
                0,
                0,
                UiProperties.BORDER_COLOR
            );

        buttonsPanel.setBorder(matteBorder);

        setIconImages(UiProperties.APP_ICONS);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WizardMainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    WizardMainForm.this.onBack();
                } catch (RestServiceException ex) {
                    WizardMainForm.this.showServerErrorMessage();
                }
                WizardMainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WizardMainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    WizardMainForm.this.onNext();
                } catch (RestServiceException ex) {
                    WizardMainForm.this.showServerErrorMessage();
                }
                WizardMainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WizardMainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    WizardMainForm.this.onReset();
                } catch (RestServiceException ex) {
                    WizardMainForm.this.showServerErrorMessage();
                }
                WizardMainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        mainController.setWizardMainForm(this);
    }

    @Override
    public void open() {
        mainController.initialize();

        final JComponent currentStepView = mainController.getCurrentStepView();
        changeStepView(currentStepView);

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
    }

    public void setEnabledNextButton(boolean enabled) {
        nextButton.setEnabled(enabled);
    }

    public void setVisibleBackButton(boolean visible) {
        backButton.setVisible(visible);
    }

    public void setVisibleResetButton(boolean visible) {
        resetButton.setVisible(visible);
    }

    public void setNextButtonTitle(NextButtonTitle title) {
        nextButton.setText(title.toString());
    }

    public void setBackButtonTitle(String title) {
        backButton.setText(title);
    }

    public void changeStepView(JComponent component) {
        stepContent.removeAll();
        stepContent.add(component);
        stepContent.updateUI();
    }

    private void onBack() {
        if (backButton.getText().equals(getMessage(MAIN_BUTTON_PAUSE))) {
            mainController.onPause();// mainController.onPause();
        } else if (backButton.getText().equals(getMessage(MAIN_BUTTON_RESUME))) {
            mainController.onResume();
        } else {
            mainController.onBack();
        }
    }

    private void onNext() {
        if (nextButton.getText().equals(NextButtonTitle.CANCEL_UPLOAD.toString())) {
            mainController.onCancel();
        } else if (nextButton.getText().equals(NextButtonTitle.START_NEW_UPLOAD.toString())) {
            mainController.onOk();
        } else {
            mainController.onNext();
        }
    }

    private void onReset() {
        mainController.onReset();
    }

    private void showServerErrorMessage() {
        messageHelper.showMainWindowMessage(
            getMessage(APP_ERROR_SERVER_NOT_RESPONDING),
            getMessage(MODALS_ERROR_TITLE),
            JOptionPane.ERROR_MESSAGE
        );
    }

}
