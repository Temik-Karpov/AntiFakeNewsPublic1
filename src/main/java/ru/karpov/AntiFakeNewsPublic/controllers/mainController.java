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
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.Subscription;
import ru.karpov.AntiFakeNewsPublic.models.userInfo;
import ru.karpov.AntiFakeNewsPublic.repos.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class mainController {

    private userRepo userRepo;
    private newsRepo newsRepo;
    private subscriptionRepo subscribeRepo;
    private markRepo markRepo;
    private imageNewsRepo imageNewsRepo;

    @Autowired
    public mainController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                          final markRepo markRepo, final imageNewsRepo imageNewsRepo)
    {
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
        this.subscribeRepo = subscribeRepo;
        this.markRepo = markRepo;
        this.imageNewsRepo = imageNewsRepo;
    }

    private int isAuth()
    {
        return SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser") ? 0 : 1;
    }

    private String getAuthUserId()
    {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @GetMapping("/")
    public String getMainPage(Model model)
    {
        model.addAttribute("publications", newsRepo.findAll());
        model.addAttribute("users", userRepo);
        model.addAttribute("isAuth", isAuth());
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
            model.addAttribute("users", userRepo);
            return "authProfilePage";
        }
        return "addUserInfoPage"; //TODO: сделать либо переход на страницу заполнения инфы, либо выспалвающее окошко
    }

    @PostMapping("/reloadMainPage")
    public String getReloadMainPage(@RequestParam("category") Integer category, Model model)
    {
        model.addAttribute("users", userRepo);
        if(category != 0) {
            model.addAttribute("publications", newsRepo.findNewsByCategoryId(category));
        }
        else
        {
            List<News> pub;
            pub = newsRepo.findAll();
            Collections.reverse(pub);
            model.addAttribute("publications", pub);
        }
        model.addAttribute("isAuth", isAuth());
        return "mainPage";
    }

    @PostMapping("/reloadAuthProfilePage")
    public String reloadAuthProfilePage(@RequestParam("category") Integer category, Model model)
    {
        final userInfo authUser = userRepo.findUserById(getAuthUserId());
        model.addAttribute("users", userRepo);
        model.addAttribute("user", authUser);
        if(category != 0) {
            model.addAttribute("publications", newsRepo.findNewsByCategoryIdAndAuthorId(category,
                    getAuthUserId()));
        }
        else
        {
            List<News> pub;
            pub = newsRepo.findAll();
            Collections.reverse(pub);
            model.addAttribute("publications", newsRepo.findNewsByAuthorId(getAuthUserId()));
        }
        model.addAttribute("isAuth", isAuth());
        return "authProfilePage";
    }

    @GetMapping("/subscriptionsPage")
    public String getSubscriptionsPage(Model model)
    {
        final List<userInfo> subscribeUsers = new ArrayList<>();
        for (Subscription subscribe : subscribeRepo.findSubscriptionByUserId(getAuthUserId()))
        {
            subscribeUsers.add(userRepo.findUserById(subscribe.getUserSubscribeId()));
        }
        model.addAttribute("subscribes", subscribeUsers);
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
        model.addAttribute("users", userRepo);
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

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, Model model) throws ServletException {
        request.logout();
        List<News> pub;
        pub = newsRepo.findAll();
        Collections.reverse(pub);
        model.addAttribute("publications", pub);
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/newsPage/{id}")
    public String getNewsPage(@PathVariable("id") Integer id,
                              Model model)
    {
        model.addAttribute("news", newsRepo.findNewsById(id));
        model.addAttribute("image", imageNewsRepo.findAllByNewsId(id).size() != 0 ?
        imageNewsRepo.findAllByNewsId(id).get(0).getImageUrl() : null);
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
        model.addAttribute("users", userRepo);
        model.addAttribute("user", user);
        if(category != 0) {
            model.addAttribute("publications", newsRepo.findNewsByCategoryIdAndAuthorId(category, userId));
        }
        else
        {
            List<News> pub;
            pub = newsRepo.findAll();
            Collections.reverse(pub);
            model.addAttribute("publications", newsRepo.findNewsByAuthorId(userId));
        }
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

    @GetMapping("/addUserInfoPage")
    public String getAddUserInfoPage()
    {
        return "addUserInfoPage";
    }
}
