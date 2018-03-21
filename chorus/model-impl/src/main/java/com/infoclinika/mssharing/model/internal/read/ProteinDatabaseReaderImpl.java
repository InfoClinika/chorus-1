package com.infoclinika.mssharing.model.internal.read;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.infoclinika.analysis.storage.cloud.CloudStorageItemReference;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.model.internal.entity.ProteinDatabase;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.Util;
import com.infoclinika.mssharing.model.internal.entity.restorable.ActiveExperiment;
import com.infoclinika.mssharing.model.internal.repository.ExperimentRepository;
import com.infoclinika.mssharing.model.internal.repository.ProteinDatabaseRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import com.infoclinika.mssharing.model.read.ProteinDatabaseReader;
import com.infoclinika.mssharing.model.write.ExperimentCategory;
import com.infoclinika.mssharing.platform.entity.Species;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Lists.transform;
import static com.infoclinika.mssharing.model.internal.entity.ProteinDatabase.ProteinDatabaseStatus.NEED_TO_RE_PERSIST;

/**
 * @author andrii.loboda
 */
@Service
public class ProteinDatabaseReaderImpl implements ProteinDatabaseReader {
    @Inject
    private ExperimentRepository experimentRepository;
    @Inject
    private ProteinDatabaseRepository proteinDatabaseRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private Transformers transformers;
    @Inject
    private RuleValidator ruleValidator;
    @Value("${amazon.active.bucket}")
    private String targetBucket;

    private final Function<ProteinDatabase, ProteinDBLine> proteinDatabaseTransformer = new Function<ProteinDatabase, ProteinDBLine>() {
        @Override
        public ProteinDBLine apply(ProteinDatabase pd) {
            return new ProteinDBLine(pd.getId(), pd.getName(), pd.getSpecie().getName(), pd.getUploadDate(),
                    pd.isbPublic(), pd.getUser().getId(), pd.isReversed());
        }
    };

    @Override
    public List<ProteinDBItem> readAvailableProteinDatabasesByExperiment(final long actor, final long experimentId) {
        final ActiveExperiment experiment = experimentRepository.findOne(experimentId);
        final Species specieOfExperiment = experiment.getSpecie();
        final ExperimentCategory experimentCategory = experiment.getExperimentCategory();

        final Iterable<ProteinDatabase> filteredDbs = Iterables.filter(getAvailableProteinDatabases(actor), new Predicate<ProteinDatabase>() {
            @Override
            public boolean apply(ProteinDatabase pd) {
                return (specieOfExperiment.isUnspecified() || specieOfExperiment.equals(pd.getSpecie()))
                        && experimentCategory == pd.getCategory();
            }
        });
        return from(filteredDbs)
                .transform(transformers.proteinDBItemTransformer)
                .toSortedList(new Comparator<ProteinDBItem>() {
                    @Override
                    public int compare(ProteinDBItem o1, ProteinDBItem o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
    }

    @Override
    public List<ProteinDBItem> readAllAvailableProteinDatabases(final long actor) {
        Set<ProteinDatabase> dbsSet = getAvailableProteinDatabases(actor);
        return from(dbsSet).transform(transformers.proteinDBItemTransformer).toSortedList(new Comparator<ProteinDBItem>() {
            @Override
            public int compare(ProteinDBItem o1, ProteinDBItem o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    @Override
    public ProteinDBDetails readProteinDatabase(long user, long proteinDatabaseId) {
        if (!ruleValidator.canReadProteinDatabase(user, proteinDatabaseId)) {
            throw new AccessDenied("User " + user + " cannot read protein database " + proteinDatabaseId);
        }
        final ProteinDatabase pd = proteinDatabaseRepository.findOne(proteinDatabaseId);
        return new ProteinDBDetails(pd.getId(), pd.getName(), pd.getSpecie().getId(), pd.getSpecie().getName(),
                pd.getContentId() != null ? new CloudStorageItemReference(targetBucket, pd.getContentId()) : null,
                ProteinDBDetails.Status.valueOf(pd.getStatus().name()), pd.getUser().getId().equals(user));
    }

    @Override
    public List<ProteinDBLine> readProteinDatabasesAccessibleByUser(long userId) {
        final User user = userRepository.findOne(userId);
        return from(proteinDatabaseRepository.findMyAndPublic(user)).transform(proteinDatabaseTransformer).toList();
    }

    @Override
    public List<ProteinDBLine> readUserProteinDatabases(long userId) {
        return from(proteinDatabaseRepository.findMy(userId)).transform(proteinDatabaseTransformer).toList();
    }

    @Override
    public List<ProteinDBLine> readPublicProteinDatabases(long userId) {
        return from(proteinDatabaseRepository.findPublic()).transform(proteinDatabaseTransformer).toList();
    }

    @Override
    public List<ProteinDBFilePersistItem> getAllMarkedToRePersist() {
        return newLinkedList(transform(proteinDatabaseRepository.findAllByStatus(NEED_TO_RE_PERSIST), new Function<ProteinDatabase, ProteinDBFilePersistItem>() {
            @Override
            public ProteinDBFilePersistItem apply(ProteinDatabase db) {
                return new ProteinDBFilePersistItem(db.getId(), ProteinDBFilePersistItem.Status.valueOf(db.getStatus().name()));
            }
        }));
    }

    private Set<ProteinDatabase> getAvailableProteinDatabases(long actor) {
        final User user = Util.USER_FROM_ID.apply(actor);
        final List<ProteinDatabase> sharedForAll = proteinDatabaseRepository.findMyAndPublic(user);
        Set<ProteinDatabase> dbsSet = Sets.newHashSet(sharedForAll);

        final List<Long> availableExpIds = experimentRepository.findAllAvailableIds(actor);
        if (!availableExpIds.isEmpty()) {
            final List<ProteinDatabase> privateProteinDatabases = proteinDatabaseRepository.findAllByExperimentIds();
            dbsSet.addAll(privateProteinDatabases);
        }
        return dbsSet;
    }
}
