package com.infoclinika.mssharing.desktop.upload.gui.swing.model;

import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.desktop.upload.model.EditFileItem;
import com.infoclinika.mssharing.desktop.upload.service.api.list.ListListener;
import com.infoclinika.mssharing.desktop.upload.service.api.list.ObservableList;

import javax.swing.table.AbstractTableModel;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.*;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *     date:   29.01.14
 */
public class EditTableModel extends AbstractTableModel implements ListListener<EditFileItem> {

    public static final int INDEX_COLUMN_ID = 0;
    public static final int NAME_COLUMN_ID = 1;
    public static final int SPECIE_COLUMN_ID = 2;
    public static final int LABELS_COLUMN_ID = 3;

    private final ObservableList<EditFileItem> list;
    private final String[] columnNames;

    public EditTableModel(ObservableList<EditFileItem> list) {
        checkNotNull(list);
        this.list = list;
        this.list.getObserver().addListener(this);

        columnNames = new String[] {"#", getMessage(TABLE_COLUMN_NAME), getMessage(TABLE_COLUMN_SPECIE),
            getMessage(TABLE_COLUMN_LABELS)};
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public void onAdd(EditFileItem item) {
        fireTableDataChanged();
    }

    @Override
    public void onRemove(EditFileItem item) {
        fireTableDataChanged();
    }

    @Override
    public void onChange(EditFileItem item, Object params) {

    }

    @Override
    public void onClear() {
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
            case SPECIE_COLUMN_ID:
                return list.get(rowIndex).getSpecie();
            case LABELS_COLUMN_ID:
                return list.get(rowIndex).getLabels();
            default:
                throw new RuntimeException("Invalid column index: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        final EditFileItem item = list.get(rowIndex);

        switch (columnIndex) {
            case SPECIE_COLUMN_ID:
                final DictionaryWrapper specieWrapper = (DictionaryWrapper) value;
                item.setSpecie(specieWrapper.getDictionary());
                break;
            case LABELS_COLUMN_ID:
                item.setLabels(value.toString());
                break;
            default:
                return;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == SPECIE_COLUMN_ID || columnIndex == LABELS_COLUMN_ID;
    }
}
