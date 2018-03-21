package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.write.LabHeadManagement;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Pavel Kaplin
 */
@Controller
@RequestMapping("/labhead")
public class LaboratoryHeadController extends ErrorHandler {

    @Inject
    private DashboardReader dashboardReader;

    @Inject
    private LabHeadManagement labHeadManagement;

    @ResponseBody
    @RequestMapping("/{lab}/users")
    public ArrayList<DashboardReader.UserLine> getLabs(@PathVariable long lab, Principal principal) {
        final long userId = getUserId(principal);
        return newArrayList(dashboardReader.readUsersByLab(userId, lab));
    }

    @RequestMapping(value = "/{lab}/users/{userToRemove}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeUserFromLaboratory(@PathVariable long lab, @PathVariable long userToRemove, Principal principal) throws Exception {
        final long labHeadId = getUserId(principal);
        labHeadManagement.removeUserFromLab(labHeadId, lab, userToRemove);
    }
}
