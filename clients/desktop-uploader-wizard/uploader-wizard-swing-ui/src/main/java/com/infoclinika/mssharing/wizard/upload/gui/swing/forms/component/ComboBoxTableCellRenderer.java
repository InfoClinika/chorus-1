package com.infoclinika.mssharing.wizard.upload.gui.swing.forms.component;

import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.upload.common.dto.DictionaryWrapper;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * @author timofey.kasyanov
 *         date:   30.01.14
 */
public class ComboBoxTableCellRenderer implements TableCellRenderer {

    private final JComboBox comboBox;
    private final JPanel panel;
    private final java.util.List<DictionaryWrapper> species;

    public ComboBoxTableCellRenderer(List<DictionaryWrapper> species) {

        this.species = species;

        final DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (DictionaryWrapper wrapper : species) {
            model.addElement(wrapper);
        }

        comboBox = new JComboBox(model);

        final int v = UiProperties.EDITABLE_CELL_MARGIN;

        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(v, v, v, v));
        panel.add(comboBox);

    }

    @Override
    public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

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
}
