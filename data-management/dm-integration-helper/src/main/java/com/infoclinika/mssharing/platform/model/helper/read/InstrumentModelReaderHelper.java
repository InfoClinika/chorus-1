package com.infoclinika.mssharing.platform.model.helper.read;


import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentModelReaderTemplate.InstrumentModelLineTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentModelRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "Guava"})
@Component
@Scope(value = "prototype")
public class InstrumentModelReaderHelper<MODEL extends InstrumentModel, MODEL_LINE extends InstrumentModelLineTemplate>
        extends AbstractReaderHelper<MODEL, MODEL_LINE, MODEL_LINE> {

    @Inject
    private InstrumentModelRepositoryTemplate<MODEL> instrumentModelRepository;

    public SingleResultBuilder<MODEL, MODEL_LINE> readById(long id) {
        final MODEL model = instrumentModelRepository.findOne(id);
        return SingleResultBuilder.builder(model, activeTransformer);
    }

    public ResultBuilder<MODEL, MODEL_LINE> readByVendor(long vendorId) {
        final List<MODEL> models = instrumentModelRepository.findByVendor(vendorId);
        return ResultBuilder.builder(models, activeTransformer);
    }

    public ResultBuilder<MODEL, MODEL_LINE> readByTechnologyType(long typeId) {
        final List<MODEL> models = instrumentModelRepository.findByStudyType(typeId);
        return ResultBuilder.builder(models, activeTransformer);
    }

    public ResultBuilder<MODEL, MODEL_LINE> readByStudyTypeAndVendor(long typeId, long vendorId) {
        final List<MODEL> models = instrumentModelRepository.findByStudyTypeAndVendor(typeId, vendorId);
        return ResultBuilder.builder(models, activeTransformer);
    }

    public PagedResultBuilder<MODEL, MODEL_LINE> readPaged(PagedItemInfo paged) {
        final PageRequest pageRequest = PagedItemsTransformerTemplate.toPageRequest(InstrumentModel.class, paged);
        final Page<MODEL> modelsPage = instrumentModelRepository.findAllAvailable(pageRequest);
        return PagedResultBuilder.builder(modelsPage, activeTransformer);
    }

    @Override
    public Function<MODEL, MODEL_LINE> getDefaultTransformer() {
        return instrumentModel -> (MODEL_LINE) new InstrumentModelLineTemplate(
                instrumentModel.getId(),
                instrumentModel.getName(),
                toDictionaryItem(instrumentModel.getStudyType()),
                toDictionaryItem(instrumentModel.getVendor()),
                toDictionaryItem(instrumentModel.getType()),
                instrumentModel.getExtensions().stream().map(VendorExtension::getExtension).collect(Collectors.toSet()),
                instrumentModel.isAdditionalFiles(),
                instrumentModel.isFolderArchiveSupport()
        );
    }

    private DictionaryItem toDictionaryItem(InstrumentStudyType instrumentStudyType) {
        return new DictionaryItem(instrumentStudyType.getId(), instrumentStudyType.getName());
    }

    private DictionaryItem toDictionaryItem(Vendor vendor) {
        return new DictionaryItem(vendor.getId(), vendor.getName());
    }

    private DictionaryItem toDictionaryItem(InstrumentType instrumentType) {
        return new DictionaryItem(instrumentType.getId(), instrumentType.getName());
    }
}
