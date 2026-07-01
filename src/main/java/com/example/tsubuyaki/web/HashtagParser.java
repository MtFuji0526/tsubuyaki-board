package com.example.tsubuyaki.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HashtagParser {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_ー]+)");

    private HashtagParser() {
    }

    public static List<String> extractTagNames(String body) {
        Set<String> tagNames = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(body);
        while (matcher.find()) {
            tagNames.add(matcher.group(1));
        }
        return List.copyOf(tagNames);
    }

    public static List<PostBodyPart> parseBodyParts(String body) {
        List<PostBodyPart> parts = new ArrayList<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(body);
        int cursor = 0;
        while (matcher.find()) {
            if (cursor < matcher.start()) {
                parts.add(new PostBodyPart(body.substring(cursor, matcher.start()), null));
            }
            String tagName = matcher.group(1);
            parts.add(new PostBodyPart("#" + tagName, tagName));
            cursor = matcher.end();
        }
        if (cursor < body.length()) {
            parts.add(new PostBodyPart(body.substring(cursor), null));
        }
        return List.copyOf(parts);
    }
}
