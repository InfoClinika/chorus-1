package com.infoclinika.mssharing.platform.model.test.sharing;

import com.infoclinika.mssharing.platform.model.PagedItemInfo;

public class AbstractPagedItemTest extends AbstractSharingTest {

    public PagedItemInfo getPagedItemRequest() {
        return new PagedItemInfo(25, 0, "name", false, "");
    }

    public PagedItemInfo getPagedItemRequest(String sortField) {
        return new PagedItemInfo(25, 0, sortField, false, "");
    }
}
