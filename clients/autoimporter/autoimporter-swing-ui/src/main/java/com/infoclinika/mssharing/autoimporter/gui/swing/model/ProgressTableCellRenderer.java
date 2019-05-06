package com.infoclinika.mssharing.autoimporter.gui.swing.model;

import com.infoclinika.mssharing.autoimporter.gui.swing.util.FormUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * author Ruslan Duboveckij
 */
public class ProgressTableCellRenderer extends DefaultTableCellRenderer {
    private final JProgressBar progressBar;

    public ProgressTableCellRenderer() {
        setOpaque(true);
        progressBar = new JProgressBar();

        progressBar.setStringPainted(true);

        progressBar.setBorderPainted(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        progressBar.setForeground(FormUtils.PROGRESS_BAR_COLOR);
        progressBar.setOpaque(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        progressBar.setValue((Integer) value);
        return progressBar;
    }
}
