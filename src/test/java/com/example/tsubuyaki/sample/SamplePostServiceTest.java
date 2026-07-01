package com.example.tsubuyaki.sample;

import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Service テストの雛形。TDD の見本として残す (削除禁止)。
 *
 * <p>Mockito で Repository をモック化し、Spring を起動せずにテストする。</p>
 */
@ExtendWith(MockitoExtension.class)
class SamplePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Service_最新50件取得_Repositoryの結果を返す")
    void findLatest50_returnsRepositoryResult() {
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(Collections.emptyList());

        assertThat(postService.findLatest50()).isEmpty();
    }
}
