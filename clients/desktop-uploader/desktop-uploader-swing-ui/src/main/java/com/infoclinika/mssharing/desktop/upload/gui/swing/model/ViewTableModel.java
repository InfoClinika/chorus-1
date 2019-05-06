package com.infoclinika.mssharing.desktop.upload.gui.swing.model;

import com.infoclinika.mssharing.desktop.upload.model.ViewFileItem;
import com.infoclinika.mssharing.desktop.upload.service.api.list.ListListener;
import com.infoclinika.mssharing.desktop.upload.service.api.list.ObservableList;

import javax.swing.table.AbstractTableModel;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.TABLE_COLUMN_NAME;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.TABLE_COLUMN_SIZE;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
public class ViewTableModel extends AbstractTableModel implements ListListener<ViewFileItem> {

    public static final int INDEX_COLUMN_ID = 0;
    public static final int NAME_COLUMN_ID = 1;
    public static final int SIZE_COLUMN_ID = 2;

    private final ObservableList<ViewFileItem> list;
    private final String[] columnNames;

    public ViewTableModel(ObservableList<ViewFileItem> list) {
        checkNotNull(list);
        this.list = list;
        this.list.getObserver().addListener(this);

        columnNames = new String[] {"#", getMessage(TABLE_COLUMN_NAME), getMessage(TABLE_COLUMN_SIZE)};
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public void onAdd(ViewFileItem item) {
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    @Override
    public void onRemove(ViewFileItem item) {
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    @Override
    public void onChange(ViewFileItem item, Object params) {
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    @Override
    public void onClear() {
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case INDEX_COLUMN_ID:
                return new Integer(rowIndex + 1);
            case NAME_COLUMN_ID:
                return list.get(rowIndex).getName();
            case SIZE_COLUMN_ID:
                return list.get(rowIndex).getSizeString();
            default:
                throw new RuntimeException("Invalid column index: " + columnIndex);

        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
}
