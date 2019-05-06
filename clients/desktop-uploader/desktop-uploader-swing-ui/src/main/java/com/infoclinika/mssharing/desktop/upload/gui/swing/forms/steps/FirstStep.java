package com.infoclinika.mssharing.desktop.upload.gui.swing.forms.steps;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.clients.common.dto.DictionaryWrapper;
import com.infoclinika.mssharing.clients.common.dto.InstrumentWrapper;
import com.infoclinika.mssharing.desktop.upload.gui.swing.controller.steps.FirstStepController;
import com.infoclinika.mssharing.desktop.upload.gui.swing.util.DisplayMessageHelper;
import com.infoclinika.mssharing.dto.response.DictionaryDTO;
import com.infoclinika.mssharing.dto.response.InstrumentDTO;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.infoclinika.mssharing.desktop.messages.MessageKey.*;
import static com.infoclinika.mssharing.desktop.messages.MessagesSource.getMessage;

@Component
public class FirstStep {
    private JPanel contentPanel;
    private JComboBox<DictionaryWrapper> technologyTypeCombo;
    private JComboBox<DictionaryWrapper> vendorsCombo;
    private JComboBox<DictionaryWrapper> labsCombo;
    private JComboBox<InstrumentWrapper> instrumentCombo;
    private JComboBox<DictionaryWrapper> specieCombo;
    private JLabel labelTechnologyType;
    private JLabel labelVendors;
    private JLabel labelLabs;
    private JLabel labelInstrument;
    private JLabel labelSpecie;

    @Inject
    private FirstStepController controller;

    @Inject
    private DisplayMessageHelper messageHelper;

    @PostConstruct
    private void postConstruct() {
        controller.setFirstStep(this);
    }

