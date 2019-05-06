package com.infoclinika.mssharing.web.controller.v2.dto;

import com.infoclinika.mssharing.model.internal.entity.upload.UploadDetails;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadFileStatus;
import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.stream.Collectors.toList;

/**
 * @author Vitalii Petkanych
 */
public class UploadDetailsDTO {
    private long id;
    private UploadType type;
    private List<UploadFileDTO> files;
    private String status;
    private byte progress;
    private long durationInMillis;

    public UploadDetailsDTO() {
    }

    public UploadDetailsDTO(
        long id,
        UploadType type,
        List<UploadFileDTO> files,
        String status,
        byte progress,
        long durationInMillis
    ) {
        this.id = id;
        this.type = type;
        this.files = files;
        this.status = status;
        this.progress = progress;
        this.durationInMillis = durationInMillis;
    }

    public static UploadDetailsDTO of(UploadDetails upload) {
        final UploadDetailsDTO dto = new UploadDetailsDTO();
        dto.setId(upload.getId());
        dto.setType(upload.getType());
        final AtomicLong sizeTotal = new AtomicLong();
        final AtomicLong sizeUploaded = new AtomicLong();
        final List<UploadFileDTO> files = upload.getFiles()
            .stream()
            .peek(file -> {
                if (file.getStatus() != UploadFileStatus.CANCELED && file.getStatus() != UploadFileStatus.FAILED) {
                    sizeTotal.addAndGet(file.getSize());
                    sizeUploaded.addAndGet(file.getSizeUploaded());
                }
            })
            .map(UploadFileDTO::of)
            .collect(toList());
        dto.setFiles(files);
        dto.setStatus(upload.getStatus() == null ? "" : upload.getStatus().name());
        dto.setProgress(calcProgressPercents(sizeTotal, sizeUploaded));
        dto.setDurationInMillis(calcDurationInMillis(upload));
        return dto;
    }

    private static byte calcProgressPercents(AtomicLong sizeTotal, AtomicLong sizeUploaded) {
        return sizeTotal.get() == 0
            ? 0
            : (byte) (100 * sizeUploaded.get() / sizeTotal.get());
    }

    private static long calcDurationInMillis(UploadDetails upload) {
        final LocalDateTime finishedOrNow = Optional.ofNullable(upload.getUploadFinished()).orElse(LocalDateTime.now());
        return Duration.between(upload.getUploadStarted(), finishedOrNow).toMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UploadType getType() {
        return type;
    }

    public void setType(UploadType type) {
        this.type = type;
    }

    public List<UploadFileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<UploadFileDTO> files) {
        this.files = files;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte getProgress() {
        return progress;
    }

    public void setProgress(byte progress) {
        this.progress = progress;
    }

    public long getDurationInMillis() {
        return durationInMillis;
    }

    public void setDurationInMillis(long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }
}
