package com.dcom.intranet.archive.controller;

import com.dcom.intranet.archive.dto.response.*;
import com.dcom.intranet.archive.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping
    public ResponseEntity<ArchivePageResponse<ArchiveListResponse>> getArchives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(archiveService.getArchives(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchArchives(
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String professorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (subjectName != null && !subjectName.isBlank()) {
            return ResponseEntity.ok(
                    archiveService.searchBySubjectName(subjectName, page, size)
            );
        }

        if (professorName != null && !professorName.isBlank()) {
            return ResponseEntity.ok(
                    archiveService.searchByProfessorName(professorName, page, size)
            );
        }

        throw new IllegalArgumentException("subjectName 또는 professorName 중 하나는 필요합니다.");
    }

    @GetMapping("/{archiveId}")
    public ResponseEntity<ArchiveDetailResponse> getArchiveDetail(
            @PathVariable Long archiveId
    ) {
        return ResponseEntity.ok(archiveService.getArchiveDetail(archiveId));
    }
}