package com.dcom.intranet.home;

import com.dcom.intranet.home.dto.HomeDashboardResponse;
import com.dcom.intranet.home.dto.HomeErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "메인 페이지 API")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @Operation(
            summary = "메인 대시보드 조회",
            description = "홈 화면에 표시할 최근 공지, 족보, 정보 공유 게시글, 활동 사진을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메인 대시보드 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = HomeDashboardResponse.class),
                            examples = @ExampleObject(
                                    name = "HomeDashboardResponse",
                                    value = """
                                            {
                                              "recentNotices": [
                                                {
                                                  "id": 1,
                                                  "title": "2026 D.COM 여름 프로젝트 팀 모집 안내",
                                                  "author": "ADMIN",
                                                  "date": "2026.06.20",
                                                  "hasAttachment": true
                                                },
                                                {
                                                  "id": 2,
                                                  "title": "정기 세미나 발표자 신청 안내",
                                                  "author": "ADMIN",
                                                  "date": "2026.06.14",
                                                  "hasAttachment": false
                                                },
                                                {
                                                  "id": 3,
                                                  "title": "동아리방 이용 수칙 변경 안내",
                                                  "author": "ADMIN",
                                                  "date": "2026.06.05",
                                                  "hasAttachment": false
                                                },
                                                {
                                                  "id": 4,
                                                  "title": "신입 부원 Git 기초 워크숍 일정",
                                                  "author": "ADMIN",
                                                  "date": "2026.05.29",
                                                  "hasAttachment": true
                                                },
                                                {
                                                  "id": 5,
                                                  "title": "기말고사 기간 활동 일정 조정 안내",
                                                  "author": "ADMIN",
                                                  "date": "2026.05.18",
                                                  "hasAttachment": true
                                                }
                                              ],
                                              "recentArchives": [
                                                {
                                                  "id": 1,
                                                  "subject": "오픈소스SW개발방법및도구",
                                                  "professor": "이성원",
                                                  "author": {
                                                    "studentNumber": "20230001",
                                                    "name": "하성준"
                                                  },
                                                  "date": "2026.05.25"
                                                },
                                                {
                                                  "id": 2,
                                                  "subject": "오픈소스SW개발방법및도구",
                                                  "professor": "이성원",
                                                  "author": {
                                                    "studentNumber": "20209999",
                                                    "name": "곽민서"
                                                  },
                                                  "date": "2026.04.25"
                                                },
                                                {
                                                  "id": 3,
                                                  "subject": "오픈소스SW개발방법및도구",
                                                  "professor": "이성원",
                                                  "author": {
                                                    "studentNumber": "20210012",
                                                    "name": "신정안"
                                                  },
                                                  "date": "2025.05.05"
                                                },
                                                {
                                                  "id": 4,
                                                  "subject": "자료구조",
                                                  "professor": "박제만",
                                                  "author": {
                                                    "studentNumber": "20220014",
                                                    "name": "최진영"
                                                  },
                                                  "date": "2026.05.20"
                                                },
                                                {
                                                  "id": 5,
                                                  "subject": "자료구조",
                                                  "professor": "박제만",
                                                  "author": {
                                                    "studentNumber": "20210032",
                                                    "name": "최진영"
                                                  },
                                                  "date": "2026.05.15"
                                                }
                                              ],
                                              "recentInfoPosts": [
                                                {
                                                  "id": 1,
                                                  "title": "시간 복잡도 Big-O 핵심 정리 (면접 필수)",
                                                  "author": {
                                                    "studentNumber": "20201234",
                                                    "name": "표지훈"
                                                  },
                                                  "date": "2026.06.20.",
                                                  "hasAttachment": true
                                                },
                                                {
                                                  "id": 2,
                                                  "title": "TCP 3-way handshake 동작 원리 정리",
                                                  "author": {
                                                    "studentNumber": "20201111",
                                                    "name": "허남준"
                                                  },
                                                  "date": "2026.06.21.",
                                                  "hasAttachment": true
                                                },
                                                {
                                                  "id": 3,
                                                  "title": "운영체제: 프로세스 vs 스레드 완벽 비교",
                                                  "author": {
                                                    "studentNumber": "20201333",
                                                    "name": "안유진"
                                                  },
                                                  "date": "2026.06.22.",
                                                  "hasAttachment": false
                                                },
                                                {
                                                  "id": 4,
                                                  "title": "DB 인덱스(B-Tree) 구조 이해하기",
                                                  "author": {
                                                    "studentNumber": "20201444",
                                                    "name": "김선호"
                                                  },
                                                  "date": "2026.06.23.",
                                                  "hasAttachment": false
                                                },
                                                {
                                                  "id": 5,
                                                  "title": "동기/비동기 & Blocking/Non-blocking 차이",
                                                  "author": {
                                                    "studentNumber": "20201555",
                                                    "name": "지창욱"
                                                  },
                                                  "date": "2026.06.24.",
                                                  "hasAttachment": true
                                                }
                                              ],
                                              "recentPhotoAlbums": [
                                                {
                                                  "id": 1,
                                                  "imageUrl": "khuBg1",
                                                  "title": "2026-1 D.COM 커리어세션",
                                                  "date": "2026.05.16",
                                                  "imageCount": 5
                                                },
                                                {
                                                  "id": 2,
                                                  "imageUrl": "khuBg2",
                                                  "title": "2026-1 D.COM 정기 세미나",
                                                  "date": "2026.05.09",
                                                  "imageCount": 8
                                                },
                                                {
                                                  "id": 3,
                                                  "imageUrl": "khuBg3",
                                                  "title": "2026-1 D.COM 네트워킹 데이",
                                                  "date": "2026.04.26",
                                                  "imageCount": 6
                                                },
                                                {
                                                  "id": 4,
                                                  "imageUrl": "khuBg1",
                                                  "title": "2026-1 D.COM 프로젝트 발표회",
                                                  "date": "2026.04.12",
                                                  "imageCount": 12
                                                },
                                                {
                                                  "id": 5,
                                                  "imageUrl": "khuBg2",
                                                  "title": "2026-1 D.COM MT",
                                                  "date": "2026.03.29",
                                                  "imageCount": 10
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = HomeErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "message": "인증이 필요합니다."
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public HomeDashboardResponse getHomeDashboard() {
        return homeService.getHomeDashboard();
    }
}
