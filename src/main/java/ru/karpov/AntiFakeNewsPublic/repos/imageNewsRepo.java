package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.karpov.AntiFakeNewsPublic.models.imageNews;

import java.util.List;

public interface imageNewsRepo extends JpaRepository<imageNews, Long> {
    List<imageNews> findAllByNewsId(final Integer newsId);
}
