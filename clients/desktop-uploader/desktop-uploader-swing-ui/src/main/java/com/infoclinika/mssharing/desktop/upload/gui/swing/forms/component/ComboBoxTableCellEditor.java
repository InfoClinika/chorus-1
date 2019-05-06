package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component;

import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FormUtils;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
public class ComboBoxTableCellEditor implements TableCellEditor {

    private final DefaultCellEditor defaultCellEditor;
    private final JComboBox comboBox;
    private final JPanel panel;
    private final java.util.List<DictionaryWrapper> species;

    public ComboBoxTableCellEditor(java.util.List<DictionaryWrapper> species) {

        this.species = species;

        final DefaultComboBoxModel<DictionaryWrapper> model = new DefaultComboBoxModel<>();

        for (DictionaryWrapper wrapper : species) {
            model.addElement(wrapper);
        }

        comboBox = new JComboBox(model);

        defaultCellEditor = new DefaultCellEditor(comboBox);

        final int v = UiProperties.EDITABLE_CELL_MARGIN;

        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(v, v, v, v));
        panel.add(comboBox);

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return FormUtils.getTableCellComponent(comboBox, panel, species, table, value, isSelected);
    }

    @Override
    public Object getCellEditorValue() {
        return defaultCellEditor.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return defaultCellEditor.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return defaultCellEditor.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return defaultCellEditor.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        defaultCellEditor.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        defaultCellEditor.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        defaultCellEditor.removeCellEditorListener(l);
    }
}
