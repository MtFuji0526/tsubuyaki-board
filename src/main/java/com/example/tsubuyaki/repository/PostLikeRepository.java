package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndClientHash(Long postId, String clientHash);

    void deleteByPostIdAndClientHash(Long postId, String clientHash);

    long countByPostId(Long postId);
}
