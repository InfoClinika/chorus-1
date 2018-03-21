/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.entity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * @author Oleksii Tymchenko
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UserLabMembershipRequestTemplate<U extends UserTemplate<?>, L extends LabTemplate<?>> extends AbstractAggregate {
    @ManyToOne(targetEntity = UserTemplate.class)
    private U user;
    @ManyToOne(targetEntity = LabTemplate.class)
    private L lab;
    private Date sent;
    private Decision decision;
    private boolean closed;

    protected UserLabMembershipRequestTemplate() {
    }

    public UserLabMembershipRequestTemplate(U user, L lab, Date sent) {
        this.user = user;
        this.lab = lab;
        this.sent = sent;
    }

    public U getUser() {
        return user;
    }

    public void setUser(U user) {
        this.user = user;
    }

    public L getLab() {
        return lab;
    }

    public void setLab(L lab) {
        this.lab = lab;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public enum Decision {
        APPROVED, REJECTED
    }
}
