package com.infoclinika.mssharing.platform.model.helper.write;

import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.write.InstrumentModelManagementTemplate.InstrumentModelDetails;
import com.infoclinika.mssharing.platform.repository.*;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author timofei.kasianov 12/6/16
 */
@Component
public class InstrumentModelManager<MODEL extends InstrumentModel, MODEL_DETAILS extends InstrumentModelDetails> {

    @Inject
    private EntityFactories entityFactories;
    @Inject
    private VendorRepositoryTemplate<Vendor> vendorRepository;
    @Inject
    private InstrumentStudyTypeRepositoryTemplate<InstrumentStudyType> instrumentStudyTypeRepository;
    @Inject
    private InstrumentTypeRepositoryTemplate<InstrumentType> instrumentTypeRepository;
    @Inject
    private VendorExtensionRepositoryTemplate<VendorExtension> vendorExtensionRepository;
    @Inject
    private InstrumentModelRepositoryTemplate<MODEL> instrumentModelRepository;


    @SuppressWarnings("unchecked")
    public MODEL create(MODEL_DETAILS details) {
        final MODEL instrumentModel = (MODEL) entityFactories.instrumentModel.get();
        return instrumentModelRepository.save(updateProperties(instrumentModel, details));
    }

    @SuppressWarnings("unchecked")
    public MODEL update(long modelId, MODEL_DETAILS details) {
        final MODEL instrumentModel = instrumentModelRepository.findOne(modelId);
        return instrumentModelRepository.save(updateProperties(instrumentModel, details));
    }

    public void delete(long modelId) {
        final MODEL model = instrumentModelRepository.findOne(modelId);
        model.getExtensions().clear();
        instrumentModelRepository.delete(modelId);
    }

    public boolean exists(long modelId) {
        return instrumentModelRepository.exists(modelId);
    }

    private MODEL updateProperties(MODEL model, MODEL_DETAILS details) {
        model.setName(details.name);
        model.setStudyType(findOrCreateStudyType(details.technologyType));
        model.setVendor(findOrCreateVendor(details.vendor));
        model.setType(findOrCreateInstrumentType(details.instrumentType));
        model.setExtensions(findOrCreateVendorExtensions(details.extensions));
        model.setAdditionalFiles(details.additionalFiles);
        model.setFolderArchiveSupport(details.folderArchiveSupport);

        return model;
    }

    private Vendor findOrCreateVendor(String vendorName) {
        return Optional
            .ofNullable(vendorRepository.findByName(vendorName))
            .orElseGet(() -> {
                final Vendor vendor = entityFactories.vendor.get();
                vendor.setName(vendorName);
                return vendor;
            });
    }

    private InstrumentStudyType findOrCreateStudyType(String type) {
        return Optional
            .ofNullable(instrumentStudyTypeRepository.findByName(type))
            .orElseGet(() -> {
                final InstrumentStudyType studyType = entityFactories.instrumentStudyType.get();
                studyType.setName(type);
                return studyType;
            });
    }

    private InstrumentType findOrCreateInstrumentType(String type) {
        return Optional
            .ofNullable(instrumentTypeRepository.findByName(type))
            .orElseGet(() -> {
                final InstrumentType instrumentType = entityFactories.instrumentType.get();
                instrumentType.setName(type);
                return instrumentType;
            });
    }

    private Set<VendorExtension> findOrCreateVendorExtensions(Set<String> extensions) {
        return extensions.stream().map(this::findOrCreateVendorExtension).collect(Collectors.toSet());
    }

    private VendorExtension findOrCreateVendorExtension(String vendorExtension) {
        final List<VendorExtension> ve =
            vendorExtensionRepository.findByExtensionWithEmptyZipExtension(vendorExtension);

        if (ve.size() > 0) {
            return ve.get(0);
        }

        final VendorExtension extension = entityFactories.vendorExtension.get();
        extension.setExtension(vendorExtension);
        extension.setZipExtension("");
        vendorExtensionRepository.save(extension);

        return extension;
    }
}
