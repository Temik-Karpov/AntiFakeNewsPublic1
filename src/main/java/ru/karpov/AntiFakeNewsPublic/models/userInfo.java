package ru.karpov.AntiFakeNewsPublic.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class userInfo {
    @Id
    private String id;
    private String username;
    private String email;
    private String description;
    private Double searchFakeRating;
    private Double fixFakeRating;
    private String imageUrl;
    private Integer countOfPublications;
    private Double averageMark;

    public userInfo(final String id, final String username,
                    final String email, final String description,
                    final Double searchFakeRating, final Double fixFakeRating,
                    final Integer countOfPublications) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.description = description;
        this.searchFakeRating = searchFakeRating;
        this.fixFakeRating = fixFakeRating;
        this.countOfPublications = countOfPublications;
    }

    public userInfo() {

    }

    public Double getAverageMark() {
        return averageMark;
    }

    public void setAverageMark(Double averageMark) {
        this.averageMark = averageMark;
    }

    public Integer getCountOfPublications() {
        return countOfPublications;
    }

    public void setCountOfPublications(Integer countOfPublications) {
        this.countOfPublications = countOfPublications;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public double getFixFakeRating() {
        return fixFakeRating;
    }

    public void setFixFakeRating(final double fixFakeRating) {
        this.fixFakeRating = fixFakeRating;
    }

    public double getSearchFakeRating() {
        return searchFakeRating;
    }

    public void setSearchFakeRating(final double searchFakeRating) {
        this.searchFakeRating = searchFakeRating;
    }

    public void increaseCountOfPublications()
    {
        this.countOfPublications++;
    }

    public void decreaseCountOfPublications()
    {
        this.countOfPublications--;
    }
}
