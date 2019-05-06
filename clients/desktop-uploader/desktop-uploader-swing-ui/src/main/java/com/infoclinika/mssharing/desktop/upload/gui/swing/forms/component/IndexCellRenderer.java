package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by Alexey on 5/24/17.
 */
public class IndexCellRenderer extends JLabel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText(Integer.toString(row + 1));
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setBackground(Color.lightGray);
        return this;
    }
}
