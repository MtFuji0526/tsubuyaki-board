package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts" })
    public String list(Model model) {
        model.addAttribute("posts", postService.findLatest50());
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    // 演習中に追加するエンドポイント:
    //   @PostMapping("/posts")           // 投稿登録
    //   @GetMapping("/posts/{id}")       // 詳細
}
