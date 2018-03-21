package com.infoclinika.mssharing.platform.entity;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

import static java.lang.String.format;

/**
 * @author Herman Zamula
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AnnotationTemplate extends AbstractPersistable {
    private static final int LONG_STRING = 4000;

    @Column(name = "a_name", length = LONG_STRING)
    private String name;
    @Column(name = "a_type")
    private Type type;
    @Column(name = "unit_name", length = LONG_STRING)
    private String units;
    @Column(name = "a_value", length = LONG_STRING)
    private String value;

    public AnnotationTemplate() {
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

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Transient
    public String getNameWithUnits() {
        return StringUtils.isEmpty(this.getUnits()) ? this.getName() : format("%s(%s)", this.getName(), this.getUnits());
    }

    public enum Type {
        STRING, INTEGER
    }
}
