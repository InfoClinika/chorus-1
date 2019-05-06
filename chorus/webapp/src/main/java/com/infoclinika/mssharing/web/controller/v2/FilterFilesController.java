package com.infoclinika.mssharing.web.controller.v2;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.model.read.FileLine;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.web.controller.v2.dto.FileDTO;
import com.infoclinika.mssharing.web.controller.v2.dto.FileFilterDTO;
import com.infoclinika.mssharing.web.controller.v2.dto.ItemBoxDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getCurrentUserId;
import static java.util.stream.Collectors.toList;

/**
 * @author Vitalii Petkanych
 */
@RestController
@RequestMapping("/v2/filesFilter")
public class FilterFilesController {

    @Inject
    private DashboardReader dashboardReader;

    @RequestMapping(method = RequestMethod.POST)
    public ItemBoxDTO<FileDTO> createFilter(@RequestBody FileFilterDTO dto) {

        final Filter filter = Filter.ALL;
        final PaginationItems.PagedItemInfo pagedInfo =
            createPagedInfoByStudyType(
                dto.getPage(),
                dto.getPageSize(),
                dto.getSortBy(),
                dto.isSortAsc(),
                dto.getQuery(),
                dto.getFilter()
            );
        final PagedItem<FileLine> fileLines = dashboardReader.filterPageableFile(getCurrentUserId(), filter, pagedInfo);
        return new ItemBoxDTO<>(
            fileLines.items.stream()
                .map(fl -> new FileDTO(fl.id, fl.name, fl.uploadDate.getTime(), fl.sizeInBytes, fl.specieId, fl.labels))
                .collect(toList()),
            fileLines.itemsCount
        );
    }

    private PaginationItems.PagedItemInfo createPagedInfoByStudyType(
        int page, int items, String sortingField,
        boolean asc, String filterQuery,
        @Nullable AdvancedFilterQueryParams advancedFilter
    ) {
        int pageZeroBased = page - 1;
        return new PaginationItems.PagedItemInfo(
            items,
            pageZeroBased,
            sortingField,
            asc,
            filterQuery,
            Optional.fromNullable(advancedFilter)
        );
    }

}
