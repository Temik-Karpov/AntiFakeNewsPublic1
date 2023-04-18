package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.Fake;

import java.util.List;

@Repository
public interface fakeRepo extends JpaRepository<Fake, Long> {
    List<Fake> findFakeByCategoryId(final Integer categoryId);
}
