package ru.skillbox.vacancybot.service.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Расчёт времени до нотификации")
class NotificationTimeCalculatorTest {

    private NotificationTimeCalculator calculatorAt(String utcInstant) {
        return new NotificationTimeCalculator(
                Clock.fixed(Instant.parse(utcInstant), ZoneOffset.UTC));
    }

    @Test
    @DisplayName("время сегодня ещё не наступило — ждём сегодня")
    void waitsUntilTodayWhenTimeIsAhead() {
        Duration delay = calculatorAt("2025-01-01T10:00:00Z")
                .delayUntilNext(LocalTime.of(15, 0), ZoneOffset.UTC);

        assertThat(delay).isEqualTo(Duration.ofHours(5));
    }

    @Test
    @DisplayName("время сегодня уже прошло — ждём до завтра")
    void waitsUntilTomorrowWhenTimePassed() {
        Duration delay = calculatorAt("2025-01-01T10:00:00Z")
                .delayUntilNext(LocalTime.of(7, 0), ZoneOffset.UTC);

        assertThat(delay).isEqualTo(Duration.ofHours(21));
    }

    @Test
    @DisplayName("время ровно сейчас — переносится на завтра")
    void waitsFullDayWhenTimeIsNow() {
        Duration delay = calculatorAt("2025-01-01T10:00:00Z")
                .delayUntilNext(LocalTime.of(10, 0), ZoneOffset.UTC);

        assertThat(delay).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("часовой пояс пользователя учитывается")
    void respectsUserZone() {
        Duration delay = calculatorAt("2025-01-01T10:00:00Z")
                .delayUntilNext(LocalTime.of(15, 0), ZoneOffset.ofHours(3));

        assertThat(delay).isEqualTo(Duration.ofHours(2));
    }

    @Test
    @DisplayName("отрицательное смещение учитывается")
    void respectsNegativeOffset() {
        Duration delay = calculatorAt("2025-01-01T10:00:00Z")
                .delayUntilNext(LocalTime.of(7, 0), ZoneOffset.ofHours(-5));

        assertThat(delay).isEqualTo(Duration.ofHours(2));
    }

    @Test
    @DisplayName("учитываются минуты")
    void respectsMinutes() {
        Duration delay = calculatorAt("2025-01-01T10:00:00Z")
                .delayUntilNext(LocalTime.of(10, 30), ZoneOffset.UTC);

        assertThat(delay).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("задержка всегда положительная и не больше суток")
    void delayIsWithinDay() {
        NotificationTimeCalculator calculator = calculatorAt("2025-01-01T10:00:00Z");

        for (int hour = 0; hour < 24; hour++) {
            Duration delay = calculator.delayUntilNext(LocalTime.of(hour, 0), ZoneOffset.UTC);

            assertThat(delay).isPositive();
            assertThat(delay).isLessThanOrEqualTo(Duration.ofHours(24));
        }
    }
}