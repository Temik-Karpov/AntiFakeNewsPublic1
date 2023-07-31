package ru.karpov.AntiFakeNewsPublic.controllers;

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
import java.util.Comparator;
import java.util.List;

@Controller
public class OpenPagesController extends mainController {

    public OpenPagesController(final userRepo userRepo, final newsRepo newsRepo,
                               final subscriptionRepo subscribeRepo, final markRepo markRepo,
                               final imageNewsRepo imageNewsRepo, final notificationRepo notificationRepo) {
        super(userRepo, newsRepo, subscribeRepo, markRepo, imageNewsRepo, notificationRepo);
    }

    @GetMapping("/")
    public String getMainPage(final Model model)
    {
        model.addAttribute("publications", newsRepo.findNewsByIsBlockedFalse());
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/authProfilePage")
    public String getAuthProfilePage(final Model model)
    {
        final userInfo authUser = userRepo.findUserById(getAuthUserId());
        if(authUser != null)
        {
            model.addAttribute("user", authUser);
            model.addAttribute("publications", newsRepo.findNewsByAuthorId(getAuthUserId()));
            model.addAttribute("users", userRepo);
            return "authProfilePage";
        }
        return "addUserInfoPage";
    }

    @PostMapping("/reloadMainPage")
    public String getReloadMainPage(@RequestParam("category") final Integer category,
                                    final Model model)
    {
        model.addAttribute("users", userRepo);
        if(category != 0) {
            model.addAttribute("publications", newsRepo.findNewsByCategoryIdAndIsBlockedFalse(category));
        }
        else
        {
            model.addAttribute("publications", newsRepo.findNewsByIsBlockedFalse());
        }
        return "mainPage";
    }

    @PostMapping("/reloadAuthProfilePage")
    public String reloadAuthProfilePage(@RequestParam("category") final Integer category,
                                        final Model model)
    {
        final userInfo authUser = userRepo.findUserById(getAuthUserId());
        model.addAttribute("users", userRepo);
        model.addAttribute("user", authUser);
        if(category != 0) {
            model.addAttribute("publications",
                    newsRepo.findNewsByCategoryIdAndAuthorId(category, getAuthUserId()));
        }
        else
        {
            List<News> pub;
            pub = newsRepo.findAll();
            Collections.reverse(pub);
            model.addAttribute("publications", newsRepo.findNewsByAuthorId(getAuthUserId()));
        }
        return "authProfilePage";
    }

    @GetMapping("/subscriptionsPage")
    public String getSubscriptionsPage(final Model model)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
        {
            return "addUserInfoPage";
        }
        final List<userInfo> subscribeUsers = new ArrayList<>();
        for (Subscription subscribe : subscribeRepo.findSubscriptionByUserId(getAuthUserId()))
        {
            subscribeUsers.add(userRepo.findUserById(subscribe.getUserSubscribeId()));
        }
        model.addAttribute("subscribes", subscribeUsers);
        return "subscriptionsPage";
    }

    @GetMapping("/profilePage/{usernameId}")
    public String getProfilePage(@PathVariable("usernameId") final String usernameId,
                                 final Model model)
    {
        userInfo user = userRepo.findUserById(usernameId);
        model.addAttribute("user", user);
        model.addAttribute("publications", newsRepo.findNewsByAuthorIdAndIsBlockedIsFalse(usernameId));
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
    public String logout(final HttpServletRequest request) throws ServletException {
        request.logout();
        return "redirect:/";
    }

    @GetMapping("/newsPage/{id}")
    public String getNewsPage(@PathVariable("id") final Integer id,
                              final Model model)
    {
        model.addAttribute("news", newsRepo.findNewsById(id));
        model.addAttribute("user",
                userRepo.findUserById(newsRepo.findNewsById(id).getAuthorId()).getUsername());
        model.addAttribute("image", imageNewsRepo.findAllByNewsId(id).size() != 0 ?
        imageNewsRepo.findAllByNewsId(id).get(0).getImageUrl() : null);
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
    public String reloadProfilePage(@PathVariable("userId") final String userId,
                                    @RequestParam("category") final Integer category,
                                    final  Model model)
    {
        userInfo user = userRepo.findUserById(userId);
        model.addAttribute("users", userRepo);
        model.addAttribute("user", user);
        if(category != 0) {
            model.addAttribute("publications",
                    newsRepo.findNewsByCategoryIdAndAuthorIdAndIsBlockedIsFalse(category, userId));
        }
        else
        {
            model.addAttribute("publications", newsRepo.findNewsByAuthorIdAndIsBlockedIsFalse(userId));
        }
        return "profilePage";
    }

    @GetMapping("/addNewsPage")
    public String getAddNewsPage(final Model model)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
        {
            return "addUserInfoPage";
        }
        model.addAttribute("publication", 0);
        return "addNewsPage";
    }

    @GetMapping("/addUserInfoPage")
    public String getAddUserInfoPage()
    {
        return "addUserInfoPage";
    }

    @GetMapping("/warningsPage")
    public String getWarningsPage(final Model model)
    {
        model.addAttribute("warnings", notificationRepo.getNotificationByUserId(getAuthUserId()));
        return "warningsPage";
    }

    @GetMapping("/ratingPage")
    public String getRatingPage(final Model model)
    {
        List<userInfo> users = userRepo.findAll();
        users.sort(Comparator.comparing(userInfo::getSearchFakeRating).reversed());
        model.addAttribute("users", users);
        return "showRatingPage";
    }
}
