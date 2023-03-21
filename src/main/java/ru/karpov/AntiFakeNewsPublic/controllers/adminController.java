package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.Notification;
import ru.karpov.AntiFakeNewsPublic.repos.*;

@Controller
public class adminController {
    private newsRepo newsRepo;
    private userRepo userRepo;
    private imageNewsRepo imageNewsRepo;
    private subscriptionRepo subscribeRepo;
    private markRepo markRepo;
    private notificationRepo notificationRepo;

    @Autowired
    public adminController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                          final markRepo markRepo, final imageNewsRepo imageNewsRepo,
                           final notificationRepo notificationRepo)
    {
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
        this.subscribeRepo = subscribeRepo;
        this.markRepo = markRepo;
        this.imageNewsRepo = imageNewsRepo;
        this.notificationRepo = notificationRepo;
    }

    @GetMapping("/userListPage")
    public String getUsersListPage(Model model)
    {
        model.addAttribute("users", userRepo.findAll());
        return "userListPage";
    }

    @GetMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") String id)
    {
        userRepo.delete(userRepo.findUserById(id));
        newsRepo.deleteAll(newsRepo.findNewsByAuthorId(id));
        for (News news : newsRepo.findNewsByAuthorId(id))
        {
            imageNewsRepo.deleteAll(imageNewsRepo.findAllByNewsId(news.getId()));
        }
        subscribeRepo.deleteAll(subscribeRepo.findSubscriptionByUserId(id));
        markRepo.deleteAll(markRepo.findMarkByUserId(id));
        return "mainPage";
    }

    @GetMapping("/notifyUserPage/{id}")
    public String getNotifyUserPage(@PathVariable("id") String id,
                             Model model)
    {
        model.addAttribute("id", id);
        return "mainPage";
    }
}
