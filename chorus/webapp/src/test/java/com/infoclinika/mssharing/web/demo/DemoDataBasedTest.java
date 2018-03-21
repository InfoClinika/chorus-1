package com.infoclinika.mssharing.web.demo;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.model.helper.SecurityHelper;
import com.infoclinika.mssharing.model.read.DashboardReader;
import com.infoclinika.mssharing.platform.model.read.LabReaderTemplate;

import javax.inject.Inject;

/**
 * @author Pavel Kaplin
 */
public class DemoDataBasedTest extends SpringSupportTest {
    @Inject
    protected DashboardReader dashboardReader;
    @Inject
    private SecurityHelper securityHelper;

    protected Long pavelKaplinAtGmail() {
        return securityHelper.getUserDetailsByEmail("pavel.kaplin@gmail.com").id;
    }

    protected long pavelKaplinAtTeamdev() {
        return securityHelper.getUserDetailsByEmail("pavel.kaplin@teamdev.com").id;
    }

    protected long firstLab() {
        ImmutableSet<LabReaderTemplate.LabLineTemplate> labLines = dashboardReader.readUserLabs(pavelKaplinAtGmail());
        LabReaderTemplate.LabLineTemplate lab = Collections2.filter(labLines, new Predicate<LabReaderTemplate.LabLineTemplate>() {
            @Override
            public boolean apply(LabReaderTemplate.LabLineTemplate input) {
                return input.name.equals("First Chorus Lab Very Long Name For Testing Ellipsize");
            }
        }).iterator().next();
        return lab.id;
    }
}
