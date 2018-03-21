package com.infoclinika.mssharing.wizard.upload.gui.swing.forms.steps;

import com.infoclinika.mssharing.wizard.upload.gui.swing.controller.steps.SecondStepController;
import com.infoclinika.mssharing.wizard.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.wizard.upload.gui.swing.model.ViewTableModel;
import com.infoclinika.mssharing.wizard.upload.gui.swing.util.InstrumentFileFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.wizard.messages.MessageKey.*;
import static com.infoclinika.mssharing.wizard.messages.MessagesSource.getMessage;
import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

@Component
public class SecondStep extends JPanel {
    private JPanel contentPanel;
    private JButton browseButton;
    private JTable filesTable;
    private JButton removeButton;
    private JScrollPane filesTableScroll;
    private JLabel labelNote;

    private final JFileChooser fileChooser = new JFileChooser();

    @Inject
    private SecondStepController controller;

    @PostConstruct
    private void postConstruct(){

        labelNote.setText(getMessage(TWO_LABEL_NOTE));
        browseButton.setText(getMessage(TWO_BUTTON_BROWSE));
        removeButton.setText(getMessage(TWO_BUTTON_REMOVE));

        filesTable.setRowMargin(UiProperties.ROW_MARGIN);
        filesTable.setRowHeight(UiProperties.ROW_HEIGHT);
        filesTable.getColumnModel().setColumnMargin(UiProperties.COLUMN_MARGIN);
        filesTable.setAutoCreateRowSorter(true);
        filesTable.setDragEnabled(true);
        filesTable.setFillsViewportHeight(true);

        final DropTarget dropTarget = controller.createDropTarget();

        filesTableScroll.setDropTarget(dropTarget);
        filesTable.setDropTarget(dropTarget);

        filesTable.getSelectionModel().setSelectionMode(MULTIPLE_INTERVAL_SELECTION);

        final ViewTableModel tableModel = (ViewTableModel) controller.createTableModel();

        filesTable.setModel(tableModel);

        filesTable.getColumnModel().getColumn(tableModel.SIZE_COLUMN).setMaxWidth(UiProperties.SIZE_COLUMN_WIDTH);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onBrowse();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveItem();
            }
        });
        removeButton.setEnabled(false);


        filesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                onSelectionChanged();
            }
        });

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(false);

        controller.setView(contentPanel);

        controller.setSecondStep(this);

    }

    public void setFileFilter(InstrumentFileFilter fileFilter){

        fileChooser.setFileFilter(fileFilter);

    }

    private void onBrowse(){

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

            final File[] selectedFiles = fileChooser.getSelectedFiles();

            controller.dropFiles(Arrays.asList(selectedFiles));

        }

    }

    private void onRemoveItem(){

        final int[] selectedRows = filesTable.getSelectedRows();

        if(selectedRows.length != 0){

            final List<Integer> toBeRemovedIndexes = newArrayList();

            for(int selectedRow : selectedRows){

                final int modelIndex = filesTable.convertRowIndexToModel(selectedRow);

                final int rowCount = filesTable.getModel().getRowCount();

                if(modelIndex < 0 || modelIndex >= rowCount){
                    continue;
                }

                toBeRemovedIndexes.add(modelIndex);

            }

            if(toBeRemovedIndexes.size() > 0){

                controller.removeItems(toBeRemovedIndexes);

                controller.filesChanged();

            }

        }

        removeButton.setEnabled(false);

    }

    private void onSelectionChanged(){

        final boolean enabled = filesTable.getSelectedRowCount() > 0;

        removeButton.setEnabled(enabled);
    }

}
