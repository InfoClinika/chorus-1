package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.steps;

import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps.FourthStepController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.IndexCellRenderer;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.ProgressBarTableCellRenderer;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.UploadZipTableModel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

@Component
public class FourthStep extends JPanel {
    private JPanel contentPanel;
    private JTable uploadsTable;

    @Inject
    private FourthStepController controller;

    @PostConstruct
    private void postConstruct() {

        uploadsTable.setRowMargin(UiProperties.ROW_MARGIN);
        uploadsTable.setRowHeight(UiProperties.ROW_HEIGHT);
        uploadsTable.getColumnModel().setColumnMargin(UiProperties.COLUMN_MARGIN);
        uploadsTable.setFillsViewportHeight(true);

        uploadsTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);

        controller.setView(contentPanel);

        controller.setFourthStep(this);

    }

    public void setTableModel(TableModel tableModel) {

        uploadsTable.setModel(tableModel);

        final TableColumnModel columnModel = uploadsTable.getColumnModel();

        final int columnCount = columnModel.getColumnCount();

        final UploadZipTableModel model = (UploadZipTableModel) tableModel;

        if (model.zipColumn < columnCount) {
            columnModel.getColumn(model.zipColumn).setCellRenderer(new ProgressBarTableCellRenderer());
            columnModel.getColumn(model.zipColumn).setMaxWidth(UiProperties.PROGRESS_COLUMN_WIDTH);
        }

        columnModel.getColumn(model.uploadColumn).setCellRenderer(new ProgressBarTableCellRenderer());
        columnModel.getColumn(model.uploadColumn).setMaxWidth(UiProperties.PROGRESS_COLUMN_WIDTH);
        columnModel.getColumn(model.speedColumn).setMaxWidth(UiProperties.SPEED_COLUMN_WIDTH);
        columnModel.getColumn(model.SIZE_COLUMN).setMaxWidth(UiProperties.SIZE_COLUMN_WIDTH);
    }

    public TableModel getTableModel() {

        return uploadsTable.getModel();

    }

    public void adjustColumnSizes() {
        TableColumn firstColumn = uploadsTable.getColumnModel().getColumn(0);
        firstColumn.setMinWidth(UiProperties.SIZE_INDEX_COLUMN_WIDTH);
        firstColumn.setMaxWidth(UiProperties.SIZE_INDEX_COLUMN_WIDTH * 2);

        TableColumn lastColumn = uploadsTable.getColumnModel().getColumn(uploadsTable.getColumnCount() - 1);
        lastColumn.setMinWidth(UiProperties.SIZE_COLUMN_WIDTH);
        lastColumn.setMaxWidth(UiProperties.SIZE_COLUMN_WIDTH * 2);

        uploadsTable.getColumnModel().getColumn(0).setCellRenderer(new IndexCellRenderer());

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(uploadsTable.getModel());
        uploadsTable.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>(2);
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(3, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.setSortable(0, false);
    }
}
