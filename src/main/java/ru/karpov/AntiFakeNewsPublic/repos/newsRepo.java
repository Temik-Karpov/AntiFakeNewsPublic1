package ru.karpov.AntiFakeNewsPublic.repos;

import jdk.jfr.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.News;

import java.util.List;

@Repository
public interface newsRepo extends JpaRepository<News, Long> {
    List<News> findNewsByAuthorId(final String userId);
    List<News> findNewsByCategoryId(final Integer categoryId);
    News findNewsById(final Integer id);
    List<News> findNewsByCategoryIdAndAuthorId(final Integer categoryId, final String userId);
}
