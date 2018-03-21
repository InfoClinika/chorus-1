package com.infoclinika.mssharing.platform.model.helper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.common.items.FileItem;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * @author Herman Zamula
 */
public interface ExperimentCreationHelperTemplate {
    /**
     * User has access to some instruments. Each instrument has model.
     *
     * @param actor - user ID
     * @param lab   - lab ID
     */
    List<DictionaryItem> availableInstrumentModels(long actor, @Nullable Long lab);


    /**
     * Returns list of instrument models for specified lab, technology type and vendor.
     */
    List<DictionaryItem> availableInstrumentModels(long actor, @Nullable Long lab, long technologyType, long vendor);


    /**
     * Returns list of instrument models for specified lab, technology type, vendor and instrument type.
     */
    List<DictionaryItem> availableInstrumentModels(long actor, @Nullable Long lab, long technologyType, long vendor, long instrumentType);

    /**
     * Returns list of instrument types for specified lab, technology type and vendor.
     */
    List<DictionaryItem> availableInstrumentTypes(long actor, @Nullable Long lab, long technologyType, long vendor);

    /**
     * User has access to some instruments. Each instrument has model.
     *
     * @param actor                      - user ID
     * @param instrumentModelRestriction - model of all returned instrument
     * @return available instruments for user with specified type restriction
     */
    List<InstrumentItem> availableInstrumentsByModel(long actor, long instrumentModelRestriction);

    /**
     * User can upload RAW file with are produced by some instrument.
     *
     * @param actor           - user ID
     * @param specie          specie id
     * @param instrumentModel - restrict instrument by model
     * @param lab             - restrict instruments by lab (optional)
     * @return available to user files that was produced by instruments that was not restricted.
     */
    List<FileItem> availableFilesByInstrumentModel(long actor, long specie, long instrumentModel, @Nullable Long lab);

    /**
     * User can upload RAW file with are produced by some instrument.
     *
     * @param actor      - user ID
     * @param specie     Specie id
     * @param instrument - restrict instrument by concrete instrument.  @return available to user files that was produced by instruments that was not restricted.
     */
    List<FileItem> availableFilesByInstrument(long actor, long specie, long instrument);

    /**
     * Used to prevent duplicate project creation with the same name
     *
     * @param actor user id to get it's experiments
     * @return experiments created by given user
     */
    Set<NamedItem> ownedExperiments(long actor);

    /**
     * @param actor user to get available projects for him
     * @return projects where user can create his experiment sorted by name
     */
    ImmutableSortedSet<NamedItem> availableProjects(long actor);

    ImmutableSortedSet<NamedItem> availableLabs(long actor);

    ImmutableSortedSet<ExperimentTypeItem> experimentTypes();

    ImmutableSet<DictionaryItem> species();

    DictionaryItem defaultSpecie();

    DictionaryItem specie(long id);

    class ExperimentTypeItem extends DictionaryItem {
        public final boolean allowed2DLC;
        public final boolean allowLabels;

        public ExperimentTypeItem(long id, String name, boolean allowed2DLC, boolean allowLabels) {
            super(id, name);
            this.allowed2DLC = allowed2DLC;
            this.allowLabels = allowLabels;
        }
    }

}
