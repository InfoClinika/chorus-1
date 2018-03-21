package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.model.read.StatisticsReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author timofey.kasyanov
 */
@Controller
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsReader statisticsReader;

    @Inject
    public StatisticsController(StatisticsReader statisticsReader) {
        this.statisticsReader = statisticsReader;
    }

    @RequestMapping(value = "/usage", method = RequestMethod.GET)
    @ResponseBody
    public StatisticsReader.UsageStatisticsInfo getUsageStatistics() {
        return new StatisticsReader.UsageStatisticsInfo(
                statisticsReader.readUsersCount(),
                statisticsReader.readFilesSize(),
                statisticsReader.readFilesCount(),
                statisticsReader.readPublicProjectsCount(),
                statisticsReader.readPublicExperimentsCount()
        );
    }

}
