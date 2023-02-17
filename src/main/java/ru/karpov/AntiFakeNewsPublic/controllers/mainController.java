package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class mainController {

    @GetMapping("/")
    public String getMainPage()
    {
        return "mainPage";
    }

}
