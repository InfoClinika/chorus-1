package com.infoclinika.mssharing.platform.model.write;

import com.infoclinika.mssharing.platform.model.write.UserManagementTemplate.PersonInfo;

/**
 * @author Herman Zamula
 */
public interface LabManagementTemplate<LAB_INFO extends LabManagementTemplate.LabInfoTemplate> {

    void editLab(Long actor, Long lab, LAB_INFO labInfo);

    Long createLab(Long actor, LAB_INFO labInfo, String contactEmail);

    Long requestLabCreation(LabInfoTemplate labInfo, String contactEmail);

    void editLabRequestInfo(Long actor, Long requestId, LabInfoTemplate labInfo);

    Long confirmLabCreation(Long actor, Long labCreationRequestId);

    void rejectLabCreation(Long actor, Long labCreationRequestId, String rejectComment);

    boolean isLabHead(long actor, long lab);

    class LabInfoTemplate {
        public final String institutionUrl;
        public final PersonInfo labHead;
        public final String labName;

        public LabInfoTemplate(String institutionUrl, PersonInfo labHead, String labName) {
            this.institutionUrl = institutionUrl;
            this.labHead = labHead;
            this.labName = labName;
        }
    }

    class StaleLabCreationRequestException extends RuntimeException {
        private final long requestId;

        public StaleLabCreationRequestException(long requestId) {
            this.requestId = requestId;
        }

        @Override
        public String getMessage() {
            return "Cannot find the request for the ID: " + requestId;
        }
    }
}
