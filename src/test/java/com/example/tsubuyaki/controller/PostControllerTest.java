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
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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

    @Test
    @DisplayName("投稿作成_入力が妥当なとき_投稿を保存して一覧へリダイレクトする")
    void createPost_withValidInput_savesPostAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "hello");
    }

    @Test
    @DisplayName("投稿作成_投稿者が空白のみのとき_フォームを再表示してエラーを渡す")
    void createPost_withBlankAuthor_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("投稿作成_本文が空白のみのとき_フォームを再表示してエラーを渡す")
    void createPost_withBlankBody_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("投稿作成_文字数上限を超えるとき_フォームを再表示してエラーを渡す")
    void createPost_withTooLongInput_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        then(postService).shouldHaveNoInteractions();
    }
}
