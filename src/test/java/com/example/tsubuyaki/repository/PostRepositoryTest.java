package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_投稿が51件あるとき_最新50件を新着順で返す")
    void findTop50ByOrderByCreatedAtDesc_with51Posts_returnsLatest50InDescendingOrder() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
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
}
