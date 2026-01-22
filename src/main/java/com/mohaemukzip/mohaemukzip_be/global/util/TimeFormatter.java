package com.mohaemukzip.mohaemukzip_be.global.util;

public class TimeFormatter {
    public static String formatDuration(String time) {
        if (time == null || time.isEmpty()) {
            return "00:00";
        }
        // DB에 이미 "MM:SS" 또는 "HH:MM:SS" 형태로 저장되어 있다고 가정
        return time;
    }
}
