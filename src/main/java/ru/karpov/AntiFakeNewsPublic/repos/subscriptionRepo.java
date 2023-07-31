package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.Subscription;

import java.util.List;

@Repository
public interface subscriptionRepo extends JpaRepository<Subscription, Long> {
    List<Subscription> findSubscriptionByUserId(final String userId);
    Subscription findSubscriptionByUserSubscribeIdAndAndUserId(final String userSubscribeId, final String userId);
}
