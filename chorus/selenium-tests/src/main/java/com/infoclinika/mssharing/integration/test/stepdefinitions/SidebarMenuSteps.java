package com.infoclinika.mssharing.integration.test.stepdefinitions;

import com.infoclinika.mssharing.integration.test.components.Button;
import com.infoclinika.mssharing.integration.test.stepdefinitions.experiment.ExperimentListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.instrument.InstrumentListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.news.NewsListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.project.ProjectsListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.sharinggroup.SharingGroupsListSteps;
import com.infoclinika.mssharing.integration.test.stepdefinitions.filelist.FileListSteps;
import org.openqa.selenium.By;

/**
 * @author Alexander Orlov
 */
public class SidebarMenuSteps extends AbstractPageSteps {

    private static final Button SHARING_GROUPS_LINK = controlFactory().button(By.cssSelector("[href='#/groups']"));
    private static final Button INSTRUMENTS_FIRST_CHORUS_LAB_LINK = controlFactory().button(By.cssSelector("[href='#/lab/1/instruments']"));
    private static final Button ALL_EXPERIMENTS_LINK = controlFactory().button(By.linkText("All Experiments"));
    private static final Button ALL_FILES_LINK = controlFactory().button(By.linkText("All Files"));
    private static final Button MY_FILES_LINK = controlFactory().button(By.linkText("My Files"));
    private static final Button NEWS_LINK = controlFactory().button(By.cssSelector("[href='#/news']"));
    private static final Button INBOX_LINK = controlFactory().button(By.cssSelector("[href='#/requests/inbox/all']"));
    private static final Button OUTBOX_LINK = controlFactory().button(By.cssSelector("[href='#/requests/outbox/all']"));
    private static final Button MY_PROJECTS_LINK = controlFactory().button(By.cssSelector("[href='#/projects/my']"));
    private static final Button MY_EXPERIMENTS_LINK = controlFactory().button(By.cssSelector("[href='#/experiments/my']"));
    private static final Button GENOME_MACHINE_LINK = controlFactory().button(By.xpath("//li[@title='Genome Machine']/a"));

    public SharingGroupsListSteps clickSharingGroups() {
        SHARING_GROUPS_LINK.click();
        return new SharingGroupsListSteps();
    }

    public InstrumentListSteps selectInstrumentsItem() {
        INSTRUMENTS_FIRST_CHORUS_LAB_LINK.scrollAndClick();
        return new InstrumentListSteps();
    }

    public boolean isInstrumentsPresent() {
        return INSTRUMENTS_FIRST_CHORUS_LAB_LINK.isPresent();
    }

    public ExperimentListSteps selectExperimentsItem() {
        ALL_EXPERIMENTS_LINK.click();
        return new ExperimentListSteps();
    }

    public FileListSteps selectItemAllFiles() {
        ALL_FILES_LINK.click();
        return new FileListSteps();
    }

    public FileListSteps selectItemMyFiles() {
        MY_FILES_LINK.click();
        return new FileListSteps();
    }

    public NewsListSteps selectNews() {
        NEWS_LINK.scrollAndClick();
        return new NewsListSteps();
    }

    public InboxListSteps selectInbox() {
        INBOX_LINK.scrollAndClick();
        return new InboxListSteps();
    }

    public OutboxListSteps selectOutbox() {
        OUTBOX_LINK.scrollAndClick();
        return new OutboxListSteps();
    }

    public ProjectsListSteps clickMyProjectsItem() {
        MY_PROJECTS_LINK.scrollAndClick();
        return new ProjectsListSteps();
    }

    public ExperimentListSteps clickMyExperimentsItem() {
        MY_EXPERIMENTS_LINK.scrollAndClick();
        return new ExperimentListSteps();
    }

    public FileListSteps clickGenomeMachineItem(){
        GENOME_MACHINE_LINK.scrollAndClick();
        return new FileListSteps();
    }
}
