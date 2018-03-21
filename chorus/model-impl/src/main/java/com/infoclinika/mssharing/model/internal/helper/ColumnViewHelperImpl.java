package com.infoclinika.mssharing.model.internal.helper;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import com.infoclinika.mssharing.model.helper.ColumnViewHelper;
import com.infoclinika.mssharing.model.internal.entity.ColumnDefinition;
import com.infoclinika.mssharing.model.internal.entity.ColumnsView;
import com.infoclinika.mssharing.model.internal.entity.User;
import com.infoclinika.mssharing.model.internal.entity.ViewColumn;
import com.infoclinika.mssharing.model.internal.read.Transformers;
import com.infoclinika.mssharing.model.internal.repository.ColumnDefinitionRepository;
import com.infoclinika.mssharing.model.internal.repository.ColumnViewRepository;
import com.infoclinika.mssharing.model.internal.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.platform.model.impl.ValidatorPreconditions.checkPresence;


/**
 * @author Herman Zamula
 */
@Service
public class ColumnViewHelperImpl implements ColumnViewHelper {


    public static final Comparator<ColumnInfo> COLUMN_COMPARATOR = new Comparator<ColumnInfo>() {
        @Override
        public int compare(ColumnInfo o1, ColumnInfo o2) {
            return o1.order.compareTo(o2.order);
        }
    };

    @Inject
    private ColumnViewRepository columnViewRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private ColumnDefinitionRepository columnDefinitionRepo;
    @Inject
    private Transformers transformers;
    @Inject
    private RuleValidator ruleValidator;

    @Override
    public long createView(Long user, ColumnViewType type, String name, Set<ColumnInfo> columns, boolean primary) {
        final User user1;
        user1 = user == null ? null : userRepository.findOne(user);
        final ColumnsView view = new ColumnsView(name, transformType(type), user1);
        final Collection<ViewColumn> values = transformViewToModel(columns);
        view.getColumns().addAll(values);
        view.setPrimary(primary);
        return columnViewRepository.save(view).getId();
    }

    @Override
    public ImmutableSortedSet<ColumnInfo> getOrderedColumnsByView(long viewId) {
        final ColumnsView columnsView = checkPresence(columnViewRepository.findOne(viewId));
        final Set<ViewColumn> columns = new HashSet<>(columnsView.getColumns());
        return transformColumns(columns);
    }

    @Override
    public ImmutableSortedSet<ColumnInfo> getDefaultColumnSet(ColumnViewType type) {
        final ColumnsView defaultView = columnViewRepository.findDefault(transformType(type));
        return transformColumns(defaultView.getColumns());
    }

    @Override
    public ImmutableSortedSet<ColumnInfo> getPrimaryColumnSetOrDefault(long actor, ColumnViewType type) {
        final Optional<ColumnsView> primary = fromNullable(columnViewRepository.findPrimary(transformType(type), actor));
        if (primary.isPresent()) {
            return transformColumns(primary.get().getColumns());
        }
        return getDefaultColumnSet(type);
    }

    private ImmutableSortedSet<ColumnInfo> transformColumns(Set<ViewColumn> columns) {
        return from(columns).transform(transformers.viewToColumnTransformer).toSortedSet(COLUMN_COMPARATOR);
    }

    @Override
    public Set<Column> readAvailable(final ColumnViewType type) {
        return from(columnDefinitionRepo.findAll()).filter(new Predicate<ColumnDefinition>() {
            @Override
            public boolean apply(ColumnDefinition input) {
                return input.getType().compareTo(transformType(type)) == 0;
            }
        }).transform(new Function<ColumnDefinition, Column>() {
            @Override
            public Column apply(ColumnDefinition input) {
                return new Column(input.getId(), input.getName(), input.getDataType(), input.isHideable(), input.isSortable(), input.getUnits());
            }
        }).toSet();
    }

    @Override
    public void updateView(long actor, ColumnView viewInfo, Set<ColumnInfo> columns) {
        final ColumnsView columnsView = checkPresence(columnViewRepository.findOne(viewInfo.id));
        if (columnsView.isDefault()) {
            throw new AccessDenied("Can't update default view");
        }
        columnsView.getColumns().clear();
        columnsView.getColumns().addAll(transformViewToModel(columns));
        columnsView.setPrimary(viewInfo.isPrimary);
        columnsView.setName(viewInfo.name);
        columnViewRepository.save(columnsView);
    }

    @Override
    public List<ColumnView> getViews(long actor, ColumnViewType type) {
        return from(columnViewRepository.findAllowed(actor, transformType(type)))
                .transform(columnViewTransformer())
                .toList();
    }

    @Override
    public ColumnView readDefault(ColumnViewType type) {
        return columnViewTransformer().apply(columnViewRepository.findDefault(transformType(type)));
    }

    @Override
    public Optional<ColumnView> readPrimary(long actor, ColumnViewType type) {
        Optional<ColumnsView> primaryView = fromNullable(columnViewRepository.findPrimary(transformType(type), actor));
        return primaryView.isPresent()
                ? Optional.of(columnViewTransformer().apply(primaryView.get()))
                : Optional.<ColumnView>absent();
    }

    @Override
    public void removeView(long actor, long viewId) {
        final User user = checkPresence(userRepository.findOne(actor));
        final ColumnsView view = columnViewRepository.findOne(viewId);
        if (!user.equals(view.getUser())) {
            throw new AccessDenied("User isn't permitted to remove columns view");
        }
        columnViewRepository.delete(view);
    }

    private Collection<ViewColumn> transformViewToModel(Set<ColumnInfo> columns) {

        return from(columns).transform(new Function<ColumnInfo, ViewColumn>() {
            @Override
            public ViewColumn apply(ColumnInfo input) {
                final ColumnDefinition columnDefinition = columnDefinitionRepo.findOne(input.originalColumn);
                return new ViewColumn(columnDefinition, input.order);
            }
        }).toSet();
    }

    private ColumnsView.Type transformType(ColumnViewType type) {
        switch (type) {
            case FILE:
                return ColumnsView.Type.FILE_META;
            case PROJECT:
                return ColumnsView.Type.PROJECT_META;
            case EXPERIMENT:
                return ColumnsView.Type.EXPERIMENT_META;
            default:
                throw new AssertionError(type);
        }
    }

    private ColumnViewType transformType(ColumnsView.Type type) {
        switch (type) {
            case FILE_META:
                return ColumnViewType.FILE;
            case PROJECT_META:
                return ColumnViewType.PROJECT;
            case EXPERIMENT_META:
                return ColumnViewType.EXPERIMENT;
            default:
                throw new AssertionError(type);
        }
    }


    private Function<ColumnsView, ColumnView> columnViewTransformer() {
        return new Function<ColumnsView, ColumnView>() {
            @Override
            public ColumnView apply(ColumnsView input) {
                return new ColumnView(input.getId(), input.getName(), input.isDefault(), input.isPrimary(), transformType(input.getType()));
            }
        };
    }

}
