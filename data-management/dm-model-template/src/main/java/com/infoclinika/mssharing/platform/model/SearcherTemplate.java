package com.infoclinika.mssharing.platform.model;

import com.google.common.collect.ImmutableList;
import com.infoclinika.mssharing.platform.model.read.ExperimentReaderTemplate.ExperimentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate.FileLineTemplate;
import com.infoclinika.mssharing.platform.model.read.InstrumentReaderTemplate.InstrumentLineTemplate;
import com.infoclinika.mssharing.platform.model.read.ProjectReaderTemplate.ProjectLineTemplate;

/**
 * @author Herman Zamula
 */
public interface SearcherTemplate<
        PROJECT extends ProjectLineTemplate,
        EXPERIMENT extends ExperimentLineTemplate,
        FILE extends FileLineTemplate,
        INSTRUMENT extends InstrumentLineTemplate> {

    ImmutableList<PROJECT> projects(long actor, String query);

    ImmutableList<EXPERIMENT> experiments(long actor, String query);

    ImmutableList<FILE> files(long actor, String query);

    ImmutableList<INSTRUMENT> instruments(long actor, String query);

    PagedItem<PROJECT> pagedProjects(long actor, PagedItemInfo pagedItemInfo);

    PagedItem<PROJECT> pagedProjectsWithId(long actor, PagedItemInfo pagedItemInfo);

    PagedItem<EXPERIMENT> pagedExperiments(long actor, PagedItemInfo pagedItemInfo);

    PagedItem<FILE> pagedFiles(long actor, PagedItemInfo pagedItemInfo);

    PagedItem<INSTRUMENT> pagedInstruments(long actor, PagedItemInfo pagedItemInfo);

    Count getItemsCount(PagedItemInfo pagedItemInfo, long actor);

    public static class Count {
        public final long instruments;
        public final long projects;
        public final long files;
        public final long experiments;

        public Count(long instruments, long projects, long files, long experiments) {
            this.instruments = instruments;
            this.projects = projects;
            this.files = files;
            this.experiments = experiments;
        }
    }
}
