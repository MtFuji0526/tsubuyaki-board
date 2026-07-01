package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_投稿があるとき_新着順の最新50件をビューに渡す")
    void getPosts_withPosts_addsLatest50ToModelInDescendingOrder() throws Exception {
        Post newer = new Post("alice", "new post", Instant.parse("2026-05-23T10:00:00Z"));
        Post older = new Post("bob", "old post", Instant.parse("2026-05-23T09:00:00Z"));
        given(postService.findLatest50()).willReturn(List.of(newer, older));

        var result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.getModelAndView().getModel().get("posts");
        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("new post", "old post");
        then(postService).should().findLatest50();
    }
}
