package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "post_likes_post_client_uk",
                columnNames = { "post_id", "client_hash" }))
public class PostLike {

    @Id
    @SequenceGenerator(name = "post_likes_seq_gen", sequenceName = "post_likes_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_likes_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "client_hash", length = 8, nullable = false)
    private String clientHash;

    protected PostLike() {
        // JPA
    }

    public PostLike(Post post, String clientHash) {
        this.post = post;
        this.clientHash = clientHash;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getClientHash() {
        return clientHash;
    }
}
