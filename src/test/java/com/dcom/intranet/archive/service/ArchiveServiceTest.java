package com.dcom.intranet.archive.service;

import com.dcom.intranet.archive.domain.Archive;
import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import com.dcom.intranet.archive.dto.request.ArchiveCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveRecordCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveUpdateRequest;
import com.dcom.intranet.archive.repository.ArchiveFileRepository;
import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArchiveServiceTest {

    private final ArchiveRepository archiveRepository = mock(ArchiveRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ArchiveFileRepository archiveFileRepository = mock(ArchiveFileRepository.class);
    private final ArchiveFileStorageService archiveFileStorageService = mock(ArchiveFileStorageService.class);
    private final ArchiveRecordRepository archiveRecordRepository = mock(ArchiveRecordRepository.class);
    private final ArchiveService archiveService = new ArchiveService(
            archiveRepository,
            userRepository,
            archiveFileRepository,
            archiveFileStorageService,
            archiveRecordRepository
    );

    @BeforeEach
    void setUp() {
        reset(
                archiveRepository,
                userRepository,
                archiveFileRepository,
                archiveFileStorageService,
                archiveRecordRepository
        );
    }

    @Test
    @DisplayName("족보 검색은 검색어의 공백을 제거해 과목명과 교수명을 조회한다")
    void searchNormalizesWhitespace() {
        when(archiveRepository.searchBySubjectNameOrProfessorNameIgnoringSpaces(
                eq("자료구조"), any(Pageable.class)
        )).thenAnswer(invocation -> org.springframework.data.domain.Page.empty());

        archiveService.getArchives(0, 10, "자료 구조");

        verify(archiveRepository).searchBySubjectNameOrProfessorNameIgnoringSpaces(
                eq("자료구조"), any(Pageable.class)
        );
    }

    @Test
    @DisplayName("단일 족보 등록은 fileIndexes가 없어도 업로드 파일을 해당 레코드에 연결한다")
    void createSingleRecordAttachesUploadedFilesWithoutFileIndexes() {
        ArchiveCreateRequest request = createRequestWithRecords(createRecordRequest(null));
        User author = user(1L);
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "sample.pdf",
                "application/pdf",
                "pdf".getBytes()
        );

        when(userRepository.findByLoginId("login"))
                .thenReturn(Optional.of(author));
        when(archiveRepository.findBySubjectNameAndProfessorName("자료구조", "박교수"))
                .thenReturn(Optional.empty());
        when(archiveFileStorageService.store(any()))
                .thenReturn(new ArchiveFileStorageService.StoredFile(
                        "sample.pdf",
                        "stored.pdf",
                        "archive/2026/08/stored.pdf",
                        "/uploads/archive/2026/08/stored.pdf",
                        3L,
                        "application/pdf"
                ));

        archiveService.createArchive(request, List.of(file), "login");

        ArgumentCaptor<ArchiveRecord> recordCaptor = ArgumentCaptor.forClass(ArchiveRecord.class);
        verify(archiveRecordRepository).save(recordCaptor.capture());

        ArchiveRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getFiles()).hasSize(1);
        assertThat(savedRecord.getFiles().get(0).getOriginalFileName()).isEqualTo("sample.pdf");
    }

    @Test
    @DisplayName("족보 등록 시 UNKNOWN 학기와 ETC 시험 유형은 null로 저장한다")
    void createArchiveStoresUnknownSemesterAndEtcExamTypeAsNull() {
        ArchiveCreateRequest request = createRequestWithRecords(
                createRecordRequest(null, Semester.UNKNOWN, ExamType.ETC)
        );

        when(userRepository.findByLoginId("login"))
                .thenReturn(Optional.of(user(1L)));
        when(archiveRepository.findBySubjectNameAndProfessorName("자료구조", "박교수"))
                .thenReturn(Optional.empty());

        archiveService.createArchive(request, List.of(), "login");

        ArgumentCaptor<ArchiveRecord> recordCaptor = ArgumentCaptor.forClass(ArchiveRecord.class);
        verify(archiveRecordRepository).save(recordCaptor.capture());

        ArchiveRecord savedRecord = recordCaptor.getValue();
        assertThat(savedRecord.getSemester()).isNull();
        assertThat(savedRecord.getExamType()).isNull();
    }

    @Test
    @DisplayName("여러 족보 레코드에 파일을 첨부할 때 fileIndexes가 없으면 요청을 거부한다")
    void createMultipleRecordsRequiresFileIndexesWhenFilesUploaded() {
        ArchiveCreateRequest request = createRequestWithRecords(
                createRecordRequest(null),
                createRecordRequest(null)
        );
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "sample.pdf",
                "application/pdf",
                "pdf".getBytes()
        );

        when(userRepository.findByLoginId("login"))
                .thenReturn(Optional.of(user(1L)));
        when(archiveRepository.findBySubjectNameAndProfessorName("자료구조", "박교수"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> archiveService.createArchive(request, List.of(file), "login"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("족보 수정 시 과목명과 교수명을 함께 수정한다")
    void updateRecordUpdatesArchiveSubjectAndProfessor() {
        User author = user(1L);
        Archive archive = archive(1L, "이전과목", "이전교수");
        ArchiveRecord record = archiveRecord(10L, author);
        archive.addRecord(record);
        ArchiveUpdateRequest request = updateRequest("새과목", "새교수");

        when(archiveRecordRepository.findById(10L))
                .thenReturn(Optional.of(record));
        when(userRepository.findByLoginId("login"))
                .thenReturn(Optional.of(author));
        when(archiveRepository.findBySubjectNameAndProfessorName("새과목", "새교수"))
                .thenReturn(Optional.empty());

        archiveService.updateRecord(1L, 10L, request, List.of(), "login");

        assertThat(archive.getSubjectName()).isEqualTo("새과목");
        assertThat(archive.getProfessorName()).isEqualTo("새교수");
    }

    @Test
    @DisplayName("족보 수정 시 같은 과목명과 교수명의 다른 아카이브가 있으면 거부한다")
    void updateRecordRejectsDuplicateArchiveMetadata() {
        User author = user(1L);
        Archive archive = archive(1L, "이전과목", "이전교수");
        Archive duplicateArchive = archive(2L, "새과목", "새교수");
        ArchiveRecord record = archiveRecord(10L, author);
        archive.addRecord(record);
        ArchiveUpdateRequest request = updateRequest("새과목", "새교수");

        when(archiveRecordRepository.findById(10L))
                .thenReturn(Optional.of(record));
        when(userRepository.findByLoginId("login"))
                .thenReturn(Optional.of(author));
        when(archiveRepository.findBySubjectNameAndProfessorName("새과목", "새교수"))
                .thenReturn(Optional.of(duplicateArchive));

        assertThatThrownBy(() -> archiveService.updateRecord(1L, 10L, request, List.of(), "login"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private ArchiveCreateRequest createRequestWithRecords(ArchiveRecordCreateRequest... records) {
        ArchiveCreateRequest request = new ArchiveCreateRequest();
        request.setSubjectName("자료구조");
        request.setProfessorName("박교수");
        request.setRecords(List.of(records));
        return request;
    }

    private ArchiveRecordCreateRequest createRecordRequest(List<Integer> fileIndexes) {
        return createRecordRequest(fileIndexes, Semester.FIRST, ExamType.MIDTERM);
    }

    private ArchiveRecordCreateRequest createRecordRequest(
            List<Integer> fileIndexes,
            Semester semester,
            ExamType examType
    ) {
        ArchiveRecordCreateRequest request = new ArchiveRecordCreateRequest();
        request.setExamYear(2026);
        request.setSemester(semester);
        request.setExamType(examType);
        request.setContent("본문");
        request.setFileIndexes(fileIndexes);
        return request;
    }

    private ArchiveUpdateRequest updateRequest(String subjectName, String professorName) {
        ArchiveUpdateRequest request = new ArchiveUpdateRequest();
        request.setSubjectName(subjectName);
        request.setProfessorName(professorName);
        request.setExamYear(2026);
        request.setSemester(Semester.FIRST);
        request.setExamType(ExamType.FINAL);
        request.setContent("수정 본문");
        request.setDeleteFileIds(List.of());
        return request;
    }

    private Archive archive(Long id, String subjectName, String professorName) {
        Archive archive = new Archive(subjectName, professorName);
        ReflectionTestUtils.setField(archive, "id", id);
        return archive;
    }

    private ArchiveRecord archiveRecord(Long id, User author) {
        ArchiveRecord record = new ArchiveRecord(
                author,
                2025,
                Semester.SECOND,
                ExamType.MIDTERM,
                "기존 본문"
        );
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }

    private User user(Long id) {
        User user = new User(
                "login",
                "password",
                "관리자",
                "20239999",
                "admin@khu.ac.kr",
                "01012345678"
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
