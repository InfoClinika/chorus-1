package com.infoclinika.mssharing.platform.model.test.helper;

import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.StoredObjectPathsTemplate;
import com.infoclinika.mssharing.platform.model.testing.helper.AbstractTestTemplate;
import com.infoclinika.mssharing.platform.model.testing.helper.SpringConfigTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * @author Herman Zamula
 */
@ContextConfiguration(classes = {SpringConfigTemplate.class, SpringConfig.class})
public class AbstractTest extends AbstractTestTemplate {

    @Inject
    protected StoredObjectPathsTemplate storedObjectPaths;

    public StorageService storageService() {
        return super.applicationContext.getBean(StorageService.class);
    }
}
