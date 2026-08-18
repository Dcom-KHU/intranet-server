package com.dcom.intranet.archive.domain;

import com.dcom.intranet.auth.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "archive_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archive_id", nullable = false)
    private Archive archive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 기존 서버 데이터 이관 시 누락되어 있을 수 있어 nullable로 둠 (신규 등록은 API 요청 검증에서 필수로 강제)
    @Column
    private Integer examYear;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "varchar(20)")
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "varchar(20)")
    private ExamType examType;

    // 설명글은 필요 사항이 아니니깐 'nullable = false' 따로 안씀
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "legacy_author_student_number")
    private String legacyAuthorStudentNumber;

    @Column(name = "legacy_author_name")
    private String legacyAuthorName;

    @Column(name = "legacy_anonymous")
    private Boolean legacyAnonymous;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 등록할 때는 등록 시간 생성, 수정 시간은 NULL로
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 수정할 때는 수정 시간 최신화
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArchiveFile> files = new ArrayList<>();

    public ArchiveRecord(User author, Integer examYear, Semester semester, ExamType examType, String content) {
        this.author = author; // 글쓴이
        this.examYear = examYear; // 시험 연도
        this.semester = normalizeSemester(semester); // 학기
        this.examType = normalizeExamType(examType); // 중간 or 기말 or 기타
        this.content = content; // 설명글
    }

    protected void setArchive(Archive archive) {
        this.archive = archive;
    }

    // 파일 자체는 저장소에민 먼저 업로드 하고서 후에 db에는 메타정보만 저장
    public void addFile(ArchiveFile file) {
        files.add(file);
        file.setRecord(this);
    }

    // service 로직에서 족보가 수정되면 JPA가 부모인 Archive의 최근수정날짜를 변경할 수 없으므로
    // service의 update 로직에 touch 함수 반영
    public void update(Integer examYear, Semester semester, ExamType examType, String content) {
        this.examYear = examYear;
        this.semester = normalizeSemester(semester);
        this.examType = normalizeExamType(examType);
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    private Semester normalizeSemester(Semester semester) {
        return semester == Semester.UNKNOWN ? null : semester;
    }

    private ExamType normalizeExamType(ExamType examType) {
        return examType == ExamType.ETC ? null : examType;
    }

    public void applyLegacyAuthor(String studentNumber, String name, Boolean anonymous) {
        this.legacyAuthorStudentNumber = studentNumber;
        this.legacyAuthorName = name;
        this.legacyAnonymous = anonymous;
    }

    public void removeFile(ArchiveFile file) {
        files.remove(file);
        file.setRecord(null);
    }
}
