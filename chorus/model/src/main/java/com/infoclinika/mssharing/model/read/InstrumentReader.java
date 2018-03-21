package com.infoclinika.mssharing.model.read;

import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

/**
 * @author Andrii Loboda
 */
public interface InstrumentReader {
    SortedSet<? extends InstrumentItem> readInstrumentItemsWhereUserIsOperator(long actor);

    List<InstrumentLine> findByInstrumentModel(long actor, long instrumentModel);

    Optional<InstrumentLine> readDefaultInstrument(long actor, long lab, long instrumentModel);
}
