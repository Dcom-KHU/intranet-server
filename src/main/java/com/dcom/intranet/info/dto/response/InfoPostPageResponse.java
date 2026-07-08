package com.dcom.intranet.info.dto.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class InfoPostPageResponse {

    private final List<InfoPostListResponse> postList;
    private final PageInfoResponse pageInfo;

    public InfoPostPageResponse(Page<InfoPostListResponse> page) {
        this.postList = page.getContent();
        this.pageInfo = new PageInfoResponse(page);
    }
}