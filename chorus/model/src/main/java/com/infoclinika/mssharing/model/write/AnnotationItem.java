package com.infoclinika.mssharing.model.write;

/**
 * @author Vitalii Petkanych
 */
public class AnnotationItem {
    public String name;
    public String value;
    public String units;
    public boolean isNumeric;

    public AnnotationItem() {
    }

    public AnnotationItem(String name, String value, String units, boolean isNumeric) {
        this.name = name;
        this.value = value;
        this.units = units;
        this.isNumeric = isNumeric;
    }
}
