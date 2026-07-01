package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿検索_キーワードを受け取ると_本文部分一致検索の結果を返す")
    void searchByBody_withKeyword_returnsRepositoryResults() {
        Post post = new Post("alice", "hello spring", LocalDateTime.of(2026, 5, 23, 10, 0));
        given(postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("hello"))
                .willReturn(List.of(post));

        List<Post> posts = postService.searchByBody("hello");

        assertThat(posts).containsExactly(post);
        then(postRepository).should().findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("hello");
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文とアバター色を受け取ると_作成日時を付けて保存する")
    void create_withAuthorBodyAndAvatarColor_savesPostWithCreatedAt() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        LocalDateTime beforeCreate = LocalDateTime.now();

        Post created = postService.create("alice", "hello", "#ff0000");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getAvatarColor()).isEqualTo("#ff0000");
        assertThat(saved.getCreatedAt()).isBetween(beforeCreate, LocalDateTime.now());
        assertThat(created).isSameAs(saved);
    }

    @Test
    @DisplayName("投稿作成_本文にハッシュタグがあるとき_タグを保存して投稿へ紐づける")
    void create_withHashtags_savesTagsAndLinksToPost() {
        Tag existingTag = new Tag("Java");
        given(tagRepository.findByName("Java")).willReturn(Optional.of(existingTag));
        given(tagRepository.findByName("春")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        Post created = postService.create("alice", "#Java の勉強会と #春 の話 #Java", "#ff0000");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(postCaptor.capture());
        assertThat(postCaptor.getValue().getTags())
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "春");
        assertThat(created.getTags())
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "春");
        then(tagRepository).should().save(any(Tag.class));
    }

    @Test
    @DisplayName("投稿削除_存在するIDを受け取ると_削除日時を設定して保存する")
    void delete_withExistingId_setsDeletedAtAndSavesPost() {
        Post post = new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        LocalDateTime beforeDelete = LocalDateTime.now();

        postService.delete(1L);

        assertThat(post.getDeletedAt()).isBetween(beforeDelete, LocalDateTime.now());
        then(postRepository).should().save(post);
    }

    @Test
    @DisplayName("いいね登録_同じclientHashのいいねが未登録のとき_いいねを保存する")
    void toggleLike_withoutExistingLike_savesLike() {
        Post post = new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0));
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
