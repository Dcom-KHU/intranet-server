package com.dcom.intranet.home;

import com.dcom.intranet.home.dto.ArchiveSummaryResponse;
import com.dcom.intranet.home.dto.AuthorResponse;
import com.dcom.intranet.home.dto.HomeDashboardResponse;
import com.dcom.intranet.home.dto.InfoPostSummaryResponse;
import com.dcom.intranet.home.dto.NoticeSummaryResponse;
import com.dcom.intranet.home.dto.PhotoAlbumSummaryResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public HomeDashboardResponse getHomeDashboard() {
        return new HomeDashboardResponse(
                recentNotices(),
                recentArchives(),
                recentInfoPosts(),
                recentPhotoAlbums()
        );
    }

    private List<NoticeSummaryResponse> recentNotices() {
        return List.of(
                new NoticeSummaryResponse(1L, "2026 D.COM 여름 프로젝트 팀 모집 안내", "ADMIN", "2026.06.20", true),
                new NoticeSummaryResponse(2L, "정기 세미나 발표자 신청 안내", "ADMIN", "2026.06.14", false),
                new NoticeSummaryResponse(3L, "동아리방 이용 수칙 변경 안내", "ADMIN", "2026.06.05", false),
                new NoticeSummaryResponse(4L, "신입 부원 Git 기초 워크숍 일정", "ADMIN", "2026.05.29", true),
                new NoticeSummaryResponse(5L, "기말고사 기간 활동 일정 조정 안내", "ADMIN", "2026.05.18", true)
        );
    }

    private List<ArchiveSummaryResponse> recentArchives() {
        return List.of(
                new ArchiveSummaryResponse(1L, "오픈소스SW개발방법및도구", "이성원", new AuthorResponse("20230001", "하성준"), "2026.05.25"),
                new ArchiveSummaryResponse(2L, "오픈소스SW개발방법및도구", "이성원", new AuthorResponse("20209999", "곽민서"), "2026.04.25"),
                new ArchiveSummaryResponse(3L, "오픈소스SW개발방법및도구", "이성원", new AuthorResponse("20210012", "신정안"), "2025.05.05"),
                new ArchiveSummaryResponse(4L, "자료구조", "박제만", new AuthorResponse("20220014", "최진영"), "2026.05.20"),
                new ArchiveSummaryResponse(5L, "자료구조", "박제만", new AuthorResponse("20210032", "최진영"), "2026.05.15")
        );
    }

    private List<InfoPostSummaryResponse> recentInfoPosts() {
        return List.of(
                new InfoPostSummaryResponse(1L, "시간 복잡도 Big-O 핵심 정리 (면접 필수)", new AuthorResponse("20201234", "표지훈"), "2026.06.20.", true),
                new InfoPostSummaryResponse(2L, "TCP 3-way handshake 동작 원리 정리", new AuthorResponse("20201111", "허남준"), "2026.06.21.", true),
                new InfoPostSummaryResponse(3L, "운영체제: 프로세스 vs 스레드 완벽 비교", new AuthorResponse("20201333", "안유진"), "2026.06.22.", false),
                new InfoPostSummaryResponse(4L, "DB 인덱스(B-Tree) 구조 이해하기", new AuthorResponse("20201444", "김선호"), "2026.06.23.", false),
                new InfoPostSummaryResponse(5L, "동기/비동기 & Blocking/Non-blocking 차이", new AuthorResponse("20201555", "지창욱"), "2026.06.24.", true)
        );
    }

    private List<PhotoAlbumSummaryResponse> recentPhotoAlbums() {
        return List.of(
                new PhotoAlbumSummaryResponse(1L, "khuBg1", "2026-1 D.COM 커리어세션", "2026.05.16", 5),
                new PhotoAlbumSummaryResponse(2L, "khuBg2", "2026-1 D.COM 정기 세미나", "2026.05.09", 8),
                new PhotoAlbumSummaryResponse(3L, "khuBg3", "2026-1 D.COM 네트워킹 데이", "2026.04.26", 6),
                new PhotoAlbumSummaryResponse(4L, "khuBg1", "2026-1 D.COM 프로젝트 발표회", "2026.04.12", 12),
                new PhotoAlbumSummaryResponse(5L, "khuBg2", "2026-1 D.COM MT", "2026.03.29", 10)
        );
    }
}
