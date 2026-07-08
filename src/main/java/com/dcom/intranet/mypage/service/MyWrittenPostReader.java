package com.dcom.intranet.mypage.service;

import com.dcom.intranet.mypage.dto.response.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostDeleteResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenPostTargetResponse;

public interface MyWrittenPostReader {

    MyWrittenPostListResponse read(Long userId, int page, int size, String type);

    MyWrittenPostTargetResponse readTarget(Long userId, Long postId, String type);

    MyWrittenPostDeleteResponse delete(Long userId, Long postId, String type);
}
