package com.infoclinika.mssharing.web.controller.request;

import java.util.Set;

/**
 * @author : Alexander Serebriyan
 */
public class SameSpeciesCheckRequest {
    public long specieId;
    public Set<Long> fileIds;
}
