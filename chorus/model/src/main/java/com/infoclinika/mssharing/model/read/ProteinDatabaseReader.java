package com.infoclinika.mssharing.model.read;

import com.google.common.base.MoreObjects;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author andrii.loboda
 */
@Transactional(readOnly = true)
public interface ProteinDatabaseReader {

    List<ProteinDBItem> readAvailableProteinDatabasesByExperiment(long user, long experiment);

    List<ProteinDBItem> readAllAvailableProteinDatabases(long user);

    ProteinDBDetails readProteinDatabase(long user, long proteinDatabase);

    List<ProteinDBLine> readProteinDatabasesAccessibleByUser(long userId);

    List<ProteinDBLine> readUserProteinDatabases(long userId);

    List<ProteinDBLine> readPublicProteinDatabases(long userId);

    List<ProteinDBFilePersistItem> getAllMarkedToRePersist();

    class ProteinDBItem {
        public final long id;
        public final String name;
        public final long typeId;
        public final String typeName;

        public ProteinDBItem(long id, String name, long typeId, String typeName) {
            this.id = id;
            this.name = name;
            this.typeId = typeId;
            this.typeName = typeName;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("name", name)
                    .add("typeId", typeId)
                    .add("typeName", typeName)
                    .toString();
        }
    }

    class ProteinDBDetails {
        public final long id;
        public final String name;
        public final long typeId;
        public final String typeName;
        public final CloudStorageItemReference remoteProteinDbRef;
        public final Status status;
        public final boolean accessible;

        public ProteinDBDetails(long id, String name, long typeId, String typeName,
                                CloudStorageItemReference remoteProteinDbRef, Status status, boolean accessible) {
            this.id = id;
            this.name = name;
            this.typeId = typeId;
            this.typeName = typeName;
            this.remoteProteinDbRef = remoteProteinDbRef;
            this.status = status;
            this.accessible = accessible;
        }

        public static enum Status{
            NOT_PERSISTED, IN_PROGRESS, PERSISTED, NEED_TO_RE_PERSIST, FAILED;
        }
    }

    class ProteinDBLine {
        public final long id;
        public final String name;
        public final String typeName;
        public final Date uploadDate;
        public final boolean bPublic;
        public final boolean bReversed;
        public final long ownerId;

        public ProteinDBLine(long id, String name, String typeName, Date uploadDate, boolean bPublic, long ownerId, boolean bReversed) {
            this.id = id;
            this.name = name;
            this.typeName = typeName;
            this.uploadDate = uploadDate;
            this.bPublic = bPublic;
            this.ownerId = ownerId;
            this.bReversed = bReversed;
        }
    }

    class ProteinDBFilePersistItem{
        public final long id;
        public final Status status;

        public ProteinDBFilePersistItem(long id, Status status) {
            this.id = id;
            this.status = status;
        }

        public enum Status{
            NOT_PERSISTED, IN_PROGRESS, PESISTED, NEED_TO_RE_PERSIST;
        }
    }
}
