package com.infoclinika.mssharing.platform.model.write;

import com.google.common.base.Optional;

import java.util.List;

/**
 * @author Herman Zamula
 */
public interface InstrumentManagementTemplate<DETAILS extends InstrumentManagementTemplate.InstrumentDetailsTemplate> {

    long createInstrument(long creator, long labId, long model, DETAILS instrumentDetails);

    void editInstrument(long actor, long instrumentId, DETAILS details);

    void deleteInstrument(long actor, long instrumentId);

    void setInstrumentOperators(long userId, long instrumentId, List<Long> newOperators);

    void addOperatorDirectly(long initiator, long instrumentId, long newOperator);

    void requestAccessToInstrument(long initiator, long instrumentId);

    void refuseAccessToInstrument(long actor, long instrumentId, long initiatorId, String refuseComment);

    void approveAccessToInstrument(long actor, long instrumentId, long initiatorId);

    long approveInstrumentCreation(long actor, long requestId);

    void refuseInstrumentCreation(long actor, long requestId, String refuseComment);

    Optional<Long> newInstrumentRequest(long creator, long labId, long model, DETAILS instrumentDetails, List<Long> operators);

    Optional<Long> updateNewInstrumentRequest(long actor, long requestId, long model, DETAILS details, List<Long> operators);

    class InstrumentDetailsTemplate {

        public final String name;
        public final String serialNumber;
        public final String peripherals;

        public InstrumentDetailsTemplate(String name, String serialNumber, String peripherals) {
            this.name = name;
            this.serialNumber = serialNumber;
            this.peripherals = peripherals;
        }
    }

    class StaleInstrumentCreationRequestException extends RuntimeException {
        private final long requestId;

        public StaleInstrumentCreationRequestException(long requestId) {
            this.requestId = requestId;
        }

        @Override
        public String getMessage() {
            return "Cannot find the request for the ID: " + requestId;
        }
    }
}
