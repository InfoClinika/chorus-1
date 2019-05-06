package com.infoclinika.mssharing.model.internal.entity.restorable;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author timofei.kasianov 8/8/18
 */
@Table(name = "dict_nt_extraction_method")
@Entity
public class NtExtractionMethod extends AbstractPersistable<Long> {

    @Basic(optional = false)
    private String title;

    public String getTitle() {
        return title;
    }

    public NtExtractionMethod setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final NtExtractionMethod that = (NtExtractionMethod) o;

        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title);
    }
}
