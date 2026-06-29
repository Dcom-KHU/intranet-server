package com.dcom.intranet.mypage;

import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;

public interface MyWrittenPostReader {

    MyWrittenPostListResponse read(Long userId, int page, int size, String type);
}
