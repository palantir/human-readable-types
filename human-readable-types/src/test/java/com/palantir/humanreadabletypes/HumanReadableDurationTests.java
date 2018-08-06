/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.humanreadabletypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.Test;

public final class HumanReadableDurationTests {

    @Test
    public void testParseNanoseconds() {
        assetStringsEqualToDuration(10, TimeUnit.NANOSECONDS, "10ns", "10 nanosecond", "10 nanoseconds");
    }

    @Test
    public void testParseMicroseconds() {
        assetStringsEqualToDuration(12, TimeUnit.MICROSECONDS, "12us", "12 microsecond", "12 microseconds");
    }

    @Test
    public void testParseMilliseconds() {
        assetStringsEqualToDuration(1, TimeUnit.MILLISECONDS, "1ms", "1 millisecond", "1 milliseconds");
    }

    @Test
    public void testParseSeconds() {
        assetStringsEqualToDuration(15, TimeUnit.SECONDS, "15s", "15 second", "15 seconds");
    }

    @Test
    public void testParseMinutes() {
        assetStringsEqualToDuration(8, TimeUnit.MINUTES, "8m", "8 minute", "8 minutes");
    }

    @Test
    public void testParseHours() {
        assetStringsEqualToDuration(7, TimeUnit.HOURS, "7h", "7 hour", "7 hours");
    }

    @Test
    public void testParseDays() {
        assetStringsEqualToDuration(14, TimeUnit.DAYS, "14d", "14 day", "14 days");
    }

    @Test
    public void testInvalidPattern() {
        assertThatThrownBy(() -> HumanReadableDuration.valueOf("One hour"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid duration: One hour");
    }

    @Test
    public void testInvalidUnits() {
        assertThatThrownBy(() -> HumanReadableDuration.valueOf("10 weeks"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid duration: 10 weeks. Wrong time unit");
    }

    @Test
    public void testEquals() {
        assertThat(HumanReadableDuration.nanoseconds(1000).equals(HumanReadableDuration.microseconds(1))).isTrue();
        assertThat(HumanReadableDuration.microseconds(1000).equals(HumanReadableDuration.milliseconds(1))).isTrue();
        assertThat(HumanReadableDuration.milliseconds(1000).equals(HumanReadableDuration.seconds(1))).isTrue();
        assertThat(HumanReadableDuration.seconds(60).equals(HumanReadableDuration.minutes(1))).isTrue();
        assertThat(HumanReadableDuration.minutes(60).equals(HumanReadableDuration.hours(1))).isTrue();
        assertThat(HumanReadableDuration.hours(24).equals(HumanReadableDuration.days(1))).isTrue();
    }

    @Test
    public void testCompareTo() {
        assertThat(HumanReadableDuration.seconds(70).compareTo(HumanReadableDuration.minutes(1))).isEqualTo(1);
        assertThat(HumanReadableDuration.seconds(86400).compareTo(HumanReadableDuration.days(1))).isEqualTo(0);
        assertThat(HumanReadableDuration.microseconds(100).compareTo(HumanReadableDuration.seconds(1))).isEqualTo(-1);
    }

    @Test
    public void testToString() {
        assertThat(HumanReadableDuration.valueOf("1 second").toString()).isEqualTo("1 second");
        assertThat(HumanReadableDuration.valueOf("1 seconds").toString()).isEqualTo("1 second");
        assertThat(HumanReadableDuration.valueOf("2 second").toString()).isEqualTo("2 seconds");
        assertThat(HumanReadableDuration.valueOf("2 seconds").toString()).isEqualTo("2 seconds");
    }

    private static void assetStringsEqualToDuration(long expectedQuantity, TimeUnit expectedTimeUnit,
            String... durationStrings) {
        assertThat(Arrays.stream(durationStrings)
                .map(HumanReadableDuration::valueOf)
                .collect(Collectors.toList())
        ).allMatch(duration -> duration.getQuantity() == expectedQuantity && duration.getUnit() == expectedTimeUnit);
    }
}
