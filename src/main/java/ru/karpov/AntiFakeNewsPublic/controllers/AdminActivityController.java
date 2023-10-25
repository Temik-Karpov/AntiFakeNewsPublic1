package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.Notification;
import ru.karpov.AntiFakeNewsPublic.repos.*;

@Controller
public class AdminActivityController extends mainController {

    private final notificationRepo notificationRepo;

    @Autowired
    public AdminActivityController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                               final markRepo markRepo, final imageNewsRepo imageNewsRepo,
                               final notificationRepo notificationRepo)
    {
        super(userRepo, newsRepo, subscribeRepo, markRepo, imageNewsRepo);
        this.notificationRepo = notificationRepo;
    }

    @GetMapping("/userListPage")
    public String getUsersListPage(final Model model)
    {
        model.addAttribute("users", userRepo.findAll());
        return "userListPage";
    }

    @GetMapping("/deleteUser/{id}")
    public RedirectView deleteUser(@PathVariable("id") final String id)
    {
        userRepo.delete(userRepo.findUserById(id));
        deleteUserNews(id);
        deleteUserSubscribe(id);
        deleteUserMarks(id);
        return new RedirectView("/userListPage");
    }

    private void deleteUserNews(final String id)
    {
        if(newsRepo.findNewsByAuthorId(id) != null) {
            newsRepo.deleteAll(newsRepo.findNewsByAuthorId(id));
            for (News news : newsRepo.findNewsByAuthorId(id)) {
                if(imageNewsRepo.findAllByNewsId(news.getId()) != null)
                    imageNewsRepo.deleteAll(imageNewsRepo.findAllByNewsId(news.getId()));
            }
        }
    }

    private void deleteUserSubscribe(final String id)
    {
        if(subscribeRepo.findSubscriptionByUserId(id) != null)
            subscribeRepo.deleteAll(subscribeRepo.findSubscriptionByUserId(id));
    }

    private void deleteUserMarks(final String id)
    {
        if(markRepo.findMarkByUserId(id) != null)
            markRepo.deleteAll(markRepo.findMarkByUserId(id));
    }

    @GetMapping("/notifyUserPage/{id}")
    public RedirectView getNotifyUserPage(@PathVariable("id") final String id,
                                          final RedirectAttributes attributes)
    {
        attributes.addFlashAttribute("id", id);
        return new RedirectView("/notificationPage");
    }

    @PostMapping("/sendNotification/{id}")
    public RedirectView sendNotification(@PathVariable("id") final String id,
                                         @ModelAttribute("notificationData") final Notification notification,
                                         final RedirectAttributes attributes)
    {
        if(notification.getName().isEmpty() || notification.getText().isEmpty())
        {
            attributes.addFlashAttribute("id", id);
            attributes.addFlashAttribute("nullError", 1);
            return new RedirectView("/notificationPage");
        }
        notification.setUserId(id);
        notificationRepo.save(notification);
        return new RedirectView("/userListPage");
    }
}
