package com.infoclinika.mssharing.web.controller.request;

import com.infoclinika.mssharing.model.write.InstrumentDetails;

import java.util.List;

/**
 * @author Pavel Kaplin
 */
public class CreateInstrumentRequest {
    public long model;
    public long lab;
    public InstrumentDetails details;
    public List<Long> operators;
}
