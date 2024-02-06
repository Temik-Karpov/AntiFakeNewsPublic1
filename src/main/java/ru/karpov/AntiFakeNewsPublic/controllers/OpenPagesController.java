package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ru.karpov.AntiFakeNewsPublic.models.Mark;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.Subscription;
import ru.karpov.AntiFakeNewsPublic.models.userInfo;
import ru.karpov.AntiFakeNewsPublic.repos.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class OpenPagesController extends mainController {

    private final notificationRepo notificationRepo;

    @Autowired
    public OpenPagesController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                          final markRepo markRepo, final imageNewsRepo imageNewsRepo,
                          final notificationRepo notificationRepo)
    {
        super(userRepo, newsRepo, subscribeRepo, markRepo, imageNewsRepo);
        this.notificationRepo = notificationRepo;
    }

    @PostMapping("/AddNews")
    public RedirectView getADDNEWS(@RequestParam("category") final Integer category,
                                   @RequestParam("name") final String name,
                                   @RequestParam("text") final String text,
                                   @RequestParam("userId") final Integer id)
    {
        return new RedirectView("/");
    }

    @PostMapping("/EditInfo")
    public RedirectView getADDNEWS(@RequestParam("firstName") final String name,
                                   @RequestParam("LastName") final String last,
                                   @RequestParam("username") final String text,
                                   @RequestParam("Age") final Integer gh)
    {
        return new RedirectView("/");
    }
    /*
    @GetMapping("/mainPage")
    public String getMainPage(final Model model)
    {
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/")
    public RedirectView getMainPage(final RedirectAttributes attributes)
    {
        attributes.addFlashAttribute("publications", newsRepo.findNewsByIsBlockedFalse());
        return new RedirectView("/mainPage");
    }
*/

    @GetMapping("/mainPage")
    public String getMainPage(final Model model)
    {
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/")
    public String getMainPage(final RedirectAttributes attributes, final Model model)
    {
        model.addAttribute("publications", newsRepo.findNewsByIsBlockedFalse());
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @PostMapping("/reloadMainPage")
    public RedirectView getReloadMainWithNewCategoryPage(@RequestParam("category") final Integer category,
                                                         final RedirectAttributes attributes)
    {
        List<News> news = getListOfNewsAtMainPageWithNewCategory(category);
        attributes.addFlashAttribute("publications", news);
        return new RedirectView("/");
    }

    private List<News> getListOfNewsAtMainPageWithNewCategory(final Integer category)
    {
        if(category != 0) {
            return newsRepo.findNewsByCategoryIdAndIsBlockedFalse(category);
        }
        else {
            return newsRepo.findNewsByIsBlockedFalse();
        }
    }

    @GetMapping("/authProfilePageInfo")
    public String getAuthProfilePage(final Model model)
    {
        model.addAttribute("user", userRepo.findUserById(getAuthUserId()));
        model.addAttribute("users", userRepo);
        return "authProfilePage";
    }

    @GetMapping("/authProfilePage")
    public RedirectView getAuthProfilePage(final RedirectAttributes attributes)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return new RedirectView("/addUserInfoPage");
        attributes.addFlashAttribute("publications", newsRepo.findNewsByAuthorId(getAuthUserId()));
        return new RedirectView("/authProfilePageInfo");
    }

    @GetMapping("/addUserInfoPage")
    public String getAddUserInfoPage()
    {
        return "addUserInfoPage";
    }

    @PostMapping("/reloadAuthProfilePage")
    public RedirectView reloadAuthProfileWithNewCategoryPage(@RequestParam("category") final Integer category,
                                                             final RedirectAttributes attributes)
    {
        List<News> news = getListOfNewsAtAuthProfilePageWithNewCategory(category);
        attributes.addFlashAttribute("publications", news);
        return new RedirectView("/authProfilePageInfo");
    }

    private List<News> getListOfNewsAtAuthProfilePageWithNewCategory(final Integer category)
    {
        if(category != 0) {
            return newsRepo.findNewsByCategoryIdAndAuthorId(category, getAuthUserId());
        }
        else {
            return newsRepo.findNewsByAuthorId(getAuthUserId());
        }
    }

    @GetMapping("/subscriptionsPage")
    public String getSubscriptionsPage(final Model model)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return "addUserInfoPage";
        model.addAttribute("subscribes", getUserSubscriptions());
        return "subscriptionsPage";
    }

    private List<userInfo> getUserSubscriptions()
    {
        final List<userInfo> subscribeUsers = new ArrayList<>();
        for (Subscription subscribe : subscribeRepo.findSubscriptionByUserId(getAuthUserId()))
        {
            subscribeUsers.add(userRepo.findUserById(subscribe.getUserSubscribeId()));
        }
        return subscribeUsers;
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
        model.addAttribute("isSub", checkSubscriptionOnCurrentUser(user));
        return "profilePage";
    }

    private Subscription checkSubscriptionOnCurrentUser(final userInfo user)
    {
        return subscribeRepo.findSubscriptionByUserSubscribeIdAndAndUserId(user.getId(), getAuthUserId());
    }

    @GetMapping("/logout")
    public RedirectView logout(final HttpServletRequest request) throws ServletException {
        request.logout();
        return new RedirectView("/");
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
        model.addAttribute("edit", isItOurNews(id));
        model.addAttribute("rate", isNewsAppreciated(id));
        return "newsPage";
    }

    private Mark isNewsAppreciated(final Integer id)
    {
        return markRepo.findMarkByUserIdAndNewsId(getAuthUserId(), id);
    }

    private Boolean isItOurNews(final Integer id)
    {
        return newsRepo.findNewsById(id).getAuthorId().equals(getAuthUserId());
    }

    @PostMapping("/reloadProfilePage/{userId}")
    public String reloadProfilePage(@PathVariable("userId") final String userId,
                                    @RequestParam("category") final Integer category,
                                    final Model model)
    {
        model.addAttribute("users", userRepo);
        model.addAttribute("user", userRepo.findUserById(userId));
        model.addAttribute("publications", getListOfNewsAtProfilePageWithNewCategory(category, userId));
        return "profilePage";
    }

    private List<News> getListOfNewsAtProfilePageWithNewCategory(final Integer category, final String userId)
    {
        if(category != 0) {
            return newsRepo.findNewsByCategoryIdAndAuthorIdAndIsBlockedIsFalse(category, userId);
        }
        else {
            return newsRepo.findNewsByAuthorIdAndIsBlockedIsFalse(userId);
        }
    }

    @GetMapping("/addNewsPage")
    public String getAddNewsPage(final Model model)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return "addUserInfoPage";
        model.addAttribute("publication", 0);
        return "addNewsPage";
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

    @GetMapping("/notificationPage")
    public String getNotificationPage()
    {
        return "notificationPage";
    }
}
