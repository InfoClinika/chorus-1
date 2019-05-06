package com.infoclinika.mssharing.autoimporter.gui.swing.model;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;

import javax.swing.table.AbstractTableModel;
import java.util.List;

import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.MAIN_TABLE_COLUMN_NAME;
import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.MAIN_TABLE_COLUMN_SIZE;
import static com.infoclinika.mssharing.autoimporter.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *     date:   21.01.14
 */
public class DuplicateModel extends AbstractTableModel implements NotifyModel<DuplicateItem> {

    public static final int NAME_COLUMN = 0;
    public static final int SIZE_COLUMN = 1;
    public static final int SIZE_WIDTH = 75;
    private final List<DuplicateItem> duplicatesList = Lists.newArrayList();

    private String columnNameName;
    private String columnSizeName;

    {
        columnNameName = getMessage(MAIN_TABLE_COLUMN_NAME);
        columnSizeName = getMessage(MAIN_TABLE_COLUMN_SIZE);
    }

    @Override
    public int getRowCount() {
        return duplicatesList.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case NAME_COLUMN:
                return columnNameName;
            case SIZE_COLUMN:
                return columnSizeName;
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DuplicateItem duplicateItem = duplicatesList.get(rowIndex);
        switch (columnIndex) {
            case NAME_COLUMN:
                return duplicateItem.getName();
            case SIZE_COLUMN:
                return duplicateItem.getSizeString();
            default:
                return "";
        }
    }

    @Override
    public void notifyInit(List<DuplicateItem> items) {

        duplicatesList.clear();

        duplicatesList.addAll(items);

        fireTableDataChanged();

    }

    @Override
    public void notifyAdd(DuplicateItem o) {

        duplicatesList.add(o);

        fireTableDataChanged();

    }

    @Override
    public void notifyRemove(DuplicateItem o) {

        duplicatesList.remove(o);

        fireTableDataChanged();

    }

}
