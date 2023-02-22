package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.karpov.AntiFakeNewsPublic.models.userInfo;
import ru.karpov.AntiFakeNewsPublic.repos.newsRepo;
import ru.karpov.AntiFakeNewsPublic.repos.userRepo;

@Controller
public class mainController {

    private userRepo userRepo;
    private newsRepo newsRepo;

    @Autowired
    public mainController(final userRepo userRepo, final newsRepo newsRepo)
    {
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
    }

    @GetMapping("/")
    public String getMainPage()
    {
        return "mainPage";
    }

    @GetMapping("/authProfilePage")
    public String getAuthProfilePage(Model model)
    {
        final String id = "";  //TODO: получение id авторизованного пользователя
        final userInfo authUser = userRepo.findUserById(id);
        if(authUser != null)
        {
            model.addAttribute("user", authUser);
            model.addAttribute("publications", newsRepo.findNewsByAuthorId(id));
            return "authProfilePage";
        }
        return ""; //TODO: сделать либо переход на страницу заполнения инфы, либо выспалвающее окошко
    }

    @GetMapping("/reloadMainPage")
    public String getReloadMainPage(@RequestParam("category") Integer category, Model model)
    {
        model.addAttribute("publications", newsRepo.findNewsByCategoryId(category));
        return "mainPage";
    }
}
