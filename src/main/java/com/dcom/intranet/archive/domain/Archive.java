package com.dcom.intranet.archive.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "archives",
        uniqueConstraints = {
                // 폴더가 (같은 교수와 과목) 폴더가 두번 생기는 것을 방지
                // 하나만 겹치는 건 가능!!
                @UniqueConstraint(
                        name = "uk_archive_subject_professor",
                        columnNames = {"subject_name", "professor_name"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Archive {

    @Id
    // GenerationType.IDENTITY 를 사용하여 MySQL이 AUTO_INCREMENT로 id를 자동 할당
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id")
    private Long id;

    // 필수데이터인 교수명과 과목명은 nullable = false 로 처리하여 에러가 날 수 있도록 함
    @Column(name = "subject_name", nullable = false, length = 100)
    private String subjectName;

    @Column(name = "professor_name", nullable = false, length = 100)
    private String professorName;

    private LocalDateTime createdAt;

    private LocalDateTime lastModifiedAt;

    // 'orphanRemoval = true'를 하면 자식 데이터와의 관계가 끊어지면 db에서도 삭제시켜줌.(물리 삭제)
    @OneToMany(mappedBy = "archive", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArchiveRecord> records = new ArrayList<>();

    public Archive(String subjectName, String professorName) {
        this.subjectName = subjectName;
        this.professorName = professorName;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.lastModifiedAt = now;
    }

    public void addRecord(ArchiveRecord record) {
        records.add(record);
        record.setArchive(this);
        touch();
    }

    // Archive 폴더에 변화가 생겼을 때 수정날짜 변경
    public void touch() {
        this.lastModifiedAt = LocalDateTime.now();
    }
}
