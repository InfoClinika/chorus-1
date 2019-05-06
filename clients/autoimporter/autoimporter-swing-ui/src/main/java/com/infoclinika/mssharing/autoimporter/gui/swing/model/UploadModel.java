package com.infoclinika.mssharing.autoimporter.gui.swing.model;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.impl.NotificationType;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.*;
import static com.infoclinika.mssharing.autoimporter.messages.MessagesSource.getMessage;
import static com.infoclinika.mssharing.autoimporter.model.bean.ItemStatus.*;

/**
 * author Ruslan Duboveckij
 */
public class UploadModel extends AbstractTableModel {

    public static final int SPEED_WIDTH = 100;
    public static final int PROGRESS_WIDTH = 200;
    private final List<UploadItem> list = newArrayList();
    public static final int SIZE_COLUMN = 1;
    public int zipColumn;
    public int uploadColumn;
    public int speedColumn;
    private int columnSize;

    private final Map<UploadItem, ItemValuesInfo> valuesInfoMap = newHashMap();
    private final Map<UploadItem, Optional<Timer>> timerMap = newHashMap();

    private String columnNameName;
    private String columnSizeName;
    private String columnZipName;
    private String columnUploadName;
    private String columnSpeedName;

    private String statusWait;
    private String statusError;
    private String statusDone;
    private String statusZip;
    private String statusCanceled;
    private String statusUnavailable;
    private String statusSizeMismatch;
    private String statusFinish;
    private String statusRetrying;

    {
        columnNameName = getMessage(MAIN_TABLE_COLUMN_NAME);
        columnSizeName = getMessage(MAIN_TABLE_COLUMN_SIZE);
        columnZipName = getMessage(MAIN_TABLE_COLUMN_ZIP);
        columnUploadName = getMessage(MAIN_TABLE_COLUMN_UPLOAD);
        columnSpeedName = getMessage(MAIN_TABLE_COLUMN_SPEED);

        statusWait = getMessage(MAIN_TABLE_UPLOAD_STATUS_WAITING);
        statusError = getMessage(MAIN_TABLE_UPLOAD_STATUS_ERROR);
        statusDone = getMessage(MAIN_TABLE_UPLOAD_STATUS_DONE);
        statusZip = getMessage(MAIN_TABLE_UPLOAD_STATUS_ZIPPING);
        statusCanceled = getMessage(MAIN_TABLE_UPLOAD_STATUS_CANCELED);
        statusUnavailable = getMessage(MAIN_TABLE_UPLOAD_STATUS_UNAVAILABLE);
        statusSizeMismatch = getMessage(MAIN_TABLE_UPLOAD_STATUS_SIZE_MISMATCH);
        statusFinish = getMessage(MAIN_TABLE_UPLOAD_STATUS_FINISHING);
        statusRetrying = getMessage(MAIN_TABLE_UPLOAD_STATUS_RETRYING);
    }

    public UploadModel(boolean archive) {
        if (archive) {
            zipColumn = 2;
            uploadColumn = 3;
            speedColumn = 4;
            columnSize = 5;
        } else {
            zipColumn = 5;
            uploadColumn = 2;
            speedColumn = 3;
            columnSize = 4;
        }
    }

    private static int toPercent(double value) {
        return (int) Math.round(value * 100);
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return columnSize;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == WaitModel.NAME_COLUMN) {
            return columnNameName;
        } else if (columnIndex == WaitModel.SIZE_COLUMN) {
            return columnSizeName;
        } else if (columnIndex == zipColumn) {
            return columnZipName;
        } else if (columnIndex == uploadColumn) {
            return columnUploadName;
        } else if (columnIndex == speedColumn) {
            return columnSpeedName;
        } else {
            return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == zipColumn) {
            return Integer.class;
        } else if (columnIndex == uploadColumn) {
            return Integer.class;
        } else {
            return String.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        final UploadItem uploadItem = list.get(rowIndex);

        if (columnIndex == WaitModel.NAME_COLUMN) {

            return uploadItem.getName();

        } else if (columnIndex == WaitModel.SIZE_COLUMN) {

            return FileUtils.byteCountToDisplaySize(uploadItem.getSize());

        } else if (columnIndex == zipColumn) {

            return toPercent(uploadItem.getZipRation());

        } else if (columnIndex == uploadColumn) {

            return toPercent(uploadItem.getUploadRatio());

        } else if (columnIndex == speedColumn) {

            return getSpeedString(uploadItem);

        } else {
            return "";
        }
    }

    public String getSpeedString(UploadItem item) {

        if (item.getStatus().equals(WAITING)) {
            return statusWait;
        } else if (item.getStatus().equals(COMPLETE)) {
            removeTimer(item);
            return statusDone;
        } else if (item.getStatus().equals(ERROR)) {
            removeTimer(item);
            return statusError;
        } else if (item.getStatus().equals(ZIPPING)) {
            return statusZip;
        } else if (item.getStatus() == CANCELED) {
            return statusCanceled;
        } else if (item.getStatus() == UPLOAD_UNAVAILABLE) {
            return statusUnavailable;
        } else if (item.getStatus() == SIZE_MISMATCH) {
            return statusSizeMismatch;
        } else if (item.getStatus().equals(UPLOADING)) {

            int percentage = toPercent(item.getUploadRatio());

            if (percentage == 100) {

                return statusFinish;

            }

            final ItemValuesInfo valuesInfo = valuesInfoMap.get(item);

            return formatSpeed(valuesInfo.lastSpeed);
        } else if (item.getStatus().equals(RETRYING)) {
            return statusRetrying;
        }

        throw new RuntimeException("Unrecognized autoimporter status: " + item.getStatus());
    }

