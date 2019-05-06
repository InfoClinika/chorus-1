package com.infoclinika.mssharing.desktop.upload.gui.swing.util;

import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.UiProperties;
import com.infoclinika.mssharing.desktop.upload.gui.swing.forms.component.IndexCellRenderer;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * @author timofey.kasyanov
 *     date: 08.04.14.
 */
public class FormUtils {

    public static void setToScreenCenter(Component component) {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        final Dimension screenSize = toolkit.getScreenSize();
        final Dimension componentSize = component.getSize();
        component.setBounds(
            screenSize.width / 2 - componentSize.width / 2,
            screenSize.height / 2 - componentSize.height / 2,
            component.getWidth(),
            component.getHeight()
        );
    }

    public static void adjustColumnSizes(JTable filesTable, java.util.List<RowSorter.SortKey> sortKeys) {
        TableColumn firstColumn = filesTable.getColumnModel().getColumn(0);
        firstColumn.setMinWidth(UiProperties.SIZE_INDEX_COLUMN_WIDTH);
        firstColumn.setMaxWidth(UiProperties.SIZE_INDEX_COLUMN_WIDTH * 2);

        TableColumn lastColumn = filesTable.getColumnModel().getColumn(filesTable.getColumnCount() - 1);
        lastColumn.setMinWidth(UiProperties.SIZE_COLUMN_WIDTH);
        lastColumn.setMaxWidth(UiProperties.SIZE_COLUMN_WIDTH * 2);

        filesTable.getColumnModel().getColumn(0).setCellRenderer(new IndexCellRenderer());

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(filesTable.getModel());
        filesTable.setRowSorter(sorter);

        sorter.setSortKeys(sortKeys);
        sorter.setSortable(0, false);
    }

    public static Component getTableCellComponent(JComboBox comboBox,
                                                  JPanel panel,
                                                  java.util.List<DictionaryWrapper> species,
                                                  JTable table, Object value, boolean isSelected) {

        final Color color = isSelected ? table.getSelectionBackground() : table.getBackground();
        panel.setBackground(color);
        final DictionaryDTO specie = (DictionaryDTO) value;

        for (DictionaryWrapper wrapper : species) {
            if (wrapper.getDictionary().getName().equals(specie.getName())) {
                comboBox.setSelectedItem(wrapper);
                break;
            }
        }
        return panel;
    }

}
