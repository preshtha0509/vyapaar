package com.vyapaar.dto;

import java.util.List;
import java.util.Map;

/**
 * Every reply uses this one shape, including errors and refusals, so the frontend only ever has
 * one thing to read. On an error, `answer` carries the message and the other fields are empty.
 */
public record ChatResponse(String answer, String sql, List<String> columns, List<Map<String, Object>> rows) {

    public static ChatResponse message(String text) {
        return new ChatResponse(text, null, List.of(), List.of());
    }
}
