package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿検索_キーワードを受け取ると_本文部分一致検索の結果を返す")
    void searchByBody_withKeyword_returnsRepositoryResults() {
        Post post = new Post("alice", "hello spring", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("hello"))
                .willReturn(List.of(post));

        List<Post> posts = postService.searchByBody("hello");

        assertThat(posts).containsExactly(post);
        then(postRepository).should().findTop50ByBodyContainingOrderByCreatedAtDesc("hello");
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文を受け取ると_作成日時を付けて保存する")
    void create_withAuthorAndBody_savesPostWithCreatedAt() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        Instant beforeCreate = Instant.now();

        Post created = postService.create("alice", "hello");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getCreatedAt()).isBetween(beforeCreate, Instant.now());
        assertThat(created).isSameAs(saved);
    }

    @Test
    @DisplayName("いいね登録_同じclientHashのいいねが未登録のとき_いいねを保存する")
    void toggleLike_withoutExistingLike_savesLike() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.getReferenceById(1L)).willReturn(post);
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abcdef12")).willReturn(false);
        given(postLikeRepository.save(any(PostLike.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.toggleLike(1L, "abcdef12");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        then(postLikeRepository).should().save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("abcdef12");
        then(postLikeRepository).should(never()).deleteByPostIdAndClientHash(1L, "abcdef12");
    }

    @Test
    @DisplayName("いいね解除_同じclientHashのいいねが登録済みのとき_いいねを削除する")
    void toggleLike_withExistingLike_deletesLike() {
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abcdef12")).willReturn(true);

        postService.toggleLike(1L, "abcdef12");

        then(postLikeRepository).should().deleteByPostIdAndClientHash(1L, "abcdef12");
        then(postLikeRepository).should(never()).save(any(PostLike.class));
        then(postRepository).shouldHaveNoInteractions();
    }
}
