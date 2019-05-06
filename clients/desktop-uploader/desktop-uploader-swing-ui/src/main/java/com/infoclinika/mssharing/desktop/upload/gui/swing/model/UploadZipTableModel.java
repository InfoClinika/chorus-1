package com.infoclinika.mssharing.desktop.upload.gui.swing.model;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.ListChangeType;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.UploadFinishedListener;
import com.infoclinika.mssharing.desktop.upload.model.UploadFileItem;
import com.infoclinika.mssharing.desktop.upload.model.UploadStatus;
import com.infoclinika.mssharing.desktop.upload.service.api.list.ListListener;
import com.infoclinika.mssharing.desktop.upload.service.api.list.ObservableList;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.*;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

/**
 * @author timofey.kasyanov
 *     date:   29.01.14
 */
public class UploadZipTableModel extends AbstractTableModel implements ListListener<UploadFileItem> {
    public static final int SIZE_COLUMN = 2;
    private static final int NAME_COLUMN = 1;
    private static final int INDEX_COLUMN_ID = 0;
    private static final int ONE_HUNDRED = 100;
    private static final Predicate<UploadFileItem> UPLOAD_FILE_ITEM_FINISHED =
        new Predicate<UploadFileItem>() {
            @Override
            public boolean test(UploadFileItem input) {
                return input.getStatus() == UploadStatus.ERROR
                    || input.getStatus() == UploadStatus.UPLOAD_COMPLETE
                    || input.getStatus() == UploadStatus.DUPLICATE
                    || input.getStatus() == UploadStatus.UPLOAD_UNAVAILABLE;
            }
        };

    public int zipColumn;
    public int uploadColumn;
    public int speedColumn;

    private final ObservableList<UploadFileItem> list;
    private final String[] columnNames;
    private final Map<UploadFileItem, ItemValuesInfo> valuesInfoMap = newHashMap();
    private final Map<UploadFileItem, Optional<Timer>> timerMap = newHashMap();

    private UploadFinishedListener uploadFinishedListener;

    private String statusWait;
    private String statusDone;
    private String statusError;
    private String statusZip;
    private String statusDuplicate;
    private String statusUnavailable;
    private String statusFinish;

    public UploadZipTableModel(ObservableList<UploadFileItem> list, boolean isArchive) {

        checkNotNull(list);
        this.list = list;
        list.getObserver().addListener(this);

        if (isArchive) {
            zipColumn = 3;
            uploadColumn = 4;
            speedColumn = 5;

            columnNames = new String[] {"#", getMessage(TABLE_COLUMN_NAME), getMessage(TABLE_COLUMN_SIZE),
                getMessage(TABLE_COLUMN_ZIP), getMessage(TABLE_COLUMN_UPLOAD), getMessage(TABLE_COLUMN_SPEED)};
        } else {
            uploadColumn = 3;
            speedColumn = 4;
            zipColumn = 5;

            columnNames = new String[] {"#", getMessage(TABLE_COLUMN_NAME), getMessage(TABLE_COLUMN_SIZE),
                getMessage(TABLE_COLUMN_UPLOAD), getMessage(TABLE_COLUMN_SPEED)};
        }

        statusWait = getMessage(ITEM_STATUS_WAIT);
        statusDone = getMessage(ITEM_STATUS_DONE);
        statusError = getMessage(ITEM_STATUS_ERROR);
        statusZip = getMessage(ITEM_STATUS_ZIP);
        statusDuplicate = getMessage(ITEM_STATUS_DUPLICATE);
        statusUnavailable = getMessage(ITEM_STATUS_UNAVAILABLE);
        statusFinish = getMessage(ITEM_STATUS_FINISH);
    }

    public void setUploadFinishedListener(UploadFinishedListener uploadFinishedListener) {
        this.uploadFinishedListener = uploadFinishedListener;
    }

    @Override
    public void onAdd(UploadFileItem item) {

        //onAdd is invoked when add all items at once
        updateTimersMap();

        updateValuesMap();

        fireTableDataChanged();
    }

    @Override
    public void onRemove(UploadFileItem item) {

        updateTimersMap();

        updateValuesMap();

        fireTableDataChanged();
    }

