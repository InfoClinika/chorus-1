package com.infoclinika.mssharing.platform.model.read;

import com.google.common.collect.ImmutableSortedSet;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Herman Zamula
 */
public interface RequestsReaderTemplate {

    ImmutableSortedSet<LabRequest> myLabsInbox(long actor);

    ImmutableSortedSet<InstrumentCreationRequestInfo> myInstrumentCreationInbox(long actor);

    ImmutableSortedSet<LabMembershipRequest> myLabMembershipInbox(long actor);

    ImmutableSortedSet<ProjectSharingInfo> myProjectSharingInbox(long actor);

    ImmutableSortedSet<LabMembershipRequest> myLabMembershipOutbox(long actor);

    enum RequestType {
        INSTRUMENT,
        MEMBERSHIP
    }

    interface OriginalRequest<T> extends Comparable<T> {
        RequestType type();
    }

    abstract class ComparableOriginalRequest<T> implements OriginalRequest<T> {
        protected int compareAllFields(int... fieldComparisonResults) {
            for (int fieldComparisonResult : fieldComparisonResults) {
                if (fieldComparisonResult != 0) {
                    return fieldComparisonResult;
                }
            }
            return 0;
        }
    }

    abstract class ComparableRequest<T> implements Comparable<T> {
        protected int compareAllFields(int... fieldComparisonResults) {
            for (int fieldComparisonResult : fieldComparisonResults) {
                if (fieldComparisonResult != 0) {
                    return fieldComparisonResult;
                }
            }
            return 0;
        }
    }

    final class InstrumentCreationRequestInfo extends ComparableRequest<InstrumentCreationRequestInfo> {

        public final String contactEmail;
        public final String instrumentName;
        public final Date sent;
        public final long creationRequestId;

        public InstrumentCreationRequestInfo(String contactEmail, String instrumentName,
                                             Date sent, long creationRequestId) {
            this.contactEmail = contactEmail;
            this.instrumentName = instrumentName;
            this.sent = sent;
            this.creationRequestId = creationRequestId;
        }

        @Override
        public int compareTo(InstrumentCreationRequestInfo o) {
            return compareAllFields(
                sent.compareTo(o.sent),
                Long.compare(creationRequestId, o.creationRequestId),
                instrumentName.compareTo(o.instrumentName),
                contactEmail.compareTo(o.contactEmail)
            );
        }
    }

    final class InstrumentRequest extends ComparableOriginalRequest<InstrumentRequest> {
        public final String requesterName;
        public final long requester;
        public final String instrumentName;
        public final Date sent;
        public final long instrument;

        public InstrumentRequest(String requesterName,
                                 long requester,
                                 String instrumentName,
                                 Date sent,
                                 long instrument) {
            this.requesterName = requesterName;
            this.requester = requester;
            this.instrumentName = instrumentName;
            this.sent = sent;
            this.instrument = instrument;
        }

        @Override
        public RequestType type() {
            return RequestType.INSTRUMENT;
        }

