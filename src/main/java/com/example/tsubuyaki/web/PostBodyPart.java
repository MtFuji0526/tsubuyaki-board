package com.example.tsubuyaki.web;

public record PostBodyPart(String text, String tagName) {

    public boolean isTag() {
        return tagName != null;
    }
}
