package com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.clients.common.Transformers;
import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.MainController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.ComboBoxTableCellEditor;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.ComboBoxTableCellRenderer;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.steps.ThirdStep;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.EditTableModel;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.bean.DesktopUploaderSession;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.DesktopUploaderHelper;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.util.List;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
@Component
public class ThirdStepController extends AbstractStepController {
    private ThirdStep thirdStep;
    private TableCellRenderer tableCellRenderer;
    private TableCellEditor tableCellEditor;

    @Inject
    private MainController mainController;

    @Inject
    private DesktopUploaderHelper helper;

    @Inject
    private DesktopUploaderSession desktopUploaderSession;

    public void setThirdStep(ThirdStep thirdStep) {
        this.thirdStep = thirdStep;
    }

    @Override
    public void activate() {
        thirdStep.setSpecieTableCellRenderer(getRendererForSpecieColumn());
        thirdStep.setSpecieTableCellEditor(getEditorForSpecieColumn());

        if (mainController.isNeedUpdateEditModel()) {
            helper.updateEditFileItemList();
            mainController.setNeedUpdateEditModel(false);
        }

        mainController.stepThreeUpdateButtons();
        thirdStep.adjustColumnSizes();
    }

    public TableModel createTableModel() {
        return new EditTableModel(helper.getEditFileItemList());
    }

    private TableCellRenderer getRendererForSpecieColumn() {
        return tableCellRenderer == null
            ? new ComboBoxTableCellRenderer(getDictionaryWrappersForSpecie())
            : tableCellRenderer;
    }

    private TableCellEditor getEditorForSpecieColumn() {
        return tableCellEditor == null
            ? new ComboBoxTableCellEditor(getDictionaryWrappersForSpecie())
            : tableCellEditor;
    }

    private List<DictionaryWrapper> getDictionaryWrappersForSpecie() {
        final List<DictionaryDTO> species = desktopUploaderSession.getSpecies();
        return Lists.transform(species, Transformers.TO_DICTIONARY_WRAPPER);
    }
}
