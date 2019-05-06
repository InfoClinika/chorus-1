package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.internal.entity.upload.UploadType;
import com.infoclinika.mssharing.web.controller.v2.UploadsController;
import com.infoclinika.mssharing.web.controller.v2.dto.UploadTypeDTO;
import com.infoclinika.mssharing.web.demo.SpringSupportTest;
import org.testng.Assert;

import javax.inject.Inject;

/**
 * Created by Alexey on 4/20/17.
 */
public class UploadControllerTest extends SpringSupportTest {

    @Inject
    private UploadsController uploadsController;

    //    @Test
    public void testUploadTypes() {
        UploadTypeDTO[] uploadTypes = uploadsController.uploadTypes();
        Assert.assertNotNull(uploadTypes);
        Assert.assertEquals(UploadType.values().length, uploadTypes.length);
        Assert.assertEquals(uploadTypes[0].getName(), UploadType.DIRECT.name());
        Assert.assertEquals(uploadTypes[1].getName(), UploadType.S3_COPY.name());
        Assert.assertEquals(uploadTypes[2].getName(), UploadType.FTP.name());
    }
}
