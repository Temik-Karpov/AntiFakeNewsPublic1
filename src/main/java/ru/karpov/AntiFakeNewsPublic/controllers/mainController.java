package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import ru.karpov.AntiFakeNewsPublic.repos.*;

@Controller
public class mainController {

    protected final userRepo userRepo;
    protected final newsRepo newsRepo;
    protected final subscriptionRepo subscribeRepo;
    protected final markRepo markRepo;
    protected final imageNewsRepo imageNewsRepo;
    protected final notificationRepo notificationRepo;

    @Autowired
    public mainController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
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


    protected String getAuthUserId()
    {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
