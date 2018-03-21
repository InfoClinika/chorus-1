package com.infoclinika.mssharing.wizard.upload.gui.swing.controller.steps;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.wizard.upload.gui.swing.controller.MainController;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.component.ComboBoxTableCellEditor;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.component.ComboBoxTableCellRenderer;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.steps.ThirdStep;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.EditTableModel;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.bean.WizardSession;
import com.infoclinika.mssharing.upload.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.upload.common.Transformers;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.WizardUploaderHelper;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.util.List;

/**
 * @author timofey.kasyanov
 *         date:   30.01.14
 */
@Component
public class ThirdStepController extends DefaultStepController {
    private ThirdStep thirdStep;
    private TableCellRenderer tableCellRenderer;
    private TableCellEditor tableCellEditor;

    @Inject
    private MainController mainController;

    @Inject
    private WizardUploaderHelper helper;

    @Inject
    private WizardSession wizardSession;

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
    }

    public TableModel createTableModel() {
        return new EditTableModel(helper.getEditFileItemList());
    }

    private TableCellRenderer getRendererForSpecieColumn() {
        if (tableCellRenderer == null) {
            final List<DictionaryDTO> species = wizardSession.getSpecies();
            final List<DictionaryWrapper> wrappers = Lists.transform(species, Transformers.TO_DICTIONARY_WRAPPER);
            tableCellRenderer = new ComboBoxTableCellRenderer(wrappers);
        }

        return tableCellRenderer;
    }

    private TableCellEditor getEditorForSpecieColumn() {
        if (tableCellEditor == null) {
            final List<DictionaryDTO> species = wizardSession.getSpecies();
            final List<DictionaryWrapper> wrappers = Lists.transform(species, Transformers.TO_DICTIONARY_WRAPPER);
            tableCellEditor = new ComboBoxTableCellEditor(wrappers);
        }

        return tableCellEditor;
    }
}
