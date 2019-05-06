package com.infoclinika.mssharing.web.demo;

import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.read.DashboardReader;

import javax.inject.Inject;

import static com.google.common.collect.Collections2.filter;
import static com.infoclinika.mssharing.platform.model.read.LabReaderTemplate.LabLineTemplate;

/**
 * @author Pavel Kaplin
 */
public class DemoDataBasedTest extends SpringSupportTest {
    @Inject
    protected DashboardReader dashboardReader;
    @Inject
    protected DemoDataPropertiesProvider demoDataPropertiesProvider;
    @Inject
    private SecurityHelper securityHelper;

    protected Long demoUser() {
        return securityHelper.getUserDetailsByEmail("demo.user@infoclinika.com").id;
    }

    protected long johnDoe() {
        return securityHelper.getUserDetailsByEmail("john.doe@infoclinika.com").id;
    }

    protected long firstLab() {
        ImmutableSet<LabLineTemplate> labLines = dashboardReader.readUserLabs(demoUser());
        LabLineTemplate lab =
            filter(labLines, input -> input.name.equals("First Chorus Lab Very Long Name For Testing Ellipsize"))
                .iterator().next();

        return lab.id;
    }
}
