package com.infoclinika.mssharing.web.controller;

import com.infoclinika.mssharing.web.appearance.AppearanceSettings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * @author : Alexander Serebriyan
 */

@Controller
@RequestMapping("/appearance")
public class AppearanceController extends ErrorHandler {

    @Inject
    private AppearanceSettings appearanceSettings;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AppearanceSettings getAppearanceSettings() {
        return appearanceSettings;
    }
}
