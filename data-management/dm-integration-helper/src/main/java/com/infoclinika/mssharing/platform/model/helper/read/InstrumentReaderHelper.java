package com.infoclinika.mssharing.platform.model.helper.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.platform.entity.InstrumentTemplate;
import com.infoclinika.mssharing.platform.entity.UserTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentAccess;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import com.infoclinika.mssharing.platform.repository.FileRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate;
import com.infoclinika.mssharing.platform.repository.InstrumentRepositoryTemplate.AccessedInstrument;
import com.infoclinika.mssharing.platform.repository.UserRepositoryTemplate;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.platform.model.helper.read.PagedResultBuilder.builder;
import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentAccess.NO_ACCESS;
import static com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentAccess.OPERATOR;

/**
 * @author : Alexander Serebriyan, Herman Zamula
 */
@Component
@Scope(value = "prototype")
public class InstrumentReaderHelper<INSTRUMENT extends InstrumentTemplate,
    INSTRUMENT_LINE extends InstrumentLineTemplate>
    extends AbstractReaderHelper<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE, InstrumentLineTemplate> {

    @Inject
    private InstrumentRepositoryTemplate<INSTRUMENT> instrumentRepository;

    @Inject
    private FileRepositoryTemplate<FileMetaDataTemplate> fileMetaDataRepository;

    @Inject
    private EntityFactories entityFactories;

    @Inject
    UserRepositoryTemplate<?> userRepositoryTemplate;

    @Override
    public Function<AccessedInstrument<INSTRUMENT>, InstrumentLineTemplate> getDefaultTransformer() {
        return accessedInstrument -> {
            final INSTRUMENT input = accessedInstrument.instrument;

            UserTemplate user = userRepositoryTemplate.findOne(accessedInstrument.accessedUser);

            final boolean containsLab = user.getLabs().contains(input.getLab());
            final InstrumentAccess access = containsLab ? OPERATOR : NO_ACCESS;

            return new InstrumentLineTemplate(
                input.getId(),
                input.getName(),
                input.getModel().getVendor().getName(),
                input.getLab().getName(),
                input.getSerialNumber(),
                input.getCreator().getId(),
                fileMetaDataRepository.countByInstrument(input.getId()),
                input.getModel().getName(),
                access
            );
        };
    }

    public ResultBuilder<INSTRUMENT, INSTRUMENT_LINE> readAllInstruments() {

        /*List<INSTRUMENT> instruments = instrumentRepository.findAll();
        return ResultBuilder.builder(instruments, activeTransformer);*/
        return null;
    }

    public ResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> readAvailableInstruments(long actor) {

        List<AccessedInstrument<INSTRUMENT>> instruments = instrumentRepository.findAllAvailableAccessed(actor);
        return ResultBuilder.builder(instruments, activeTransformer);

    }

    public ResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> readInstrumentLinesByLab(long actor,
                                                                                                   long lab) {
        List<AccessedInstrument<INSTRUMENT>> instrumentsByLab = instrumentRepository.findByLabAccessed(actor, lab);
        return ResultBuilder.builder(instrumentsByLab, activeTransformer);
    }

    public ResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> readInstrumentLinesByLabModelAndName(
        long actor, long lab, long model, String name) {
        final AccessedInstrument<INSTRUMENT> instrument =
            instrumentRepository.findByLabModelAndNameAccessed(actor, lab, model, name);
        return instrument != null ?
            ResultBuilder.builder(newArrayList(instrument), activeTransformer) :
            ResultBuilder.builder(newArrayList(), activeTransformer);
    }

    public PagedResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> readInstruments(long actor,
                                                                                               Pageable pageable,
                                                                                               String nameFilter) {
        final Page<AccessedInstrument<INSTRUMENT>> instrumentsPage =
            instrumentRepository.findAllAvailableAccessed(actor, nameFilter, pageable);
        return builder(instrumentsPage, activeTransformer);
    }

    public PagedResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> readInstrumentsByLab(long actor,
                                                                                                    long lab,
                                                                                                    Pageable pageable,
                                                                                                    String nameFilter) {
        final Page<AccessedInstrument<INSTRUMENT>> instrumentPage =
            instrumentRepository.findByLabAccessed(actor, lab, pageable, nameFilter);
        return builder(instrumentPage, activeTransformer);
    }

    public ResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> readOperatedInstruments(long actor) {
        final UserTemplate<?> user = entityFactories.userFromId.apply(actor);
        return ResultBuilder.builder(instrumentRepository.findAccessedWhereOperatorIs(actor), activeTransformer);
    }

    public ResultBuilder<AccessedInstrument<INSTRUMENT>, INSTRUMENT_LINE> readInstrumentLinesByLabAndStudyType(
        long actor, long lab, long studyType) {
        final List<AccessedInstrument<INSTRUMENT>> list =
            instrumentRepository.findByLabAndStudyTypeAccessed(actor, lab, studyType);
        return ResultBuilder.builder(list, activeTransformer);
    }
}
