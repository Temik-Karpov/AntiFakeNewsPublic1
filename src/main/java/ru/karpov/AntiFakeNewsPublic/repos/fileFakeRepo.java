package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.fileFake;

@Repository
public interface fileFakeRepo extends JpaRepository<fileFake, Long> {

}
