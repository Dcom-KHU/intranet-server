package com.dcom.intranet.home;

import com.dcom.intranet.home.dto.HomeDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @GetMapping
    public HomeDashboardResponse getHomeDashboard() {
        return homeService.getHomeDashboard();
    }
}
