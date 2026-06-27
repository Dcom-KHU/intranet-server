package com.dcom.intranet.archive.service;

import com.dcom.intranet.archive.domain.Archive;
import com.dcom.intranet.archive.dto.response.*;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dcom.intranet.archive.dto.response.ArchiveDetailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiveService {

    private final ArchiveRepository archiveRepository;

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
}