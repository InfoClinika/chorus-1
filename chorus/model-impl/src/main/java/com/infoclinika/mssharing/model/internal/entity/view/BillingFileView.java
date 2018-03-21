package com.infoclinika.mssharing.model.internal.entity.view;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "billing_file_view")
public class BillingFileView {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "archiveId")
    private String archiveId;
    @Column(name = "contentId")
    private String contentId;
    @Column(name = "instrument_id")
    private Long instrument;
    @Column(name = "bill_lab")
    private Long billLab;
    @Column(name = "instrument_lab")
    private Long instrumentLab;
    @Column(name = "owner_id")
    private Long owner;
    @Column(name = "owner_name")
    private String ownerName;
    @Column(name = "instrument_name")
    private String instrumentName;
    @Column(name = "last_charging_date")
    private Date lastChargingDate;
    @Column(name = "file_name")
    private String fileName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "billing_user_function_item_view", joinColumns = @JoinColumn(name = "file_id"))
    private Set<BillingUserFunctionsView> userFunctionsViews = newHashSet();

    public BillingFileView(Long id, String archiveId, String contentId, Long instrument, Long billLab, Long instrumentLab, Long owner) {
        this.id = id;
        this.archiveId = archiveId;
        this.contentId = contentId;
        this.instrument = instrument;
        this.billLab = billLab;
        this.instrumentLab = instrumentLab;
        this.owner = owner;
    }

    public BillingFileView() {
    }

    public Long getId() {
        return id;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public String getContentId() {
        return contentId;
    }

    public Long getInstrument() {
        return instrument;
    }

    public Long getBillLab() {
        return billLab;
    }

    public Long getInstrumentLab() {
        return instrumentLab;
    }

    public Set<BillingUserFunctionsView> getUserFunctionsViews() {
        return userFunctionsViews;
    }

    public Long getOwner() {
        return owner;
    }

    public Date getLastChargingDate() {
        return lastChargingDate;
    }

    public String getFileName() {
        return fileName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getInstrumentName() {
        return instrumentName;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BillingFileView that = (BillingFileView) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
