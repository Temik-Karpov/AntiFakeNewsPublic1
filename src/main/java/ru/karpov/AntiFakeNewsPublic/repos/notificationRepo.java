package ru.karpov.AntiFakeNewsPublic.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.karpov.AntiFakeNewsPublic.models.Notification;

@Repository
public interface notificationRepo extends JpaRepository<Notification, Long> {

}