    public void notifyInit(List<UploadItem> uploadItems) {
        list.clear();
        list.addAll(uploadItems);
        fireTableDataChanged();

        updateTimersMap();

        updateValuesMap();
    }

    private String formatSpeed(long bytes) {

        if (bytes > FileUtils.ONE_MB) {

            double bytesDouble = (double) bytes / (double) FileUtils.ONE_MB;

            double newDouble = new BigDecimal(bytesDouble).setScale(2, RoundingMode.FLOOR).doubleValue();

            return String.format("%s / s", newDouble + " MB");

        } else {
            return String.format("%s / s", FileUtils.byteCountToDisplaySize(bytes));
        }

    }

    private void fireProgressBar(final UploadItem item, int columnNumber) {

        final int row = list.indexOf(item);

        if (row >= 0) {

            fireTableCellUpdated(row, columnNumber);

            final Optional<Timer> itemTimer = timerMap.get(item);

            if (itemTimer == null) {
                return;
            }

            if (!itemTimer.isPresent()) {

                final Optional<Timer> timer = Optional.of(new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (row >= 0 && row < list.size()) {
                            updateSpeed(item);

                            fireTableCellUpdated(row, speedColumn);
                        }

                        timerMap.put(item, Optional.<Timer>absent());

                    }
                }));

                timerMap.put(item, timer);

                timer.get().setRepeats(false);
                timer.get().start();
            }

        }

    }

    private void updateSpeed(UploadItem item) {

        final ItemValuesInfo valuesInfo = valuesInfoMap.get(item);

        final long oldUploadedValue = valuesInfo.lastUploadedValue;
        final long newUploadedValue = item.getUploadedValue();
        final long oldTime = valuesInfo.lastTimeInMillis;
        final long newTime = System.currentTimeMillis();

        if (oldTime != newTime) {

            final long timeDiff = newTime - oldTime;

            long speed = (newUploadedValue - oldUploadedValue) * 1000 / timeDiff;

            valuesInfo.lastSpeed = (valuesInfo.lastSpeed + speed) / 2;

        }

        valuesInfo.lastTimeInMillis = newTime;
        valuesInfo.lastUploadedValue = newUploadedValue;

        if (valuesInfo.lastSpeed < 0) {
            valuesInfo.lastSpeed = 0;
        }

    }

    private void updateTimersMap() {

        timerMap.clear();

        for (UploadItem item : list) {

            timerMap.put(item, Optional.<Timer>absent());

        }

    }

    private void removeTimer(UploadItem item) {

        final Optional<Timer> remove = timerMap.remove(item);

        if (remove != null && remove.isPresent()) {
            remove.get().stop();
        }

    }

    private void updateValuesMap() {

        valuesInfoMap.clear();

        for (UploadItem item : list) {

            final ItemValuesInfo valuesInfo = new ItemValuesInfo(
                System.currentTimeMillis(),
                item.getUploadedValue()
            );

            valuesInfoMap.put(item, valuesInfo);

        }

    }

    public void notify(NotificationType type, UploadItem item) {

        switch (type) {

            case ADD_ITEM:

                updateItem(item);
                fireTableDataChanged();
                updateTimersMap();
                updateValuesMap();

                break;

            case REMOVE_ITEM:

                list.remove(item);
                fireTableDataChanged();
                updateTimersMap();
                updateValuesMap();

                break;

            case SIZE_VALUE:

                updateItem(item);

                final int sizeValueRow = list.indexOf(item);

                fireTableCellUpdated(sizeValueRow, SIZE_COLUMN);

                break;

            case UPLOAD_VALUE:

                updateItem(item);
                fireProgressBar(item, uploadColumn);

                break;

            case ZIP_VALUE:

                updateItem(item);
                fireProgressBar(item, zipColumn);

                break;

            case STATUS_VALUE:

                updateItem(item);

                final int statusValueRow = list.indexOf(item);

                fireTableCellUpdated(statusValueRow, speedColumn);

                break;

            case CLEAR_ITEMS:

                notifyInit(new ArrayList<UploadItem>());

                break;

            default:

                throw new RuntimeException("Unrecognizable notification type: " + type);

        }

    }

    private void updateItem(UploadItem item) {

        final int row = list.indexOf(item);

        if (row >= 0) {

            list.set(row, item);

        } else {

            list.add(item);

        }

    }

    private static class ItemValuesInfo {

        private long lastTimeInMillis;
        private long lastUploadedValue;
        private long lastSpeed = 0;

        private ItemValuesInfo(long lastTimeInMillis, long lastUploadedValue) {
            this.lastTimeInMillis = lastTimeInMillis;
            this.lastUploadedValue = lastUploadedValue;
        }
    }

}
