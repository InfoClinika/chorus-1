package com.infoclinika.mssharing.platform.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author Herman Zamula
 */
public interface InstrumentReaderTemplate<INSTRUMENT_LINE extends InstrumentReaderTemplate.InstrumentLineTemplate> {

    /**
     * Returns all instruments where actor has access
     *
     * @param actor user to get instruments
     * @return Set of instruments
     */
    Set<INSTRUMENT_LINE> readInstruments(long actor);

    Set<INSTRUMENT_LINE> readInstrumentsByLab(long actor, long lab);

    PagedItem<INSTRUMENT_LINE> readInstruments(long actor, PagedItemInfo pagedItemInfo);

    PagedItem<INSTRUMENT_LINE> readInstrumentsByLab(long actor, long lab, PagedItemInfo pagedItemInfo);

    SortedSet<InstrumentItem> readInstrumentsWhereUserIsOperator(long actor);

    enum InstrumentAccess {
        NO_ACCESS, OPERATOR, PENDING
    }

    class InstrumentLineTemplate {
        public final Long id;
        public final String name;
        public final String vendor;
        public final String lab;
        public final String serialNumber;
        public final long creator;
        public final String model;
        public final long files;
        public final InstrumentAccess access;

        public InstrumentLineTemplate(long id, String name, String vendor, String lab, String serial, long creator, long files, String model, InstrumentAccess access) {
            this.id = id;
            this.name = name;
            this.vendor = vendor;
            this.lab = lab;
            this.serialNumber = serial;
            this.creator = creator;
            this.files = files;
            this.model = model;
            this.access = access;
        }

        public InstrumentLineTemplate(InstrumentLineTemplate other) {
            id = other.id;
            name = other.name;
            vendor = other.vendor;
            lab = other.lab;
            serialNumber = other.serialNumber;
            creator = other.creator;
            model = other.model;
            files = other.files;
            this.access = other.access;
        }

        @Override
        @SuppressWarnings("all")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InstrumentLineTemplate)) return false;

            InstrumentLineTemplate that = (InstrumentLineTemplate) o;

            if (creator != that.creator) return false;
            if (files != that.files) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (lab != null ? !lab.equals(that.lab) : that.lab != null) return false;
            if (model != null ? !model.equals(that.model) : that.model != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (serialNumber != null ? !serialNumber.equals(that.serialNumber) : that.serialNumber != null)
                return false;
            if (vendor != null ? !vendor.equals(that.vendor) : that.vendor != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
            result = 31 * result + (lab != null ? lab.hashCode() : 0);
            result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
            result = 31 * result + (int) (creator ^ (creator >>> 32));
            result = 31 * result + (model != null ? model.hashCode() : 0);
            result = 31 * result + (int) (files ^ (files >>> 32));
            return result;
        }
    }
}
