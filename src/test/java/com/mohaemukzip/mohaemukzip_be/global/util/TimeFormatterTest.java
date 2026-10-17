package com.mohaemukzip.mohaemukzip_be.global.util;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class TimeFormatterTest {
    @ParameterizedTest
    @CsvSource({"01:10:30,70:30", "01:00:00,60:00", "00:10:30,10:30", "02:59:59,179:59", "70:30,70:30", "00:00,00:00", "2147483647:00:00,128849018820:00", "bad,00:00", "01:60:00,00:00", "-1:10:00,00:00", "10:60,00:00", "10:-1,00:00"})
    void convertsDuration(String input, String expected) {
        assertThat(TimeFormatter.formatDuration(input)).isEqualTo(expected);
    }
    @Test void absentDurationUsesExistingDefault() {
        assertThat(TimeFormatter.formatDuration(null)).isEqualTo("00:00");
        assertThat(TimeFormatter.formatDuration("")).isEqualTo("00:00");
    }
}
