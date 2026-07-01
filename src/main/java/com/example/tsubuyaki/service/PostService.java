package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import com.example.tsubuyaki.web.HashtagParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository, TagRepository tagRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
        this.tagRepository = tagRepository;
    }

    public List<Post> findLatest50() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> searchByBody(String keyword) {
        return repository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(keyword);
    }

    public List<Post> findByTagName(String name) {
        return repository.findDistinctTop50ByTagsNameAndDeletedAtIsNullOrderByCreatedAtDesc(name);
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        Post post = new Post(author, body, avatarColor, LocalDateTime.now());
        HashtagParser.extractTagNames(body).stream()
                .map(this::findOrCreateTag)
                .forEach(post::addTag);
        return repository.save(post);
    }

    private Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));
    }

    @Transactional
    public void delete(Long id) {
        repository.findById(id).ifPresent(post -> {
            post.delete(LocalDateTime.now());
            repository.save(post);
        });
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        if (postLikeRepository.existsByPostIdAndClientHash(postId, clientHash)) {
            postLikeRepository.deleteByPostIdAndClientHash(postId, clientHash);
            return;
        }

        Post post = repository.getReferenceById(postId);
        postLikeRepository.save(new PostLike(post, clientHash));
    }
}
