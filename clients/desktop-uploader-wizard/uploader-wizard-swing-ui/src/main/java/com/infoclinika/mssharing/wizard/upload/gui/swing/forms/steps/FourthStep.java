package com.infoclinika.mssharing.wizard.upload.gui.swing.forms.steps;

import com.infoclinika.mssharing.wizard.upload.gui.swing.controller.steps.FourthStepController;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.component.ProgressBarTableCellRenderer;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.UploadZipTableModel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

@Component
public class FourthStep extends JPanel {
    private JPanel contentPanel;
    private JTable uploadsTable;

    @Inject
    private FourthStepController controller;

    @PostConstruct
    private void postConstruct(){

        uploadsTable.setRowMargin(UiProperties.ROW_MARGIN);
        uploadsTable.setRowHeight(UiProperties.ROW_HEIGHT);
        uploadsTable.getColumnModel().setColumnMargin(UiProperties.COLUMN_MARGIN);
        uploadsTable.setAutoCreateRowSorter(true);
        uploadsTable.setFillsViewportHeight(true);

        uploadsTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);

        controller.setView(contentPanel);

        controller.setFourthStep(this);

    }

    public void setTableModel(TableModel tableModel){

        uploadsTable.setModel(tableModel);

        final TableColumnModel columnModel = uploadsTable.getColumnModel();

        final int columnCount = columnModel.getColumnCount();

        final UploadZipTableModel model = (UploadZipTableModel) tableModel;

        if(model.ZIP_COLUMN < columnCount){
            columnModel.getColumn(model.ZIP_COLUMN).setCellRenderer(new ProgressBarTableCellRenderer());
            columnModel.getColumn(model.ZIP_COLUMN).setMaxWidth(UiProperties.PROGRESS_COLUMN_WIDTH);
        }

        columnModel.getColumn(model.UPLOAD_COLUMN).setCellRenderer(new ProgressBarTableCellRenderer());
        columnModel.getColumn(model.UPLOAD_COLUMN).setMaxWidth(UiProperties.PROGRESS_COLUMN_WIDTH);
        columnModel.getColumn(model.SPEED_COLUMN).setMaxWidth(UiProperties.SPEED_COLUMN_WIDTH);
        columnModel.getColumn(model.SIZE_COLUMN).setMaxWidth(UiProperties.SIZE_COLUMN_WIDTH);


    }

    public TableModel getTableModel(){

        return uploadsTable.getModel();

    }

}