        @Override
        public int compareTo(InstrumentRequest o) {
            return compareAllFields(
                sent.compareTo(o.sent),
                requesterName.compareTo(o.requesterName),
                instrumentName.compareTo(o.instrumentName),
                Long.compare(instrument, o.instrument),
                Long.compare(requester, o.requester)
            );
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof InstrumentRequest)) {
                return false;
            }
            InstrumentRequest that = (InstrumentRequest) o;
            return requester == that.requester &&
                instrument == that.instrument &&
                Objects.equals(requesterName, that.requesterName) &&
                Objects.equals(instrumentName, that.instrumentName) &&
                Objects.equals(sent, that.sent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(requesterName, requester, instrumentName, sent, instrument);
        }
    }

    final class LabRequest extends ComparableRequest<LabRequest> {
        public final String contactEmail;
        public final String labName;
        public final Date sent;
        public final long labRequest;

        public LabRequest(String contactEmail, String labName, Date sent, long labRequest) {
            this.contactEmail = contactEmail;
            this.labName = labName;
            this.sent = sent;
            this.labRequest = labRequest;
        }

        @Override
        public int compareTo(LabRequest o) {
            return compareAllFields(
                sent.compareTo(o.sent),
                Long.compare(labRequest, o.labRequest),
                labName.compareTo(o.labName),
                contactEmail.compareTo(o.contactEmail)
            );
        }
    }

    final class ProjectSharingInfo extends ComparableRequest<ProjectSharingInfo> {
        public final long projectSharingRequest;
        public final String requesterName;
        public final long requester;
        public final String projectName;
        public final long project;
        public final Date sent;
        public final List<String> experimentLinks;

        public ProjectSharingInfo(long projectSharingRequest,
                                  String requesterName,
                                  long requester,
                                  String projectName,
                                  long project,
                                  Date sent,
                                  List<String> experimentLinks) {
            this.projectSharingRequest = projectSharingRequest;
            this.requesterName = requesterName;
            this.requester = requester;
            this.projectName = projectName;
            this.project = project;
            this.sent = sent;
            this.experimentLinks = experimentLinks;
        }

        @Override
        public int compareTo(ProjectSharingInfo o) {
            return compareAllFields(
                sent.compareTo(o.sent),
                Long.compare(requester, o.requester),
                requesterName.compareTo(o.requesterName),
                Long.compare(project, o.project),
                projectName.compareTo(o.projectName),
                Long.compare(projectSharingRequest, o.projectSharingRequest)
            );
        }
    }

    final class LabMembershipRequest extends ComparableOriginalRequest<LabMembershipRequest> {
        public final long labId;
        public final String labName;
        public final String requesterName;
        public final String requesterEmail;
        public final Date sent;
        public final long requestId;


        public LabMembershipRequest(long request,
                                    long labId,
                                    Date sent,
                                    String requesterEmail,
                                    String requesterName,
                                    String labName) {
            this.requestId = request;
            this.sent = sent;
            this.requesterEmail = requesterEmail;
            this.requesterName = requesterName;
            this.labName = labName;
            this.labId = labId;
        }

        @Override
        public RequestType type() {
            return RequestType.MEMBERSHIP;
        }

        @Override
        public int compareTo(LabMembershipRequest o) {
            return compareAllFields(
                sent.compareTo(o.sent),
                Long.compare(requestId, o.requestId),
                requesterName.compareTo(o.requesterName),
                Long.compare(labId, o.labId),
                labName.compareTo(o.labName),
                requesterEmail.compareTo(o.requesterEmail)
            );
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LabMembershipRequest)) {
                return false;
            }
            LabMembershipRequest that = (LabMembershipRequest) o;
            return labId == that.labId &&
                requestId == that.requestId &&
                Objects.equals(labName, that.labName) &&
                Objects.equals(requesterName, that.requesterName) &&
                Objects.equals(requesterEmail, that.requesterEmail) &&
                Objects.equals(sent, that.sent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(labId, labName, requesterName, requesterEmail, sent, requestId);
        }
    }

    final class GeneralRequest extends ComparableRequest<GeneralRequest> {
        public final String requesterName;
        public final Date sent;
        public final RequestType type;
        public final OriginalRequest originalRequest;
        public final String description;
        public final boolean showActions;  //todo [pavel.kaplin] for mocking purposes, move this parameter to type

        public GeneralRequest(InstrumentRequest originalRequest) {
            this.originalRequest = originalRequest;
            this.requesterName = originalRequest.requesterName;
            this.sent = originalRequest.sent;
            this.type = originalRequest.type();
            this.description = null;
            this.showActions = true;
        }

        public GeneralRequest(LabMembershipRequest originalRequest) {
            this.originalRequest = originalRequest;
            this.requesterName = originalRequest.requesterName;
            this.sent = originalRequest.sent;
            this.type = originalRequest.type();
            this.description = null;
            this.showActions = true;
        }

        public GeneralRequest(String requesterName,
                              Date sent,
                              RequestType type,
                              OriginalRequest originalRequest,
                              String description,
                              boolean showActions) {
            this.requesterName = requesterName;
            this.sent = sent;
            this.type = type;
            this.originalRequest = originalRequest;
            this.description = description;
            this.showActions = showActions;
        }

        @Override
        public int compareTo(GeneralRequest o) {
            return compareAllFields(
                sent.compareTo(o.sent),
                requesterName.compareTo(o.requesterName),
                hashCode() - o.hashCode()
            );
        }
    }


    final class RequestCounter {
        public final long number;

        public RequestCounter(long number) {
            this.number = number;
        }
    }

    class InstrumentRequestDetails {

        public final String requesterName;
        public final String instrumentName;
        public final Date sent;
        public final String vendor;
        public final String type;
        public final String model;
        public final String serialNumber;
        public final String hplc;
        public final String peripherals;
        public final String lab;

        public InstrumentRequestDetails(String requesterName, String instrumentName, Date sent, String vendor,
                                        String type, String model, String serialNumber,
                                        String hplc, String peripherals, String lab) {

            this.requesterName = requesterName;
            this.instrumentName = instrumentName;
            this.sent = sent;
            this.vendor = vendor;
            this.type = type;
            this.model = model;
            this.serialNumber = serialNumber;
            this.hplc = hplc;
            this.peripherals = peripherals;
            this.lab = lab;
        }
    }
}
