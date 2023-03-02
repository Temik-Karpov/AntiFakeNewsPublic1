package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.karpov.AntiFakeNewsPublic.models.userInfo;
import ru.karpov.AntiFakeNewsPublic.repos.markRepo;
import ru.karpov.AntiFakeNewsPublic.repos.newsRepo;
import ru.karpov.AntiFakeNewsPublic.repos.subscriptionRepo;
import ru.karpov.AntiFakeNewsPublic.repos.userRepo;

@Controller
public class mainController {

    private userRepo userRepo;
    private newsRepo newsRepo;
    private subscriptionRepo subscribeRepo;
    private markRepo markRepo;

    @Autowired
    public mainController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                          final markRepo markRepo)
    {
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
        this.subscribeRepo = subscribeRepo;
        this.markRepo = markRepo;
    }

    private int isAuth()
    {
        return SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser") ? 0 : 1;
    }

    private String getAuthUserId()
    {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String id = authentication.getName();
        return id;
    }

    @GetMapping("/")
    public String getMainPage()
    {
        return "mainPage";
    }

    @GetMapping("/authProfilePage")
    public String getAuthProfilePage(Model model)
    {
        final userInfo authUser = userRepo.findUserById(getAuthUserId());
        model.addAttribute("isAuth", isAuth());
        if(authUser != null)
        {
            model.addAttribute("user", authUser);
            model.addAttribute("publications", newsRepo.findNewsByAuthorId(getAuthUserId()));
            return "authProfilePage";
        }
        return ""; //TODO: сделать либо переход на страницу заполнения инфы, либо выспалвающее окошко
    }

    @GetMapping("/reloadMainPage")
    public String getReloadMainPage(@RequestParam("category") Integer category, Model model)
    {
        model.addAttribute("publications", newsRepo.findNewsByCategoryId(category));
        model.addAttribute("isAuth", isAuth());
        return "mainPage";
    }

    @PostMapping("/reloadAuthProfilePage")
    public String reloadAuthProfilePage(@RequestParam("category") Integer category, Model model)
    {
        final userInfo authUser = userRepo.findUserById(getAuthUserId());
        model.addAttribute("user", authUser);
        model.addAttribute("publications", newsRepo.findNewsByCategoryId(category));
        model.addAttribute("isAuth", isAuth());
        return "authProfilePage";
    }

    @GetMapping("/subscriptionsPage")
    public String getSubscriptionsPage(Model model)
    {
        model.addAttribute("subscribes", subscribeRepo.findSubscriptionByUserId(getAuthUserId()));
        model.addAttribute("isAuth", isAuth());
        return "subscriptionsPage";
    }

    @GetMapping("/profilePage/{usernameId}")
    public String getProfilePage(@PathVariable("usernameId") String usernameId, Model model)
    {
        userInfo user = userRepo.findUserById(usernameId);
        model.addAttribute("user", user);
        model.addAttribute("publications", newsRepo.findNewsByAuthorId(usernameId));
        model.addAttribute("isAuth", isAuth());
        if(getAuthUserId().equals(user.getId()))
        {
            return "authProfilePage";
        }
        if(subscribeRepo.findSubscriptionByUserSubscribeIdAndAndUserId(user.getId(), getAuthUserId()) != null)
        {
            model.addAttribute("isSub", 1);
        }
        else
        {
            model.addAttribute("isSub", 0);
        }
        return "profilePage";
    }

    @GetMapping("/newsPage/{id}")
    public String getNewsPage(@PathVariable("id") Integer id,
                              Model model)
    {
        model.addAttribute("news", newsRepo.findNewsById(id));
        model.addAttribute("isAuth", isAuth());
        if(newsRepo.findNewsById(id).getAuthorId().equals(getAuthUserId()))
        {
            model.addAttribute("edit", 1);
        }
        else
        {
            model.addAttribute("edit", 0);
        }
        if (markRepo.findMarkByUserIdAndNewsId(getAuthUserId(), id) != null) {
            model.addAttribute("rate", 1);
        } else {
            model.addAttribute("rate", 0);
        }
        return "newsPage";
    }

    @PostMapping("/reloadProfilePage/{userId}")
    public String reloadProfilePage(@PathVariable("userId") String userId,
                                    @RequestParam("category") Integer category,
                                    Model model)
    {
        userInfo user = userRepo.findUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("publications", newsRepo.findNewsByCategoryId(category));
        model.addAttribute("isAuth", isAuth());
        return "profilePage";
    }

    @GetMapping("/addNewsPage")
    public String getAddNewsPage(Model model)
    {
        model.addAttribute("publication", 0);
        model.addAttribute("isAuth", isAuth());
        return "addNewsPage";
    }
}
