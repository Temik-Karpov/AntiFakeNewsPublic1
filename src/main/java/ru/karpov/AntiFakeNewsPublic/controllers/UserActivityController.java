package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ru.karpov.AntiFakeNewsPublic.models.*;
import ru.karpov.AntiFakeNewsPublic.repos.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

@Controller
public class UserActivityController extends mainController {

    @Autowired
    public UserActivityController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                               final markRepo markRepo, final imageNewsRepo imageNewsRepo)
    {
        super(userRepo, newsRepo, subscribeRepo, markRepo, imageNewsRepo);
    }

    @PostMapping("/addNews")
    public RedirectView addNews(@ModelAttribute("newsData") final News news,
                                @RequestParam("photo") final MultipartFile[] files,
                                final RedirectAttributes attributes) throws IOException {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return new RedirectView("/addUserInfoPage");

        if(news.getText().isEmpty() || news.getName().isEmpty()) {
            attributes.addFlashAttribute("nullError", 1);
            attributes.addFlashAttribute("publication", 0);
            return new RedirectView("/addNewsPage");
        }
        news.setBlocked(false);
        news.setAuthorId(getAuthUserId());
        news.setDate(Date.from(Instant.now()));
        newsRepo.save(news);
        userInfo user = userRepo.findUserById(getAuthUserId());
        user.increaseCountOfPublications();
        userRepo.save(user);
        addFilesToNews(files, news);
        return new RedirectView("/");
    }


    @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
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
    public RedirectView deleteNews(@PathVariable("id") final Integer id) {
        newsRepo.delete(newsRepo.findNewsById(id));
        deleteImageNewsFromPathAndRepo(id);
        return new RedirectView("/");
    }

    private void deleteImageNewsFromPathAndRepo(final Integer id) {
        for(imageNews image : imageNewsRepo.findAllByNewsId(id))
        {
            final Path data = Paths.get(image.getImageUrl());
            deleteImageNewsFromPath(data);
        }
        imageNewsRepo.deleteAll(imageNewsRepo.findAllByNewsId(id));
    }

    private void deleteImageNewsFromPath(final Path data)
    {
        try {
            Files.delete(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/editNews/{id}")
    public String editNews(@PathVariable("id") final Integer id,
                           final Model model)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return "addUserInfoPage";
        model.addAttribute("publication", newsRepo.findNewsById(id));
        model.addAttribute("users", userRepo);
        newsRepo.delete(newsRepo.findNewsById(id));     //todo: сделать отмену изменения новости(чтобы не удалялась)
        return "addNewsPage";
    }

    @PostMapping("/rateNews/{id}")
    public RedirectView rateNews(@PathVariable("id") final Integer id,
                           @RequestParam("mark") final  Integer mark)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return new RedirectView("/addUserInfoPage");
        Mark newMark = new Mark(id, getAuthUserId(), mark);
        markRepo.save(newMark);
        calculateUserAverageMark(id);
        return new RedirectView("/");
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
    public RedirectView subscribeUser(@PathVariable("id") final String id)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return new RedirectView("/addUserInfoPage");
        Subscription subscribe = new Subscription();
        subscribe.setUserSubscribeId(id);
        subscribe.setUserId(getAuthUserId());
        subscribeRepo.save(subscribe);
        return new RedirectView("/");
    }

    @GetMapping("/unsubscribeUser/{id}")
    public RedirectView unsubscribeUser(@PathVariable("id") final String id)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            return new RedirectView("/addUserInfoPage");
        subscribeRepo.delete(subscribeRepo.findSubscriptionByUserSubscribeIdAndAndUserId(id, getAuthUserId()));
        return new RedirectView("/");
    }

    @PostMapping("/addUserInfo")
    public RedirectView addUserInfo(@ModelAttribute("userInfo") final userInfo user,
                                    @RequestParam("image") final MultipartFile file,
                                    final RedirectAttributes attributes) throws IOException {
        if(user.getUsername().isEmpty() || user.getDescription().isEmpty() || file.getBytes().length < 1)
        {
            attributes.addFlashAttribute("nullError", 1);
            return new RedirectView("/addUserInfoPage");
        }
        if(file.getBytes().length > 31457280)
        {
            attributes.addFlashAttribute("imageError", 1);
            return new RedirectView("/addUserInfoPage");
        }
        if(user.getDescription().length() > 300)
        {
            attributes.addFlashAttribute("descError", 1);
            return new RedirectView("/addUserInfoPage");
        }
        if(user.getUsername().length() > 15)
        {
            attributes.addFlashAttribute("descError", 1);
            return new RedirectView("/addUserInfoPage");
        }
        user.setId(getAuthUserId());
        user.setCountOfPublications(0);
        user.setFixFakeRating(10.0);
        user.setSearchFakeRating(10.0);
        Path fileNameAndPath = Paths.get("D:/temik/Work/Data/AntiFakeNewsPublic/src/main/resources/static/profileImages",
                file.getOriginalFilename());
            //Path fileNameAndPath = Paths.get("D:/Programs/Work/AnitFakeNewsPublic/src/main/resources/static/profileImages",
            //file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        user.setImageUrl("/profileImages/" + file.getOriginalFilename());
        userRepo.save(user);
        return new RedirectView("/authProfilePage");
    }

}
