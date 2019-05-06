package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.steps;

import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps.SecondStepController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.desktop.upload.gui.swing.model.ViewTableModel;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.FormUtils;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.InstrumentFileFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.desktop.messages.MessageKey.*;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;
import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;

@Component
public class SecondStep extends JPanel {
    @Inject
    private SecondStepController controller;

    private final Set<String> extensions = new HashSet<>();
    private final Pattern extensionPattern = Pattern.compile(".*\\..+");
    private final ImageIcon removeIcon =
        new ImageIcon(ClassLoader.getSystemClassLoader().getResource("images/item-delete.png"));
    private JFileChooser fileChooser = new JFileChooser();

    private JPanel contentPanel;
    private JButton browseButton;
    private JTable filesTable;
    private JButton removeButton;
    private JScrollPane filesTableScroll;
    private JLabel labelNote;
    private JPanel extensionPanel;
    private JTextField extensionTextField;
    private JButton addExtensionButton;
    private JScrollPane extensionScrollPane;
    private JPanel extensionListPanel;

    @PostConstruct
    private void postConstruct() {

        initExtensionPanel();
        labelNote.setText(getMessage(TWO_LABEL_NOTE));
        browseButton.setText(getMessage(TWO_BUTTON_BROWSE));
        removeButton.setText(getMessage(TWO_BUTTON_REMOVE));

        filesTable.setRowMargin(UiProperties.ROW_MARGIN);
        filesTable.setRowHeight(UiProperties.ROW_HEIGHT);
        filesTable.getColumnModel().setColumnMargin(UiProperties.COLUMN_MARGIN);
        filesTable.setDragEnabled(true);
        filesTable.setFillsViewportHeight(true);

        final DropTarget dropTarget = controller.createDropTarget();

        filesTableScroll.setDropTarget(dropTarget);
        filesTable.setDropTarget(dropTarget);

        filesTable.getSelectionModel().setSelectionMode(MULTIPLE_INTERVAL_SELECTION);


        final ViewTableModel tableModel = (ViewTableModel) controller.createTableModel();
        filesTable.setModel(tableModel);


        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SecondStep.this.onBrowse();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SecondStep.this.onRemoveItem();
            }
        });
        removeButton.setEnabled(false);


        filesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                SecondStep.this.onSelectionChanged();
            }
        });

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(false);

        controller.setView(contentPanel);

        controller.setSecondStep(this);
    }

    public void initExtensionButtons(List<String> extensions) {
        this.extensions.clear();
        extensionListPanel.removeAll();
        for (String extension : extensions) {
            addExtension(extension);
        }

        extensionTextField.setText(getMessage(EXTENSION_INPUT_TOOLTIP_TEXT));
        extensionTextField.setForeground(Color.lightGray);
    }

    public void setFileFilter(InstrumentFileFilter fileFilter) {
        fileChooser.setFileFilter(fileFilter);
    }

    public void adjustColumnSizes() {
        List<RowSorter.SortKey> sortKeys = new ArrayList<>(2);
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        FormUtils.adjustColumnSizes(filesTable, sortKeys);
    }

    private void initExtensionPanel() {
        extensionTextField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                extensionTextField.setForeground(Color.black);
                if (extensionTextField.getText().equals(getMessage(EXTENSION_INPUT_TOOLTIP_TEXT))) {
                    extensionTextField.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (extensionTextField.getText().isEmpty() ||
                    extensionTextField.getText().equals(getMessage(EXTENSION_INPUT_TOOLTIP_TEXT))) {
                    extensionTextField.setText(getMessage(EXTENSION_INPUT_TOOLTIP_TEXT));
                    extensionTextField.setForeground(Color.lightGray);
                }
            }
        });

        extensionTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            private void validateInput() {
                String extension = extensionTextField.getText().toLowerCase();
                addExtensionButton.setEnabled(!extensionTextField.getText().isEmpty()
                    && !extensionTextField.getText().equals(getMessage(EXTENSION_INPUT_TOOLTIP_TEXT))
                    && !extensions.contains(extension)
                    && extensionPattern.matcher(extension).matches());
            }
        });

        addExtensionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SecondStep.this.addExtension(extensionTextField.getText().toLowerCase());
            }
        });
    }

    private void addExtension(String extension) {
        String normalized = extension.toLowerCase();
        this.extensions.add(normalized);
        browseButton.setEnabled(!extensions.isEmpty());
        controller.applyFileFilterExtension(new ArrayList<>(extensions));

        extensionTextField.setText("");

        JButton extensionButton = new JButton(normalized, removeIcon);
        extensionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extensions.remove(extensionButton.getText());
                browseButton.setEnabled(!extensions.isEmpty());
                controller.applyFileFilterExtension(new ArrayList<>(extensions));

                Container parent = extensionButton.getParent();
                parent.remove(extensionButton);
                parent.validate();
                parent.repaint();
            }
        });
        extensionListPanel.add(extensionButton);
        extensionListPanel.validate();
        extensionListPanel.repaint();
        extensionScrollPane.validate();
        extensionScrollPane.repaint();
    }

    private void onBrowse() {
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            final File[] selectedFiles = fileChooser.getSelectedFiles();
            controller.dropFiles(Arrays.asList(selectedFiles));
        }
    }

    private void onRemoveItem() {

        final int[] selectedRows = filesTable.getSelectedRows();

        if (selectedRows.length != 0) {

            final List<Integer> toBeRemovedIndexes = newArrayList();

            for (int selectedRow : selectedRows) {

                final int modelIndex = filesTable.convertRowIndexToModel(selectedRow);

                final int rowCount = filesTable.getModel().getRowCount();

                if (modelIndex < 0 || modelIndex >= rowCount) {
                    continue;
                }

                toBeRemovedIndexes.add(modelIndex);

            }

            if (toBeRemovedIndexes.size() > 0) {

                controller.removeItems(toBeRemovedIndexes);

                controller.filesChanged();

            }

        }

        removeButton.setEnabled(false);

    }

    private void onSelectionChanged() {

        final boolean enabled = filesTable.getSelectedRowCount() > 0;

        removeButton.setEnabled(enabled);
    }
}

