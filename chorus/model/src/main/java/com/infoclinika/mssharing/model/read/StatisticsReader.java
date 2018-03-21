package com.infoclinika.mssharing.model.read;

/**
 * @author timofey.kasyanov
 */
public interface StatisticsReader {
    long readUsersCount();

    long readFilesSize();

    long readFilesCount();

    long readPublicProjectsCount();

    long readPublicExperimentsCount();

    class UsageStatisticsInfo {
        public final long usersCount;
        public final long filesSize;
        public final long filesCount;
        public final long projectsCount;
        public final long experimentsCount;

        public UsageStatisticsInfo(long usersCount, long filesSize, long filesCount, long projectsCount, long experimentsCount) {
            this.usersCount = usersCount;
            this.filesSize = filesSize;
            this.filesCount = filesCount;
            this.projectsCount = projectsCount;
            this.experimentsCount = experimentsCount;
        }
    }
}
