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
import com.palantir.humanreadabletypes.HumanReadableByteCount.ByteUnit;
import com.palantir.logsafe.exceptions.SafeNullPointerException;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public final class HumanReadableByteCountTests {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testParseByte() {
        assertStringsEqualToBytes(10L, "10", "10b", "10 byte", "10 bytes");
    }

    @Test
    public void testParseKibiBytes() {
        assertStringsEqualToBytes(1024L * 10L, "10k", "10kb", "10 kibibyte", "10 kibibytes");
    }

    @Test
    public void testParseMibiBytes() {
        assertStringsEqualToBytes((long) Math.pow(1024L, 2L) * 10L, "10m", "10mb", "10 mibibyte", "10 mibibytes");
    }

    @Test
    public void testParseMebiBytes() {
        assertStringsEqualToBytes((long) Math.pow(1024L, 2L) * 10L, "10m", "10mb", "10 mebibyte", "10 mebibytes");
    }

    @Test
    public void testParseGibiBytes() {
        assertStringsEqualToBytes((long) Math.pow(1024L, 3L) * 10L, "10g", "10gb", "10 gibibyte", "10 gibibytes");
    }

    @Test
    public void testParseTebiBytes() {
        assertStringsEqualToBytes((long) Math.pow(1024L, 4L) * 10L, "10t", "10tb", "10 tebibyte", "10 tebibytes");
    }

    @Test
    public void testParsePebiBytes() {
        assertStringsEqualToBytes((long) Math.pow(1024L, 5L) * 10L, "10p", "10pb", "10 pebibyte", "10 pebibytes");
    }

    @Test
    public void testInvalidString() {
        assertThatThrownBy(() -> HumanReadableByteCount.valueOf("Ten bytes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid byte string: {byteCount=Ten bytes}");
    }

    @Test
    public void testInvalidUnits() {
        assertThatThrownBy(() -> HumanReadableByteCount.valueOf("10 kilobytes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid byte string: 10 kilobytes. Wrong byte unit");
    }

    @Test
    public void testEquals() {
        assertThat(HumanReadableByteCount.bytes(1024)).isEqualTo(HumanReadableByteCount.kibibytes(1));
        assertThat(HumanReadableByteCount.bytes(1)).isEqualTo(HumanReadableByteCount.valueOf("1"));
        assertThat(HumanReadableByteCount.mebibytes(1024)).isEqualTo(HumanReadableByteCount.gibibytes(1));
        assertThat(HumanReadableByteCount.tebibytes(1024)).isEqualTo(HumanReadableByteCount.pebibytes(1));
        assertThat(HumanReadableByteCount.bytes(1024)).isNotEqualTo(HumanReadableByteCount.mebibytes(1));
    }

    @Test
    public void testCompareTo() {
        assertThat(HumanReadableByteCount.bytes(2048).compareTo(HumanReadableByteCount.kibibytes(1)))
                .isEqualTo(1);
        assertThat(HumanReadableByteCount.bytes(1024).compareTo(HumanReadableByteCount.kibibytes(1)))
                .isEqualTo(0);
        assertThat(HumanReadableByteCount.mebibytes(1).compareTo(HumanReadableByteCount.gibibytes(1)))
                .isEqualTo(-1);
    }

    @Test
    public void testToString() {
        assertThat(HumanReadableByteCount.valueOf("1 byte").toString()).isEqualTo("1 byte");
        assertThat(HumanReadableByteCount.valueOf("1 bytes").toString()).isEqualTo("1 byte");
        assertThat(HumanReadableByteCount.valueOf("2 byte").toString()).isEqualTo("2 bytes");
        assertThat(HumanReadableByteCount.valueOf("2 bytes").toString()).isEqualTo("2 bytes");
    }

    @Test
    public void testToJsonString() throws Exception {
        assertThat(toJsonString(HumanReadableByteCount.valueOf("1 byte"))).isEqualTo("\"1 byte\"");
        assertThat(toJsonString(HumanReadableByteCount.valueOf("1 bytes"))).isEqualTo("\"1 byte\"");
        assertThat(toJsonString(HumanReadableByteCount.valueOf("2 byte"))).isEqualTo("\"2 bytes\"");
        assertThat(toJsonString(HumanReadableByteCount.valueOf("2 bytes"))).isEqualTo("\"2 bytes\"");
    }

    @Test
    public void testMapNulls() {
        HumanReadableByteCount bytes = HumanReadableByteCount.valueOf("1 byte");
        assertThatThrownBy(() -> bytes.map(null)).isInstanceOf(SafeNullPointerException.class);
        assertThatThrownBy(() -> bytes.map((_val, _unit) -> null)).isInstanceOf(SafeNullPointerException.class);
    }

    @Test
    public void testMap() {
        HumanReadableByteCount bytes = HumanReadableByteCount.valueOf("5mb");
        assertThat(bytes.<ByteUnit>map((_quantity, unit) -> unit)).isEqualTo(ByteUnit.MiB);
        assertThat(bytes.<Long>map((quantity, _unit) -> quantity)).isEqualTo(5L);
    }

    private static void assertStringsEqualToBytes(long expectedBytes, String... byteCounts) {
        assertThat(Arrays.stream(byteCounts)
                        .map(HumanReadableByteCountTests::parseFromString)
                        .map(HumanReadableByteCount::toBytes)
                        .collect(Collectors.toList()))
                .allMatch(Predicate.isEqual(expectedBytes));
    }

    private static HumanReadableByteCount parseFromString(String durationString) {
        try {
            return objectMapper.treeToValue(TextNode.valueOf(durationString), HumanReadableByteCount.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse duration from string", e);
        }
    }

    private String toJsonString(HumanReadableByteCount humanReadableByteCount) throws JsonProcessingException {
        return objectMapper.writeValueAsString(humanReadableByteCount);
    }
}
