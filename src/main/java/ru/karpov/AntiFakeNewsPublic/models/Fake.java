package ru.karpov.AntiFakeNewsPublic.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Fake {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Integer newsId;
    private String userId;
    private String name;
    private String text;
    private Boolean isTrue;
    private Integer categoryId;

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(final Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getTrue() {
        return isTrue;
    }

    public void setTrue(final Boolean aTrue) {
        isTrue = aTrue;
    }

    public Fake() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Integer getNewsId() {
        return newsId;
    }

    public void setNewsId(final Integer newsId) {
        this.newsId = newsId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Fake(final Integer newsId, final String userId, final String name, final String text,
                final Integer categoryId) {
        this.newsId = newsId;
        this.userId = userId;
        this.name = name;
        this.text = text;
        this.isTrue = false;
        this.categoryId = categoryId;
    }
}
