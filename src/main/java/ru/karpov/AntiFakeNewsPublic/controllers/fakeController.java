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
import ru.karpov.AntiFakeNewsPublic.models.Fake;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.fileFake;
import ru.karpov.AntiFakeNewsPublic.repos.fakeRepo;
import ru.karpov.AntiFakeNewsPublic.repos.fileFakeRepo;
import ru.karpov.AntiFakeNewsPublic.repos.newsRepo;
import ru.karpov.AntiFakeNewsPublic.repos.userRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Controller
public class fakeController {

    private final userRepo userRepo;
    private newsRepo newsRepo;
    private fakeRepo fakeRepo;
    private final fileFakeRepo fileFakeRepo;

    @Autowired
    fakeController(final userRepo userRepo, final newsRepo newsRepo, final fakeRepo fakeRepo,
                   final fileFakeRepo fileFakeRepo) {
        this.newsRepo = newsRepo;
        this.userRepo = userRepo;
        this.fakeRepo = fakeRepo;
        this.fileFakeRepo = fileFakeRepo;
    }

    private String getAuthUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @GetMapping("/addFakePage/{newsId}")
    public String getAddFakePage(@PathVariable("newsId") final Integer newsId,
                                 final Model model) {
        model.addAttribute("newsId", newsId);
        return "addFakePage";
    }

    @PostMapping("/addFake/{newsId}")
    public String addFake(@RequestParam("Title") final String problem,
                          @PathVariable("newsId") final Integer newsId,
                          @RequestParam("text") final String proofs,
                          @RequestParam("files") final MultipartFile[] files,
                          Model model) throws IOException {
        if (userRepo.findUserById(getAuthUserId()) == null) {
            return "addUserInfoPage";
        }
        if (problem.isEmpty() || proofs.isEmpty()) {
            model.addAttribute("nullError", 1);
            return "addFakePage";
        }

        Fake newFake = new Fake(newsId, getAuthUserId(), problem, proofs,
                newsRepo.findNewsById(newsId).getIdCategory());
        fakeRepo.save(newFake);

        if(files.length > 0) {
            for (MultipartFile file : files) {
                StringBuilder fileNames = new StringBuilder();
                Path fileNameAndPath = Paths.get("D:/temik/Work/Data/AntiFakeNewsPublic/src/main/resources/static/fakeFiles",
                        file.getOriginalFilename());
                //Path fileNameAndPath = Paths.get("D:/Programs/Work/AnitFakeNewsPublic/src/main/resources/static/fakeFiles",
                // file.getOriginalFilename());
                fileNames.append(file.getOriginalFilename());
                Files.write(fileNameAndPath, file.getBytes());
                final fileFake fileFake = new fileFake();
                fileFake.setFileUrl("/fakeFiles/" + file.getOriginalFilename());
                fileFake.setFakeId(newFake.getId());
                fileFakeRepo.save(fileFake);
            }
        }
        return "redirect:/";
    }

    @GetMapping("/fakesPage")
    public String getFakesPage(final Model model)
    {
        model.addAttribute("users", userRepo);
        model.addAttribute("fakes", fakeRepo.findFakeByCategoryId(1));
        return "fakesPage";
    }

    @PostMapping("/reloadFakesPage")
    public String getReloadMainPage(@RequestParam("category") final Integer category,
                                    final Model model)
    {
        model.addAttribute("users", userRepo);
        model.addAttribute("fakes", fakeRepo.findFakeByCategoryId(category));
        return "fakesPage";
    }
}
