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

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.Test;

public final class HumanReadableByteCountTests {

    @Test
    public void testParseByte() {
        assetStringsEqualToBytes(10L, "10", "10b", "10 byte", "10 bytes");
    }

    @Test
    public void testParseKibiBytes() {
        assetStringsEqualToBytes(1024L * 10L, "10k", "10kb", "10 kibibyte", "10 kibibytes");
    }

    @Test
    public void testParseMibiBytes() {
        assetStringsEqualToBytes((long) Math.pow(1024L, 2L) * 10L, "10m", "10mb", "10 mibibyte", "10 mibibytes");
    }

    @Test
    public void testParseMebiBytes() {
        assetStringsEqualToBytes((long) Math.pow(1024L, 2L) * 10L, "10m", "10mb", "10 mebibyte", "10 mebibytes");
    }

    @Test
    public void testParseGibiBytes() {
        assetStringsEqualToBytes((long) Math.pow(1024L, 3L) * 10L, "10g", "10gb", "10 gibibyte", "10 gibibytes");
    }

    @Test
    public void testParseTebiBytes() {
        assetStringsEqualToBytes((long) Math.pow(1024L, 4L) * 10L, "10t", "10tb", "10 tebibyte", "10 tebibytes");
    }

    @Test
    public void testParsePebiBytes() {
        assetStringsEqualToBytes((long) Math.pow(1024L, 5L) * 10L, "10p", "10pb", "10 pebibyte", "10 pebibytes");
    }

    @Test
    public void testInvalidString() {
        assertThatThrownBy(() -> HumanReadableByteCount.valueOf("Ten bytes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid byte string: Ten bytes");
    }

    @Test
    public void testInvalidUnits() {
        assertThatThrownBy(() -> HumanReadableByteCount.valueOf("10 kilobytes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid byte string: 10 kilobytes. Wrong byte unit");
    }

    @Test
    public void testEquals() {
        assertThat(HumanReadableByteCount.bytes(1024).equals(HumanReadableByteCount.kibibytes(1))).isTrue();
        assertThat(HumanReadableByteCount.bytes(1).equals(HumanReadableByteCount.valueOf("1"))).isTrue();
        assertThat(HumanReadableByteCount.mebibytes(1024).equals(HumanReadableByteCount.gibibytes(1))).isTrue();
        assertThat(HumanReadableByteCount.tebibytes(1024).equals(HumanReadableByteCount.pebibytes(1))).isTrue();
        assertThat(HumanReadableByteCount.bytes(1024).equals(HumanReadableByteCount.mebibytes(1))).isFalse();
    }

    @Test
    public void testCompareTo() {
        assertThat(HumanReadableByteCount.bytes(2048).compareTo(HumanReadableByteCount.kibibytes(1))).isEqualTo(1);
        assertThat(HumanReadableByteCount.bytes(1024).compareTo(HumanReadableByteCount.kibibytes(1))).isEqualTo(0);
        assertThat(HumanReadableByteCount.mebibytes(1).compareTo(HumanReadableByteCount.gibibytes(1))).isEqualTo(-1);
    }

    @Test
    public void testToString() {
        assertThat(HumanReadableByteCount.valueOf("1 byte").toString()).isEqualTo("1 byte");
        assertThat(HumanReadableByteCount.valueOf("1 bytes").toString()).isEqualTo("1 byte");
        assertThat(HumanReadableByteCount.valueOf("2 byte").toString()).isEqualTo("2 bytes");
        assertThat(HumanReadableByteCount.valueOf("2 bytes").toString()).isEqualTo("2 bytes");
    }

    private static void assetStringsEqualToBytes(long expectedBytes, String... byteCounts) {
        assertThat(Arrays.stream(byteCounts)
                .map(HumanReadableByteCount::valueOf)
                .map(HumanReadableByteCount::toBytes)
                .collect(Collectors.toList())
        ).allMatch(Predicate.isEqual(expectedBytes));
    }
}
