package com.infoclinika.mssharing.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static com.infoclinika.mssharing.platform.web.security.RichUser.getUserId;

/**
 * @author vladislav.kovchug
 */
@Controller
@RequestMapping("/preferences")
public class ChartsPreferencesController {

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object readPreferences(Principal principal) {
        throw new RuntimeException();
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void savePreferences(@RequestBody Object request, Principal principal) {

    }
}
