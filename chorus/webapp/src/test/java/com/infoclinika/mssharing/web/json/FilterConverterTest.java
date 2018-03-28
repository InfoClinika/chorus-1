package com.infoclinika.mssharing.web.json;

import com.infoclinika.mssharing.platform.model.read.Filter;
import com.infoclinika.mssharing.platform.web.json.FilterConverter;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Pavel Kaplin
 */
public class FilterConverterTest {
    private FilterConverter converter = new FilterConverter();

    @Test
    public void testConvert() {
        assertEquals(Filter.ALL, converter.convert("all"));
        assertEquals(Filter.MY, converter.convert("my"));
        assertEquals(Filter.PUBLIC, converter.convert("public"));
        assertEquals(Filter.SHARED_WITH_ME, converter.convert("shared"));
    }

    @Test
    public void testAllValuesCouldBeHandled() {
        assertEquals(4, Filter.values().length);
    }

}
