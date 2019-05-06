package com.infoclinika.mssharing.platform.model.read;

import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.InstrumentItem;

import java.util.Objects;
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

    PagedItem<INSTRUMENT_LINE> readInstruments(long actor, PagedItemInfo pagedItemInfo);

    Set<INSTRUMENT_LINE> readInstrumentsByLab(long actor, long lab);

    PagedItem<INSTRUMENT_LINE> readInstrumentsByLab(long actor, long lab, PagedItemInfo pagedItemInfo);

    Set<INSTRUMENT_LINE> readInstrumentsByLabAndStudyType(long actor, long lab, long studyType);

    SortedSet<InstrumentItem> readInstrumentsWhereUserIsOperator(long actor);

    enum InstrumentAccess {
        NO_ACCESS, OPERATOR
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

        public InstrumentLineTemplate(long id, String name, String vendor, String lab, String serial, long creator,
                                      long files, String model, InstrumentAccess access) {
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
            access = other.access;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof InstrumentLineTemplate)) {
                return false;
            }
            InstrumentLineTemplate that = (InstrumentLineTemplate) o;
            return creator == that.creator &&
                files == that.files &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(vendor, that.vendor) &&
                Objects.equals(lab, that.lab) &&
                Objects.equals(serialNumber, that.serialNumber) &&
                Objects.equals(model, that.model) &&
                access == that.access;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, vendor, lab, serialNumber, creator, model, files, access);
        }
    }
}
