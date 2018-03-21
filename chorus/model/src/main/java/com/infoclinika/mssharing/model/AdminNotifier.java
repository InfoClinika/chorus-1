package com.infoclinika.mssharing.model;

/**
 * @author Herman Zamula
 */
public interface AdminNotifier {

    void sendCommonEmail(long receiver, String title, String body);

}
