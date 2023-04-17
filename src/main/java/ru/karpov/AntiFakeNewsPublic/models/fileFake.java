package ru.karpov.AntiFakeNewsPublic.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class fileFake {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Integer fakeId;
    private String fileUrl;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Integer getFakeId() {
        return fakeId;
    }

    public void setFakeId(final Integer fakeId) {
        this.fakeId = fakeId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(final String imageUrl) {
        this.fileUrl = imageUrl;
    }
}
