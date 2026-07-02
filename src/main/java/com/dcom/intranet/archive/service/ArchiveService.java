package com.dcom.intranet.archive.service;

import com.dcom.intranet.archive.domain.Archive;
import com.dcom.intranet.archive.domain.ArchiveFile;
import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.archive.dto.request.ArchiveCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveRecordCreateRequest;
import com.dcom.intranet.archive.dto.response.*;
import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.archive.repository.ArchiveFileRepository;
import lombok.Getter;
import org.springframework.core.io.Resource;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dcom.intranet.archive.dto.response.ArchiveDetailResponse;
import com.dcom.intranet.archive.dto.request.ArchiveUpdateRequest;
import com.dcom.intranet.archive.dto.response.ArchiveUpdateResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final UserRepository userRepository;
    private final ArchiveFileRepository archiveFileRepository;
    private final ArchiveFileStorageService archiveFileStorageService;
    private final ArchiveRecordRepository archiveRecordRepository;

    public ArchivePageResponse<ArchiveListResponse> getArchives(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "lastModifiedAt")
        );

        Page<ArchiveListResponse> archives = archiveRepository.findAll(pageable)
                .map(ArchiveListResponse::new);

        return new ArchivePageResponse<>(archives);
    }

    public ArchivePageResponse<ArchiveProfessorGroupResponse> searchBySubjectName(
            String subjectName,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "professorName")
                        .and(Sort.by(Sort.Direction.DESC, "lastModifiedAt"))
        );

        Page<ArchiveProfessorGroupResponse> result =
                archiveRepository.findBySubjectNameContaining(subjectName, pageable)
                        .map(ArchiveProfessorGroupResponse::new);

        return new ArchivePageResponse<>(result);
    }

    public ArchivePageResponse<ArchiveSubjectGroupResponse> searchByProfessorName(
            String professorName,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "subjectName")
                        .and(Sort.by(Sort.Direction.DESC, "lastModifiedAt"))
        );

        Page<ArchiveSubjectGroupResponse> result =
                archiveRepository.findByProfessorNameContaining(professorName, pageable)
                        .map(ArchiveSubjectGroupResponse::new);

        return new ArchivePageResponse<>(result);
    }

    public ArchiveDetailResponse getArchiveDetail(Long archiveId) {
        Archive archive = archiveRepository.findById(archiveId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "족보 아카이브를 찾을 수 없습니다."
                ));

        return new ArchiveDetailResponse(archive);
    }

    @Transactional
    public ArchiveCreateResponse createArchive(
            ArchiveCreateRequest request,
            List<MultipartFile> files,
            String loginId
    ) {
        List<MultipartFile> requestFiles = files == null
                ? List.of()
                : files;

        User author = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        Archive archive = resolveArchive(request);

        // 새 Archive면 여기서 저장되고, 기존 Archive면 관리 상태로 유지됨
        archiveRepository.save(archive);

        List<ArchiveRecord> createdRecords = new ArrayList<>();

        for (ArchiveRecordCreateRequest recordRequest : request.getRecords()) {
            List<Integer> fileIndexes = resolveFileIndexes(recordRequest);

            validateFileIndexes(fileIndexes, requestFiles);

            validateRecordHasContentOrFiles(recordRequest, fileIndexes);

            ArchiveRecord record = new ArchiveRecord(
                    author,
                    recordRequest.getExamYear(),
                    recordRequest.getSemester(),
                    recordRequest.getExamType(),
                    recordRequest.getContent()
            );

            for (Integer fileIndex : fileIndexes) {
                MultipartFile multipartFile = requestFiles.get(fileIndex);

                ArchiveFileStorageService.StoredFile storedFile =
                        archiveFileStorageService.store(multipartFile);

                ArchiveFile archiveFile = new ArchiveFile(
                        storedFile.getOriginalFileName(),
                        storedFile.getStoredFileName(),
                        storedFile.getObjectKey(),
                        storedFile.getFileUrl(),
                        storedFile.getFileSize(),
                        storedFile.getContentType()
                );

                record.addFile(archiveFile);
            }

            archive.addRecord(record);
            archiveRecordRepository.save(record);
            createdRecords.add(record);
        }

        archiveRepository.flush();
        archiveRecordRepository.flush();

        List<Long> recordIds = createdRecords.stream()
                .map(ArchiveRecord::getId)
                .toList();

        LocalDateTime createdAt = createdRecords.get(0).getCreatedAt();

        return new ArchiveCreateResponse(
                archive.getId(),
                recordIds,
                createdAt
        );
    }

    private Archive resolveArchive(ArchiveCreateRequest request) {
        if (request.getArchiveId() != null) {
            return archiveRepository.findById(request.getArchiveId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "족보 아카이브를 찾을 수 없습니다."
                    ));
        }

        if (request.getSubjectName() == null || request.getSubjectName().isBlank()
                || request.getProfessorName() == null || request.getProfessorName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "archiveId가 없으면 subjectName과 professorName은 필수입니다."
            );
        }

        return archiveRepository
                .findBySubjectNameAndProfessorName(
                        request.getSubjectName(),
                        request.getProfessorName()
                )
                .orElseGet(() -> new Archive(
                        request.getSubjectName(),
                        request.getProfessorName()
                ));
    }

    private List<Integer> resolveFileIndexes(ArchiveRecordCreateRequest recordRequest) {
        if (recordRequest.getFileIndexes() == null || recordRequest.getFileIndexes().isEmpty()) {
            return List.of();
        }

        return recordRequest.getFileIndexes()
                .stream()
                .distinct()
                .toList();
    }

    @Transactional
    public ArchiveUpdateResponse updateRecord(
            Long archiveId,
            Long recordId,
            ArchiveUpdateRequest request,
            List<MultipartFile> files,
            String loginId
    ) {
        ArchiveRecord record = findRecordInArchive(archiveId, recordId);
        validateOwnerOrAdmin(record, loginId);

        List<MultipartFile> safeFiles = files == null ? List.of() : files;

        List<Long> deleteFileIds = request.getDeleteFileIds() == null
                ? List.of()
                : request.getDeleteFileIds().stream().distinct().toList();

        validateUpdateRequest(record, request, safeFiles, deleteFileIds);

        // 1. 기본 정보 수정
        record.update(
                request.getExamYear(),
                request.getSemester(),
                request.getExamType(),
                request.getContent()
        );

        // 2. 선택된 기존 파일 삭제
        for (Long deleteFileId : deleteFileIds) {
            ArchiveFile targetFile = findFileInRecord(record, deleteFileId);

            archiveFileStorageService.delete(targetFile.getFileUrl());
            record.removeFile(targetFile);
        }

        // 3. 새 파일 추가
        for (MultipartFile multipartFile : safeFiles) {
            if (multipartFile == null || multipartFile.isEmpty()) {
                continue;
            }

            ArchiveFileStorageService.StoredFile storedFile =
                    archiveFileStorageService.store(multipartFile);

            ArchiveFile archiveFile = new ArchiveFile(
                    storedFile.getOriginalFileName(),
                    storedFile.getStoredFileName(),
                    storedFile.getObjectKey(),
                    storedFile.getFileUrl(),
                    storedFile.getFileSize(),
                    storedFile.getContentType()
            );

            record.addFile(archiveFile);
        }

        // 4. 상위 Archive 최근 수정일 갱신
        record.getArchive().touch();

        return new ArchiveUpdateResponse(
                record.getId(),
                record.getUpdatedAt()
        );
    }

    private ArchiveRecord findRecordInArchive(Long archiveId, Long recordId) {
        ArchiveRecord record = archiveRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "족보 레코드를 찾을 수 없습니다."
                ));

        if (!record.getArchive().getId().equals(archiveId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 아카이브에 속한 족보 레코드가 아닙니다."
            );
        }

        return record;
    }

    private void validateOwnerOrAdmin(ArchiveRecord record, String loginId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));

        boolean isOwner = record.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "작성자 또는 관리자만 수정/삭제할 수 있습니다."
            );
        }
    }

    private ArchiveFile findFileInRecord(ArchiveRecord record, Long fileId) {
        return record.getFiles().stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "해당 족보 레코드에 속한 파일을 찾을 수 없습니다."
                ));
    }

    private void validateUpdateRequest(
            ArchiveRecord record,
            ArchiveUpdateRequest request,
            List<MultipartFile> newFiles,
            List<Long> deleteFileIds
    ) {
        for (Long deleteFileId : deleteFileIds) {
            findFileInRecord(record, deleteFileId);
        }

        int currentFileCount = record.getFiles().size();
        int deleteFileCount = deleteFileIds.size();

        long addFileCount = newFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .count();

        int remainingFileCount = currentFileCount - deleteFileCount + (int) addFileCount;

        boolean hasContent = request.getContent() != null
                && !request.getContent().isBlank();

        if (!hasContent && remainingFileCount <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "본문 내용 또는 파일 중 하나는 필요합니다."
            );
        }
    }

    @Transactional
    public void deleteRecord(Long archiveId, Long recordId, String loginId) {
        ArchiveRecord record = findRecordInArchive(archiveId, recordId);
        validateOwnerOrAdmin(record, loginId);

        Archive archive = record.getArchive();

        // 로컬 저장소 실제 파일 삭제
        List<ArchiveFile> filesToDelete = new ArrayList<>(record.getFiles());

        for (ArchiveFile file : filesToDelete) {
            archiveFileStorageService.delete(file.getFileUrl());
        }

        // DB에서 Record 물리 삭제
        archive.removeRecord(record);

        // 마지막 족보였다면 빈 Archive도 삭제
        if (archive.hasNoRecords()) {
            archiveRepository.delete(archive);
        }
    }

    @Getter
    public static class DownloadFile {
        private final Resource resource;
        private final String fileName;
        private final String contentType;

        public DownloadFile(Resource resource, String fileName, String contentType) {
            this.resource = resource;
            this.fileName = fileName;
            this.contentType = contentType;
        }
    }

    @Transactional(readOnly = true)
    public DownloadFile downloadFile(Long archiveId, Long recordId, Long fileId) {
        ArchiveRecord record = findRecordInArchive(archiveId, recordId);

        ArchiveFile file = archiveFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "파일을 찾을 수 없습니다."
                ));

        if (!file.getRecord().getId().equals(record.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "해당 족보 레코드에 속한 파일이 아닙니다."
            );
        }

        Resource resource = archiveFileStorageService.loadAsResource(file.getFileUrl());

        return new DownloadFile(
                resource,
                file.getOriginalFileName(),
                file.getContentType()
        );
    }

    private void validateFileIndexes(
            List<Integer> fileIndexes,
            List<MultipartFile> files
    ) {
        if (fileIndexes == null || fileIndexes.isEmpty()) {
            return;
        }

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fileIndexes가 존재하지만 업로드된 파일이 없습니다."
            );
        }

        for (Integer fileIndex : fileIndexes) {
            if (fileIndex == null || fileIndex < 0 || fileIndex >= files.size()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "fileIndexes 값이 업로드된 파일 범위를 벗어났습니다."
                );
            }

            MultipartFile file = files.get(fileIndex);

            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "fileIndexes가 가리키는 파일이 비어 있습니다."
                );
            }
        }
    }

    private void validateRecordHasContentOrFiles(
            ArchiveRecordCreateRequest recordRequest,
            List<Integer> fileIndexes
    ) {
        boolean hasContent = recordRequest.getContent() != null
                && !recordRequest.getContent().isBlank();

        boolean hasFiles = fileIndexes != null && !fileIndexes.isEmpty();

        if (!hasContent && !hasFiles) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "본문 내용 또는 파일 중 하나는 필요합니다."
            );
        }
    }
}