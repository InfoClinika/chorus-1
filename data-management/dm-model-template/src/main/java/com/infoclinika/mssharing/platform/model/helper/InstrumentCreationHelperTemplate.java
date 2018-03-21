package com.infoclinika.mssharing.platform.model.helper;

import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;

/**
 * @author Stanislav Kurilin, Herman Zamula
 */
public interface InstrumentCreationHelperTemplate<OPERATOR extends InstrumentCreationHelperTemplate.PotentialOperator> {
    ImmutableSortedSet<OPERATOR> availableOperators(long lab);

    ImmutableSortedSet<DictionaryItem> studyTypes();

    ImmutableSortedSet<DictionaryItem> vendors(long studyType);

    ImmutableSortedSet<DictionaryItem> vendors();

    ImmutableSortedSet<DictionaryItem> vendorsWithFolderArchiveUploadSupport();

    ImmutableSortedSet<DictionaryItem> models(long vendor);

    ImmutableSortedSet<DictionaryItem> models(long vendor, long technologyType);

    class PotentialOperator {
        public final long id;
        public final String name;
        public final String email;

        public PotentialOperator(long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }
}
