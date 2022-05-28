/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.humanreadabletypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public final class HumanReadableDurationTests {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testParseNanoseconds() {
        assertStringsEqualToDuration(10, TimeUnit.NANOSECONDS, "10ns", "10 nanosecond", "10 nanoseconds");
    }

    @Test
    public void testParseMicroseconds() {
        assertStringsEqualToDuration(12, TimeUnit.MICROSECONDS, "12us", "12 microsecond", "12 microseconds");
    }

    @Test
    public void testParseMilliseconds() {
        assertStringsEqualToDuration(1, TimeUnit.MILLISECONDS, "1ms", "1 millisecond", "1 milliseconds");
    }

    @Test
    public void testParseSeconds() {
        assertStringsEqualToDuration(15, TimeUnit.SECONDS, "15s", "15 second", "15 seconds");
    }

    @Test
    public void testParseMinutes() {
        assertStringsEqualToDuration(8, TimeUnit.MINUTES, "8m", "8 minute", "8 minutes");
    }

    @Test
    public void testParseHours() {
        assertStringsEqualToDuration(7, TimeUnit.HOURS, "7h", "7 hour", "7 hours");
    }

    @Test
    public void testParseDays() {
        assertStringsEqualToDuration(14, TimeUnit.DAYS, "14d", "14 day", "14 days");
    }

    @Test
    public void testInvalidPattern() {
        assertThatThrownBy(() -> HumanReadableDuration.valueOf("One hour"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid duration: {duration=One hour}");
    }

    @Test
    public void testInvalidUnits() {
        assertThatThrownBy(() -> HumanReadableDuration.valueOf("10 weeks"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid duration. Wrong time unit: {duration=10 weeks}");
    }

    @Test
    public void testEquals() {
        assertThat(HumanReadableDuration.nanoseconds(1000)).isEqualTo(HumanReadableDuration.microseconds(1));
        assertThat(HumanReadableDuration.microseconds(1000)).isEqualTo(HumanReadableDuration.milliseconds(1));
        assertThat(HumanReadableDuration.milliseconds(1000)).isEqualTo(HumanReadableDuration.seconds(1));
        assertThat(HumanReadableDuration.seconds(60)).isEqualTo(HumanReadableDuration.minutes(1));
        assertThat(HumanReadableDuration.minutes(60)).isEqualTo(HumanReadableDuration.hours(1));
        assertThat(HumanReadableDuration.hours(24)).isEqualTo(HumanReadableDuration.days(1));
    }

    @Test
    public void testCompareTo() {
        assertThat(HumanReadableDuration.seconds(70).compareTo(HumanReadableDuration.minutes(1)))
                .isEqualTo(1);
        assertThat(HumanReadableDuration.seconds(86400).compareTo(HumanReadableDuration.days(1)))
                .isEqualTo(0);
        assertThat(HumanReadableDuration.microseconds(100).compareTo(HumanReadableDuration.seconds(1)))
                .isEqualTo(-1);
    }

    @Test
    public void testToString() {
        assertThat(HumanReadableDuration.valueOf("1 second").toString()).isEqualTo("1 second");
        assertThat(HumanReadableDuration.valueOf("1 seconds").toString()).isEqualTo("1 second");
        assertThat(HumanReadableDuration.valueOf("2 second").toString()).isEqualTo("2 seconds");
        assertThat(HumanReadableDuration.valueOf("2 seconds").toString()).isEqualTo("2 seconds");
    }

    @Test
    public void testToJson() throws Exception {
        assertThat(toJsonString(HumanReadableDuration.valueOf("1 second"))).isEqualTo("\"1 second\"");
        assertThat(toJsonString(HumanReadableDuration.valueOf("1 seconds"))).isEqualTo("\"1 second\"");
        assertThat(toJsonString(HumanReadableDuration.valueOf("2 second"))).isEqualTo("\"2 seconds\"");
        assertThat(toJsonString(HumanReadableDuration.valueOf("2 seconds"))).isEqualTo("\"2 seconds\"");
    }

    private static void assertStringsEqualToDuration(
            long expectedQuantity, TimeUnit expectedTimeUnit, String... durationStrings) {
        assertThat(Arrays.stream(durationStrings)
                        .map(HumanReadableDurationTests::parseFromString)
                        .collect(Collectors.toList()))
                .allMatch(duration ->
                        duration.getQuantity() == expectedQuantity && duration.getUnit() == expectedTimeUnit);
    }

    private static HumanReadableDuration parseFromString(String durationString) {
        try {
            return objectMapper.treeToValue(TextNode.valueOf(durationString), HumanReadableDuration.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse duration from string", e);
        }
    }

    private String toJsonString(HumanReadableDuration humanReadableDuration) throws JsonProcessingException {
        return objectMapper.writeValueAsString(humanReadableDuration);
    }
}
