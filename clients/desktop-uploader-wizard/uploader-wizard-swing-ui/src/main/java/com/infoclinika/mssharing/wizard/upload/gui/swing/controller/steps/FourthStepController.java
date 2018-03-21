package com.infoclinika.mssharing.wizard.upload.gui.swing.controller.steps;

import com.infoclinika.mssharing.wizard.upload.gui.swing.controller.MainController;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.steps.FourthStep;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.UploadZipTableModel;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.UploadFinishedListener;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.WizardUploaderHelper;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.table.TableModel;

/**
 * @author timofey.kasyanov
 *         date:   30.01.14
 */
@Component
public class FourthStepController extends DefaultStepController {
    private FourthStep fourthStep;

    @Inject
    private MainController mainController;

    @Inject
    private WizardUploaderHelper helper;

    public FourthStepController() {
    }

    public void setFourthStep(FourthStep fourthStep) {
        this.fourthStep = fourthStep;
    }

    private final UploadFinishedListener uploadFinishListener = new UploadFinishListenerImpl();

    @Override
    public void activate() {
        mainController.setUploadFinished(false);
        fourthStep.setTableModel(createTableModel());
        helper.updateUploadList();
        mainController.stepFourUpdateButtons();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    helper.startUpload();
                } catch (Exception ignore) {
                }
            }
        }).start();
    }


    public void removeUploadFinishListener() {
        final UploadZipTableModel tableModel = (UploadZipTableModel) fourthStep.getTableModel();
        tableModel.setUploadFinishedListener(null);
    }

    private TableModel createTableModel() {
        final UploadZipTableModel model = new UploadZipTableModel(helper.getUploadZipList(), helper.isArchive());
        model.setUploadFinishedListener(uploadFinishListener);

        return model;

    }

    private void onUploadFinished() {
        mainController.onUploadFinished();
    }

    private class UploadFinishListenerImpl implements UploadFinishedListener {
        @Override
        public void uploadFinished() {
            onUploadFinished();
        }
    }
}
