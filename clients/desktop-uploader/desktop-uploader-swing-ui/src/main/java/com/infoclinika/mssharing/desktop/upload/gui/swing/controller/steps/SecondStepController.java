package com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps;

import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.MainController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.steps.SecondStep;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.ViewTableModel;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.DesktopUploaderHelper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FilesDropTarget;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.InstrumentFileFilter;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.SecondStepHelper;
import com.infoclinika.mssharing.desktop.upload.model.ViewFileItem;
import com.infoclinika.mssharing.desktop.upload.service.impl.list.ViewFileItemList;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.table.TableModel;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
@Component
public class SecondStepController extends AbstractStepController {
    private SecondStep secondStep;

    @Inject
    private MainController mainController;

    @Inject
    private DesktopUploaderHelper helper;

    @Inject
    private SecondStepHelper secondStepHelper;

    public void setSecondStep(SecondStep secondStep) {
        this.secondStep = secondStep;
    }

    @Override
    public void activate() {
        List<String> extensions = helper.getInstrumentDefaultExtensions();
        secondStep.initExtensionButtons(extensions);
        applyFileFilterExtension(extensions);
        mainController.stepTwoUpdateButtons();
        secondStep.adjustColumnSizes();
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
        helper.getViewFileItemList()
            .add(new ViewFileItem(file));
    }

    public void removeItems(List<Integer> indexes) {
        final ViewFileItemList list = helper.getViewFileItemList();
        final List<ViewFileItem> toBeRemoved = new ArrayList<>();
        for (Integer index : indexes) {
            toBeRemoved.add(list.get(index));
        }
        list.removeAll(toBeRemoved);
    }

    public void filesChanged() {
        mainController.stepTwoUpdateButtons();
        mainController.setNeedUpdateEditModel(true);
        secondStep.adjustColumnSizes();
    }

    public void applyFileFilterExtension(List<String> extensions) {
        helper.setExtensions(extensions);
        final InstrumentFileFilter fileFilter = helper.createFileFilter(extensions);
        secondStepHelper.setInstrumentFileFilter(fileFilter);
        secondStep.setFileFilter(fileFilter);
    }

}