    @Override
    public void onChange(UploadFileItem item, Object params) {

        final ListChangeType changeType = (ListChangeType) params;

        checkNotNull(changeType, "Unsupported change params");

        if (changeType == ListChangeType.ZIP_CHANGE || changeType == ListChangeType.ZIP_ERROR_CHANGE) {

            fireProgressBar(item, zipColumn);

            if (changeType == ListChangeType.ZIP_ERROR_CHANGE) {
                checkUploadFinished();
            }

        } else if (changeType == ListChangeType.UPLOAD_CHANGE || changeType == ListChangeType.UPLOAD_ERROR_CHANGE) {

            fireProgressBar(item, uploadColumn);

            if (changeType == ListChangeType.UPLOAD_ERROR_CHANGE) {
                checkUploadFinished();
            }

        } else if (changeType == ListChangeType.FILE_SIZE_CHANGE) {

            final int row = list.indexOf(item);

            if (row >= 0) {

                fireTableCellUpdated(row, SIZE_COLUMN);

            }

        } else if (changeType == ListChangeType.UPLOAD_COMPLETE_CHANGE) {

            checkUploadFinished();

        }

    }

    private void checkUploadFinished() {

        if (uploadFinishedListener != null) {

            if (list.size() > 0 && list.stream().allMatch(UPLOAD_FILE_ITEM_FINISHED)) {

                uploadFinishedListener.uploadFinished();

            }

        }

    }

    @Override
    public void onClear() {

        updateTimersMap();

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
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == zipColumn || columnIndex == uploadColumn) {
            return Integer.class;
        } else {
            return String.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        final UploadFileItem item = list.get(rowIndex);
        if (columnIndex == INDEX_COLUMN_ID) {
            return new Integer(rowIndex + 1);
        } else if (columnIndex == NAME_COLUMN) {
            return item.getName();
        } else if (columnIndex == SIZE_COLUMN) {
            return getSizeString(item);
        } else if (columnIndex == uploadColumn) {
            return toPercent(item.getUploadRatio());
        } else if (columnIndex == INDEX_COLUMN_ID) {
            return toPercent(item.getUploadRatio());
        } else if (columnIndex == speedColumn) {
            return getSpeedString(item);
        } else if (columnIndex == zipColumn) {
            return toPercent(item.getZipRatio());
        } else {
            throw new RuntimeException("Invalid column index: " + columnIndex);
        }
    }

    private String getSizeString(UploadFileItem item) {

        return FileUtils.byteCountToDisplaySize(item.getFileSize());

    }

    private String getSpeedString(UploadFileItem item) {

        if (item.getStatus() == UploadStatus.WAITING || item.getStatus() == UploadStatus.ZIP_COMPLETE) {
            return statusWait;
        } else if (item.getStatus() == UploadStatus.UPLOAD_COMPLETE) {
            return statusDone;
        } else if (item.getStatus() == UploadStatus.ERROR) {
            return statusError;
        } else if (item.getStatus() == UploadStatus.DUPLICATE) {
            return statusDuplicate;
        } else if (item.getStatus() == UploadStatus.UPLOAD_UNAVAILABLE) {
            return statusUnavailable;
        } else if (item.getStatus() == UploadStatus.ZIPPING) {
            return statusZip;
        } else if (item.getStatus() == UploadStatus.UPLOADING) {

            int percentage = toPercent(item.getUploadRatio());

            if (percentage == ONE_HUNDRED && item.getStatus() == UploadStatus.UPLOADING) {

                return statusFinish;

            }

            final ItemValuesInfo valuesInfo = valuesInfoMap.get(item);

            return formatSpeed(valuesInfo.lastSpeed);
        }

        throw new RuntimeException("Unrecognized upload status: " + item.getStatus());

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

    private int toPercent(double value) {
        return (int) Math.round(value * ONE_HUNDRED);
    }


    private void fireProgressBar(final UploadFileItem item, int columnNumber) {

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
                            UploadZipTableModel.this.updateSpeed(item);
                            UploadZipTableModel.this.fireTableCellUpdated(row, speedColumn);
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

    private void updateSpeed(UploadFileItem item) {

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

        for (UploadFileItem item : list) {

            timerMap.put(item, Optional.<Timer>absent());

        }

    }

    private void updateValuesMap() {

        valuesInfoMap.clear();

        for (UploadFileItem item : list) {

            final ItemValuesInfo valuesInfo = new ItemValuesInfo(
                System.currentTimeMillis(),
                item.getUploadedValue()
            );

            valuesInfoMap.put(item, valuesInfo);

        }

    }

    private static class ItemValuesInfo {
        private long lastTimeInMillis;
        private long lastUploadedValue;
        private long lastSpeed;

        private ItemValuesInfo(long lastTimeInMillis, long lastUploadedValue) {
            this.lastTimeInMillis = lastTimeInMillis;
            this.lastUploadedValue = lastUploadedValue;
        }
    }

}
