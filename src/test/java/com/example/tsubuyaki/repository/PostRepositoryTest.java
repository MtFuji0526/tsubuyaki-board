package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("投稿一覧_投稿が51件あるとき_最新50件を新着順で返す")
    void findTop50ByOrderByCreatedAtDesc_with51Posts_returnsLatest50InDescendingOrder() {
        LocalDateTime base = LocalDateTime.of(2026, 5, 23, 0, 0);
        var posts = IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post("user", "post-%02d".formatted(index), base.plusSeconds(index)))
                .toList();
        postRepository.saveAll(posts);

        var latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts)
                .extracting(Post::getBody)
                .startsWith("post-51")
                .endsWith("post-02")
                .doesNotContain("post-01");
    }

    @Test
    @DisplayName("投稿保存_アバター色があるとき_保存後に同じ値で取得できる")
    void save_withAvatarColor_persistsAvatarColor() {
        Post saved = postRepository.save(new Post(
                "alice",
                "hello",
                "#ff0000",
                LocalDateTime.of(2026, 5, 23, 10, 0)));

        postRepository.flush();

        assertThat(postRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Post::getAvatarColor)
                .isEqualTo("#ff0000");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含むとき_該当投稿を新着順で返す")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_withKeyword_returnsMatchesInDescendingOrder() {
        postRepository.save(new Post("alice", "hello spring", LocalDateTime.of(2026, 5, 23, 9, 0)));
        postRepository.save(new Post("bob", "unmatched body", LocalDateTime.of(2026, 5, 23, 10, 0)));
        postRepository.save(new Post("carol", "hello thymeleaf", LocalDateTime.of(2026, 5, 23, 11, 0)));

        var posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("hello");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("hello thymeleaf", "hello spring");
    }

    @Test
    @DisplayName("いいね集計_投稿にいいねが登録されているとき_件数を返す")
    void countByPostId_withLikes_returnsLikeCount() {
        Post post = postRepository.save(new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0)));
        postLikeRepository.save(new PostLike(post, "abcdef12"));
        postLikeRepository.save(new PostLike(post, "34567890"));

        long count = postLikeRepository.countByPostId(post.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("いいね削除_投稿IDとclientHashが一致するとき_対象のいいねだけ削除する")
    void deleteByPostIdAndClientHash_withMatchingLike_deletesOnlyMatchedLike() {
        Post post = postRepository.save(new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0)));
        postLikeRepository.save(new PostLike(post, "abcdef12"));
        postLikeRepository.save(new PostLike(post, "34567890"));

        postLikeRepository.deleteByPostIdAndClientHash(post.getId(), "abcdef12");

        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "abcdef12")).isFalse();
        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "34567890")).isTrue();
    }
}
