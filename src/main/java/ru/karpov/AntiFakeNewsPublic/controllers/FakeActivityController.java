package ru.karpov.AntiFakeNewsPublic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import ru.karpov.AntiFakeNewsPublic.models.Fake;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.Notification;
import ru.karpov.AntiFakeNewsPublic.models.fileFake;
import ru.karpov.AntiFakeNewsPublic.repos.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FakeActivityController extends mainController {

    private final fakeRepo fakeRepo;
    private final fileFakeRepo fileFakeRepo;
    private final notificationRepo notificationRepo;

    @Autowired
    public FakeActivityController(final userRepo userRepo, final newsRepo newsRepo, final imageNewsRepo imageNewsRepo,
                                  final notificationRepo notificationRepo, final fakeRepo fakeRepo,
                                  final subscriptionRepo subscribeRepo, final markRepo markRepo,
                                  final fileFakeRepo fileFakeRepo)
    {
        super(userRepo, newsRepo, subscribeRepo, markRepo, imageNewsRepo);
        this.notificationRepo = notificationRepo;
        this.fakeRepo = fakeRepo;
        this.fileFakeRepo = fileFakeRepo;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void checkUserAvailabilityInSystem()
    {
        if(userRepo.findUserById(getAuthUserId()) == null)
            callGetAddUserInfoPage();
    }

    private String callGetAddUserInfoPage()
    {
        return "addUserInfoPage";
    }

    @GetMapping("/addFakePage/{newsId}")
    public String getAddFakePage(@PathVariable("newsId") final Integer newsId,
                                 final Model model) {
        model.addAttribute("newsId", newsId);
        return "addFakePage";
    }

    @PostMapping("/addFake/{newsId}")
    public RedirectView addFake(@ModelAttribute("fakeData") final Fake fake,
                                @PathVariable("newsId") final Integer newsId,
                                @RequestParam("files") final MultipartFile[] files,
                                Model model) throws IOException {
        checkUserAvailabilityInSystem();
        isProblemAndProofsEmpty(fake.getName(), fake.getText(), model);
        fake.setUserId(getAuthUserId());
        fake.setNewsId(newsId);
        fake.setCategoryId(newsRepo.findNewsById(newsId).getCategoryId());
        addFilesIntoFake(files, fake);
        fakeRepo.save(fake);
        blockNews(newsId);
        return new RedirectView("/");
    }

    private void blockNews(final Integer newsId)
    {
        News currentNews = newsRepo.findNewsById(newsId);
        currentNews.setBlocked(true);
        newsRepo.save(currentNews);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
    private void addFilesIntoFake(final MultipartFile[] files, final Fake newFake) throws IOException {
        for (MultipartFile file : files) {
            if(file.getBytes().length > 0) {
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
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void isProblemAndProofsEmpty(final String problem, final String proofs,
                                           final Model model)
    {
        if (problem.isEmpty() || proofs.isEmpty()) {
            model.addAttribute("nullError", 1);
            getAddFakePage();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private String getAddFakePage()
    {
        return "addFakePage";
    }

    @GetMapping("/fakesPage")
    public String getFakesPage(final Model model)
    {
        model.addAttribute("users", userRepo);
        model.addAttribute("fakes", fakeRepo.findFakeByAdminId(null));
        return "fakesPage";
    }

    @PostMapping("/reloadFakesPage")
    public String getReloadMainPage(@RequestParam("category") final Integer category,
                                    final Model model)
    {
        model.addAttribute("users", userRepo);
        model.addAttribute("fakes", fakeRepo.findFakeByCategoryIdAndAdminId(category, null));
        return "fakesPage";
    }

    @GetMapping("/fakePage/{id}")
    public String getFakePage(@PathVariable("id") final Integer fakeId,
                              final Model model)
    {
        Fake currentFake = fakeRepo.findFakeById(fakeId);
        model.addAttribute("fake", currentFake);
        model.addAttribute("news", newsRepo.findNewsById(currentFake.getNewsId()));
        model.addAttribute("user",
                userRepo.findUserById(newsRepo.findNewsById(currentFake.getNewsId()).getAuthorId()).getUsername());
        model.addAttribute("image", imageNewsRepo.findAllByNewsId(currentFake.getNewsId()).size() != 0 ?
                imageNewsRepo.findAllByNewsId((currentFake.getNewsId())).get(0).getImageUrl() : null);
        model.addAttribute("files", fileFakeRepo.findFileFakeByFakeId(fakeId));     //TODO: файлы не отображаются
        return "fakePage";
    }

    @GetMapping("/notAcceptFake/{id}")
    public RedirectView notAcceptFake(@PathVariable("id") final Integer fakeId)
    {
        Fake currentFake = fakeRepo.findFakeById(fakeId);
        currentFake.setAdminId(getAuthUserId());
        currentFake.setTrue(false);
        fakeRepo.save(currentFake);
        News fakeNews = newsRepo.findNewsById(fakeRepo.findFakeById(fakeId).getNewsId());
        fakeNews.setBlocked(false);
        newsRepo.save(fakeNews);
        //TODO: снижение рейтинга для пользователя, который нашел фейк
        return new RedirectView("/");
    }

    @GetMapping("/acceptFake/{id}")
    public RedirectView acceptFake(@PathVariable("id") final Integer fakeId)
    {
        Fake currentFake = fakeRepo.findFakeById(fakeId);
        currentFake.setAdminId(getAuthUserId());
        currentFake.setTrue(true);
        fakeRepo.save(currentFake);
        sendNotification(fakeId);
        for(Fake fake : fakeRepo.findFakeByNewsIdAndIsTrueFalse(fakeRepo.findFakeById(fakeId).getNewsId()))
        {
            //TODO: повышение рейтинга для пользователя, который нашел фейк и снизить для неправильного админа
        }
        return new RedirectView("/");
    }

    private void sendNotification(final Integer fakeId)
    {
        Notification newNotification = new Notification();
        News currentNews = newsRepo.findNewsById(fakeRepo.findFakeById(fakeId).getNewsId());
        newNotification.setName("Your news is fake");
        newNotification.setText("Please, edit your news or delete it. " +
                "http://localhost:8081/newsPage/" + currentNews.getId());
        newNotification.setUserId(currentNews.getAuthorId());
        notificationRepo.save(newNotification);
    }
}
