package com.infoclinika.mssharing.platform.entity;

import javax.persistence.*;

/**
 * @author Herman Zamula
 */
@MappedSuperclass
public abstract class AbstractPersistable {

    @Id
    @TableGenerator(
            name = "sequence_generator",
            table = "hibernate_sequences",
            pkColumnName = "sequence_name",
            valueColumnName = "sequence_next_hi_value",
            initialValue = 1,
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "sequence_generator")
    private Long id;


    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.domain.Persistable#getId()
     */
    public Long getId() {

        return id;
    }


    /**
     * Sets the id of the entity.
     *
     * @param id the id to set
     */
    public void setId(final Long id) {

        this.id = id;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.domain.Persistable#isNew()
     */
    public boolean isNew() {

        return null == getId();
    }


    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return String.format("Entity of type %s with id: %s", this.getClass()
                .getName(), getId());
    }


    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        AbstractPersistable that = (AbstractPersistable) obj;

        return null != this.getId() && this.getId().equals(that.getId());
    }


    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 17;
        final int hashMultiplier = 31;

        hashCode += null == getId() ? 0 : getId().hashCode() * hashMultiplier;

        return hashCode;
    }
}
