package com.infoclinika.mssharing.autoimporter.gui.swing.model;

import com.infoclinika.mssharing.autoimporter.gui.swing.forms.MainForm;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.FormUtils;

import javax.swing.*;
import java.awt.*;

/**
 * author Ruslan Duboveckij
 */
public class IconCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        final MainForm.ContextWrapper contextWrapper = (MainForm.ContextWrapper) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list,
            value, index, isSelected, cellHasFocus
        );

        label.setText(contextWrapper.toString());
        label.setIcon(contextWrapper.info.isStarted() ?
            FormUtils.IS_STARTED_ICON : FormUtils.IS_STOPPED_ICON);
        return label;
    }
}
