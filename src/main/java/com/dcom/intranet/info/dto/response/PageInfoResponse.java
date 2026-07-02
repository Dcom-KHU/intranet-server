package com.dcom.intranet.info.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageInfoResponse {

    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;

    public PageInfoResponse(Page<?> page) {
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }
}