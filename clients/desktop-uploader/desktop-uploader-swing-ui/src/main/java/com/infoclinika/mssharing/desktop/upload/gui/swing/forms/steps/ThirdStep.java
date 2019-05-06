package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.steps;

import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps.ThirdStepController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.LabelsCellEditor;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.LabelsCellRenderer;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.ToNextEditableListener;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.EditTableModel;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FormUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static com.infoclinika.mssharing.desktop.messages.MessageKey.THREE_LABEL_NOTE_ONE;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.THREE_LABEL_NOTE_TWO;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

@Component
public class ThirdStep extends JPanel {
    private static final String NONE = "none";
    private JPanel contentPanel;
    private JTable filesTable;
    private JLabel labelNoteOne;
    private JLabel labelNoteTwo;

    @Inject
    private ThirdStepController controller;

    @PostConstruct
    private void postConstruct() {

        labelNoteOne.setText(getMessage(THREE_LABEL_NOTE_ONE));
        labelNoteTwo.setText(getMessage(THREE_LABEL_NOTE_TWO));

        filesTable.setRowMargin(UiProperties.ROW_MARGIN);
        filesTable.setRowHeight(UiProperties.ROW_HEIGHT);
        filesTable.getColumnModel().setColumnMargin(UiProperties.COLUMN_MARGIN);
        filesTable.setFillsViewportHeight(true);
        filesTable.setRowSelectionAllowed(false);
        filesTable.setColumnSelectionAllowed(false);
        filesTable.setCellSelectionEnabled(false);

        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), NONE);

        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), NONE);

        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), NONE);

        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), NONE);
        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), NONE);
        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), NONE);
        filesTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), NONE);

        filesTable.setModel(controller.createTableModel());

        final TableColumn tableColumn = filesTable.getColumnModel().getColumn(EditTableModel.LABELS_COLUMN_ID);

        final LabelsCellEditor labelsCellEditor = new LabelsCellEditor();

        labelsCellEditor.setToNextEditableListener(new ToNextEditableListener() {
            @Override
            public void toNextEditable(int keyCode) {
                ThirdStep.this.toNextRowEditLabels(keyCode, labelsCellEditor);
            }
        });

        tableColumn.setCellEditor(labelsCellEditor);
        tableColumn.setCellRenderer(new LabelsCellRenderer());

        controller.setView(contentPanel);
        controller.setThirdStep(this);
    }

    public void setSpecieTableCellRenderer(TableCellRenderer renderer) {
        final TableColumn tableColumn = filesTable.getColumnModel().getColumn(EditTableModel.SPECIE_COLUMN_ID);
        tableColumn.setCellRenderer(renderer);
    }

    public void setSpecieTableCellEditor(TableCellEditor editor) {
        final TableColumn tableColumn = filesTable.getColumnModel().getColumn(EditTableModel.SPECIE_COLUMN_ID);
        tableColumn.setCellEditor(editor);
    }

    public void adjustColumnSizes() {
        List<RowSorter.SortKey> sortKeys = new ArrayList<>(2);
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
        FormUtils.adjustColumnSizes(filesTable, sortKeys);
    }

    private void toNextRowEditLabels(int keyCode, LabelsCellEditor labelsCellEditor) {

        labelsCellEditor.stopCellEditing();

        int selectedRow = filesTable.getSelectedRow();

        int nextSelectedRow;

        if (keyCode == KeyEvent.VK_UP) {

            nextSelectedRow = selectedRow - 1;

        } else {

            nextSelectedRow = selectedRow + 1;

        }

        if (nextSelectedRow >= filesTable.getRowCount()) {

            nextSelectedRow = 0;

        } else if (nextSelectedRow < 0) {

            nextSelectedRow = filesTable.getRowCount() - 1;

        }

        filesTable.setRowSelectionInterval(nextSelectedRow, nextSelectedRow);

        final MouseEvent mouseEvent =
            new MouseEvent(filesTable, MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, 2, false, MouseEvent.BUTTON1);

        filesTable.editCellAt(nextSelectedRow, EditTableModel.LABELS_COLUMN_ID, mouseEvent);

        labelsCellEditor.grabFocus();

    }

}
