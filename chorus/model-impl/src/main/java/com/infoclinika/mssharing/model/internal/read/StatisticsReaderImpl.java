package com.infoclinika.mssharing.model.internal.read;

import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.FileMetaDataRepository;
import com.infoclinika.mssharing.model.internal.repository.ProjectRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.StatisticsReader;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author timofey.kasyanov
 */
@Service
public class StatisticsReaderImpl implements StatisticsReader {

    @Inject
    private UserRepository userRepository;
    @Inject
    private FileMetaDataRepository fileMetaDataRepository;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private ExperimentRepository experimentRepository;

    @Override
    public long readUsersCount() {
        return userRepository.count();
    }

    @Override
    public long readFilesSize() {
        return fileMetaDataRepository.sizeOfAll();
    }

    @Override
    public long readFilesCount() {
        return fileMetaDataRepository.count();
    }

    @Override
    public long readPublicProjectsCount() {
        return projectRepository.countOnlyPublic();
    }

    @Override
    public long readPublicExperimentsCount() {
        return experimentRepository.countOnlyPublic();
    }
}
