package com.infoclinika.mssharing.model.test.sharing;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.model.PaginationItems.AdvancedFilterQueryParams;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;

public class AbstractPagedItemTest extends AbstractSharingTest {

    public PagedItemInfo getPagedItemRequest() {
        return new PaginationItems.PagedItemInfo(25, 0, "name", false, "", Optional.<AdvancedFilterQueryParams>absent());
    }

    public PagedItemInfo getPagedItemRequest(String sortField) {
        return new PaginationItems.PagedItemInfo(25, 0, sortField, false, "", Optional.<AdvancedFilterQueryParams>absent());
    }
}
