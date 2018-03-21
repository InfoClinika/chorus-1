package com.infoclinika.mssharing.platform.model.impl.helper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.InstrumentModel;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.Vendor;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.helper.InstrumentCreationHelperTemplate.PotentialOperator;
import com.infoclinika.mssharing.platform.repository.InstrumentModelRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentStudyTypeRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.VendorRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Comparator;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author Herman Zamula
 */
@Transactional(readOnly = true)
public abstract class DefaultInstrumentCreationHelper<OPERATOR extends PotentialOperator> implements InstrumentCreationHelperTemplate<OPERATOR> {

    @Inject
    private VendorRepositoryTemplate<?> vendorRepository;
    @Inject
    private InstrumentStudyTypeRepositoryTemplate<?> studyTypeRepository;
    @Inject
    private InstrumentModelRepositoryTemplate<?> instrumentModelRepository;
    @Inject
    private UserRepositoryTemplate<?> userRepository;
    @Inject
    private TransformersTemplate transformers;

    @Override
    public ImmutableSortedSet<OPERATOR> availableOperators(final long labId) {
        return from(userRepository.findAllUsersByLab(labId))
                .transform(new Function<UserTemplate, OPERATOR>() {
                    @Override
                    public OPERATOR apply(UserTemplate input) {
                        return transformOperator(input);
                    }
                })
                .toSortedSet(operatorComparator());
    }

    @Override
    public ImmutableSortedSet<DictionaryItem> studyTypes() {
        return from(studyTypeRepository.findAll())
                .transform(transformers.dictionaryItemTransformer())
                .toSortedSet(transformers.dictionaryItemComparator());
    }

    @Override
    public ImmutableSortedSet<DictionaryItem> vendors(long studyType) {
        return from(instrumentModelRepository.findByStudyType(studyType))
                .transform(new Function<InstrumentModel, Vendor>() {
                    @Override
                    public Vendor apply(InstrumentModel instrumentModel) {
                        return instrumentModel.getVendor();
                    }
                })
                .transform(transformers.dictionaryItemTransformer())
                .toSortedSet(transformers.dictionaryItemComparator());
    }

    @Override
    public ImmutableSortedSet<DictionaryItem> vendors() {

        return from(vendorRepository.findAll())
                .transform(transformers.dictionaryItemTransformer())
                .toSortedSet(transformers.dictionaryItemComparator());
    }

    @Override
    public ImmutableSortedSet<DictionaryItem> vendorsWithFolderArchiveUploadSupport() {

        return from(instrumentModelRepository.findWithFolderArchiveUploadSupport())
                .transform(new Function<InstrumentModel, Vendor>() {
                    @Override
                    public Vendor apply(InstrumentModel input) {
                        return input.getVendor();
                    }
                })
                .transform(transformers.dictionaryItemTransformer())
                .toSortedSet(transformers.dictionaryItemComparator());
    }

    @Override
    public ImmutableSortedSet<DictionaryItem> models(long vendor) {

        return from(instrumentModelRepository.findByVendor(vendor))
                .transform(transformers.dictionaryItemTransformer())
                .toSortedSet(transformers.dictionaryItemComparator());
    }

    @Override
    public ImmutableSortedSet<DictionaryItem> models(long vendor, long studyType) {

        return from(instrumentModelRepository.findByStudyTypeAndVendor(studyType, vendor))
                .transform(transformers.dictionaryItemTransformer())
                .toSortedSet(transformers.dictionaryItemComparator());
    }

    protected Comparator<PotentialOperator> operatorComparator() {
        return new Comparator<PotentialOperator>() {
            @Override
            public int compare(PotentialOperator o1, PotentialOperator o2) {
                return o1.email.compareTo(o2.email);
            }
        };
    }

    protected abstract OPERATOR transformOperator(UserTemplate input);
}
