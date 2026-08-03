package com.dcom.intranet.photo.domain;

import com.dcom.intranet.auth.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    @OrderColumn(name = "upload_order")
    private List<PhotoPostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "photoPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<PhotoComment> comments = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PhotoPost() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public PhotoPost(String eventName, LocalDate activityDate, List<String> imageUrls) {
        this(null, eventName, activityDate, "", null);
        if (imageUrls == null) {
            return;
        }

        imageUrls.stream()
                .map(imageUrl -> new PhotoPostImage(imageUrl, imageUrl, imageUrl, imageUrl, 0L, null))
                .forEach(this.images::add);
    }

    public PhotoPost(String eventName, LocalDate activityDate, String description, List<PhotoPostImage> images) {
        this(null, eventName, activityDate, description, images);
    }

    public PhotoPost(User author, String eventName, LocalDate activityDate, String description, List<PhotoPostImage> images) {
        this.author = author;
        this.eventName = eventName;
        this.activityDate = activityDate;
        this.description = description == null ? "" : description;
        this.images = images == null ? new ArrayList<>() : new ArrayList<>(images);
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

    public User getAuthor() {
        return author;
    }

    public List<String> getImageUrls() {
        if (images == null) {
            return List.of();
        }

        return images.stream()
                .map(PhotoPostImage::getFileUrl)
                .toList();
    }

    public List<PhotoPostImage> getImages() {
        return images == null ? List.of() : List.copyOf(images);
    }

    public List<PhotoComment> getComments() {
        return comments == null ? List.of() : List.copyOf(comments);
    }

    public String getCoverImageUrl() {
        return images.isEmpty() ? null : images.get(0).getFileUrl();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void update(String eventName, LocalDate activityDate, String description) {
        this.eventName = eventName;
        this.activityDate = activityDate;
        this.description = description == null ? "" : description;
    }

    public void replaceImages(List<String> imageUrls) {
        this.images.clear();
        if (imageUrls != null) {
            imageUrls.stream()
                    .map(imageUrl -> new PhotoPostImage(imageUrl, imageUrl, imageUrl, imageUrl, 0L, null))
                    .forEach(this.images::add);
        }
    }

    public void replaceImageFiles(List<PhotoPostImage> images) {
        this.images.clear();
        if (images != null) {
            this.images.addAll(images);
        }
    }
}
