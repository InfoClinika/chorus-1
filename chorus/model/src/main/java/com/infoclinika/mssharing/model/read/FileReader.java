package com.infoclinika.mssharing.model.read;

import com.infoclinika.mssharing.model.PaginationItems;
import com.infoclinika.mssharing.platform.model.PagedItem;
import com.infoclinika.mssharing.platform.model.read.FileReaderTemplate;
import com.infoclinika.mssharing.platform.model.read.Filter;

/**
 * @author timofei.kasianov 3/29/18
 */
public interface FileReader<LINE extends FileReaderTemplate.FileLineTemplate> extends FileReaderTemplate<LINE> {

    PagedItem<LINE> filterPageableFile(long actor, Filter filter, PaginationItems.PagedItemInfo pagedInfo);

}
