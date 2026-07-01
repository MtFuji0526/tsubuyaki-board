package com.example.tsubuyaki.sample;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository テストの雛形。TDD の見本として残す (削除禁止)。
 *
 * <p>受講生は本ファイルを参考にしつつ、別ファイルに自分のテストを書く。</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class SamplePostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("Repository_保存と取得_往復で同じ値が返る")
    void save_and_findAll_roundTrip() {
        postRepository.save(new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0)));

        List<Post> all = postRepository.findAll();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getAuthor()).isEqualTo("alice");
        assertThat(all.get(0).getBody()).isEqualTo("hello");
    }
}
