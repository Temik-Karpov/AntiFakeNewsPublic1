package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.Subscription;

@Repository
public interface subscriptionRepo extends JpaRepository<Subscription, Long> {
}
