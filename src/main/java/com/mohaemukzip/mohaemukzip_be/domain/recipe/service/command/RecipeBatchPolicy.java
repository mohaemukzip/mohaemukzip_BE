package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import java.util.List;
import java.util.regex.Pattern;

public final class RecipeBatchPolicy {
    public static final int MAX_RECIPES = 50;
    public static final String VIDEO_ID_PATTERN = "[A-Za-z0-9_-]{11}";
    private static final Pattern VIDEO_ID = Pattern.compile(VIDEO_ID_PATTERN);
    private RecipeBatchPolicy() {}
    public static List<String> normalize(List<String> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > MAX_RECIPES
                || ids.stream().anyMatch(id -> id == null || !VIDEO_ID.matcher(id).matches())) {
            throw new BusinessException(ErrorStatus.BAD_REQUEST);
        }
        return ids.stream().distinct().toList();
    }
    public static String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) return text;
        int end = maxChars - 1;
        if (Character.isHighSurrogate(text.charAt(end - 1))) end--;
        return text.substring(0, end) + "…";
    }
}
