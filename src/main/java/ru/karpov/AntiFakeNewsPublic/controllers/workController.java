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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
public class workController {

    private userRepo userRepo;
    private newsRepo newsRepo;
    private subscriptionRepo subscribeRepo;
    private markRepo markRepo;
    private imageNewsRepo imageNewsRepo;

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

    private int isAuth()
    {
        return SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser") ? 0 : 1;
    }

    private String getAuthUserId()
    {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping("/addNews")
    public String addNews(@RequestParam("Title") String title, @RequestParam("text") String text,
                          @RequestParam("category") Integer category,
                          @RequestParam("photo") MultipartFile file,
                          Model model) throws IOException {
        model.addAttribute("isAuth", isAuth());
        if(text.isEmpty() || title.isEmpty())
        {
            model.addAttribute("nullError", 1);
            model.addAttribute("publication", 0);
            return "addNewsPage";
        }
        News news = new News();
        news.setName(title);
        news.setIdCategory(category);
        news.setText(text);
        news.setDate(Date.from(Instant.now()));
        news.setAuthorId(getAuthUserId());
        newsRepo.save(news);
        userInfo user = userRepo.findUserById(getAuthUserId());
        user.increaseCountOfPublications();
        userRepo.save(user);

        //TODO: несколько снимков сразу добавлять
        if(file.getBytes().length > 0) {
            StringBuilder fileNames = new StringBuilder();
            Path fileNameAndPath = Paths.get("D:/temik/Work/Data/AntiFakeNewsPublic/src/main/resources/static/images",
                    file.getOriginalFilename());
            fileNames.append(file.getOriginalFilename());
            Files.write(fileNameAndPath, file.getBytes());
            final imageNews image = new imageNews();
            image.setImageUrl("/images/" + file.getOriginalFilename());
            image.setNewsId(news.getId());
            imageNewsRepo.save(image);
        }

        List<News> pub;     //TODO: надо что то сделать с переворотм списка
        pub = newsRepo.findAll();
        Collections.reverse(pub);
        model.addAttribute("publications", pub);
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/deleteNews/{id}")
    public String deleteNews(@PathVariable("id") Integer id, Model model)
    {
        model.addAttribute("isAuth", isAuth());
        News news = newsRepo.findNewsById(id);
        newsRepo.delete(news);
        imageNewsRepo.deleteAll(imageNewsRepo.findAllByNewsId(id));     //todo: удаление картинок из path
        List<News> pub;
        pub = newsRepo.findAll();
        Collections.reverse(pub);
        model.addAttribute("publications", pub);
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/editNews/{id}")
    public String editNews(@PathVariable("id") Integer id, Model model)
    {
        model.addAttribute("isAuth", isAuth());
        model.addAttribute("publication", newsRepo.findNewsById(id));
        model.addAttribute("users", userRepo);
        newsRepo.delete(newsRepo.findNewsById(id));     //todo: сделать отмену изменения новости(чтобы не удалялась)
        return "addNewsPage";
    }

    @PostMapping("/rateNews/{id}")
    public String rateNews(@PathVariable("id") Integer id,
                           @RequestParam("mark") Integer mark,
                           Model model)
    {
        model.addAttribute("isAuth", isAuth());

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

        List<News> pub;
        pub = newsRepo.findAll();
        Collections.reverse(pub);
        model.addAttribute("publications", pub);
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/subscribeUser/{id}")
    public String subscribeUser(@PathVariable("id") String id, Model model)
    {
        model.addAttribute("isAuth", isAuth());
        Subscription subscribe = new Subscription();
        subscribe.setUserSubscribeId(id);
        subscribe.setUserId(getAuthUserId());
        subscribeRepo.save(subscribe);

        List<News> pub;
        pub = newsRepo.findAll();
        Collections.reverse(pub);
        model.addAttribute("publications", pub);
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @GetMapping("/unsubscribeUser/{id}")
    public String unsubscribeUser(@PathVariable("id") String id, Model model)
    {
        model.addAttribute("isAuth", isAuth());
        subscribeRepo.delete(subscribeRepo.findSubscriptionByUserSubscribeIdAndAndUserId(id, getAuthUserId()));

        List<News> pub;
        pub = newsRepo.findAll();
        Collections.reverse(pub);
        model.addAttribute("publications", pub);
        model.addAttribute("users", userRepo);
        return "mainPage";
    }

    @PostMapping("/addUserInfo")
    public String addUserInfo(@RequestParam("username") String username,
                              @RequestParam("email") String email,
                              @RequestParam("description") String description,
                              @RequestParam("image") MultipartFile file,
                              Model model) throws IOException {
        model.addAttribute("isAuth", isAuth());
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

        System.out.println(file.getBytes().length);
        StringBuilder fileNames = new StringBuilder();
        Path fileNameAndPath = Paths.get("D:/temik/Work/Data/AntiFakeNewsPublic/src/main/resources/static/images",
                file.getOriginalFilename());
        fileNames.append(file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        newUser.setImageUrl("/images/" + file.getOriginalFilename());

        userRepo.save(newUser);
        model.addAttribute("publications", newsRepo.findAll());
        model.addAttribute("users", userRepo);
        return "mainPage";
    }
}
