package com.mohaemukzip.mohaemukzip_be.global.util;

public class TimeFormatter {
    public static String formatDuration(String time) {
        if (time == null || time.isEmpty()) {
            return "00:00";
        }
        // DB에 이미 "MM:SS" 또는 "HH:MM:SS" 형태로 저장되어 있다고 가정
        String[] parts = time.split(":");
        try {
            if (parts.length == 3) {  // HH:MM:SS → 총 MM:SS
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]) + (hours * 60);
                int seconds = Integer.parseInt(parts[2]);
                if (hours < 0 || minutes < 0 || seconds < 0 || seconds >= 60) {
                    return "00:00";
                }

                int totalMinutes = hours * 60 + minutes;
                return String.format("%02d:%02d", totalMinutes, seconds);
            } else if (parts.length == 2) {  // MM:SS → 그대로
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);

                if (minutes < 0 || seconds < 0 || seconds >= 60) {
                    return "00:00";
                }

                return String.format("%02d:%02d", minutes, seconds);
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            // 잘못된 형식 무시
        }
        return "00:00";  // 안전 기본값
    }
}
