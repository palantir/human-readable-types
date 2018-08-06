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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A human-readable type for binary quantities.
 * <p>
 * This class allows for parsing strings representing binary quantities into usable byte quantities. Strings should match
 * {@link HumanReadableByteString#BYTE_STRING_PATTERN} which represents the numeric binary quantity with a suffix
 * representing the {@link ByteUnit} to use for this byte string. Suffixes may be pluralized or not regardless of the
 * actual numeric quantity.
 * <p>
 * All {@code equal}, {@code compareTo} and {@code hashCode} implementations assume normalized values, i.e. they work
 * off of the total number of bytes. Therefore, {@code 1024 bytes} would be equivalent to {@code 1 kibibyte}.
 */
public final class HumanReadableByteString implements Comparable<HumanReadableByteString>, Serializable {
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = -1847327625440651894L;

    /**
     * The pattern for parsing byte strings.
     */
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

    /**
     * Obtains a new {@link HumanReadableByteString} using {@link ByteUnit#BYTE}.
     *
     * @param size the number of bytes
     */
    public static HumanReadableByteString bytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.BYTE);
    }

    /**
     * Obtains a new {@link HumanReadableByteString} using {@link ByteUnit#KiB}.
     *
     * @param size the number of kibibytes
     */
    public static HumanReadableByteString kibibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.KiB);
    }

    /**
     * Obtains a new {@link HumanReadableByteString} using {@link ByteUnit#MiB}.
     *
     * @param size the number of mebibytes
     */
    public static HumanReadableByteString mebibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.MiB);
    }

    /**
     * Obtains a new {@link HumanReadableByteString} using {@link ByteUnit#GiB}.
     *
     * @param size the number of gibibytes
     */
    public static HumanReadableByteString gibibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.GiB);
    }

    /**
     * Obtains a new {@link HumanReadableByteString} using {@link ByteUnit#TiB}.
     *
     * @param size the number of tebibytes
     */
    public static HumanReadableByteString tebibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.TiB);
    }

    /**
     * Obtains a new {@link HumanReadableByteString} using {@link ByteUnit#PiB}.
     *
     * @param size the number of pebibytes
     */
    public static HumanReadableByteString pebibytes(long size) {
        return new HumanReadableByteString(size, ByteUnit.PiB);
    }

    /**
     * Constructs a new {@link HumanReadableByteString} from the provided string representation.
     *
     * @param byteString the string representation of this byte string
     * @return the parsed {@link HumanReadableByteString}
     * @throws IllegalArgumentException if the provided byte string is invalid
     * @throws NumberFormatException if the provided size cannot be parsed
     */
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

    /**
     * The size of this byte string in {@link HumanReadableByteString.ByteUnit binary units}.
     */
    public long getSize() {
        return size;
    }

    /**
     * The {@link ByteUnit binary unit} of this byte string.
     */
    public ByteUnit getUnit() {
        return unit;
    }

    /**
     * The total number of bytes represented by this byte string.
     */
    public long toBytes() {
        return unit.toBytes(size);
    }

    /**
     * Compares this byte string to the specified {@code HumanReadableByteString}.
     * <p>
     * The comparison is based on the total number of bytes.
     * It is "consistent with equals", as defined by {@link Comparable}.
     *
     * @param otherByteString  the other byte string to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    @Override
    public int compareTo(HumanReadableByteString otherByteString) {
        if (unit == otherByteString.unit) {
            return Long.compare(size, otherByteString.size);
        }
        return Long.compare(toBytes(), otherByteString.toBytes());
    }

    /**
     * Checks if this byte string is equal to the specified {@code HumanReadableByteString}.
     * <p>
     * The comparison is based on the total length of the durations.
     *
     * @param otherByteString  the other byte string, null returns false
     * @return true if the other byte string is equal to this one
     */
    @Override
    public boolean equals(Object otherByteString) {
        if (this == otherByteString) {
            return true;
        }
        if ((otherByteString == null) || (getClass() != otherByteString.getClass())) {
            return false;
        }
        final HumanReadableByteString other = (HumanReadableByteString) otherByteString;
        if (unit == other.unit) {
            return size == other.size;
        }
        return toBytes() == other.toBytes();
    }

    /**
     * A hash code for this byte string.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        long bytes = toBytes();
        return (int) (bytes ^ (bytes >>> 32));
    }

    /**
     * A human-readable string representation of this byte string.
     *
     * @return a human-readable string representation of this byte string, not null
     */
    @Override
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (size == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(size) + ' ' + units;
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
