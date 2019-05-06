package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Elena Kurilina
 */
@Entity
@Table(name = "files_download_group")
public class FilesDownloadGroup extends AbstractPersistable<Long> {

    @Column(name = "experiment_id")
    public Long experimentId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "filegroup_to_job",
        joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "job_id", referencedColumnName = "id", nullable = false)
    )
    private Set<FileDownloadJob> jobs = newHashSet();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "filegroup_to_user",
        joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    )
    private Set<User> notifiers = newHashSet();

    public FilesDownloadGroup(Long experimentId) {
        this.experimentId = experimentId;
    }

    public FilesDownloadGroup() {
    }

    public Set<FileDownloadJob> getJobs() {
        return jobs;
    }

    public Set<User> getNotifiers() {
        return notifiers;
    }
}
