package com.infoclinika.mssharing.wizard.upload.gui.swing.controller;

import com.infoclinika.mssharing.dto.NotSupportVendor;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.infoclinika.mssharing.wizard.upload.gui.swing.controller.steps.*;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.WizardMainForm;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.bean.WizardSession;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.DisplayMessageHelper;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.WizardUploaderHelper;
import com.infoclinika.mssharing.wizard.upload.service.impl.list.EditFileItemList;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.*;
import java.util.Formatter;

import static com.infoclinika.mssharing.wizard.messages.MessageKey.*;
import static com.infoclinika.mssharing.wizard.messages.MessagesSource.getMessage;
import static com.infoclinika.mssharing.wizard.upload.gui.swing.forms.WizardMainForm.NextButtonTitle.*;

/**
 * @author timofey.kasyanov
 *         date:   28.01.14
 */
@Component
public class MainController {
    private static final String STEP_NUMBER_IS_OUT_OF_RANGE_OF_STEPS_NUMBER = "Step number is out of range of steps. Number";
    private static final String COLON_SIGN = ": ";
    private WizardMainForm wizardMainForm;
    private int currentStepCode = 1;
    private JComponent currentStepView;
    private boolean needUpdateEditModel = false;
    private boolean uploadFinished = false;

    @Inject
    private WizardUploaderHelper helper;

    @Inject
    private WizardSession wizardSession;

    @Inject
    private FirstStepController firstStepController;

    @Inject
    private SecondStepController secondStepController;

    @Inject
    private ThirdStepController thirdStepController;

    @Inject
    private FourthStepController fourthStepController;

    @Inject
    private DisplayMessageHelper messageHelper;

    public void setUploadFinished(boolean uploadFinished) {
        this.uploadFinished = uploadFinished;
    }

    public void setWizardMainForm(WizardMainForm wizardMainForm) {
        this.wizardMainForm = wizardMainForm;
    }

    public boolean isNeedUpdateEditModel() {
        return needUpdateEditModel;
    }

    public void setNeedUpdateEditModel(boolean needUpdateEditModel) {
        this.needUpdateEditModel = needUpdateEditModel;
    }

    public void initialize() {
        currentStepCode = 1;
        firstStepController.initialize();
        currentStepView = firstStepController.getView();
    }

    public JComponent getCurrentStepView() {
        return currentStepView;
    }

    public void stepOneUpdateButtons() {
        wizardMainForm.setVisibleResetButton(false);
        wizardMainForm.setVisibleBackButton(false);
        wizardMainForm.setNextButtonTitle(NEXT);

        final boolean specieSelected = wizardSession.getWizardContext().getSpecie() != null;

        final InstrumentDTO defaultInstrument = firstStepController.getDefaultInstrument().getInstrument();
        final InstrumentDTO selectedInstrument = wizardSession.getWizardContext().getInstrument();

        final boolean instrumentSelected =
                selectedInstrument != null && !selectedInstrument.getName().equals(defaultInstrument.getName());

        final boolean stepOneValid = specieSelected && instrumentSelected;

        wizardMainForm.setEnabledNextButton(stepOneValid);
    }

    public void stepTwoUpdateButtons() {
        wizardMainForm.setVisibleResetButton(true);
        wizardMainForm.setVisibleBackButton(true);
        wizardMainForm.setNextButtonTitle(NEXT);
        wizardMainForm.setEnabledNextButton(true);
    }

    public void stepThreeUpdateButtons() {
        wizardMainForm.setVisibleResetButton(true);
        wizardMainForm.setVisibleBackButton(true);
        wizardMainForm.setNextButtonTitle(UPLOAD);

        final EditFileItemList list = wizardSession.getWizardContext().getEditFileItemsList();

        wizardMainForm.setEnabledNextButton(list.size() > 0);
    }

    public void stepFourUpdateButtons() {
        wizardMainForm.setVisibleResetButton(false);
        wizardMainForm.setVisibleBackButton(false);
        wizardMainForm.setEnabledNextButton(true);
        wizardMainForm.setNextButtonTitle(CANCEL_UPLOAD);
    }

    public void onNext() {
        StepController controller;
        switch (currentStepCode) {
            case 1:
                controller = secondStepController;
                break;
            case 2:
                controller = thirdStepController;
                break;
            case 3:
                controller = fourthStepController;
                break;
            default:
                throw new RuntimeException(STEP_NUMBER_IS_OUT_OF_RANGE_OF_STEPS_NUMBER + COLON_SIGN + currentStepCode);
        }

        try {
            controller.activate();

            ++currentStepCode;

            changeStepView(controller);
        } catch (NotSupportVendor e) {
            final Formatter formatter = new Formatter(new StringBuilder());
            messageHelper.showMainWindowMessage(
                    formatter.format(getMessage(MODALS_NO_VENDOR_TEXT), e.getVendorName()).toString(),
                    getMessage(MODALS_ERROR_TITLE),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void onBack() {
        StepController controller;
        switch (currentStepCode) {
            case 2:
                controller = firstStepController;
                break;
            case 3:
                controller = secondStepController;
                break;
            default:
                throw new RuntimeException(STEP_NUMBER_IS_OUT_OF_RANGE_OF_STEPS_NUMBER + COLON_SIGN + currentStepCode);
        }

        controller.activate();

        --currentStepCode;

        changeStepView(controller);
    }

    public void onCancel() {
        final boolean confirmed = messageHelper.showConfirmationDialog(
                getMessage(MODALS_CANCEL_UPLOAD_TEXT),
                getMessage(MODALS_CONFIRM_TITLE)
        );

        if (!confirmed || uploadFinished) {
            return;
        }

        fourthStepController.removeUploadFinishListener();

        helper.cancelUpload();
        helper.clearAllLists();

        currentStepCode = 1;
        setNeedUpdateEditModel(true);

        firstStepController.activate();
        changeStepView(firstStepController);
    }

    public void onReset() {
        helper.clearAllLists();

        currentStepCode = 1;
        setNeedUpdateEditModel(true);

        firstStepController.activate();
        changeStepView(firstStepController);
    }

    public void instrumentChanged() {
        helper.clearAllLists();
    }

    private void changeStepView(StepController controller) {
        wizardMainForm.changeStepView(controller.getView());
    }

    public void onUploadFinished() {
        uploadFinished = true;
        wizardMainForm.setNextButtonTitle(START_NEW_UPLOAD);
    }

    public void onOk() {
        helper.clearAllLists();

        currentStepCode = 1;
        setNeedUpdateEditModel(true);

        firstStepController.activate();
        changeStepView(firstStepController);
    }
}
