package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component;

import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FormUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
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
    public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                            boolean hasFocus, int row, int column) {
        return FormUtils.getTableCellComponent(comboBox, panel, species, table, value, isSelected);

    }
}
