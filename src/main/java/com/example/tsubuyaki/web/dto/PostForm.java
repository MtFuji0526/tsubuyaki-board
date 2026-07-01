package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.web.validation.NotWhitespaceOnly;
import com.example.tsubuyaki.domain.Post;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotWhitespaceOnly(message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotWhitespaceOnly(message = "本文を入力してください")
    @Size(max = 280, message = "本文は 280 文字以内で入力してください")
    private String body;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "アバター色はカラーコードで選択してください")
    private String avatarColor = Post.DEFAULT_AVATAR_COLOR;

    public PostForm() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }
}
