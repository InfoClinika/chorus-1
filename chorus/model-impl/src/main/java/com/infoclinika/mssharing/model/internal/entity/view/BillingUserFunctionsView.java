package com.infoclinika.mssharing.model.internal.entity.view;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author Herman Zamula
 */
@Embeddable
public class BillingUserFunctionsView {

    @Column(name = "lab_id")
    private Long lab;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "path")
    private String path;
    @Column(name = "user_name")
    private String userName;

    public BillingUserFunctionsView(Long lab, Long userId, String path, String userName) {
        this.lab = lab;
        this.userId = userId;
        this.path = path;
        this.userName = userName;
    }

    public BillingUserFunctionsView() {
    }

    public Long getLab() {
        return lab;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPath() {
        return path;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BillingUserFunctionsView that = (BillingUserFunctionsView) o;

        if (lab != null ? !lab.equals(that.lab) : that.lab != null) {
            return false;
        }
        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = lab != null ? lab.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        return result;
    }
}
