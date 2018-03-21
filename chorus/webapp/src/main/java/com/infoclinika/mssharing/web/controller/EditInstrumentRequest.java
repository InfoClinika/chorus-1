package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.write.InstrumentDetails;

import java.util.List;

/**
 * @author Pavel Kaplin
 */
public class EditInstrumentRequest {
    public long id;
    public InstrumentDetails details;
    public List<Long> operators;
}
