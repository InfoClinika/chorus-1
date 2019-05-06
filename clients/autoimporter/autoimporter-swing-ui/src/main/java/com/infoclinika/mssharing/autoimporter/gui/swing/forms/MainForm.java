package com.infoclinika.mssharing.autoimporter.gui.swing.forms;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.autoimporter.gui.swing.api.Frame;
import com.infoclinika.mssharing.autoimporter.gui.swing.model.*;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.*;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService;
import com.infoclinika.mssharing.clients.common.web.api.WebService;
import com.infoclinika.mssharing.dto.VendorEnum;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.infoclinika.mssharing.autoimporter.gui.swing.util.FormUtils.*;
import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.*;
import static com.infoclinika.mssharing.autoimporter.messages.MessagesSource.getMessage;

@Component
@Lazy
public class MainForm extends JFrame implements Frame {
    private static final int ROW_MARGIN = 3;
    private static final int ROW_HEIGHT = 20;
    private static final int CONFIG_HEIGHT = 35;
    private JPanel mainPanel;
    private JList configList;
    private FancyButton addConfig;
    private FancyButton deleteConfig;
    private JButton logoutBtn;
    private JPanel logPanel;
    private FancyButton startOrStopBtn;
    private JLabel userNameLabel;
    private JPanel detailPanel;
    private JLabel instrumentLabel;
    private JLabel folderLabel;
    private JTextPane labelsTextPane;
    private JTable uploadTable;
    private JTable waitTable;
    private FancyButton copyButton;
    private JLabel totalConfigLabel;
    private JLabel totalStartedLabel;
    private JLabel currentConfigLabel;
    private JTable duplicateTable;
    private JLabel uploadCompleteActionLabel;
    private JLabel labelConfList;
    private JLabel labelCurrentConf;
    private JLabel labelTotalConf;
    private JLabel labelTotalStarted;
    private JLabel labelFolder;
    private JLabel labelInstrument;
    private JLabel labelLabels;
    private JLabel labelCompleteAction;
    private JLabel labelLoggedAs;
    private DefaultListModel configModel;
    private AtomicInteger startedCount = new AtomicInteger(0);
    private String actionNothing;
    private String actionDelete;
    private String actionMove;
    private String deleteConfText;
    private String thermoText;
    private String notEmptyText;
    private String logoutText;
    private String confirmTitle;
    private String warningTitle;

    @Inject
    private UploadServiceAdapter uploadService;

    @Inject
    private WebService webService;

    @Inject
    private FrameLazyFactory frameLazyFactory;

    @Inject
    private AppModel appModel;

    @Inject
    private WaitListNotificationListener waitListNotificationListener;

    @Inject
    private UploadListNotificationListener uploadListNotificationListener;

    @Inject
    private DuplicateNotificationListener duplicateNotificationListener;

