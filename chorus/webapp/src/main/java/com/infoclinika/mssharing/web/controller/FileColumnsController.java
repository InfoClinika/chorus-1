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
import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author Herman Zamula
 */
@Controller
@RequestMapping("/files/column-view")
public class FileColumnsController extends ErrorHandler {

    @Inject
    protected ColumnViewHelper columnViewHelper;
    @Inject
    private DashboardReader dashboardReader;

    public FileColumnsController() {
    }

    @RequestMapping(value = "/views", method = RequestMethod.GET)
    @ResponseBody
    public List<ColumnViewHelper.ColumnView> getColumnViews(Principal principal) {
        return columnViewHelper.getViews(getUserId(principal), ColumnViewHelper.ColumnViewType.FILE);
    }

    @RequestMapping(value = "/views/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<ColumnViewHelper.ColumnInfo> getOrderedColumns(@PathVariable long id) {
        return columnViewHelper.getOrderedColumnsByView(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void createOrUpdateColumnOrder(@RequestBody ColumnOrderRequest request, Principal principal) {
        ImmutableSet<ColumnViewHelper.ColumnInfo> columnInfos = from(request.columns)
            .transform(input -> new ColumnViewHelper.ColumnInfo(input.columnId, input.order))
            .toSet();

        Optional<ColumnViewHelper.ColumnView> primaryView =
            columnViewHelper.readPrimary(getUserId(principal), ColumnViewHelper.ColumnViewType.FILE);
        if (primaryView.isPresent()) {
            columnViewHelper.updateView(getUserId(principal), primaryView.get(), columnInfos);
        } else {
            columnViewHelper.createView(
                getUserId(principal),
                ColumnViewHelper.ColumnViewType.FILE,
                request.name,
                columnInfos,
                request.isPrimary
            );
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void removeColumnOrder(@PathVariable long id, Principal principal) {
        columnViewHelper.removeView(getUserId(principal), id);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public Set<ColumnViewHelper.Column> getAvailable() {
        return columnViewHelper.readAvailable(ColumnViewHelper.ColumnViewType.FILE);
    }

    @RequestMapping(value = "/default", method = RequestMethod.GET)
    @ResponseBody
    public ColumnViewHelper.ColumnView getDefault() {
        return columnViewHelper.readDefault(ColumnViewHelper.ColumnViewType.FILE);
    }

    @RequestMapping(value = "/selected", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<ColumnViewHelper.ColumnInfo> getPrimaryOrDefault(Principal principal) {
        return columnViewHelper.getPrimaryColumnSetOrDefault(
            getUserId(principal),
            ColumnViewHelper.ColumnViewType.FILE
        );
    }

    @RequestMapping(value = "/default/columns", method = RequestMethod.GET)
    @ResponseBody
    public ImmutableSortedSet<ColumnViewHelper.ColumnInfo> getDefaultColumns() {
        return columnViewHelper.getDefaultColumnSet(ColumnViewHelper.ColumnViewType.FILE);
    }
}
