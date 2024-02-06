package ru.karpov.AntiFakeNewsPublic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.models.userInfo;
import ru.karpov.AntiFakeNewsPublic.repos.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class OpenPagesControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final userRepo userRepo;
    private final newsRepo newsRepo;
    private final subscriptionRepo subscribeRepo;
    private final markRepo markRepo;
    private final imageNewsRepo imageNewsRepo;

    @Autowired
    public OpenPagesControllerTest(final userRepo userRepo, final newsRepo newsRepo,
                                   final subscriptionRepo subscribeRepo, final markRepo markRepo,
                                   final imageNewsRepo imageNewsRepo) {
        this.userRepo = userRepo;
        this.newsRepo = newsRepo;
        this.subscribeRepo = subscribeRepo;
        this.markRepo = markRepo;
        this.imageNewsRepo = imageNewsRepo;
    }

    @BeforeEach
    public void start()
    {
        userRepo.deleteAll();
        //newsRepo.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void openMainPageTest() throws Exception {
        userInfo user = new userInfo();
        user.setId("123");
        userRepo.save(user);
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                //.andExpect(model().size(1))
                //.andExpect(model().attributeExists("publications"))
                //.andExpect(model().attributeExists("users"))
                //.andExpect(model().attribute("publications", newsRepo))
                //.andExpect(model().attribute("users", userRepo))
                //.andExpect(view().name("mainPage"))
                .andDo(print())
                .andExpect(flash().attributeExists("publications"))
                .andExpect(flash().attributeCount(1))
                //.andExpect(flash().attribute("publications", newsRepo.findNewsByIsBlockedFalse()))
                .andExpect(redirectedUrl("/mainPage"));
                //.andExpect(status().isOk());
    }
}