    @PostConstruct
    public void init() {
        setTitle(getMessage(MAIN_TITLE));
        labelConfList.setText(getMessage(MAIN_LABEL_CONFIG_LIST));
        labelCurrentConf.setText(getMessage(MAIN_LABEL_CURRENT_CONFIG));
        labelTotalConf.setText(getMessage(MAIN_LABEL_TOTAL_CONFIGS));
        labelTotalStarted.setText(getMessage(MAIN_LABEL_TOTAL_STARTED));
        labelFolder.setText(getMessage(MAIN_LABEL_FOLDER));
        labelInstrument.setText(getMessage(MAIN_LABEL_INSTRUMENT));
        labelLabels.setText(getMessage(MAIN_LABEL_LABELS));
        labelCompleteAction.setText(getMessage(MAIN_LABEL_COMPLETE_ACTION));
        labelLoggedAs.setText(getMessage(MAIN_LABEL_LOGGED_IN));
        logoutBtn.setText(getMessage(MAIN_BUTTON_SIGN_OUT));

        actionNothing = getMessage(MAIN_LABEL_COMPLETE_ACTION_NOTHING);
        actionDelete = getMessage(MAIN_LABEL_COMPLETE_ACTION_DELETE_FILES);
        actionMove = getMessage(MAIN_LABEL_COMPLETE_ACTION_MOVE_FILES);
        deleteConfText = getMessage(MODALS_DELETE_CONFIG_TEXT);
        thermoText = getMessage(MODALS_THERMO_UNAVAILABLE_TEXT);
        notEmptyText = getMessage(MODALS_RUN_NOT_EMPTY_CONFIG_TEXT);
        logoutText = getMessage(MODALS_LOGOUT_TEXT);
        confirmTitle = getMessage(MODALS_CONFIRM_TITLE);
        warningTitle = getMessage(MODALS_WARNING_TITLE);

        setIconImages(FormUtils.APP_ICONS);

        addConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainForm.this.onAddConfig();
            }
        });
        deleteConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                MainForm.this.onDeleteConfig();
                MainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
        startOrStopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                MainForm.this.startOrStopFileUpload();
                MainForm.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(appModel.getFolder()), null);
            }
        });

        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainForm.this.onLogout();
            }
        });

        configList.setModel(configModel = new DefaultListModel());
        configList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    MainForm.this.onSelectionConfigList();
                }
            }
        );
        configList.setDragEnabled(true);
        configList.setCellRenderer(new IconCellRenderer());
        configList.setFixedCellHeight(CONFIG_HEIGHT);

        uploadTable.setRowMargin(ROW_MARGIN);
        uploadTable.setRowHeight(ROW_HEIGHT);
        waitTable.setRowMargin(ROW_MARGIN);
        waitTable.setRowHeight(ROW_HEIGHT);
        duplicateTable.setRowHeight(ROW_HEIGHT);
        duplicateTable.setRowHeight(ROW_HEIGHT);

        uploadTable.setAutoCreateRowSorter(true);
        waitTable.setAutoCreateRowSorter(true);
        duplicateTable.setAutoCreateRowSorter(true);

        setContentPane(mainPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
    }

    private void onSelectionConfigList() {

        final int selectedIndex = configList.getSelectedIndex();

        if (selectedIndex < 0) {

            deleteConfig.setEnabled(false);
            startOrStopBtn.setEnabled(false);

        } else {

            onListValueChange((ContextWrapper) configList.getSelectedValue());

            startedToggle();

        }

    }

    private void startOrStopFileUpload() {
        if (appModel.isStarted()) {
            onStopWatch();
        } else {
            onStartWatch();
        }
    }

    @Override
    public void open() {
        setToScreenCenter(this);

        userNameLabel.setText(uploadService.getUserName());

        reloadConfigList();

        final boolean isSelected = appModel.getContextInfo().isPresent();
        displayListSelected(isSelected);
        deleteConfig.setEnabled(isSelected);
        currentConfigLabel.setText(isSelected ? appModel.getName() : "");

        if (isSelected) {
            currentConfigLabel.setText(appModel.getName());
        } else {
            currentConfigLabel.setText("");
        }

        setVisible(true);
    }

    private void reloadConfigList() {
        configModel.clear();
        for (ContextInfo info : uploadService.getContexts()) {
            configModel.addElement(new ContextWrapper(info));
            if (info.isStarted()) {
                startedCount.incrementAndGet();
            }
        }
        totalConfigLabel.setText(String.valueOf(configModel.size()));
        totalStartedLabel.setText(String.valueOf(startedCount));
    }

    @Override
    public void close() {
        setVisible(false);
        dispose();
    }

    @Override
    public void clear() {
        appModel.setContextInfo(null);
    }

    private void onListValueChange(ContextWrapper model) {

        appModel.setContextInfo(model.info);

        final String folder = appModel.getFolder();

        folderLabel.setText(folder);
        instrumentLabel.setText(appModel.getContextInfo().get().getInstrument().getName());
        labelsTextPane.setText(appModel.getLabels());
        currentConfigLabel.setText(appModel.getName());

        final String completeActionText = getCompleteActionText(model.info);

        uploadCompleteActionLabel.setText(completeActionText);

        reloadLogTable(folder);
    }

    private String getCompleteActionText(ContextInfo contextInfo) {

        final UploadConfigurationService.CompleteAction completeAction = contextInfo.getCompleteAction();

        switch (completeAction) {

            case NOTHING:
                return actionNothing;

            case DELETE_FILE:
                return actionDelete;

            case MOVE_FILE:
                return actionMove + " " + contextInfo.getFolderToMoveFiles();

            default:
                throw new IllegalArgumentException("Unrecognizable complete action: " + completeAction);

        }

    }

    private void reloadLogTable(String folder) {
        final WaitModel waitModel = new WaitModel();

        waitModel.notifyInit(uploadService.getWaitItem(folder));
        waitTable.setModel(waitModel);
        waitListNotificationListener.init(waitModel);
        getColumn(waitTable, WaitModel.SIZE_COLUMN).setMaxWidth(WaitModel.SIZE_WIDTH);

        final Optional<ContextInfo> contextInfo = appModel.getContextInfo();
        final boolean archive =
            contextInfo.isPresent() && webService.isArchivingRequired(contextInfo.get().getInstrument());
        final UploadModel uploadModel = new UploadModel(archive);

        final List<UploadItem> items = uploadService.getUploadItem(folder);
        Collections.reverse(items);
        uploadModel.notifyInit(items);
        uploadTable.setModel(uploadModel);
        uploadListNotificationListener.init(uploadModel);

        final DuplicateModel duplicateModel = new DuplicateModel();

        duplicateModel.notifyInit(uploadService.getDuplicateItems(folder));
        duplicateTable.setModel(duplicateModel);
        duplicateNotificationListener.init(duplicateModel);
        getColumn(duplicateTable, DuplicateModel.SIZE_COLUMN).setMaxWidth(DuplicateModel.SIZE_WIDTH);

        getColumn(uploadTable, WaitModel.SIZE_COLUMN).setMaxWidth(WaitModel.SIZE_WIDTH);
        getColumn(uploadTable, uploadModel.uploadColumn).setMaxWidth(UploadModel.PROGRESS_WIDTH);
        getColumn(uploadTable, uploadModel.speedColumn).setMaxWidth(UploadModel.SPEED_WIDTH);
        getColumn(uploadTable, uploadModel.speedColumn).setWidth(UploadModel.SPEED_WIDTH);
        getColumn(uploadTable, uploadModel.speedColumn).setPreferredWidth(UploadModel.SPEED_WIDTH);

        if (archive) {
            getColumn(uploadTable, uploadModel.zipColumn).setCellRenderer(new ProgressTableCellRenderer());
            getColumn(uploadTable, uploadModel.zipColumn).setMaxWidth(UploadModel.PROGRESS_WIDTH);
        }

        getColumn(uploadTable, uploadModel.uploadColumn).setCellRenderer(new ProgressTableCellRenderer());
    }

    private TableColumn getColumn(JTable table, int column) {
        return table.getColumnModel().getColumn(column);
    }

    private void startedToggle() {

        if (appModel.getContextInfo() != null) {

            startedToggle(appModel.isStarted());

        } else {

            startedToggle(false);

        }
    }

    private void startedToggle(boolean started) {

        displayListSelected(true);
        logPanel.setVisible(started);

        deleteConfig.setEnabled(!started);

        startOrStopBtn.setEnabled(true);
        startOrStopBtn.setIcon(started ? STOP : START);
        startOrStopBtn.setToolTipText(started ?
            getMessage(MAIN_TOOLTIP_BUTTON_STOP_CONFIG) :
            getMessage(MAIN_TOOLTIP_BUTTON_RUN_CONFIG)
        );
        startOrStopBtn.setRolloverIcon(started ? STOP_HOVER : START_HOVER);
        startOrStopBtn.setPressedIcon(started ? STOP_PRESSED : START_PRESSED);
    }

    private void displayListSelected(boolean selected) {
        detailPanel.setVisible(selected);
        startOrStopBtn.setEnabled(selected);
    }

    private void onAddConfig() {
        frameLazyFactory.getAddConfigDialog().open();
    }

    public void addContext(ContextInfo info) {
        configModel = (DefaultListModel) configList.getModel();
        configList.clearSelection();
        configModel.addElement(new ContextWrapper(info));
        totalConfigLabel.setText(String.valueOf(configModel.size()));
    }

    private void onDeleteConfig() {

        final int confirm =
            JOptionPane.showConfirmDialog(
                this,
                deleteConfText + " " + appModel.getName(),
                confirmTitle,
                JOptionPane.YES_NO_OPTION
            );

        if (confirm == JOptionPane.YES_OPTION) {
            final String folder = appModel.getFolder();
            removeContext(folder);
        }
    }

    private void removeContext(String folder) {

        final boolean result = uploadService.removeContext(folder);

        if (result) {

            configModel = (DefaultListModel) configList.getModel();
            configModel.removeElement(configList.getSelectedValue());
            configList.clearSelection();
            appModel.setContextInfo(null);
            currentConfigLabel.setText("");
            totalConfigLabel.setText(String.valueOf(configModel.size()));
            displayListSelected(false);

        }

    }

    private void onStartWatch() {

        if (isThermoFileCheckingEnabled() && !uploadService.isThermoFileCheckingAvailable()) {
            JOptionPane.showMessageDialog(
                this,
                thermoText,
                warningTitle,
                JOptionPane.WARNING_MESSAGE
            );
        }

        final String folderName = appModel.getFolder();

        if (!checkFileExists(this, folderName)) {
            return;
        }

        final int size = uploadService.getFolderListLength(folderName);

        if (size != 0) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                notEmptyText + "(" + size + ")",
                confirmTitle,
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.NO_OPTION) {
                return;
            }
        }
        startWatch();
    }

    private boolean isThermoFileCheckingEnabled() {
        final com.google.common.base.Optional<ContextInfo> contextInfo = appModel.getContextInfo();
        if (contextInfo.isPresent()) {
            final VendorEnum vendorEnum = VendorEnum.getVendorEnum(contextInfo.get().getInstrument().getVendor().name);

            return vendorEnum == VendorEnum.THERMO && uploadService.isThermoFileCheckingEnabled();
        }

        return false;
    }

    private void startWatch() {
        startOrStopBtn.setEnabled(false);

        final boolean result = uploadService.startWatch(appModel.getContextInfo().get());

        if (result) {

            startedToggle(true);
            appModel.getContextInfo().get().onStarted();
            configList.updateUI();
            totalStartedLabel.setText(String.valueOf(startedCount.incrementAndGet()));

        }

    }

    private void onStopWatch() {
        startOrStopBtn.setEnabled(false);

        final boolean result = uploadService.stopWatch(appModel.getFolder());

        if (result) {

            startedToggle(false);
            appModel.getContextInfo().get().onStopped();
            configList.updateUI();
            totalStartedLabel.setText(String.valueOf(startedCount.decrementAndGet()));

        }

    }

    private void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            logoutText,
            confirmTitle,
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            close();
            uploadService.clearConfig();
            final LoginForm loginForm = frameLazyFactory.getLoginForm();
            loginForm.clear();
            loginForm.open();
        }
    }

    private void createUIComponents() {
        addConfig =
            new FancyButton(ADD, ADD_PRESSED, ADD_HOVER, getMessage(MAIN_TOOLTIP_BUTTON_ADD_CONFIG));
        deleteConfig =
            new FancyButton(DELETE, DELETE_PRESSED, DELETE_HOVER, getMessage(MAIN_TOOLTIP_BUTTON_REMOVE_CONFIG));
        startOrStopBtn =
            new FancyButton(START, START_PRESSED, START_HOVER, getMessage(MAIN_TOOLTIP_BUTTON_RUN_CONFIG));
        copyButton = new FancyButton(
                CLIPBOARD, CLIPBOARD_PRESSED, CLIPBOARD_HOVER, getMessage(MAIN_TOOLTIP_BUTTON_COPY_TO_CLIPBOARD)
        );
    }

    public class ContextWrapper {
        public final ContextInfo info;

        public ContextWrapper(ContextInfo info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return info.getName();
        }
    }
}
