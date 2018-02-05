package com.forrent.assignment.demo;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Controller
public class ParkLocatorController extends WebMvcConfigurerAdapter {

    @GetMapping("/parkLocator")
    public String parkLocatorForm(Model model) {
        model.addAttribute("parkLocator", new ParkLocator());
        return "parkLocator";
    }

    @PostMapping("/parkLocator")
    public String parkLocatorSubmit(@ModelAttribute ParkLocator parkLocator, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "parkLocator";
        }

        return "result";
    }
}
