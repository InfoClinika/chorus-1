package com.infoclinika.mssharing.platform.model.impl.read;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.entity.LabTemplate;
import com.infoclinika.mssharing.platform.model.RuleValidator;
import com.infoclinika.mssharing.platform.model.TransformersTemplate;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.LabItem;
import com.infoclinika.mssharing.platform.model.helper.read.LabReaderHelper;
import com.infoclinika.mssharing.platform.model.impl.DefaultTransformingTemplate;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.SortedSet;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkAccess;

/**
 * @author : Alexander Serebriyan
 */
@Transactional(readOnly = true)
public abstract class DefaultLabReader<LAB extends LabTemplate, LAB_LINE extends LabReaderTemplate.LabLineTemplate>
        implements LabReaderTemplate<LAB_LINE>, DefaultTransformingTemplate<LAB, LAB_LINE> {

    @Inject
    protected LabReaderHelper<LAB, LAB_LINE> labReaderHelper;
    @Inject
    protected TransformersTemplate transformers;
    @Inject
    private InstrumentRepositoryTemplate instrumentRepository;
    @Inject
    private RuleValidator ruleValidator;

    @PostConstruct
    private void init() {
        labReaderHelper.setTransformer(new Function<LAB, LAB_LINE>() {
            @Nullable
            @Override
            public LAB_LINE apply(LAB input) {
                return transform(input);
            }
        });
    }

    @Override
    public ImmutableSet<LAB_LINE> readUserLabs(long actor) {

        return labReaderHelper.readUserLabs(actor)
                .transform()
                .toSet();
    }

    @Override
    public SortedSet<LabItem> readLabItems(final long userId) {

        return labReaderHelper.readUserLabs(userId)
                .transform(new Function<LAB, LabItem>() {

                    @Override
                    public LabItem apply(LAB lab) {
                        return new LabItem(lab.getId(), lab.getName(), lab.getHead().getId(), filteredLabInstrumentsByOperator(lab, userId));
                    }

                    private ImmutableSortedSet<InstrumentItem> filteredLabInstrumentsByOperator(LAB lab, long actor) {
                        //noinspection unchecked
                        return from(instrumentRepository.findWhereOperatorIsByLab(lab.getId(), actor))
                                .transform(transformers.instrumentItemTransformer())
                                .toSortedSet(transformers.instrumentItemComparator());
                    }
                })
                .toSortedSet(transformers.namedItemComparator());

    }

    @Override
    public LAB_LINE readLab(long id) {
        return labReaderHelper.readLab(id).transform();
    }

    @Override
    public LAB_LINE readLabByName(String name) {
        return labReaderHelper.readLabByName(name).transform();
    }

    @Override
    public ImmutableSet<LAB_LINE> readAllLabs(long actor) {
        beforeReadAllLabs(actor);

        return labReaderHelper.readAllLabs()
                .transform()
                .toSet();
    }

    protected void beforeReadAllLabs(long actor) {
        checkAccess(ruleValidator.canReadLabs(actor), "User should be admin to read labs list");
    }
}
