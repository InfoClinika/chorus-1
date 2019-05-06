package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component;

import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author timofey.kasyanov
 *     date:   30.01.14
 */
public class ProgressBarTableCellRenderer implements TableCellRenderer {
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;
    private final JProgressBar progressBar;

    public ProgressBarTableCellRenderer() {

        progressBar = new JProgressBar();

        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        progressBar.setForeground(UiProperties.PROGRESS_BAR_COLOR);
        progressBar.setMinimum(MIN_VALUE);
        progressBar.setMaximum(MAX_VALUE);
        progressBar.setIgnoreRepaint(false);
        progressBar.setOpaque(true);

    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        progressBar.setValue((Integer) value);
        return progressBar;
    }

}
