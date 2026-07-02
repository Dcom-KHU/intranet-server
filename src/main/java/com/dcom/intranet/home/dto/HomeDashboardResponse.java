package com.dcom.intranet.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "메인 대시보드 응답")
public record HomeDashboardResponse(
        @Schema(description = "최근 공지 목록")
        List<NoticeSummaryResponse> recentNotices,

        @Schema(description = "최근 족보 목록")
        List<ArchiveSummaryResponse> recentArchives,

        @Schema(description = "최근 정보 공유 게시글 목록")
        List<InfoPostSummaryResponse> recentInfoPosts,

        @Schema(description = "최근 활동 사진 앨범 목록")
        List<PhotoAlbumSummaryResponse> recentPhotoAlbums
) {
}
