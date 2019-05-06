package com.infoclinika.mssharing.autoimporter.gui.swing.forms;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.infoclinika.mssharing.autoimporter.gui.swing.api.Frame;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.FormUtils;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.FrameLazyFactory;
import com.infoclinika.mssharing.autoimporter.gui.swing.util.UploadServiceAdapter;
import com.infoclinika.mssharing.autoimporter.model.bean.ContextInfo;
import com.infoclinika.mssharing.autoimporter.model.util.UploadTransformer;
import com.infoclinika.mssharing.clients.common.DialogUtil;
import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.clients.common.dto.InstrumentWrapper;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.infoclinika.mssharing.autoimporter.messages.MessageKey.*;
import static com.infoclinika.mssharing.autoimporter.messages.MessagesSource.getMessage;
import static com.infoclinika.mssharing.autoimporter.service.api.UploadConfigurationService.CompleteAction;
import static com.infoclinika.mssharing.clients.common.Transformers.TO_DICTIONARY_WRAPPER;
import static com.infoclinika.mssharing.clients.common.Transformers.TO_INSTRUMENT_WRAPPER;

@Component
@Lazy
public class ConfigDialog extends JDialog implements Frame {
    private static final DictionaryWrapper defaultTechnologyType = new DictionaryWrapper(
        new DictionaryDTO(-1, getMessage(CONFIG_COMBO_SELECT_ONE))
    );
    private final DictionaryWrapper defaultVendor = TO_DICTIONARY_WRAPPER.apply(
        new DictionaryDTO(-1, getMessage(CONFIG_COMBO_SELECT_ONE))
    );
    private final DictionaryWrapper defaultLab = TO_DICTIONARY_WRAPPER.apply(
        new DictionaryDTO(-1, getMessage(CONFIG_COMBO_SELECT_ONE))
    );
    private static final InstrumentWrapper defaultInstrument = TO_INSTRUMENT_WRAPPER.apply(
        new InstrumentDTO(
            -1,
            getMessage(CONFIG_COMBO_SELECT_ONE),
            null,
            -1,
            "",
            -1
        )
    );
    private static final Color ERROR_COLOR = Color.RED;
    private static final Border ERROR_BORDER = BorderFactory.createLineBorder(ERROR_COLOR);
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_BORDER_COLOR = SystemColor.windowBorder;
    private static final Border DEFAULT_BORDER = BorderFactory.createLineBorder(DEFAULT_BORDER_COLOR);

    private final JFileChooser folderChooser = new JFileChooser();
    private final JFileChooser moveFolderChooser = new JFileChooser();

    private JPanel contentPanel;

    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton folderChooseBtn;
    private JButton chooseFolderToMoveFiles;

    private JTextField folderField;
    private JTextField nameField;
    private JTextField folderToMoveFiles;

    private JTextArea labelsField;

    private JComboBox<DictionaryWrapper> technologyTypesCb;
    private JComboBox<DictionaryWrapper> vendorsCb;
    private JComboBox<DictionaryWrapper> labsCb;
    private JComboBox<InstrumentWrapper> instrumentCb;

    private JLabel labelFolder;
    private JLabel labelName;
    private JLabel labelTechnologyType;
    private JLabel labelVendor;
    private JLabel labelLab;
    private JLabel labelInstrument;
    private JLabel labelLabels;
    private JLabel labelFolderToMoveFiles;
    private JLabel labelCompleteAction;

    private JRadioButton doNothingRadioButton;
    private JRadioButton deleteRadioButton;
    private JRadioButton moveRadioButton;

    private Set<String> existNames;
    private Set<String> watchedFolders;

    private DictionaryWrapper defaultSpecie;
    private CompleteAction completeAction = CompleteAction.NOTHING;

    @Inject
    private FrameLazyFactory frameLazyFactory;

    @Inject
    private UploadServiceAdapter uploadService;

    private static <T, D> DefaultComboBoxModel<D> toComboBoxModel(List<T> list, D def, Function<T, D> transformer) {
        final DefaultComboBoxModel<D> model = new DefaultComboBoxModel<>();
        model.addElement(def);
        for (D t : Collections2.transform(list, transformer)) {
            model.addElement(t);
        }
        model.setSelectedItem(model.getElementAt(0));

        return model;
    }

