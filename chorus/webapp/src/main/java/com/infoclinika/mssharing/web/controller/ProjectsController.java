package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Function;
import com.infoclinika.mssharing.blog.persistence.BlogService;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.DetailsReader;
import com.infoclinika.mssharing.model.read.ProjectLine;
import com.infoclinika.mssharing.model.read.dto.details.ProjectItem;
import com.infoclinika.mssharing.model.write.ProjectInfo;
import com.infoclinika.mssharing.model.write.SharingManagement;
import com.infoclinika.mssharing.model.write.StudyManagement;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;
import com.infoclinika.mssharing.platform.model.common.items.NamedItem;
import com.infoclinika.mssharing.platform.model.helper.ProjectCreationHelperTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.model.write.SharingManagementTemplate;
import com.infoclinika.mssharing.web.controller.request.AccessExperimentDownloadRequest;
import com.infoclinika.mssharing.web.controller.request.CopyProjectConfirmationRequest;
import com.infoclinika.mssharing.web.controller.request.ProjectOperationRequest;
import com.infoclinika.mssharing.web.controller.response.DetailsResponse;
import com.infoclinika.mssharing.web.controller.response.ProjectIdResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.transformValues;
import static com.infoclinika.mssharing.platform.web.security.RichUser.get;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Pavel Kaplin
 */

@Controller
@RequestMapping("/projects")
public class ProjectsController extends PagedItemsController {
    private static final String PROJECT_WAS_COPIED = "Project was copied";
    private static final String PROJECT_UPDATED = "Project updated";
    private static final String SUCCESS = "Success";
    private static final String PROJECT_CREATED = "Project created";

    @Inject
    private StudyManagement studyManagement;
    @Inject
    private DashboardReader dashboardReader;
    @Inject
    private DetailsReader detailsReader;
    @Inject
    private SharingManagement sharingManagement;
    @Inject
    private ProjectCreationHelperTemplate projectCreationHelperTemplate;
    @Inject
    private BlogService blogService;

    public ProjectsController() {
    }

    @RequestMapping(value = "/allowedForWriting", method = RequestMethod.GET)
    @ResponseBody
    public List<ProjectLine> getProjectsAllowedForWriting(Principal principal) {
        return newArrayList(dashboardReader.readProjectsAllowedForWriting(getUserId(principal)));
    }

    @RequestMapping(value = "/{filter}", method = RequestMethod.GET)
    @ResponseBody
    public List<ProjectLine> getProjects(
            @PathVariable final Filter filter, Principal principal) {
        return newArrayList(dashboardReader.readProjects(getUserId(principal), filter));
    }

