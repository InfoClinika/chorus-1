package com.infoclinika.mssharing.wizard.upload.gui.swing.model;

import com.infoclinika.mssharing.wizard.upload.model.ViewFileItem;
import com.infoclinika.mssharing.wizard.upload.service.api.list.ListListener;
import com.infoclinika.mssharing.wizard.upload.service.api.list.ObservableList;

import javax.swing.table.AbstractTableModel;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.wizard.messages.MessageKey.*;
import static com.infoclinika.mssharing.wizard.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *         date:   27.01.14
 */
public class ViewTableModel extends AbstractTableModel implements ListListener<ViewFileItem> {

    public final int NAME_COLUMN = 0;
    public final int SIZE_COLUMN = 1;

    private final ObservableList<ViewFileItem> list;

    private String columnNameName;
    private String columnSizeName;

    {
        columnNameName = getMessage(TABLE_COLUMN_NAME);
        columnSizeName = getMessage(TABLE_COLUMN_SIZE);
    }

    public ViewTableModel(ObservableList<ViewFileItem> list) {

        checkNotNull(list);

        this.list = list;

        list.getObserver().addListener(this);

    }

    @Override
    public String getColumnName(int columnIndex) {

        if (columnIndex == NAME_COLUMN) {
            return columnNameName;
        } else if (columnIndex == SIZE_COLUMN) {
            return columnSizeName;
        } else {
            return "";
        }
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
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        switch (columnIndex){

            case NAME_COLUMN:
                return list.get(rowIndex).getName();

            case SIZE_COLUMN:
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
