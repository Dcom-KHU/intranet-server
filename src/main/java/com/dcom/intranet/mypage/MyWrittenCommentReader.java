package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;

public interface MyWrittenCommentReader {

    MyWrittenCommentListResponse read(Long userId, int page, int size, String type);
}
