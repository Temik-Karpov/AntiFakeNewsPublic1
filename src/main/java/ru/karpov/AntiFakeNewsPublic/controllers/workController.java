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
public class workController {

    private final userRepo userRepo;
    private final newsRepo newsRepo;
    private final subscriptionRepo subscribeRepo;
    private final markRepo markRepo;
    private final imageNewsRepo imageNewsRepo;

    @Autowired
    public workController(final userRepo userRepo, final newsRepo newsRepo, final subscriptionRepo subscribeRepo,
                          final markRepo markRepo, final imageNewsRepo imageNewsRepo)
    {
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
        this.subscribeRepo = subscribeRepo;
        this.markRepo = markRepo;
        this.imageNewsRepo = imageNewsRepo;
    }

    private String getAuthUserId()
    {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping("/addNews")
    public String addNews(@RequestParam("Title") final String title, @RequestParam("text") final String text,
                          @RequestParam("category") final Integer category,
                          @RequestParam("photo") final MultipartFile[] files,
                          Model model) throws IOException {
        if(userRepo.findUserById(getAuthUserId()) == null)
        {
            return "addUserInfoPage";
        }
        if(text.isEmpty() || title.isEmpty())
        {
            model.addAttribute("nullError", 1);
            model.addAttribute("publication", 0);
            return "addNewsPage";
        }
        News news = new News();
        news.setName(title);
        news.setCategoryId(category);
        news.setText(text);
        news.setDate(Date.from(Instant.now()));
        news.setAuthorId(getAuthUserId());
        news.setBlocked(false);
        newsRepo.save(news);
        userInfo user = userRepo.findUserById(getAuthUserId());
        user.increaseCountOfPublications();
        userRepo.save(user);

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
        return "redirect:/";
    }

    @GetMapping("/deleteNews/{id}")
    public String deleteNews(@PathVariable("id") final Integer id)
    {
        News news = newsRepo.findNewsById(id);
        newsRepo.delete(news);
        imageNewsRepo.deleteAll(imageNewsRepo.findAllByNewsId(id));     //todo: удаление картинок из path
        return "redirect:/";
    }

    @GetMapping("/editNews/{id}")
    public String editNews(@PathVariable("id") final Integer id,
                           final Model model)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
        {
            return "addUserInfoPage";
        }
        model.addAttribute("publication", newsRepo.findNewsById(id));
        model.addAttribute("users", userRepo);
        newsRepo.delete(newsRepo.findNewsById(id));     //todo: сделать отмену изменения новости(чтобы не удалялась)
        return "addNewsPage";
    }

    @PostMapping("/rateNews/{id}")
    public String rateNews(@PathVariable("id")final  Integer id,
                           @RequestParam("mark")final  Integer mark)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
        {
            return "addUserInfoPage";
        }

        Mark newMark = new Mark();
        newMark.setUserId(getAuthUserId());
        newMark.setNewsId(id);
        newMark.setMark(mark);
        markRepo.save(newMark);

        userInfo user = userRepo.findUserById(newsRepo.findNewsById(id).getAuthorId());
        int sum = 0;
        for(Mark previousMark : markRepo.findMarkByUserId(user.getId()))
        {
            sum += previousMark.getMark();
        }
        user.setAverageMark((double) (sum / user.getCountOfPublications()));
        userRepo.save(user);

        return "redirect:/";
    }

    @GetMapping("/subscribeUser/{id}")
    public String subscribeUser(@PathVariable("id") final String id)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
        {
            return "addUserInfoPage";
        }
        Subscription subscribe = new Subscription();
        subscribe.setUserSubscribeId(id);
        subscribe.setUserId(getAuthUserId());
        subscribeRepo.save(subscribe);

        return "redirect:/";
    }

    @GetMapping("/unsubscribeUser/{id}")
    public String unsubscribeUser(@PathVariable("id") final String id)
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
        {
            return "addUserInfoPage";
        }
        subscribeRepo.delete(subscribeRepo.findSubscriptionByUserSubscribeIdAndAndUserId(id, getAuthUserId()));

        return "redirect:/";
    }

    @PostMapping("/addUserInfo")
    public String addUserInfo(@RequestParam("username") final String username,
                              @RequestParam("email") final String email,
                              @RequestParam("description") final String description,
                              @RequestParam("image") final MultipartFile file,
                              Model model) throws IOException {
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
        userInfo newUser = new userInfo();
        newUser.setUsername(username);
        newUser.setId(getAuthUserId());
        newUser.setDescription(description);
        newUser.setCountOfPublications(0);
        newUser.setEmail(email);
        newUser.setFixFakeRating(10);
        newUser.setSearchFakeRating(10);

        StringBuilder fileNames = new StringBuilder();
        Path fileNameAndPath = Paths.get("D:/temik/Work/Data/AntiFakeNewsPublic/src/main/resources/static/profileImages",
                file.getOriginalFilename());
        //Path fileNameAndPath = Paths.get("D:/Programs/Work/AnitFakeNewsPublic/src/main/resources/static/profileImages",
            //file.getOriginalFilename());
        fileNames.append(file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        newUser.setImageUrl("/profileImages/" + file.getOriginalFilename());

        userRepo.save(newUser);
        return "redirect:/authProfilePage";
    }
}
