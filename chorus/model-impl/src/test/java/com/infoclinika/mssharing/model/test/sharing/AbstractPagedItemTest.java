package com.infoclinika.mssharing.model.test.sharing;

import com.google.common.base.Optional;
import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.platform.model.PagedItemInfo;

public class AbstractPagedItemTest extends AbstractSharingTest {
    public static final int DEFAULT_PAGE_SIZE = 25;

    public PagedItemInfo getPagedItemRequest() {
        return new PaginationItems.PagedItemInfo(DEFAULT_PAGE_SIZE, 0, "name", false, "", Optional.absent());
    }

    public PagedItemInfo getPagedItemRequest(String sortField) {
        return new PaginationItems.PagedItemInfo(DEFAULT_PAGE_SIZE, 0, sortField, false, "", Optional.absent());
    }
}
