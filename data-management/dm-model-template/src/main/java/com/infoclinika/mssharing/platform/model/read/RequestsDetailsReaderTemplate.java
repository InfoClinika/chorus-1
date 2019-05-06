package com.infoclinika.mssharing.platform.model.read;

import java.util.Date;

/**
 * @author Herman Zamula
 */
public interface RequestsDetailsReaderTemplate<
    INSTRUMENT_CREATION extends RequestsDetailsReaderTemplate.InstrumentCreationItemTemplate,
    LAB_CREATION extends DetailsReaderTemplate.LabItemTemplate> {

    INSTRUMENT_CREATION readInstrumentCreation(long actor, long request);

    LAB_CREATION readLabRequestDetails(long actor, long lab);

    class InstrumentCreationItemTemplate {

        public final long id;
        public final String name;
        public final String serialNumber;
        public final String peripherals;
        public final String labName;
        public final long labId;
        public final long model;
        public final long vendor;
        public final UserItem requester;
        public final Date sent;

        public InstrumentCreationItemTemplate(long id,
                                              String name,
                                              String serialNumber,
                                              String peripherals,
                                              String labName,
                                              long labId,
                                              long model,
                                              long vendor,
                                              UserItem requester,
                                              Date sent) {
            this.id = id;
            this.name = name;
            this.serialNumber = serialNumber;
            this.peripherals = peripherals;
            this.labName = labName;
            this.labId = labId;
            this.model = model;
            this.vendor = vendor;
            this.requester = requester;
            this.sent = sent;
        }

        public InstrumentCreationItemTemplate(InstrumentCreationItemTemplate other) {
            this.id = other.id;
            this.name = other.name;
            this.serialNumber = other.serialNumber;
            this.peripherals = other.peripherals;
            this.labName = other.labName;
            this.labId = other.labId;
            this.model = other.model;
            this.vendor = other.vendor;
            this.requester = other.requester;
            this.sent = other.sent;
        }
    }

    class UserItem {
        public final long id;
        public final String email;
        public final String name;

        public UserItem(long id, String email, String name) {
            this.id = id;
            this.email = email;
            this.name = name;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            UserItem userItem = (UserItem) o;

            if (id != userItem.id) {
                return false;
            }
            if (email != null ? !email.equals(userItem.email) : userItem.email != null) {
                return false;
            }
            return !(name != null ? !name.equals(userItem.name) : userItem.name != null);

        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (email != null ? email.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }
}
