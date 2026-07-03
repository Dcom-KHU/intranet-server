package com.dcom.intranet.photo.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "photo_posts")
public class PhotoPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long albumId;

    @Column(nullable = false, length = 100)
    private String eventName;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Lob
    @Column
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "photo_post_images",
            joinColumns = @JoinColumn(name = "album_id")
    )
    @OrderColumn(name = "upload_order")
    @Column(name = "image_url", nullable = false)
    private List<String> imageUrls = new ArrayList<>();

    @OneToMany(mappedBy = "photoPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<PhotoComment> comments = new ArrayList<>();

    protected PhotoPost() {
    }

    public PhotoPost(String eventName, LocalDate activityDate, List<String> imageUrls) {
        this(eventName, activityDate, "", imageUrls);
    }

    public PhotoPost(String eventName, LocalDate activityDate, String description, List<String> imageUrls) {
        this.eventName = eventName;
        this.activityDate = activityDate;
        this.description = description == null ? "" : description;
        this.imageUrls = imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getImageUrls() {
        return Collections.unmodifiableList(imageUrls);
    }

    public List<PhotoComment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    public String getCoverImageUrl() {
        return imageUrls.isEmpty() ? null : imageUrls.get(0);
    }

    public void update(String eventName, LocalDate activityDate, String description, List<String> imageUrls) {
        this.eventName = eventName;
        this.activityDate = activityDate;
        this.description = description == null ? "" : description;
        this.imageUrls.clear();
        if (imageUrls != null) {
            this.imageUrls.addAll(imageUrls);
        }
    }
}
