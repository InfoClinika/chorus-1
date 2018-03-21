package com.infoclinika.mssharing.web.controller.request;

/**
 * @author andrii.loboda
 */
public class CompleteExperimentAnnotationRequest {
    public long experimentId;
    //all of the attachments including the existing attachments
    public long annotationAttachmentId;

    @Override
    public String toString() {
        return "CompleteExperimentAnnotationRequest{" +
                "experimentId=" + experimentId +
                ", annotationAttachmentId=" + annotationAttachmentId +
                '}';
    }
}
