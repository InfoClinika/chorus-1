package com.infoclinika.mssharing.wizard.upload.gui.swing.forms.component;

import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.upload.common.dto.DictionaryWrapper;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.*;

/**
 * @author timofey.kasyanov
 *         date:   30.01.14
 */
public class ComboBoxTableCellEditor implements TableCellEditor {

    private final DefaultCellEditor defaultCellEditor;
    private final JComboBox comboBox;
    private final JPanel panel;
    private final java.util.List<DictionaryWrapper> species;

    public ComboBoxTableCellEditor(java.util.List<DictionaryWrapper> species) {

        this.species = species;

        final DefaultComboBoxModel model = new DefaultComboBoxModel();

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

        final Color color = isSelected ? table.getSelectionBackground() : table.getBackground();

        panel.setBackground(color);

        final DictionaryDTO specie = (DictionaryDTO) value;

        for(DictionaryWrapper wrapper : species){
            if(wrapper.getDictionary().getName().equals(specie.getName())){
                comboBox.setSelectedItem(wrapper);
                break;
            }
        }

        return panel;

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
