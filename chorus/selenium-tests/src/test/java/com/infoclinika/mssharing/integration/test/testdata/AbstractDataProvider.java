package com.infoclinika.mssharing.integration.test.testdata;

import com.infoclinika.mssharing.integration.test.helper.EnvironmentSpecificData;

import static com.infoclinika.mssharing.integration.test.helper.EnvironmentSpecificData.deserializeData;

/**
 * @author Alexander Orlov
 */
public abstract class AbstractDataProvider {

    protected static EnvironmentSpecificData environmentSpecificData = deserializeData(System.getProperty("path.to.data.file").toLowerCase());

}
