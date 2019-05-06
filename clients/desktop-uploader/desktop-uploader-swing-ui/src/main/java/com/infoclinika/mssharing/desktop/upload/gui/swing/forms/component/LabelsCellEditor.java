package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component;

import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.awt.event.KeyEvent.*;

/**
 * @author timofey.kasyanov
 *     date:   05.02.14
 */
public class LabelsCellEditor implements TableCellEditor {

    private static final List<Integer> KEY_CODES_TO_NEXT_EDITABLE =
        newArrayList(VK_DOWN, VK_UP, VK_ENTER, VK_TAB);

    private final DefaultCellEditor defaultCellEditor;
    private final JTextField textField;
    private ToNextEditableListener toNextEditableListener;

    public LabelsCellEditor() {

        textField = new JTextField();
        textField.setFocusable(true);

        defaultCellEditor = new DefaultCellEditor(textField);
        defaultCellEditor.setClickCountToStart(1);

        final int v = UiProperties.EDITABLE_CELL_MARGIN;
        textField.setBorder(BorderFactory.createEmptyBorder(v, v, v, v));

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!(e.getOppositeComponent() instanceof JTable)) {
                    if (!stopCellEditing()) {
                        cancelCellEditing();
                    }
                }
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                final int keyCode = e.getKeyCode();

                if (toNextEditableListener != null && KEY_CODES_TO_NEXT_EDITABLE.contains(keyCode)) {

                    toNextEditableListener.toNextEditable(keyCode);

                }

            }
        });

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setText((String) value);
        return textField;
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

    public void setToNextEditableListener(ToNextEditableListener toNextEditableListener) {
        this.toNextEditableListener = toNextEditableListener;
    }

    public void grabFocus() {
        textField.grabFocus();
    }

}
