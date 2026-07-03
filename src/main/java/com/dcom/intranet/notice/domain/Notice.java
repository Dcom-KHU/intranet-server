package com.dcom.intranet.notice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private Long authorId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoticeFile> files = new ArrayList<>();

    protected Notice() {
    }

    public Notice(String title, Long authorId, LocalDateTime createdAt) {
        this(title, "", authorId, createdAt, List.of());
    }

    public Notice(String title, String content, Long authorId, LocalDateTime createdAt) {
        this(title, content, authorId, createdAt, List.of());
    }

    public Notice(String title, String content, Long authorId, LocalDateTime createdAt, List<NoticeFile> files) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        addFiles(files);
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<NoticeFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void update(String title, String content, LocalDateTime updatedAt) {
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }

    public void addFile(NoticeFile file) {
        if (file == null) {
            return;
        }
        files.add(file);
        file.setNotice(this);
    }

    public void addFiles(List<NoticeFile> files) {
        if (files == null) {
            return;
        }
        files.forEach(this::addFile);
    }

    public void removeFile(NoticeFile file) {
        files.remove(file);
        file.setNotice(null);
    }
}
