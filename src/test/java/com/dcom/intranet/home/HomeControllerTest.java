package com.dcom.intranet.home;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dcom.intranet.home.controller.HomeController;
import com.dcom.intranet.home.service.HomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
@Import(HomeService.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getHomeDashboardReturnsRecentMockDataWithoutUnusedFields() throws Exception {
        mockMvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                .andExpect(jsonPath("$.data.recentNotices", hasSize(5)))
                .andExpect(jsonPath("$.data.recentArchives", hasSize(5)))
                .andExpect(jsonPath("$.data.recentInfoPosts", hasSize(5)))
                .andExpect(jsonPath("$.data.recentPhotoAlbums", hasSize(5)))
                .andExpect(jsonPath("$.data.recentNotices[0].title").value("2026 D.COM 여름 프로젝트 팀 모집 안내"))
                .andExpect(jsonPath("$.data.recentArchives[0].subject").value("오픈소스SW개발방법및도구"))
                .andExpect(jsonPath("$.data.recentArchives[0].author.name").value("하성준"))
                .andExpect(jsonPath("$.data.recentArchives[0].author.studentNumber").value("20230001"))
                .andExpect(jsonPath("$.data.recentArchives[0].date").value("2026.05.25"))
                .andExpect(jsonPath("$.data.recentArchives[0].semester").doesNotExist())
                .andExpect(jsonPath("$.data.recentInfoPosts[0].author.name").value("표지훈"))
                .andExpect(jsonPath("$.data.recentPhotoAlbums[0].title").value("2026-1 D.COM 커리어세션"))
                .andExpect(jsonPath("$.recentNotices").doesNotExist())
                .andExpect(jsonPath("$.recentAnnouncements").doesNotExist())
                .andExpect(jsonPath("$.mainMenu").doesNotExist())
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist());
    }
}
