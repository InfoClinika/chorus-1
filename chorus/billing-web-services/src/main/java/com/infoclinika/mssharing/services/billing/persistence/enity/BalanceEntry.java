package com.infoclinika.mssharing.services.billing.persistence.enity;

/**
 * @author timofey 29.03.16.
 */
public interface BalanceEntry {
    long getBalance();

    long getScaledToPayValue();

    long getTimestamp();
}
