package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.Notification;
import ru.karpov.AntiFakeNewsPublic.repos.*;

@Controller
public class AdminActivityController extends mainController {

    public AdminActivityController(final userRepo userRepo, final newsRepo newsRepo,
                                   final subscriptionRepo subscribeRepo, final markRepo markRepo,
                                   final imageNewsRepo imageNewsRepo, final notificationRepo notificationRepo) {
        super(userRepo, newsRepo, subscribeRepo, markRepo, imageNewsRepo, notificationRepo);
    }

    @GetMapping("/userListPage")
    public String getUsersListPage(final Model model)
    {
        model.addAttribute("users", userRepo.findAll());
        return "userListPage";
    }

    @GetMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") final String id)
    {
        userRepo.delete(userRepo.findUserById(id));
        if(newsRepo.findNewsByAuthorId(id) != null) {
            newsRepo.deleteAll(newsRepo.findNewsByAuthorId(id));
            for (News news : newsRepo.findNewsByAuthorId(id)) {
                if(imageNewsRepo.findAllByNewsId(news.getId()) != null)
                    imageNewsRepo.deleteAll(imageNewsRepo.findAllByNewsId(news.getId()));
            }
        }
        if(subscribeRepo.findSubscriptionByUserId(id) != null)
            subscribeRepo.deleteAll(subscribeRepo.findSubscriptionByUserId(id));
        if(markRepo.findMarkByUserId(id) != null)
            markRepo.deleteAll(markRepo.findMarkByUserId(id));
        return "forward:/userListPage";
    }

    @GetMapping("/notifyUserPage/{id}")
    public String getNotifyUserPage(@PathVariable("id") final String id,
                             Model model)
    {
        model.addAttribute("id", id);
        return "notificationPage";
    }

    @PostMapping("/sendNotification/{id}")
    public String sendNotification(@PathVariable("id") final String id,
                                   @RequestParam("name") final String name,
                                   @RequestParam("notification") final String notification,
                                   Model model)
    {
        if(name.isEmpty() || notification.isEmpty())
        {
            model.addAttribute("id", id);
            model.addAttribute("nullError", 1);
            return "notificationPage";
        }
        Notification newNotification = new Notification();
        newNotification.setName(name);
        newNotification.setText(notification);
        newNotification.setUserId(id);
        notificationRepo.save(newNotification);
        return "redirect:/userListPage";
    }
}
