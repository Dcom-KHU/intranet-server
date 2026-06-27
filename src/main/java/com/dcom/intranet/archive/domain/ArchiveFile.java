package com.dcom.intranet.archive.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "archive_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private ArchiveRecord record;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false, length = 500)
    private String objectKey;

    @Column(length = 500)
    private String fileUrl;

    private Long fileSize;

    @Column(length = 100)
    private String contentType;

    @Column(nullable = false)
    private int downloadCount = 0;

    private LocalDateTime createdAt;

    public ArchiveFile(String originalFileName, String storedFileName, String objectKey,
                       String fileUrl, Long fileSize, String contentType) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.objectKey = objectKey;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.createdAt = LocalDateTime.now();
    }

    protected void setRecord(ArchiveRecord record) {
        this.record = record;
    }

    public void increaseDownloadCount() {
        this.downloadCount++;
    }
}