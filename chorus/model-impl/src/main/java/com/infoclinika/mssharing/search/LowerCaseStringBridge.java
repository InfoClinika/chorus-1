package com.infoclinika.mssharing.search;

import org.hibernate.search.bridge.TwoWayStringBridge;

/**
 * All fields that annotated to use this class as a bridge will be stored in index in lowercase.
 * Values in lowercase Helpful for sorting by field using Lucene Search API.
 *
 * @author Natalia.Zolochevska@Teamdev.com
 */
public class LowerCaseStringBridge implements TwoWayStringBridge {

    public String objectToString(Object object) {
        if (object == null) return "";
        return object.toString().toLowerCase();
    }

    @Override
    public Object stringToObject(String stringValue) {
        return stringValue;
    }
}