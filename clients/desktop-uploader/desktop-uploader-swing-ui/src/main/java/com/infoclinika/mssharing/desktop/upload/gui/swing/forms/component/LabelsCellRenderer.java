package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component;

import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author timofey.kasyanov
 *     date:   05.02.14
 */
public class LabelsCellRenderer implements TableCellRenderer {

    private final JPanel panel;
    private final JLabel label;

    public LabelsCellRenderer() {

        final int v = UiProperties.EDITABLE_CELL_MARGIN;

        label = new JLabel();

        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(v, v, v, v));
        panel.add(label);

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        final Color color = isSelected ? table.getSelectionBackground() : table.getBackground();

        panel.setBackground(color);

        label.setText((String) value);

        return panel;
    }
}
