package com.example.tsubuyaki.sample;

import com.example.tsubuyaki.controller.PostController;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Controller テストの雛形。TDD の見本として残す (削除禁止)。
 *
 * <p>@WebMvcTest で Spring の MVC スライスのみ起動し、Service はモック化する。</p>
 */
@WebMvcTest(PostController.class)
class SamplePostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("Controller_投稿一覧_GET_/posts_は posts/list ビューを返す")
    void getPosts_rendersListView() throws Exception {
        given(postService.findLatest50()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"));
    }
}
