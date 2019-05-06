package com.infoclinika.mssharing.web.controller;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.infoclinika.mssharing.model.helper.ColumnViewHelper;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.web.controller.request.ColumnOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.FluentIterable.from;
import static com.infoclinika.mssharing.model.helper.ColumnViewHelper.*;
import static com.infoclinika.mssharing.model.helper.ColumnViewHelper.ColumnViewType.PROJECT;
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Alexander Orlov
 */

@Controller
@RequestMapping("/projects/column-view")
public class ProjectColumnsController {

    @Inject
    protected ColumnViewHelper columnViewHelper;
    @Inject
    private DashboardReader dashboardReader;

    @RequestMapping(value = "/views", method = RequestMethod.GET)
    @ResponseBody
    public List<ColumnView> getColumnViews(Principal principal) {
        return columnViewHelper.getViews(getUserId(principal), PROJECT);
    }

    @RequestMapping(value = "/views/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<ColumnInfo> getOrderedColumns(@PathVariable long id) {
        return columnViewHelper.getOrderedColumnsByView(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void createOrUpdateColumnOrder(@RequestBody ColumnOrderRequest request, Principal principal) {
        ImmutableSet<ColumnInfo> columnInfos = from(request.columns)
            .transform(input -> new ColumnInfo(input.columnId, input.order))
            .toSet();

        Optional<ColumnView> primaryView = columnViewHelper.readPrimary(getUserId(principal), PROJECT);
        if (primaryView.isPresent()) {
            columnViewHelper.updateView(getUserId(principal), primaryView.get(), columnInfos);
        } else {
            columnViewHelper.createView(
                getUserId(principal),
                PROJECT,
                request.name,
                columnInfos,
                request.isPrimary
            );
        }
    }

    //todo: is this method used anywhere?
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeColumnOrder(@PathVariable long id, Principal principal) {
        columnViewHelper.removeView(getUserId(principal), id);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public Set<Column> getAvailable() {
        return columnViewHelper.readAvailable(PROJECT);
    }

    @RequestMapping(value = "/default", method = RequestMethod.GET)
    @ResponseBody
    public ColumnView getDefault() {
        return columnViewHelper.readDefault(PROJECT);
    }

    @RequestMapping(value = "/selected", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<ColumnInfo> getPrimaryOrDefault(Principal principal) {
        return columnViewHelper.getPrimaryColumnSetOrDefault(
            getUserId(principal),
            PROJECT
        );
    }

    @RequestMapping(value = "/default/columns", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<ColumnInfo> getDefaultColumns() {
        return columnViewHelper.getDefaultColumnSet(PROJECT);
    }
}
