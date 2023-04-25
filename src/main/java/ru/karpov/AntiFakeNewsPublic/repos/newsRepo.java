package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.News;

import java.util.List;

@Repository
public interface newsRepo extends JpaRepository<News, Long> {
    List<News> findNewsByAuthorId(final String userId);
    List<News> findNewsByAuthorIdAndIsBlockedIsFalse(final String userId);
    List<News> findNewsByCategoryIdAndIsBlockedFalse(final Integer categoryId);
    List<News> findNewsByCategoryIdAndAuthorId(final Integer categoryId, final String userId);
    News findNewsById(final Integer id);
    List<News> findNewsByCategoryIdAndAuthorIdAndIsBlockedIsFalse(final Integer categoryId, final String userId);
    List<News> findNewsByIsBlockedFalse();
}
