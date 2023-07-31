package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.karpov.AntiFakeNewsPublic.models.*;
import ru.karpov.AntiFakeNewsPublic.repos.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

@Controller
public class UserActivityController extends OpenPagesController {

    public UserActivityController(final userRepo userRepo, final newsRepo newsRepo,
                                  final subscriptionRepo subscribeRepo, final markRepo markRepo,
                                  final imageNewsRepo imageNewsRepo, final notificationRepo notificationRepo) {
        super(userRepo, newsRepo, subscribeRepo, markRepo, imageNewsRepo, notificationRepo);
    }

    @PostMapping("/addNews")
    public String addNews(@RequestParam("Title") final String title, @RequestParam("text") final String text,
                          @RequestParam("category") final Integer category,
                          @RequestParam("photo") final MultipartFile[] files,
                          final Model model) throws IOException {
        String returnedValue = checkUserAvailabilityInSystem();
        returnedValue = isNewsTextAndTitleEmpty(text, title, model);
        News news = new News(title, category, text, Date.from(Instant.now()),
                getAuthUserId(), false);
        newsRepo.save(news);
        userInfo user = userRepo.findUserById(getAuthUserId());
        user.increaseCountOfPublications();
        userRepo.save(user);
        addFilesToNews(files, news);
        return "redirect:/";
    }

    private String isNewsTextAndTitleEmpty(final String text, final String title,
                                           final Model model)
    {
        if(text.isEmpty() || title.isEmpty())
        {
            model.addAttribute("nullError", 1);
            model.addAttribute("publication", 0);
            return "addNewsPage";
        }
        return null;
    }

    private void addFilesToNews(final MultipartFile[] files, final News news) throws IOException {
        for (MultipartFile file : files) {
            if(file.getBytes().length > 0) {
                StringBuilder fileNames = new StringBuilder();
                Path fileNameAndPath = Paths.get("D:/temik/Work/Data/AntiFakeNewsPublic/src/main/resources/static/newsImages",
                        file.getOriginalFilename());
                //Path fileNameAndPath = Paths.get("D:/Programs/Work/AnitFakeNewsPublic/src/main/resources/static/newsImages",
                // file.getOriginalFilename());
                fileNames.append(file.getOriginalFilename());
                Files.write(fileNameAndPath, file.getBytes());
                final imageNews image = new imageNews();
                image.setImageUrl("/newsImages/" + file.getOriginalFilename());
                image.setNewsId(news.getId());
                imageNewsRepo.save(image);
            }
        }
    }

    @GetMapping("/deleteNews/{id}")
    public String deleteNews(@PathVariable("id") final Integer id)
    {
        newsRepo.delete(newsRepo.findNewsById(id));
        imageNewsRepo.deleteAll(imageNewsRepo.findAllByNewsId(id));     //todo: удаление картинок из path
        return "redirect:/";
    }

    @GetMapping("/editNews/{id}")
    public String editNews(@PathVariable("id") final Integer id,
                           final Model model)
    {
        final String returnedValue = checkUserAvailabilityInSystem();
        model.addAttribute("publication", newsRepo.findNewsById(id));
        model.addAttribute("users", userRepo);
        newsRepo.delete(newsRepo.findNewsById(id));     //todo: сделать отмену изменения новости(чтобы не удалялась)
        return "addNewsPage";
    }

    @PostMapping("/rateNews/{id}")
    public String rateNews(@PathVariable("id") final Integer id,
                           @RequestParam("mark") final  Integer mark)
    {
        final String returnedValue = checkUserAvailabilityInSystem();
        Mark newMark = new Mark(id, getAuthUserId(), mark);
        markRepo.save(newMark);
        calculateUserAverageMark(id);
        return "redirect:/";
    }

    private void calculateUserAverageMark(final Integer id)
    {
        userInfo user = userRepo.findUserById(newsRepo.findNewsById(id).getAuthorId());
        int sum = 0;
        for(Mark previousMark : markRepo.findMarkByUserId(user.getId()))
        {
            sum += previousMark.getMark();
        }
        user.setAverageMark((double) (sum / user.getCountOfPublications()));
        userRepo.save(user);
    }

    @GetMapping("/subscribeUser/{id}")
    public String subscribeUser(@PathVariable("id") final String id)
    {
        final String returnedValue = checkUserAvailabilityInSystem();
        Subscription subscribe = new Subscription();
        subscribe.setUserSubscribeId(id);
        subscribe.setUserId(getAuthUserId());
        subscribeRepo.save(subscribe);

        return "redirect:/";
    }

    @GetMapping("/unsubscribeUser/{id}")
    public String unsubscribeUser(@PathVariable("id") final String id)
    {
        final String returnedValue = checkUserAvailabilityInSystem();
        subscribeRepo.delete(subscribeRepo.findSubscriptionByUserSubscribeIdAndAndUserId(id, getAuthUserId()));
        return "redirect:/";
    }

    @PostMapping("/addUserInfo")
    public String addUserInfo(@RequestParam("username") final String username,
                              @RequestParam("email") final String email,
                              @RequestParam("description") final String description,
                              @RequestParam("image") final MultipartFile file,
                              final Model model) throws IOException {
        final String returnedValue = isUserInfoCorrect(username, description, file, model);
        userInfo newUser = new userInfo(getAuthUserId(), username, email, description,
                10.0, 10.0, 0);

        Path fileNameAndPath = Paths.get("D:/temik/Work/Data/AntiFakeNewsPublic/src/main/resources/static/profileImages",
                file.getOriginalFilename());
            //Path fileNameAndPath = Paths.get("D:/Programs/Work/AnitFakeNewsPublic/src/main/resources/static/profileImages",
            //file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        newUser.setImageUrl("/profileImages/" + file.getOriginalFilename());

        userRepo.save(newUser);
        return "redirect:/authProfilePage";
    }

    private String isUserInfoCorrect(final String username,
                                     final String description, final MultipartFile file,
                                     final Model model) throws IOException {
        if(username.isEmpty() || description.isEmpty() || file.getBytes().length < 1)
        {
            model.addAttribute("nullError", 1);
            return "addUserInfoPage";
        }
        if(file.getBytes().length > 31457280)
        {
            model.addAttribute("imageError", 1);
            return "addUserInfoPage";
        }
        if(description.length() > 300)
        {
            model.addAttribute("descError", 1);
            return "addUserInfoPage";
        }
        if(username.length() > 15)
        {
            model.addAttribute("descError", 1);
            return "addUserInfoPage";
        }
        return null;
    }

}
