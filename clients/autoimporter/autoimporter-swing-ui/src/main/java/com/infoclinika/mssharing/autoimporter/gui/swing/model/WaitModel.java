package com.infoclinika.mssharing.autoimporter.gui.swing.model;

import com.google.common.collect.Lists;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import org.apache.commons.io.FileUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.MAIN_TABLE_COLUMN_NAME;
import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.MAIN_TABLE_COLUMN_SIZE;
import static com.infoclinika.mssharing.autoimporter.messages.MessagesSource.getMessage;

/**
 * @author Ruslan Duboveckij
 */
public class WaitModel extends AbstractTableModel implements NotifyModel<WaitItem> {
    public static final int NAME_COLUMN = 0;
    public static final int SIZE_COLUMN = 1;
    public static final int SIZE_WIDTH = 75;
    private final List<WaitItem> waitList = Lists.newArrayList();

    private String columnNameName;
    private String columnSizeName;

    {
        columnNameName = getMessage(MAIN_TABLE_COLUMN_NAME);
        columnSizeName = getMessage(MAIN_TABLE_COLUMN_SIZE);
    }

    @Override
    public int getRowCount() {
        return waitList.size();
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
        WaitItem waitItem = waitList.get(rowIndex);
        switch (columnIndex) {
            case NAME_COLUMN:
                return waitItem.getName();
            case SIZE_COLUMN:
                return FileUtils.byteCountToDisplaySize(waitItem.getSize());
            default:
                return "";
        }
    }

    @Override
    public void notifyInit(List<WaitItem> waitItems) {
        waitList.clear();
        waitList.addAll(waitItems);
        fireTableDataChanged();
    }

    @Override
    public void notifyAdd(WaitItem o) {
        waitList.add(o);
        fireTableDataChanged();
    }

    @Override
    public void notifyRemove(WaitItem o) {
        waitList.remove(o);
        fireTableDataChanged();
    }
}
