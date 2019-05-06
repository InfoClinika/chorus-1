package com.infoclinika.mssharing.model.internal.repository;

import com.infoclinika.mssharing.model.internal.entity.CopyProjectRequest;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveProject;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface CopyProjectRequestRepository extends CrudRepository<CopyProjectRequest, Long> {
    List<CopyProjectRequest> findByReceiver(User receiver);

    List<CopyProjectRequest> findByReceiverAndProject(User receiver, ActiveProject project);

    List<CopyProjectRequest> findByProject(ActiveProject project);
}
