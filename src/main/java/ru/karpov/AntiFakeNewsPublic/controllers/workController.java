package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.karpov.AntiFakeNewsPublic.repos.markRepo;
import ru.karpov.AntiFakeNewsPublic.repos.newsRepo;
import ru.karpov.AntiFakeNewsPublic.repos.subscriptionRepo;
import ru.karpov.AntiFakeNewsPublic.repos.userRepo;

@Controller
public class workController {

    private userRepo userRepo;
    private newsRepo newsRepo;
    private subscriptionRepo subscribeRepo;
    private markRepo markRepo;

    @Autowired
    public workController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                          final markRepo markRepo)
    {
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
        this.subscribeRepo = subscribeRepo;
        this.markRepo = markRepo;
    }


}