    @RequestMapping(value = "/paged/{filter}", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<ProjectLine> getPagedProjects(@PathVariable Filter filter,
                                                   @RequestParam int page, @RequestParam int items,
                                                   @RequestParam String sortingField, @RequestParam boolean asc,
                                                   @RequestParam(required = false) @Nullable String filterQuery,
                                                   Principal principal) {
        final long actor = getUserId(principal);
        final PagedItemInfo pagedInfo = createPagedInfo(page, items, sortingField, asc, filterQuery);
        return dashboardReader.readProjects(actor, filter, pagedInfo);
    }

    @RequestMapping(value = "/paged", method = RequestMethod.GET)
    @ResponseBody
    public PagedItem<ProjectLine> getPagedProjects(@RequestParam int page, @RequestParam int items,
                                                   @RequestParam(required = false) Long labId,
                                                   @RequestParam String sortingField, @RequestParam boolean asc,
                                                   @RequestParam(required = false) @Nullable String filterQuery,
                                                   Principal principal) {
        final long actor = getUserId(principal);
        final PagedItemInfo pagedInfo = createPagedInfo(page, items, sortingField, asc, filterQuery);
        return dashboardReader.readProjectsByLab(actor, labId, pagedInfo);
    }

    @RequestMapping(value = "/details/{id}/short", method = RequestMethod.GET)
    @ResponseBody
    public ProjectLine getProject(@PathVariable final Long id, Principal principal) {
        return dashboardReader.readProject(getUserId(principal), id);
    }

    @RequestMapping(value = "/{filter}/shortDetails", method = RequestMethod.GET)
    @ResponseBody
    public Set<NamedItem> getProjectsShortItems(
            @PathVariable final Filter filter, Principal principal) {
        return projectCreationHelperTemplate.ownedProjects(getUserId(principal));
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ProjectIdResponse save(@RequestBody ProjectOperationRequest project, Principal principal) {
        ProjectInfo projectInfo = new ProjectInfo(project.getName(),
                project.getAreaOfResearch(), project.getDescription(), project.lab);
        long projectId = 0;
        try {
            projectId = studyManagement.createProject(getUserId(principal), projectInfo);
            updatePolicy(getUserId(principal), projectId, project);
            setBlogEnabled(principal, projectId, project.isBlogEnabled());
            return new ProjectIdResponse(projectId, null, PROJECT_CREATED);
        } catch (IllegalArgumentException e) {
            return new ProjectIdResponse(projectId, e.getMessage(), null);
        }
    }

    @RequestMapping(value = "/sharing", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void saveProjectSharingRequest(@RequestBody AccessExperimentDownloadRequest accessExperimentRequest, Principal principal) {
        studyManagement.newProjectSharingRequest(get(principal).getId(), accessExperimentRequest.getExperimentId(), accessExperimentRequest.getDownloadExperimentLink());
    }

    @RequestMapping(value = "/sharing", method = RequestMethod.GET)
    @ResponseBody
    public DashboardReader.ProjectSharingRequestInfo readProjectSharingRequest(@RequestParam long experimentId, Principal principal) {
        return dashboardReader.readProjectSharingRequest(get(principal).getId(), experimentId);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/copy/{id}")
    @ResponseBody
    public ProjectIdResponse copy(@PathVariable long id, @RequestParam long newOwner, @RequestParam long billLaboratory, @RequestParam boolean emailNotification,
                                  Principal principal) {
        try {
            id = studyManagement.copyProject(getUserId(principal), new StudyManagement.CopyProjectInfo(id, newOwner, getUserId(principal), billLaboratory, emailNotification));
            return new ProjectIdResponse(id, null, PROJECT_WAS_COPIED);
        } catch (IllegalArgumentException e) {
            return new ProjectIdResponse(id, e.getMessage(), null);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/copy/request")
    @ResponseBody
    public ProjectIdResponse sendCopyRequest(@RequestBody CopyProjectConfirmationRequest request, Principal principal) {
        try {
            studyManagement.newProjectCopyRequest(getUserId(principal), request.newOwner, request.project);
            return new ProjectIdResponse(request.project, null, PROJECT_WAS_COPIED);
        } catch (IllegalArgumentException e) {
            return new ProjectIdResponse(request.project, e.getMessage(), null);
        }
    }

    private void updatePolicy(long actor, long projectId, ProjectOperationRequest project) {
        sharingManagement.updateSharingPolicy(actor, projectId, transformAccessLevel(project.getColleagues()),
                transformAccessLevel(project.getGroups()), project.isWithEmailNotification());
    }

    private Map<Long, SharingManagementTemplate.Access> transformAccessLevel(Map<Long, Boolean> colleagues) {
        return transformValues(colleagues, new Function<Boolean, SharingManagementTemplate.Access>() {
            @Override
            public SharingManagementTemplate.Access apply(@Nullable Boolean input) {
                return (input != null && input) ? SharingManagementTemplate.Access.WRITE : SharingManagementTemplate.Access.READ;
            }
        });
    }

    @RequestMapping(value = "/details/{id}", method = RequestMethod.GET)
    @ResponseBody
    public DetailsResponse getDetails(@PathVariable final Long id, Principal principal) {
        final ProjectItem project = detailsReader.readProject(getUserId(principal), id);
        return DetailsResponse.ok(project);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ProjectIdResponse updateProject(@RequestBody ProjectOperationRequest project, Principal principal) throws Exception {

        ProjectInfo projectInfo = new ProjectInfo(project.getName(),
                project.getAreaOfResearch(), project.getDescription(), project.lab);
        try {
            studyManagement.updateProject(getUserId(principal), project.getProjectId(), projectInfo);
        } catch (IllegalArgumentException e) {
            return new ProjectIdResponse(0, e.getMessage(), null);
        }
        updatePolicy(getUserId(principal), project.getProjectId(), project);
        setBlogEnabled(principal, project.getProjectId(), project.isBlogEnabled());
        return new ProjectIdResponse(project.getProjectId(), null, PROJECT_UPDATED);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeProject(@RequestParam long project, @RequestParam boolean removePermanently, Principal principal) throws Exception {
        final long userId = getUserId(principal);
        if (removePermanently) {
            studyManagement.removeProject(userId, project);
        } else {
            studyManagement.moveProjectToTrash(userId, project);
        }
        blogService.deleteBlog(project);
    }

    private void setBlogEnabled(Principal principal, long project, boolean enabled) {
        long actor = getUserId(principal);
        studyManagement.setBlogEnabled(actor, project, enabled);
        blogService.createOrUpdateBlog(project, detailsReader.readProject(actor, project).name);
        if (!enabled) {
            blogService.disableBlog(project);
        }
    }

}
