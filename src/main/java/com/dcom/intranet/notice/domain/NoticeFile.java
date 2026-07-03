package com.dcom.intranet.notice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notice_files")
public class NoticeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    protected NoticeFile() {
    }

    public NoticeFile(String originalFileName, String fileUrl) {
        this.originalFileName = originalFileName;
        this.fileUrl = fileUrl;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    void setNotice(Notice notice) {
        this.notice = notice;
    }
}
