package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.Mark;

import java.util.List;

@Repository
public interface markRepo extends JpaRepository<Mark, Long> {
    Mark findMarkByUserIdAndNewsId(final String userId, final Integer newsId);
    List<Mark> findMarkByUserId(final String userId);
}
