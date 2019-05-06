package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.repository.InstrumentModelRepository;
import com.infoclinika.mssharing.model.internal.repository.VendorRepository;
import com.infoclinika.mssharing.model.read.InstrumentModelReader;
import com.infoclinika.mssharing.platform.entity.AbstractPersistable;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.Vendor;
import com.infoclinika.mssharing.platform.entity.VendorExtension;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate.toFilterQuery;
import static com.infoclinika.mssharing.platform.model.helper.PagedItemsTransformerTemplate.toPageRequest;

/**
 * @author timofei.kasianov 12/8/16
 */
@SuppressWarnings("Guava")
@Component
public class InstrumentModelReaderImpl implements InstrumentModelReader {

    @Inject
    private InstrumentModelRepository instrumentModelRepository;
    @Inject
    private VendorRepository vendorRepository;
    @Inject
    private RuleValidator ruleValidator;

    @Override
    public PagedItem<InstrumentModelLine> read(long actor, PagedItemInfo pagedItem) {

        final PageRequest pageRequest = toPageRequest(InstrumentModel.class, pagedItem);
        final String query = toFilterQuery(pagedItem);
        final Page<InstrumentModel> page = instrumentModelRepository.findPage(query, pageRequest);
        final List<InstrumentModel> instrumentModels = page.getContent();

        if (instrumentModels.isEmpty()) {
            return new PagedItem<>(
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                new ArrayList<>()
            );
        }

        final List<Long> ids = instrumentModels.stream().map(AbstractPersistable::getId).collect(Collectors.toList());
        final List<Map<String, Long>> instrumentCounts = instrumentModelRepository.findInstrumentCounts(ids);
        final List<InstrumentModelLine> modelLines = getTransformer(instrumentCounts).apply(instrumentModels);

        return new PagedItem<>(
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize(),
            modelLines
        );
    }

    @Override
    public boolean isNameUnique(long actor, String name, Long vendor, Long model) {

        if (!ruleValidator.canUserReadInstrumentModels(actor)) {
            throw new AccessDenied("User with ID: " + actor + " is not allowed to read instrument models");
        }

        if (vendor == null) {
            return true;
        }

        final Vendor vendorEntity = vendorRepository.findOne(vendor);

        if (model == null) {
            return ruleValidator.canInstrumentModelBeCreatedWithName(name, vendorEntity.getName());
        } else {
            return ruleValidator.canInstrumentModelBeUpdatedWithName(model, name, vendorEntity.getName());
        }

    }

    private Function<List<InstrumentModel>, List<InstrumentModelLine>> getTransformer(
        List<Map<String, Long>> idToInstrumentCountList
    ) {

        final Map<Long, Long> itToInstrumentCount = idToInstrumentCountList
            .stream()
            .collect(Collectors.toMap(m -> m.get("id"), m -> m.get("count")));

        return instrumentModels -> instrumentModels
            .stream()
            .map(imi -> new InstrumentModelLine(
                imi.getId(),
                imi.getName(),
                new DictionaryItem(imi.getStudyType().getId(), imi.getStudyType().getName()),
                new DictionaryItem(imi.getVendor().getId(), imi.getVendor().getName()),
                new DictionaryItem(imi.getType().getId(), imi.getType().getName()),
                imi.getExtensions().stream().map(VendorExtension::getExtension).collect(Collectors.toSet()),
                imi.isAdditionalFiles(),
                imi.isFolderArchiveSupport(),
                itToInstrumentCount.get(imi.getId())
            ))
            .collect(Collectors.toList());
    }
}
