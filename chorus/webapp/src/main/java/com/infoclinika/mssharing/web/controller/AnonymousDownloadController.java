package com.infoclinika.mssharing.web.controller;


import com.infoclinika.mssharing.model.helper.ExperimentDownloadHelper;
import com.infoclinika.mssharing.platform.model.helper.ExperimentDownloadHelperTemplate;
import com.infoclinika.mssharing.web.downloader.BulkDownloadHelper;
import com.infoclinika.mssharing.web.downloader.DownloadRequestNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

import static com.infoclinika.mssharing.platform.web.security.RichUser.get;

@Controller
@RequestMapping("/anonymous/download")
public class AnonymousDownloadController extends ErrorHandler {

    @Inject
    private BulkDownloadHelper bulkDownloadHelper;

    @Inject
    private ExperimentDownloadHelper experimentDownloadHelper;

    @RequestMapping(value = "/experiment/{token}", method = RequestMethod.GET)
    public String download(@PathVariable("token") String token, HttpServletResponse response)
        throws DownloadRequestNotFoundException, IOException {
        if (!experimentDownloadHelper.isDownloadTokenAvailable(token)) {
            return "redirect:/pages/404.html";
        }
        ExperimentDownloadHelperTemplate.ExperimentItemTemplate experiment =
            experimentDownloadHelper.getExperimentByDownloadToken(token);

        BulkDownloadHelper.Request request = new BulkDownloadHelper.ChorusRequest(experiment.creator,
            experiment.files,
            experiment.experiment,
            true,
            null
        );
        bulkDownloadHelper.download(request, response);
        return null;
    }

    @RequestMapping(value = "/experiment/send", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void sendDownloadLink(@RequestBody EmailLinkRequest request, Principal principal) {
        experimentDownloadHelper.sendDownloadExperimentLinkEmail(
            get(principal).getId(),
            request.experiment,
            request.email
        );
    }

    public static class EmailLinkRequest {
        public String email;
        public long experiment;
    }

}
