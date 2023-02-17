package ru.karpov.AntiFakeNewsPublic.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String userId;
    private String userSubscribeId;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getUserSubscribeId() {
        return userSubscribeId;
    }

    public void setUserSubscribeId(final String userSubscribeId) {
        this.userSubscribeId = userSubscribeId;
    }
}