    public void initialize() {
        labelLabs = new JLabel(getMessage(ONE_LABEL_lAB));
        final List<DictionaryDTO> labs = controller.getLabs();
        labsCombo = new JComboBox<>(createComboBoxModelForDictionaryWrapper(
            labs,
            labs.size() == 1 ? new DictionaryWrapper(labs.get(0)) : controller.getDefaultLab()
        ));

        labelTechnologyType = new JLabel(getMessage(ONE_LABEL_TECHNOLOGY_TYPE));
        technologyTypeCombo = new JComboBox<>(createComboBoxModelForDictionaryWrapper(
            controller.getTechnologyTypes(),
            controller.getDefaultTechnologyType()
        ));

        labelVendors = new JLabel(getMessage(ONE_LABEL_VENDOR));

        DictionaryWrapper defaultVendor = controller.getDefaultVendor();
        vendorsCombo = new JComboBox<>(createComboBoxModelForDictionaryWrapper(
            Collections.singletonList(defaultVendor.getDictionary()),
            defaultVendor
        ));

        labelInstrument = new JLabel(getMessage(ONE_LABEL_INSTRUMENT));
        instrumentCombo = new JComboBox<>(createComboBoxModelForInstruments(
            new ArrayList<>(),
            controller.getDefaultInstrument()
        ));

        labelSpecie = new JLabel(getMessage(ONE_LABEL_SPECIE));
        specieCombo = new JComboBox<>(createComboBoxModelForDictionaryWrapper(
            controller.getSpecies(),
            controller.getDefaultSpecie()
        ));

        labsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FirstStep.this.onStateChanged();
            }
        });

        technologyTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FirstStep.this.onTechnologyTypeChanged();
            }
        });

        vendorsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FirstStep.this.onStateChanged();
            }
        });

        instrumentCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FirstStep.this.onInstrumentSelect();
            }
        });

        specieCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FirstStep.this.onSpecieSelect();
            }
        });

        contentPanel = new JPanel();
        addAllComponents(false);
        controller.setView(contentPanel);
    }

    private void addAllComponents(boolean addInstrument) {
        contentPanel.removeAll();
        contentPanel.setBackground(Color.WHITE);

        final boolean addLab = controller.getLabs().size() > 1;

        final String encodedRowSpecs;
        if (addLab && addInstrument) {
            encodedRowSpecs = "pref:grow, 20px, fill:30px, 20px, 20px, fill:30px, 20px,  20px, fill:30px, 20px," +
                " 20px, fill:30px, 20px, 20px, fill:30px, pref:grow";
        } else if (addLab) {
            encodedRowSpecs = "pref:grow, 20px, fill:30px, 20px, 20px, fill:30px, 20px,  20px, fill:30px, 20px," +
                " 20px, fill:30px, 0px, 0px, 0px, pref:grow";
        } else if (addInstrument) {
            encodedRowSpecs = "pref:grow, 0px, 0px, 0px, 20px, fill:30px, 20px,  20px, fill:30px, 20px," +
                " 20px, fill:30px, 20px, 20px, fill:30px, pref:grow";
        } else {
            encodedRowSpecs = "pref:grow, 0px, 0px, 0px, 20px, fill:30px, 20px,  20px, fill:30px, 20px," +
                " 20px, fill:30px, 0px, 0px, 0px, pref:grow";
        }

        final String encodedColumnSpecs = "pref:grow, 300px, pref:grow";
        contentPanel.setLayout(new FormLayout(encodedColumnSpecs, encodedRowSpecs));
        final CellConstraints cc = new CellConstraints();

        if (controller.getLabs().size() > 1) {
            contentPanel.add(labelLabs, cc.xy(2, 2));
            contentPanel.add(labsCombo, cc.xy(2, 3));
        }

        contentPanel.add(labelTechnologyType, cc.xy(2, 5));
        contentPanel.add(technologyTypeCombo, cc.xy(2, 6));
        contentPanel.add(labelVendors, cc.xy(2, 8));
        contentPanel.add(vendorsCombo, cc.xy(2, 9));

        if (addInstrument) {
            contentPanel.add(labelInstrument, cc.xy(2, 11));
            contentPanel.add(instrumentCombo, cc.xy(2, 12));
            contentPanel.add(labelSpecie, cc.xy(2, 14));
            contentPanel.add(specieCombo, cc.xy(2, 15));
        } else {
            contentPanel.add(labelSpecie, cc.xy(2, 11));
            contentPanel.add(specieCombo, cc.xy(2, 12));
        }

        contentPanel.revalidate();
    }

    private void onTechnologyTypeChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final DictionaryWrapper technologyType = (DictionaryWrapper) technologyTypeCombo.getSelectedItem();
                long technologyTypeId = technologyType.getDictionary().getId();
                vendorsCombo.removeAllItems();

                List<DictionaryDTO> vendors = controller.getVendorsByTechnologyType(technologyTypeId);
                for (DictionaryDTO vendorDto : vendors) {
                    vendorsCombo.addItem(new DictionaryWrapper(vendorDto));
                }
                if (vendors.size() > 0) {
                    vendorsCombo.setSelectedItem(vendors.get(0));
                }
            }
        });
    }

    private void onStateChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final DictionaryWrapper lab = (DictionaryWrapper) labsCombo.getSelectedItem();
                final DictionaryWrapper technologyType = (DictionaryWrapper) technologyTypeCombo.getSelectedItem();
                final DictionaryWrapper vendor = (DictionaryWrapper) vendorsCombo.getSelectedItem();

                long labId = lab.getDictionary().getId();
                long technologyTypeId = technologyType.getDictionary().getId();
                long vendorId = vendor.getDictionary().getId();

                if (labId > 0 && technologyTypeId > 0 && vendorId > 0) {
                    final List<InstrumentDTO> instruments = controller.getInstruments(
                        labId,
                        technologyTypeId,
                        vendorId
                    );

                    if (instruments.isEmpty()) {
                        final Optional<DictionaryWrapper> defaultInstrumentModel =
                            FirstStep.this.getDefaultInstrumentModel(technologyTypeId, vendorId);

                        if (defaultInstrumentModel.isPresent()) {
                            long defaultInstrumentModelId = defaultInstrumentModel.get().getDictionary().getId();
                            final InstrumentWrapper defaultInstrument =
                                controller.createDefaultInstrument(labId, defaultInstrumentModelId);
                            instrumentCombo.setModel(
                                FirstStep.this.createComboBoxModelForInstruments(new ArrayList<>(), defaultInstrument)
                            );
                            FirstStep.this.addAllComponents(false);
                        } else {
                            FirstStep.this.showNoInstrumentModelFoundError();
                            instrumentCombo.setModel(FirstStep.this.createComboBoxModelForInstruments(
                                new ArrayList<>(),
                                controller.getDefaultInstrument()
                            ));
                            FirstStep.this.addAllComponents(false);
                        }
                    } else {
                        final InstrumentDTO instrument = instruments.get(0);
                        if (instruments.size() == 1 && instrument.getName().equalsIgnoreCase("Default")) {
                            instrumentCombo.setModel(
                                FirstStep.this
                                    .createComboBoxModelForInstruments(instruments, new InstrumentWrapper(instrument))
                            );
                            FirstStep.this.addAllComponents(false);

                        } else {
                            instrumentCombo.setModel(
                                FirstStep.this
                                    .createComboBoxModelForInstruments(instruments, controller.getDefaultInstrument())
                            );
                            FirstStep.this.addAllComponents(true);
                        }
                    }
                } else {
                    instrumentCombo.setModel(
                        FirstStep.this
                            .createComboBoxModelForInstruments(new ArrayList<>(), controller.getDefaultInstrument()));
                    FirstStep.this.addAllComponents(false);
                }

                contentPanel.revalidate();
            }
        });
    }

    private void showNoInstrumentModelFoundError() {
        messageHelper.showMainWindowMessage(
            getMessage(MODALS_NO_INSTRUMENT_MODEL),
            getMessage(MODALS_ERROR_TITLE),
            JOptionPane.WARNING_MESSAGE
        );
    }

    private Optional<DictionaryWrapper> getDefaultInstrumentModel(long technologyType, long vendor) {
        final List<DictionaryDTO> instrumentModels = controller.getInstrumentModels(technologyType, vendor);
        for (DictionaryDTO instrumentModel : instrumentModels) {
            if (instrumentModel.getName().equalsIgnoreCase("default")) {
                return Optional.of(new DictionaryWrapper(instrumentModel));
            }
        }

        return Optional.absent();
    }

    private void onInstrumentSelect() {
        final InstrumentWrapper selectedInstrument = (InstrumentWrapper) instrumentCombo.getSelectedItem();
        controller.changeSelectedInstrument(selectedInstrument);

        //if change selection from default instrument, then remove "Select One" instrument
        if (!selectedInstrument.toString().equals(controller.getDefaultInstrument().toString())) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) instrumentCombo.getModel();
            model.removeElement(controller.getDefaultInstrument());
        }
    }

    private void onSpecieSelect() {
        final DictionaryWrapper selectedSpecie = (DictionaryWrapper) specieCombo.getSelectedItem();
        controller.changeSelectedSpecie(selectedSpecie);
    }

    private DefaultComboBoxModel<DictionaryWrapper> createComboBoxModelForDictionaryWrapper(
        List<DictionaryDTO> items,
        DictionaryWrapper defaultValue) {

        final DefaultComboBoxModel<DictionaryWrapper> model = new DefaultComboBoxModel<>();
        for (DictionaryDTO dictionary : items) {
            model.addElement(new DictionaryWrapper(dictionary));
        }
        model.setSelectedItem(defaultValue);

        return model;
    }

    private DefaultComboBoxModel<InstrumentWrapper> createComboBoxModelForInstruments(List<InstrumentDTO> instruments,
                                                                                      InstrumentWrapper defaultValue) {
        final DefaultComboBoxModel<InstrumentWrapper> model = new DefaultComboBoxModel<>();
        model.addElement(defaultValue);

        for (InstrumentDTO instrument : instruments) {
            model.addElement(new InstrumentWrapper(instrument));
        }

        model.setSelectedItem(defaultValue);
        controller.changeSelectedInstrument(defaultValue);

        return model;
    }
}
