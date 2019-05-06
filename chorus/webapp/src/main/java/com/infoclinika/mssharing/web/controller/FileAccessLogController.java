package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.read.FileAccessLogReader;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.web.controller.request.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.security.Principal;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author vladislav.kovchug
 */

@Controller
public class FileAccessLogController extends PagedItemsController {

    @Inject
    private FileAccessLogReader fileAssessLogReader;

    @RequestMapping(value = "/file-access-log", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<FileAccessLogReader.FileAccessLogDTO> getFiles(PageRequest pageRequest, Principal principal) {
        return fileAssessLogReader.readLogs(getUserId(principal), createPagedInfo(pageRequest));
    }

}
