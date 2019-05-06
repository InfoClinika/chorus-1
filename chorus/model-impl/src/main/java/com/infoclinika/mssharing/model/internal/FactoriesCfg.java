package com.infoclinika.mssharing.model.internal;

import com.google.common.base.Supplier;
import com.infoclinika.mssharing.model.internal.entity.*;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveFileMetaData;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import com.infoclinika.mssharing.platform.entity.*;
import com.infoclinika.mssharing.platform.entity.restorable.ExperimentTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.FileMetaDataTemplate;
import com.infoclinika.mssharing.platform.entity.restorable.ProjectTemplate;
import com.infoclinika.mssharing.platform.model.EntityFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Herman Zamula
 */
@Configuration
public class FactoriesCfg {

    @Bean
    public EntityFactories entityFactories() {

        return new EntityFactories.Builder()
                .user(new Supplier<UserTemplate>() {
                    @Override
                    public UserTemplate get() {
                        return new User();
                    }
                })
                .project(new Supplier<ProjectTemplate>() {
                    @Override
                    public ProjectTemplate get() {
                        return new ActiveProject();
                    }
                })
                .experiment(new Supplier<ExperimentTemplate>() {
                    @Override
                    public ExperimentTemplate get() {
                        return new ActiveExperiment();
                    }
                })
                .fileMetaData(new Supplier<FileMetaDataTemplate>() {
                    @Override
                    public FileMetaDataTemplate get() {
                        return new ActiveFileMetaData();
                    }
                })
                .rawFile(new Supplier<ExperimentFileTemplate>() {
                    @Override
                    public ExperimentFileTemplate get() {
                        return new RawFile();
                    }
                })
                .instrument(new Supplier<InstrumentTemplate>() {
                    @Override
                    public InstrumentTemplate get() {
                        return new Instrument();
                    }
                })
                .factor(new Supplier<FactorTemplate>() {
                    @Override
                    public FactorTemplate get() {
                        return new Factor();
                    }
                })
                .level(new Supplier<LevelTemplate>() {
                    @Override
                    public LevelTemplate get() {
                        return new Level();
                    }
                })
                .group(new Supplier<GroupTemplate>() {
                    @Override
                    public GroupTemplate get() {
                        return new Group();
                    }
                })
                .lab(new Supplier<LabTemplate>() {
                    @Override
                    public LabTemplate get() {
                        return new Lab();
                    }
                })
                .instrumentRequest(new Supplier<InstrumentCreationRequestTemplate>() {
                    @Override
                    public InstrumentCreationRequestTemplate get() {
                        return new InstrumentCreationRequest();
                    }
                })
                .build();
    }
}
