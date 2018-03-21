package com.infoclinika.mssharing.model.helper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Herman Zamula
 */
@Transactional
public interface ColumnViewHelper {

    long createView(@Nullable Long user, ColumnViewType type, String name, Set<ColumnInfo> columns, boolean primary);

    ImmutableSortedSet<ColumnInfo> getOrderedColumnsByView(long viewId);

    ImmutableSortedSet<ColumnInfo> getDefaultColumnSet(ColumnViewType type);

    ImmutableSortedSet<ColumnInfo> getPrimaryColumnSetOrDefault(long actor, ColumnViewType type);

    Set<Column> readAvailable(ColumnViewType type);

    void updateView(long actor, ColumnView columnView, Set<ColumnInfo> columns);

    List<ColumnView> getViews(long actor, ColumnViewType type);

    ColumnView readDefault(ColumnViewType type);

    Optional<ColumnView> readPrimary(long actor, ColumnViewType type);

    void removeView(long actor, long viewId);

    static class Column {
        public final Long id;
        public final String name;
        public final String dataType;
        public final boolean hideable;
        public final boolean sortable;
        public final int units;

        public Column(Long id, String name, String dataType, boolean hideable, boolean sortable, int units) {
            this.id = id;
            this.name = name;
            this.dataType = dataType;
            this.hideable = hideable;
            this.sortable = sortable;
            this.units = units;
        }
    }

    //TODO: separate
    static class ColumnInfo{
        public final String name;
        public final String modelViewName;
        public final Integer order;
        public final boolean hideable;
        public final boolean sortable;
        public final Long originalColumn;
        public final String dataType;
        public final Long units;

        public ColumnInfo(String name, String modelViewName, Integer order, boolean hideable, boolean sortable, Long originalColumn, String dataType, long units) {
            this.name = name;
            this.modelViewName = modelViewName;
            this.order = order;
            this.hideable = hideable;
            this.sortable = sortable;
            this.originalColumn = originalColumn;
            this.dataType = dataType;
            this.units = units;
        }

        public ColumnInfo(String name, Integer order, boolean hideable, boolean sortable, Long originalColumn) {
            this.name = name;
            this.order = order;
            this.hideable = hideable;
            this.sortable = sortable;
            this.originalColumn = originalColumn;
            this.units = null;
            modelViewName = null;
            dataType = null;
        }

        public ColumnInfo(long originalColumn, int order) {
            this.originalColumn = originalColumn;
            this.order = order;
            name = null;
            modelViewName = null;
            hideable = false;
            sortable = false;
            dataType = null;
            units = null;
        }
    }

    enum ColumnViewType {
        FILE,
        PROJECT,
        EXPERIMENT
    }

    class ColumnView {
        public final Long id;
        public final String name;
        public final boolean isDefault;
        public final boolean isPrimary;
        public final ColumnViewType type;

        public ColumnView(long id, String name, boolean aDefault, boolean primary, ColumnViewType type) {
            this.id = id;
            this.name = name;
            isDefault = aDefault;
            isPrimary = primary;
            this.type = type;
        }

        public ColumnView(Long id, String name, boolean primary, ColumnViewType type) {
            this.id = id;
            this.name = name;
            isPrimary = primary;
            this.type = type;
            isDefault = false;
        }
    }
}
