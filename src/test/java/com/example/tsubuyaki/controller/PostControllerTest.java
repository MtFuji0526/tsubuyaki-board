package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
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
        Post newer = new Post("alice", "new post", LocalDateTime.of(2026, 5, 23, 10, 0));
        ReflectionTestUtils.setField(newer, "id", 2L);
        Post older = new Post("bob", "old post", LocalDateTime.of(2026, 5, 23, 9, 0));
        ReflectionTestUtils.setField(older, "id", 1L);
        given(postService.findLatest50()).willReturn(List.of(newer, older));

        var result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("href=\"/posts/2\"")))
                .andExpect(content().string(containsString("href=\"/posts/1\"")))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.getModelAndView().getModel().get("posts");
        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("new post", "old post");
        then(postService).should().findLatest50();
    }

    @Test
    @DisplayName("投稿一覧_アバター色があるとき_投稿者名の直後に同じ色の丸アイコンを表示する")
    void getPosts_withAvatarColor_showsCircularIconNextToAuthor() throws Exception {
        Post post = new Post("alice", "hello", "#00aa55", LocalDateTime.of(2026, 5, 23, 10, 0));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.findLatest50()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*<span class=\"post__author\">alice</span>\\s*"
                                + "<span class=\"post__avatar\".*")))
                .andExpect(content().string(containsString("background-color: #00aa55")));

        then(postService).should().findLatest50();
    }

    @Test
    @DisplayName("投稿一覧_検索クエリがあるとき_本文で絞り込み検索フォームに検索語を保持する")
    void getPosts_withSearchQuery_addsSearchResultsAndQueryToModel() throws Exception {
        Post matched = new Post("alice", "hello spring", LocalDateTime.of(2026, 5, 23, 10, 0));
        ReflectionTestUtils.setField(matched, "id", 1L);
        given(postService.searchByBody("hello")).willReturn(List.of(matched));

        var result = mockMvc.perform(get("/posts").param("q", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("q", "hello"))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("value=\"hello\"")))
                .andExpect(content().string(containsString("hello spring")))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.getModelAndView().getModel().get("posts");
        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("hello spring");
        then(postService).should().searchByBody("hello");
        then(postService).should(never()).findLatest50();
    }

    @Test
    @DisplayName("タグ別投稿一覧_タグ名を指定したとき_該当投稿だけをビューに渡す")
    void getPostsByTag_withTagName_addsTaggedPostsToModel() throws Exception {
        Post matched = new Post("alice", "#Java spring", LocalDateTime.of(2026, 5, 23, 10, 0));
        ReflectionTestUtils.setField(matched, "id", 1L);
        given(postService.findByTagName("Java")).willReturn(List.of(matched));

        var result = mockMvc.perform(get("/tags/Java"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("tagName", "Java"))
                .andExpect(content().string(containsString("#Java spring")))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.getModelAndView().getModel().get("posts");
        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("#Java spring");
        then(postService).should().findByTagName("Java");
    }

    @Test
    @DisplayName("投稿詳細_存在するIDのとき_対象投稿を表示する")
    void getPostDetail_withExistingId_showsPostDetail() throws Exception {
        Post post = new Post("alice", "detail body", "#ff0000", LocalDateTime.of(2026, 5, 23, 10, 0));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("detail body")))
                .andExpect(content().string(containsString("background-color: #ff0000")))
                .andExpect(content().string(containsString("3 件")))
                .andExpect(content().string(containsString("action=\"/posts/1/delete\"")));

        then(postService).should().findById(1L);
        then(postService).should().countLikes(1L);
    }

    @Test
    @DisplayName("投稿詳細_本文にハッシュタグがあるとき_タグ一覧へのリンクとして表示する")
    void getPostDetail_withHashtagBody_showsHashtagLink() throws Exception {
        Post post = new Post("alice", "今日は #Java の話", LocalDateTime.of(2026, 5, 23, 10, 0));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(0L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("今日は ")))
                .andExpect(content().string(containsString("href=\"/tags/Java\"")))
                .andExpect(content().string(containsString(">#Java</a>")))
                .andExpect(content().string(containsString(" の話")));

        then(postService).should().findById(1L);
        then(postService).should().countLikes(1L);
    }

    @Test
    @DisplayName("投稿詳細_存在しないIDのとき_404を返す")
    void getPostDetail_withMissingId_returnsNotFound() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());

        then(postService).should().findById(999L);
    }

    @Test
    @DisplayName("投稿作成_入力が妥当なとき_投稿を保存して一覧へリダイレクトする")
    void createPost_withValidInput_savesPostAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello")
                        .param("avatarColor", "#ff0000"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "hello", "#ff0000");
    }

    @Test
    @DisplayName("投稿削除_存在するIDを指定したとき_論理削除して一覧へ302リダイレクトする")
    void deletePost_withExistingId_softDeletesPostAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().delete(1L);
    }

    @Test
    @DisplayName("投稿作成_投稿者が未入力のとき_フォームを再表示してエラーを渡す")
    void createPost_withoutAuthor_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("body", "hello")
                        .param("avatarColor", "#ff0000"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        then(postService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("投稿作成_投稿者が空白のみのとき_フォームを再表示してエラーを渡す")
    void createPost_withBlankAuthor_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "hello")
                        .param("avatarColor", "#ff0000"))
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

    @ParameterizedTest
    @ValueSource(strings = { "\u3000\u3000", " \u3000" })
    @DisplayName("投稿作成_本文が全角スペースを含む空白のみのとき_フォームを再表示してエラーを渡す")
    void createPost_withFullWidthSpacesOnlyBody_redisplaysFormWithErrors(String body) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", body))
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

    @Test
    @DisplayName("いいね登録_同一clientHashから初回アクセス_トグル処理して詳細へ302リダイレクトする")
    void toggleLike_firstAccess_togglesLikeAndRedirectsToDetail() throws Exception {
        String remoteAddr = "192.0.2.10";
        String userAgent = "MockBrowser/1.0";

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddr);
                            request.addHeader("User-Agent", userAgent);
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().toggleLike(1L, clientHash(remoteAddr, userAgent));
    }

    @Test
    @DisplayName("いいね解除_同一clientHashから2回目アクセス_トグル処理して詳細へ302リダイレクトする")
    void toggleLike_secondAccess_togglesLikeAndRedirectsToDetail() throws Exception {
        String remoteAddr = "192.0.2.10";
        String userAgent = "MockBrowser/1.0";

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddr);
                            request.addHeader("User-Agent", userAgent);
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().toggleLike(1L, clientHash(remoteAddr, userAgent));
    }

    private String clientHash(String remoteAddr, String userAgent) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((remoteAddr + userAgent).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
