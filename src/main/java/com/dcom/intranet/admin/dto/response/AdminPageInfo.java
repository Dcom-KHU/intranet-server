package com.dcom.intranet.admin.dto.response;

import org.springframework.data.domain.Page;

public record AdminPageInfo(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static AdminPageInfo from(Page<?> page) {
        return new AdminPageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
