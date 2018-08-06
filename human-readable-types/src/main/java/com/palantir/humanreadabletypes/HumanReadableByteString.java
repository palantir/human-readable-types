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

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HumanReadableByteString implements Comparable<HumanReadableByteString> {
    private static final Pattern BYTE_STRING_PATTERN = Pattern.compile("([0-9]+)\\s?([a-rt-z]+)?s?");

    private static final Map<String, ByteUnit> SUFFIXES = createSuffixes();

    private static Map<String, ByteUnit> createSuffixes() {
        Map<String, ByteUnit> suffixes = new HashMap<>();
        suffixes.put("b", ByteUnit.BYTE);
        suffixes.put("byte", ByteUnit.BYTE);
        suffixes.put("k", ByteUnit.KiB);
        suffixes.put("kb", ByteUnit.KiB);
        suffixes.put("kibibyte", ByteUnit.KiB);
        suffixes.put("m", ByteUnit.MiB);
        suffixes.put("mb", ByteUnit.MiB);
        suffixes.put("mibibyte", ByteUnit.MiB);
        suffixes.put("g", ByteUnit.GiB);
        suffixes.put("gb", ByteUnit.GiB);
        suffixes.put("gibibyte", ByteUnit.GiB);
        suffixes.put("t", ByteUnit.TiB);
        suffixes.put("tb", ByteUnit.TiB);
        suffixes.put("tebibyte", ByteUnit.TiB);
        suffixes.put("p", ByteUnit.PiB);
        suffixes.put("pb", ByteUnit.PiB);
        suffixes.put("pebibyte", ByteUnit.PiB);
        return suffixes;
    }

    public static HumanReadableByteString bytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.BYTE);
    }

    public static HumanReadableByteString kibibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.KiB);
    }

    public static HumanReadableByteString mebibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.MiB);
    }

    public static HumanReadableByteString gibibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.GiB);
    }

    public static HumanReadableByteString tebibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.TiB);
    }

    public static HumanReadableByteString pebibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.PiB);
    }

    @JsonCreator
    public static HumanReadableByteString valueOf(String byteString) {
        String lower = byteString.toLowerCase(Locale.ROOT).trim();

        try {
            Matcher matcher = BYTE_STRING_PATTERN.matcher(lower);

            Preconditions.checkArgument(matcher.matches(), "Invalid byte string: %s", byteString);

            long size = Long.parseLong(matcher.group(1));
            String suffix = matcher.group(2);

            if (suffix != null && !SUFFIXES.containsKey(suffix)) {
                throw new IllegalArgumentException("Invalid byte string: " + byteString + ". Wrong byte unit");
            }

            return new HumanReadableByteString(size, suffix != null ? SUFFIXES.get(suffix) : ByteUnit.BYTE);

        } catch (NumberFormatException e) {
            String byteError = "Size must be specified as bytes (b), "
                    + "kibibytes (k), mebibytes (m), gibibytes (g), tebibytes (t), or pebibytes(p). "
                    + "E.g. 50b, 100k, or 250m.";

            throw new NumberFormatException(byteError + "\n" + e.getMessage());
        }
    }

    private final long size;
    private final ByteUnit unit;

    private HumanReadableByteString(long size, ByteUnit unit) {
        this.size = size;
        this.unit = Preconditions.checkNotNull(unit, "unit must not be null");
    }

    /** The size of this byte string in {@link HumanReadableByteString.ByteUnit}s. */
    public long getSize() {
        return size;
    }

    /** The binary unit for this byte string. */
    public ByteUnit getUnit() {
        return unit;
    }

    /** The number of bytes represented by this byte string. */
    public long toBytes() {
        return unit.toBytes(size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final HumanReadableByteString other = (HumanReadableByteString) obj;
        if (unit == other.unit) {
            return size == other.size;
        }
        return toBytes() == other.toBytes();
    }

    @Override
    public int hashCode() {
        long bytes = toBytes();
        return (int) (bytes ^ (bytes >>> 32));
    }

    @Override
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (size == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(size) + ' ' + units;
    }

    @Override
    public int compareTo(HumanReadableByteString other) {
        if (unit == other.unit) {
            return Long.compare(size, other.size);
        }
        return Long.compare(toBytes(), other.toBytes());
    }

    enum ByteUnit {
        BYTE(1, "bytes"),
        KiB(1024L, "kibibytes"),
        MiB((long) Math.pow(1024L, 2L), "mebibytes"),
        GiB((long) Math.pow(1024L, 3L), "gibibytes"),
        TiB((long) Math.pow(1024L, 4L), "tebibytes"),
        PiB((long) Math.pow(1024L, 5L), "pebibytes");

        private final long multiplier;
        private final String suffix;

        ByteUnit(long multiplier, String suffix) {
            this.multiplier = multiplier;
            this.suffix = suffix;
        }

        public long toBytes(long sizeValue) {
            Preconditions.checkArgument(sizeValue >= 0, "Negative size value. Size must be positive: %s", sizeValue);
            return sizeValue * multiplier;
        }

        @Override
        public String toString() {
            return suffix;
        }
    }
}
