package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.internal.helper.NgsExportService;
import com.infoclinika.mssharing.platform.web.security.RichUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author slava.nekhaienko
 */
@RestController
@RequestMapping(value = "/export")
@Api(description = "Experiment controllers", tags = {"experiments"})
public class NgsExportController extends ErrorHandler {

    @Inject
    private NgsExportService ngsExportService;

    @ApiOperation(
        value = "Retrieves an experiment specified by {experimentId} in Celgene JSON format",
        httpMethod = "GET",
        produces = "application/json",
        tags = {"experiments"}
    )
    @RequestMapping(value = "/experiment/{experimentId}", method = RequestMethod.GET)
    public List<Map<String, String>> getExperimentInCelgeneFormat(@PathVariable long experimentId) {
        return ngsExportService.getRowsForJson(RichUser.getCurrentUserId(), experimentId);
    }

    @ApiOperation(
        value = "Retrieves an experiment specified by {experimentId} in Celgene JSON format",
        httpMethod = "GET",
        produces = "application/json",
        tags = {"experiments"}
    )
    @RequestMapping(value = "/experiment/{experimentId}.json", method = RequestMethod.GET)
    public List<Map<String, String>> getExperimentInCelgeneFormatJson(@PathVariable long experimentId) {
        return ngsExportService.getRowsForJson(RichUser.getCurrentUserId(), experimentId);
    }

    @ApiOperation(
        value = "Retrieves an experiment specified by {experimentId} in Celgene csv format",
        httpMethod = "GET",
        produces = "text/csv",
        tags = {"experiments"}
    )
    @RequestMapping(value = "/experiment/{experimentId}.csv", method = RequestMethod.GET)
    public void getExperimentInCelgeneFormatCsv(@PathVariable long experimentId, HttpServletResponse response)
        throws IOException {
        long userId = RichUser.getCurrentUserId();

        response.addHeader(
            "Content-disposition",
            "attachment;filename=" + ngsExportService.getCsvFileName(userId, experimentId) + ".csv"
        );
        response.setContentType("text/csv");

        response.getWriter().write(ngsExportService.getCsvText(userId, experimentId));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
