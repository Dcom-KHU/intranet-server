package com.dcom.intranet.archive.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class ArchivePageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;

    public ArchivePageResponse(Page<T> pageResult) {
        this.content = pageResult.getContent();
        this.page = pageResult.getNumber();
        this.size = pageResult.getSize();
        this.totalPages = pageResult.getTotalPages();
        this.totalElements = pageResult.getTotalElements();
    }
}