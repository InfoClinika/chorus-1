package com.infoclinika.mssharing.wizard.upload.gui.swing.controller.steps;

import com.infoclinika.mssharing.wizard.upload.gui.swing.controller.MainController;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.steps.SecondStep;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.ViewTableModel;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.FilesDropTarget;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.InstrumentFileFilter;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.SecondStepHelper;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.WizardUploaderHelper;
import com.infoclinika.mssharing.wizard.upload.model.ViewFileItem;
import com.infoclinika.mssharing.wizard.upload.service.impl.list.ViewFileItemList;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.table.TableModel;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author timofey.kasyanov
 *         date:   30.01.14
 */
@Component
public class SecondStepController extends DefaultStepController {
    private SecondStep secondStep;

    @Inject
    private MainController mainController;

    @Inject
    private WizardUploaderHelper helper;

    @Inject
    private SecondStepHelper secondStepHelper;

    public void setSecondStep(SecondStep secondStep) {
        this.secondStep = secondStep;
    }

    @Override
    public void activate() {
        final InstrumentFileFilter fileFilter = helper.createFileFilter();
        secondStepHelper.setInstrumentFileFilter(fileFilter);
        secondStep.setFileFilter(fileFilter);
        mainController.stepTwoUpdateButtons();
    }

    public TableModel createTableModel() {
        return new ViewTableModel(helper.getViewFileItemList());
    }

    public DropTarget createDropTarget() {
        return new FilesDropTarget(new FilesDropTarget.FilesDropListener() {
            @Override
            public void filesDropped(List<File> files) {
                secondStepHelper.filesDropped(files);
            }
        });
    }

    public void dropFiles(List<File> files) {
        secondStepHelper.filesDropped(files);
    }

    public void addItem(File file) {
        final ViewFileItemList list = helper.getViewFileItemList();

        final ViewFileItem item = new ViewFileItem(file);
        list.add(item);
    }

    public void removeItems(List<Integer> indexes) {
        final ViewFileItemList list = helper.getViewFileItemList();
        final List<ViewFileItem> toBeRemoved = newArrayList();
        for (Integer index : indexes) {
            final ViewFileItem item = list.get(index);
            toBeRemoved.add(item);
        }

        for (ViewFileItem item : toBeRemoved) {
            list.remove(item);
        }
    }

    public void filesChanged() {
        mainController.stepTwoUpdateButtons();
        mainController.setNeedUpdateEditModel(true);
    }
}
