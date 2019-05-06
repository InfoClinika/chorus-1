package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.FileAccessLog;
import com.infoclinika.mssharing.model.internal.repository.FileAccessLogRepository;
import com.infoclinika.mssharing.model.read.FileAccessLogReader;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static com.google.common.collect.FluentIterable.from;

/**
 * @author vladislav.kovchug
 */

@Service
public class FileAccessLogReaderImpl implements FileAccessLogReader {

    @Inject
    private RuleValidator ruleValidator;

    @Inject
    private Transformers.PagedItemsTransformer pagedItemsTransformer;

    @Inject
    private FileAccessLogRepository fileAccessLogRepository;

    private final Function<FileAccessLog, FileAccessLogDTO> fileAccessLogTransformer =
        input -> new FileAccessLogDTO(
            input.getId(),
            input.getUserEmail(),
            input.getUserLabName(),
            input.getFileSize(),
            input.getFileContentId(),
            input.getFileArchiveId(),
            input.getFileName(),
            input.getOperationType().toString(),
            input.getOperationDate()
        );

    @Override
    public PagedItem<FileAccessLogDTO> readLogs(long actor, PagedItemInfo pagedItem) {
        if (!ruleValidator.hasAdminRights(actor)) {
            throw new AccessDenied("User cannot read file access log");
        }

        final PageRequest pageRequest = pagedItemsTransformer.toPageRequest(FileAccessLog.class, pagedItem);
        final Page<FileAccessLog> logs = fileAccessLogRepository.findAll(pageRequest);

        return new PagedItem<>(
            logs.getTotalPages(),
            logs.getTotalElements(),
            logs.getNumber(),
            logs.getNumberOfElements(),
            from(logs).transform(fileAccessLogTransformer).toList()
        );
    }
}
