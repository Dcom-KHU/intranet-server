package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostTargetResponse;

public interface MyWrittenPostReader {

    MyWrittenPostListResponse read(Long userId, int page, int size, String type);

    MyWrittenPostTargetResponse readTarget(Long userId, Long postId, String type);
}
