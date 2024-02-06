package ru.karpov.AntiFakeNewsPublic.Services;

import ru.karpov.AntiFakeNewsPublic.models.News;

public interface NewsService {
    void addNews(final News news, final String authorId);
    void deleteNews(final  News news);
    void editNews(final News news);
    void rateNews(final Integer newsId);
}
