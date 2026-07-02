package com.dcom.intranet.notice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @ElementCollection(fetch = FetchType.LAZY)
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
        this.files = files == null ? new ArrayList<>() : new ArrayList<>(files);
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

    public void update(String title, String content, List<NoticeFile> files, LocalDateTime updatedAt) {
        this.title = title;
        this.content = content;
        this.files.clear();
        if (files != null) {
            this.files.addAll(files);
        }
        this.updatedAt = updatedAt;
    }

    @Embeddable
    public static class NoticeFile {

        @Column(nullable = false)
        private String fileName;

        @Column(nullable = false)
        private String fileUrl;

        protected NoticeFile() {
        }

        public NoticeFile(String fileName, String fileUrl) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }
    }
}
