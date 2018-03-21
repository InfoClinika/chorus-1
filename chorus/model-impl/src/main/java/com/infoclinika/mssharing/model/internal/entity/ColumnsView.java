package com.infoclinika.mssharing.model.internal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
public class ColumnsView extends AbstractPersistable<Long> {

    private String name;

    @ManyToOne
    @Nullable
    private User user;

    private Type type;

    private boolean isPrimary = false;

    private boolean isDefault = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "columns_view_id")
    private Set<ViewColumn> columns = newHashSet();

    public ColumnsView() {
    }

    public ColumnsView(String name, Type type, @Nullable User user) {
        this.name = name;
        this.type = type;
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Set<ViewColumn> getColumns() {
        return columns;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    @Nullable
    public User getUser() {
        return user;
    }

    public enum Type {
        FILE_META,
        PROJECT_META,
        EXPERIMENT_META
    }

}
