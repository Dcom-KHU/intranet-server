package com.dcom.intranet.mypage.service;

import com.dcom.intranet.mypage.exception.MyPageApiException;

import com.dcom.intranet.mypage.dto.response.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostDeleteResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostTargetResponse;
import org.springframework.http.HttpStatus;

public class EmptyMyWrittenPostReader implements MyWrittenPostReader {

    @Override
    public MyWrittenPostListResponse read(Long userId, int page, int size, String type) {
        return MyWrittenPostListResponse.empty(page, size);
    }

    @Override
    public MyWrittenPostTargetResponse readTarget(Long userId, Long postId, String type) {
        throw new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 글을 찾을 수 없습니다.");
    }

    @Override
    public MyWrittenPostDeleteResponse delete(Long userId, Long postId, String type) {
        throw new MyPageApiException(HttpStatus.NOT_FOUND, "작성한 글을 찾을 수 없습니다.");
    }
}
