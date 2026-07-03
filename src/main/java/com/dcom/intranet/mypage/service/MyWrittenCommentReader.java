package com.dcom.intranet.mypage.service;

import com.dcom.intranet.mypage.dto.response.MyWrittenCommentDeleteResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.response.MyWrittenCommentTargetResponse;

public interface MyWrittenCommentReader {

    MyWrittenCommentListResponse read(Long userId, int page, int size, String type);

    MyWrittenCommentTargetResponse readTarget(Long userId, Long commentId, String type);

    MyWrittenCommentDeleteResponse delete(Long userId, Long commentId, String type);
}
