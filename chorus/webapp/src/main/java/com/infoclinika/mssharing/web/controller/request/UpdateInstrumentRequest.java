package com.infoclinika.mssharing.web.controller.request;

import com.infoclinika.mssharing.model.write.InstrumentDetails;

import java.util.List;

/**
 * @author timofey.kasyanov
 *     date: 12.05.2014
 */
public class UpdateInstrumentRequest {

    public long id;
    public long model;
    public InstrumentDetails details;
    public List<Long> operators;
}
