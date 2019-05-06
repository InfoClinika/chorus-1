package com.infoclinika.mssharing.services.billing.persistence.enity;


import com.infoclinika.mssharing.model.internal.entity.payment.ChargeableItem;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Herman Zamula
 */
@Entity
@Table(name = "monthly_summary")
public class MonthlySummary extends AbstractPersistable<Long> {

    @Column(name = "lab_id", nullable = false)
    private long labId;

    @Column(name = "calculation_date")
    private Date calculationDate;

    @Column(name = "monthly_total")
    private long monthlyTotal;

    @Column(name = "end_month_balance")
    private long endMonthBalance;

    @Column(name = "logged_month")
    private Date loggedMonth;

    @ElementCollection
    @CollectionTable(name = "monthly_total_by_feature", joinColumns = @JoinColumn(name = "monthly_summary_id"))
    @MapKeyColumn(name = "feature")
    @Column(name = "monthly_total")
    private Map<ChargeableItem.Feature, Long> totalByFeature = new HashMap<>();

    public long getLabId() {
        return labId;
    }

    public void setLabId(long labId) {
        this.labId = labId;
    }

    public Date getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(Date calculationDate) {
        this.calculationDate = calculationDate;
    }

    public long getMonthlyTotal() {
        return monthlyTotal;
    }

    public void setMonthlyTotal(long monthlyTotal) {
        this.monthlyTotal = monthlyTotal;
    }

    public long getEndMonthBalance() {
        return endMonthBalance;
    }

    public void setEndMonthBalance(long endMonthBalance) {
        this.endMonthBalance = endMonthBalance;
    }

    public Map<ChargeableItem.Feature, Long> getTotalByFeature() {
        return totalByFeature;
    }

    public Date getLoggedMonth() {
        return loggedMonth;
    }

    public void setLoggedMonth(Date loggedMonth) {
        this.loggedMonth = loggedMonth;
    }
}