    @PostConstruct
    public void init() {
        contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);

        setTitle(getMessage(CONFIG_TITLE));

        labelFolder = new JLabel(getMessage(CONFIG_LABEL_FOLDER));
        folderField = new JTextField();
        folderChooseBtn = new JButton(getMessage(CONFIG_BUTTON_BROWSE));
        folderChooseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onFolderChoose();
            }
        });
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setFileFilter(new FileNameExtensionFilter(getMessage(CONFIG_FILE_FILTER_FOLDERS_ONLY), ".*"));
        folderChooser.setAcceptAllFileFilterUsed(false);

        labelName = new JLabel(getMessage(CONFIG_LABEL_NAME));
        nameField = new JTextField();

        labelLab = new JLabel(getMessage(CONFIG_LABEL_LABS));
        labsCb = new JComboBox<>();
        labsCb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onStateChanged();
            }
        });

        labelTechnologyType = new JLabel(getMessage(CONFIG_LABEL_TECHNOLOGY_TYPE));
        technologyTypesCb = new JComboBox<>();
        technologyTypesCb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onStateChanged();
            }
        });

        labelVendor = new JLabel(getMessage(CONFIG_LABEL_VENDOR));
        vendorsCb = new JComboBox<>();
        vendorsCb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onStateChanged();
            }
        });

        labelInstrument = new JLabel(getMessage(CONFIG_LABEL_INSTRUMENT));
        instrumentCb = new JComboBox<>();
        instrumentCb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onInstrumentSelect();
            }
        });

        labelLabels = new JLabel(getMessage(CONFIG_LABEL_LABELS));
        labelsField = new JTextArea();
        labelsField.setBorder(DEFAULT_BORDER);

        labelCompleteAction = new JLabel(getMessage(CONFIG_LABEL_COMPLETE_ACTION));
        doNothingRadioButton = new JRadioButton(getMessage(CONFIG_LABEL_ACTION_NOTHING));
        doNothingRadioButton.setBackground(Color.WHITE);
        deleteRadioButton = new JRadioButton(getMessage(CONFIG_LABEL_ACTION_DELETE_FILES));
        deleteRadioButton.setBackground(Color.WHITE);
        moveRadioButton = new JRadioButton(getMessage(CONFIG_LABEL_ACTION_MOVE_FILES));
        moveRadioButton.setBackground(Color.WHITE);

        final ActionListener radionButtonActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onRadioButtonCheck();
            }
        };

        doNothingRadioButton.addActionListener(radionButtonActionListener);
        deleteRadioButton.addActionListener(radionButtonActionListener);
        moveRadioButton.addActionListener(radionButtonActionListener);

        final ButtonGroup actionGroup = new ButtonGroup();
        actionGroup.add(doNothingRadioButton);
        actionGroup.add(deleteRadioButton);
        actionGroup.add(moveRadioButton);

        labelFolderToMoveFiles = new JLabel();
        folderToMoveFiles = new JTextField();
        chooseFolderToMoveFiles = new JButton(getMessage(CONFIG_BUTTON_BROWSE));
        chooseFolderToMoveFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onSpecifyFolderToMove();
            }
        });

        moveFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        moveFolderChooser.setFileFilter(new FileNameExtensionFilter(getMessage(CONFIG_FILE_FILTER_FOLDERS_ONLY), ".*"));
        moveFolderChooser.setAcceptAllFileFilterUsed(false);

        buttonCancel = new JButton(getMessage(CONFIG_BUTTON_CANCEL));
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.close();
            }
        });

        buttonOK = new JButton(getMessage(CONFIG_BUTTON_OK));
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.onOK();
            }
        });

        setResizable(false);
        setContentPane(contentPanel);
        setModal(true);
        setLocationRelativeTo(frameLazyFactory.getMainForm());
        setIconImages(FormUtils.APP_ICONS);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigDialog.this.close();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        addAllComponents(false);

        doNothingRadioButton.setSelected(true);
        doNothingRadioButton.doClick();
        buttonOK.requestFocus();

        pack();
    }

    private void addAllComponents(boolean addInstrument) {
        contentPanel.removeAll();

        contentPanel.setLayout(new FormLayout(
            "10px, 400px, 10px",
            "10px, 20px, fill:30px, 10px, 20px, fill:30px, 10px, fill:pref:grow, 10px, 20px, fill:50px:grow, 10px, " +
                "20px, pref, 20px, fill:30px, 10px"
        ));

        final CellConstraints cc = new CellConstraints();
        contentPanel.add(labelFolder, cc.xy(2, 2));

        final JPanel folderSelectionPanel = new JPanel(new BorderLayout(5, 5));
        folderSelectionPanel.setBackground(Color.WHITE);
        folderSelectionPanel.add(folderField, BorderLayout.CENTER);
        folderSelectionPanel.add(folderChooseBtn, BorderLayout.EAST);

        contentPanel.add(folderSelectionPanel, cc.xy(2, 3));
        contentPanel.add(labelName, cc.xy(2, 5));
        contentPanel.add(nameField, cc.xy(2, 6));

        contentPanel.add(getInstrumentSelectionPanel(addInstrument), cc.xy(2, 8));
        contentPanel.add(labelLabels, cc.xy(2, 10));
        contentPanel.add(labelsField, cc.xy(2, 11));
        contentPanel.add(labelCompleteAction, cc.xy(2, 13));

        final JPanel actionSelectionPanel = new JPanel();
        actionSelectionPanel.setLayout(new FormLayout(
            "10px, pref:grow",
            "20px, 20px, 20px, 0px, 20px, fill:30px"
        ));
        actionSelectionPanel.setBackground(Color.WHITE);

        actionSelectionPanel.add(doNothingRadioButton, cc.xy(2, 1));
        actionSelectionPanel.add(deleteRadioButton, cc.xy(2, 2));
        actionSelectionPanel.add(moveRadioButton, cc.xy(2, 3));
        actionSelectionPanel.add(labelFolderToMoveFiles, cc.xy(2, 5));

        final JPanel folderToRemovePanel = new JPanel(new BorderLayout(5, 5));
        folderToRemovePanel.setBackground(Color.WHITE);
        folderToRemovePanel.add(folderToMoveFiles, BorderLayout.CENTER);
        folderToRemovePanel.add(chooseFolderToMoveFiles, BorderLayout.EAST);

        actionSelectionPanel.add(folderToRemovePanel, cc.xy(2, 6));

        contentPanel.add(actionSelectionPanel, cc.xy(2, 14));

        final JPanel buttonPanel = new JPanel(new FormLayout(
            "pref:grow, fill:80px, 5px, fill:80px",
            "fill:pref:grow"
        ));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(buttonCancel, cc.xy(2, 1));
        buttonPanel.add(buttonOK, cc.xy(4, 1));

        contentPanel.add(buttonPanel, cc.xy(2, 16));

        contentPanel.revalidate();
        this.pack();
    }

    private JPanel getInstrumentSelectionPanel(boolean addInstrument) {
        final JPanel instrumentSelectionPanel = new JPanel();
        instrumentSelectionPanel.setBackground(Color.WHITE);

        final boolean addLab = uploadService.getLabs().size() > 1;

        final String encodedRowSpecs;
        if (addLab && addInstrument) {
            encodedRowSpecs = "20px, fill:30px, 10px, 20px, fill:30px, 10px, 20px, fill:30px, 10px, 20px, fill:30px";
        } else if (addLab) {
            encodedRowSpecs = "20px, fill:30px, 10px, 20px, fill:30px, 10px, 20px, fill:30px";
        } else if (addInstrument) {
            encodedRowSpecs = "0px, 0px, 0px, 20px, fill:30px, 10px, 20px, fill:30px, 10px, 20px, fill:30px";
        } else {
            encodedRowSpecs = "0px, 0px, 0px, 20px, fill:30px, 10px, 20px, fill:30px, 0px, 0px, 0px";
        }

        instrumentSelectionPanel.setLayout(new FormLayout("pref:grow", encodedRowSpecs));
        final CellConstraints cc = new CellConstraints();

        if (addLab) {
            instrumentSelectionPanel.add(labelLab, cc.xy(1, 1));
            instrumentSelectionPanel.add(labsCb, cc.xy(1, 2));
        }

        instrumentSelectionPanel.add(labelTechnologyType, cc.xy(1, 4));
        instrumentSelectionPanel.add(technologyTypesCb, cc.xy(1, 5));
        instrumentSelectionPanel.add(labelVendor, cc.xy(1, 7));
        instrumentSelectionPanel.add(vendorsCb, cc.xy(1, 8));

        if (addInstrument) {
            instrumentSelectionPanel.add(labelInstrument, cc.xy(1, 10));
            instrumentSelectionPanel.add(instrumentCb, cc.xy(1, 11));
        }

        return instrumentSelectionPanel;
    }

    @Override
    public void open() {
        clear();

        open(defaultLab, defaultTechnologyType, defaultVendor, defaultInstrument);
    }

    private void open(
        DictionaryWrapper defLab,
        DictionaryWrapper defTechnologyType,
        DictionaryWrapper defVendor,
        InstrumentWrapper defInstrument
    ) {
        final List<ContextInfo> contexts = uploadService.getContexts();
        existNames = UploadTransformer.toSetDto(contexts, UploadTransformer.CONTEXT_TO_STRING);
        watchedFolders = UploadTransformer.toSetDto(contexts, UploadTransformer.CONTEXT_TO_FOLDER);

        final List<DictionaryDTO> labs = uploadService.getLabs();
        labsCb.setModel(toComboBoxModel(labs, labs.size() == 1 ? new DictionaryWrapper(labs.get(0)) : defLab,
            TO_DICTIONARY_WRAPPER
        ));
        technologyTypesCb
            .setModel(toComboBoxModel(uploadService.getTechnologyTypes(), defTechnologyType, TO_DICTIONARY_WRAPPER));
        vendorsCb.setModel(toComboBoxModel(uploadService.getVendors(), defVendor, TO_DICTIONARY_WRAPPER));

        instrumentCb.setModel(toComboBoxModel(new ArrayList<>(), defInstrument, TO_INSTRUMENT_WRAPPER));
        defaultSpecie = TO_DICTIONARY_WRAPPER.apply(uploadService.getDefaultSpecie());

        FormUtils.setToScreenCenter(this);
        setVisible(true);
        buttonOK.setFocusable(true);
    }

    @Override
    public void close() {
        setVisible(false);
        dispose();
    }

    @Override
    public void clear() {
        nameField.setText("");
        folderField.setText("");
        instrumentCb.setSelectedItem(defaultInstrument);
        labelsField.setText("");
        folderToMoveFiles.setText("");
        clearError();
    }

    private void clearError() {
        setDefault(labelName, nameField, getMessage(CONFIG_LABEL_NAME));
        setDefault(labelFolder, folderField, getMessage(CONFIG_LABEL_FOLDER));
        setDefault(labelTechnologyType, technologyTypesCb, getMessage(CONFIG_LABEL_TECHNOLOGY_TYPE));
        setDefault(labelVendor, vendorsCb, getMessage(CONFIG_LABEL_VENDOR));
        setDefault(labelLab, labsCb, getMessage(CONFIG_LABEL_LABS));
        setDefault(labelInstrument, instrumentCb, getMessage(CONFIG_LABEL_INSTRUMENT));
        setDefault(labelFolderToMoveFiles, folderToMoveFiles, getMessage(CONFIG_LABEL_SPECIFY_FOLDER));
        labelLabels.setText(getMessage(CONFIG_LABEL_LABELS));
    }

    private void onOK() {
        final String name = nameField.getText();
        final String folder = folderField.getText();
        final DictionaryDTO technologyType = ((DictionaryWrapper) technologyTypesCb.getSelectedItem()).getDictionary();
        final DictionaryDTO vendor = ((DictionaryWrapper) vendorsCb.getSelectedItem()).getDictionary();
        final DictionaryDTO lab = ((DictionaryWrapper) labsCb.getSelectedItem()).getDictionary();
        final InstrumentDTO instrument = ((InstrumentWrapper) instrumentCb.getSelectedItem()).getInstrument();
        final DictionaryDTO specie = defaultSpecie.getDictionary();
        final String label = labelsField.getText();
        final String folderToMove = folderToMoveFiles.getText();

        clearError();

        boolean isError = false;

        if (StringUtils.isEmpty(name)) {
            setError(labelName, nameField, getMessage(CONFIG_LABEL_NAME_ERROR_EMPTY));
            isError = true;
        } else if (existNames.contains(name)) {
            setError(labelName, nameField, getMessage(CONFIG_LABEL_NAME_ERROR_EXISTS));
            isError = true;
        }

        if (StringUtils.isEmpty(folder)) {
            setError(labelFolder, folderField, getMessage(CONFIG_LABEL_FOLDER_ERROR_EMPTY));
            isError = true;
        } else if (watchedFolders.contains(folder)) {
            setError(labelFolder, folderField, getMessage(CONFIG_LABEL_FOLDER_ERROR_EXISTS));
            isError = true;
        }

        if (technologyType.getId() == -1) {
            setError(labelTechnologyType, technologyTypesCb, getMessage(CONFIG_LABEL_TECHNOLOGY_TYPE_ERROR_EMPTY));
            isError = true;
        }

        if (vendor.getId() == -1) {
            setError(labelVendor, vendorsCb, getMessage(CONFIG_LABEL_VENDOR_ERROR_EMPTY));
            isError = true;
        }

        if (lab.getId() == -1 && instrument.getId() == -1) {
            setError(labelLab, labsCb, getMessage(CONFIG_LABEL_LAB_ERROR_EMPTY));
            isError = true;
        }

        if (instrument.getId() == -1) {
            setError(labelInstrument, instrumentCb, getMessage(CONFIG_LABEL_INSTRUMENT_ERROR_EMPTY));
            isError = true;
        }

        if (!isError && completeAction == CompleteAction.MOVE_FILE) {
            if (StringUtils.isEmpty(folderToMove)) {
                setError(
                    labelFolderToMoveFiles, folderToMoveFiles, getMessage(CONFIG_LABEL_SPECIFY_FOLDER_ERROR_EMPTY)
                );
                isError = true;
            } else if (!FormUtils.checkFileExists(this, folderToMove)) {
                isError = true;
            }
        }

        if (!isError && FormUtils.checkFileExists(this, folder)) {
            final ContextInfo info = new ContextInfo(
                0,
                name,
                folder,
                false,
                label,
                uploadService.getInstrument(instrument.getId()),
                specie,
                new Date(),
                completeAction,
                folderToMove
            );

            addContext(info);
        }
    }

    private void setError(JLabel label, JComponent field, String msg) {
        setPanelStyle(label, field, msg, ERROR_COLOR, ERROR_BORDER);
    }

    private void setDefault(JLabel label, JComponent field, String msg) {
        setPanelStyle(label, field, msg, DEFAULT_COLOR, DEFAULT_BORDER);
    }

    private void setPanelStyle(JLabel label, JComponent field, String msg, Color color, Border border) {
        label.setText(msg);
        label.setForeground(color);
        field.setBorder(border);
    }

    private void addContext(ContextInfo info) {
        uploadService.addContext(info, this);
        frameLazyFactory.getMainForm().addContext(info);
        clear();
        close();
    }

    private void onFolderChoose() {
        if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = folderChooser.getSelectedFile();
            folderField.setText(selectedFile.getAbsolutePath());
            nameField.setText(selectedFile.getName());
            clearError();
        }
    }

    private void onSpecifyFolderToMove() {
        if (moveFolderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File selectedFile = moveFolderChooser.getSelectedFile();
            folderToMoveFiles.setText(selectedFile.getAbsolutePath());
            setDefault(labelFolderToMoveFiles, folderToMoveFiles, getMessage(CONFIG_LABEL_SPECIFY_FOLDER));
        }
    }

    private void onRadioButtonCheck() {
        if (doNothingRadioButton.isSelected()) {
            completeAction = CompleteAction.NOTHING;
            enableSpecifyFolder(false);
            setDefault(labelFolderToMoveFiles, folderToMoveFiles, getMessage(CONFIG_LABEL_SPECIFY_FOLDER));
        } else if (deleteRadioButton.isSelected()) {
            completeAction = CompleteAction.DELETE_FILE;
            enableSpecifyFolder(false);
            setDefault(labelFolderToMoveFiles, folderToMoveFiles, getMessage(CONFIG_LABEL_SPECIFY_FOLDER));
        } else {
            completeAction = CompleteAction.MOVE_FILE;
            enableSpecifyFolder(true);
        }
    }

    private void enableSpecifyFolder(boolean enabled) {
        labelFolderToMoveFiles.setEnabled(enabled);
        folderToMoveFiles.setEnabled(enabled);
        chooseFolderToMoveFiles.setEnabled(enabled);
    }

    private void onStateChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                buttonOK.setEnabled(true);

                final DictionaryWrapper lab = (DictionaryWrapper) labsCb.getSelectedItem();
                final DictionaryWrapper technologyType = (DictionaryWrapper) technologyTypesCb.getSelectedItem();
                final DictionaryWrapper vendor = (DictionaryWrapper) vendorsCb.getSelectedItem();

                long labId = lab.getDictionary().getId();
                long technologyTypeId = technologyType.getDictionary().getId();
                long vendorId = vendor.getDictionary().getId();

                if (labId > 0 && technologyTypeId > 0 && vendorId > 0) {
                    final List<InstrumentDTO> instruments = uploadService.getInstruments(
                        labId,
                        technologyTypeId,
                        vendorId
                    );

                    if (instruments.isEmpty()) {
                        final Optional<DictionaryWrapper> defaultInstrumentModel =
                            getDefaultInstrumentModel(technologyTypeId, vendorId);

                        if (defaultInstrumentModel.isPresent()) {
                            long defaultInstrumentModelId = defaultInstrumentModel.get().getDictionary().getId();
                            final InstrumentDTO defaultInstrument =
                                uploadService.createDefaultInstrument(labId, defaultInstrumentModelId);
                            instrumentCb.setModel(
                                toComboBoxModel(new ArrayList<>(), new InstrumentWrapper(defaultInstrument),
                                    TO_INSTRUMENT_WRAPPER
                                ));
                            addAllComponents(false);
                        } else {
                            showNoInstrumentModelFoundError();
                            instrumentCb
                                .setModel(toComboBoxModel(new ArrayList<>(), defaultInstrument, TO_INSTRUMENT_WRAPPER));
                            addAllComponents(false);
                        }
                    } else {
                        final InstrumentDTO instrument = instruments.get(0);
                        if (instruments.size() == 1 && instrument.getName().equalsIgnoreCase("Default")) {
                            instrumentCb.setModel(
                                toComboBoxModel(instruments, new InstrumentWrapper(instrument), TO_INSTRUMENT_WRAPPER));
                            addAllComponents(false);

                        } else {
                            instrumentCb
                                .setModel(toComboBoxModel(instruments, defaultInstrument, TO_INSTRUMENT_WRAPPER));
                            addAllComponents(true);
                        }
                    }
                } else {
                    instrumentCb.setModel(toComboBoxModel(new ArrayList<>(), defaultInstrument, TO_INSTRUMENT_WRAPPER));
                    addAllComponents(false);
                }

                contentPanel.revalidate();
            }
        });
    }

    private Optional<DictionaryWrapper> getDefaultInstrumentModel(long technologyType, long vendor) {
        final List<DictionaryDTO> instrumentModels = uploadService.getInstrumentModels(technologyType, vendor);
        for (DictionaryDTO instrumentModel : instrumentModels) {
            if (instrumentModel.getName().equalsIgnoreCase("default")) {
                return Optional.of(TO_DICTIONARY_WRAPPER.apply(instrumentModel));
            }
        }

        return Optional.absent();
    }

    private void onInstrumentSelect() {
        clearError();

        final InstrumentWrapper selectedInstrument = (InstrumentWrapper) instrumentCb.getSelectedItem();

        //if change selection from default instrument, then remove "Select One" instrument
        if (!selectedInstrument.toString().equals(defaultInstrument.toString())) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) instrumentCb.getModel();
            model.removeElement(defaultInstrument);
        }
    }

    private void showNoInstrumentModelFoundError() {
        DialogUtil.showMessage(
            this,
            getMessage(MODALS_NOT_INSTRUMENT_MODEL_TEXT),
            getMessage(MODALS_ERROR_TITLE),
            JOptionPane.WARNING_MESSAGE
        );

        buttonOK.setEnabled(false);
    }
}
