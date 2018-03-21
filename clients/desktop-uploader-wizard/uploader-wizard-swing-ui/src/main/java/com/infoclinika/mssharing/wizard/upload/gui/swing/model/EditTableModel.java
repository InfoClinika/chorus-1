package com.infoclinika.mssharing.wizard.upload.gui.swing.model;

import com.infoclinika.mssharing.upload.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.wizard.upload.model.EditFileItem;
import com.infoclinika.mssharing.wizard.upload.service.api.list.ListListener;
import com.infoclinika.mssharing.wizard.upload.service.api.list.ObservableList;

import javax.swing.table.AbstractTableModel;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.infoclinika.mssharing.wizard.messages.MessageKey.*;
import static com.infoclinika.mssharing.wizard.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *         date:   29.01.14
 */
public class EditTableModel extends AbstractTableModel implements ListListener<EditFileItem> {

    private static final int NAME_COLUMN = 0;
    public static final int SPECIE_COLUMN = 1;
    public static final int LABELS_COLUMN = 2;

    private final ObservableList<EditFileItem> list;

    private String columnNameName;
    private String columnSpecieName;
    private String columnLabelsName;

    {
        columnNameName = getMessage(TABLE_COLUMN_NAME);
        columnSpecieName = getMessage(TABLE_COLUMN_SPECIE);
        columnLabelsName = getMessage(TABLE_COLUMN_LABELS);
    }

    public EditTableModel(ObservableList<EditFileItem> list) {

        checkNotNull(list);

        this.list = list;

        list.getObserver().addListener(this);

    }

    @Override
    public String getColumnName(int columnIndex) {

        if (columnIndex == NAME_COLUMN) {
            return columnNameName;
        } else if (columnIndex == SPECIE_COLUMN) {
            return columnSpecieName;
        } else if (columnIndex == LABELS_COLUMN) {
            return columnLabelsName;
        } else {
            return "";
        }
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
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        switch (columnIndex){

            case NAME_COLUMN:
                return list.get(rowIndex).getName();

            case SPECIE_COLUMN:
                return list.get(rowIndex).getSpecie();

            case LABELS_COLUMN:
                return list.get(rowIndex).getLabels();

            default:
                throw new RuntimeException("Invalid column index: " + columnIndex);

        }

    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        if(rowIndex < 0 || rowIndex >= list.size()){
            return;
        }

        final EditFileItem item = list.get(rowIndex);

        if(columnIndex == SPECIE_COLUMN){

            final DictionaryWrapper specieWrapper = (DictionaryWrapper) aValue;

            item.setSpecie(specieWrapper.getDictionary());

        } else if(columnIndex == LABELS_COLUMN){

            item.setLabels(aValue.toString());

        }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == SPECIE_COLUMN || columnIndex == LABELS_COLUMN;
    }
}
