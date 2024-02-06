package ru.karpov.AntiFakeNewsPublic.Services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.karpov.AntiFakeNewsPublic.Services.NewsService;
import ru.karpov.AntiFakeNewsPublic.models.News;
import ru.karpov.AntiFakeNewsPublic.repos.newsRepo;

import java.time.Instant;
import java.util.Date;

@Service
public class NewsServiceImpl implements NewsService {

    private newsRepo newsRepo;

    @Autowired
    public NewsServiceImpl(final newsRepo newsRepo)
    {
        this.newsRepo = newsRepo;
    }

    @Override
    public void addNews(final News news, final String authorId) {
        news.setBlocked(false);
        news.setAuthorId(authorId);
        news.setDate(Date.from(Instant.now()));
        newsRepo.save(news);
    }

    @Override
    public void deleteNews(final News news) {
        newsRepo.delete(news);
    }

    @Override
    public void editNews(final News news) {
        newsRepo.saveAndFlush(news);
    }

    @Override
    public void rateNews(final Integer newsId) {

    }
}
