package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.isatabmapping.api.ISATabTemplate;
import com.infoclinika.mssharing.isatabmapping.api.ISATabTools;
import com.infoclinika.mssharing.model.features.ApplicationFeature;
import com.infoclinika.mssharing.model.helper.FeaturesHelper;
import com.infoclinika.mssharing.model.internal.helper.ZipHelper;
import com.infoclinika.mssharing.model.internal.helper.isaexport.ISATabTemplateConverter;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.infoclinika.util.FilenameUtil.replaceForbiddenPathCharacters;

/**
 * @author sergii.ivanov
 */
@Controller
@RequestMapping(value = "/isaexport")
public class ISATabExportController extends ErrorHandler {

    public static final String CONFIG_PATH = "isa-tab/config";
    @Inject
    private ISATabTemplateConverter isaTabConverter;
    @Inject
    private FeaturesHelper featuresHelper;

    @RequestMapping(value = "/{experimentId}", method = RequestMethod.GET)
    @ResponseBody
    public void getISATab(
        @PathVariable long experimentId,
        HttpServletResponse response
    ) throws IOException {
        if (isISAExportFeatureEnabled()) {
            ISATabTemplate isaTemplate = isaTabConverter.convertToISATab(experimentId);
            final String javaTempDir = System.getProperty("java.io.tmpdir");
            String targetDir = javaTempDir + "/isatab_" + generateUniqueSequence();
            new File(targetDir).mkdir();

            final String configPath = ISATabExportController.class.getClassLoader()
                .getResource(CONFIG_PATH)
                .getFile();

            ISATabTools.saveISATabFile(isaTemplate, targetDir, configPath);

            final String safeProjectName = replaceForbiddenPathCharacters(isaTemplate.getProject().getName());
            final File zip = new File(javaTempDir + "/" + safeProjectName + ".zip");
            ZipHelper.zipDir(new File(targetDir), zip);

            ServletOutputStream out = response.getOutputStream();
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment;filename=" + zip.getName());

            IOUtils.copy(new FileInputStream(zip), out);
        } else {
            throw new AccessDenied("Feature is not enabled for current user");
        }
    }

    private boolean isISAExportFeatureEnabled() {
        return featuresHelper.isEnabled(ApplicationFeature.ISA_TAB_EXPORT);
    }

    private String generateUniqueSequence() {
        return RequestContextHolder.currentRequestAttributes().getSessionId() + "_" + System.currentTimeMillis();
    }
}
