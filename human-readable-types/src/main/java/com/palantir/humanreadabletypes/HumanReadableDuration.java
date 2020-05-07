/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.humanreadabletypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A human-readable type for durations.
 * <p>
 * This class allows for parsing strings representing durations into usable time quantities. Strings should match
 * {@link HumanReadableDuration#DURATION_PATTERN} which represents the numeric duration value with a suffix representing
 * the {@link TimeUnit} to use for this duration. Suffixes may be pluralized or not regardless of the actual numeric
 * quantity.
 * <p>
 * All {@code equal}, {@code compareTo} and {@code hashCode} implementations assume normalized values, i.e. they work
 * off of the total length of the duration. Therefore, {@code 60 seconds} would be equivalent to {@code 1 minute}.
 */
public final class HumanReadableDuration implements Comparable<HumanReadableDuration>, Serializable {
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 4215573187492209716L;

    /**
     * The pattern for parsing duration strings.
     */
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");

    private static final Map<String, TimeUnit> SUFFIXES = createSuffixes();

    private static Map<String, TimeUnit> createSuffixes() {
        Map<String, TimeUnit> suffixes = new HashMap<>();
        suffixes.put("ns", TimeUnit.NANOSECONDS);
        suffixes.put("nanosecond", TimeUnit.NANOSECONDS);
        suffixes.put("nanoseconds", TimeUnit.NANOSECONDS);
        suffixes.put("us", TimeUnit.MICROSECONDS);
        suffixes.put("microsecond", TimeUnit.MICROSECONDS);
        suffixes.put("microseconds", TimeUnit.MICROSECONDS);
        suffixes.put("ms", TimeUnit.MILLISECONDS);
        suffixes.put("millisecond", TimeUnit.MILLISECONDS);
        suffixes.put("milliseconds", TimeUnit.MILLISECONDS);
        suffixes.put("s", TimeUnit.SECONDS);
        suffixes.put("second", TimeUnit.SECONDS);
        suffixes.put("seconds", TimeUnit.SECONDS);
        suffixes.put("m", TimeUnit.MINUTES);
        suffixes.put("minute", TimeUnit.MINUTES);
        suffixes.put("minutes", TimeUnit.MINUTES);
        suffixes.put("h", TimeUnit.HOURS);
        suffixes.put("hour", TimeUnit.HOURS);
        suffixes.put("hours", TimeUnit.HOURS);
        suffixes.put("d", TimeUnit.DAYS);
        suffixes.put("day", TimeUnit.DAYS);
        suffixes.put("days", TimeUnit.DAYS);
        return suffixes;
    }

    private final long count;
    private final TimeUnit unit;

    /**
     * Obtains a new {@link HumanReadableDuration} using {@link TimeUnit#NANOSECONDS}.
     *
     * @param count the number of nanoseconds
     */
    public static HumanReadableDuration nanoseconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.NANOSECONDS);
    }

    /**
     * Obtains a new {@link HumanReadableDuration} using {@link TimeUnit#MICROSECONDS}.
     *
     * @param count the number of microseconds
     */
    public static HumanReadableDuration microseconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.MICROSECONDS);
    }

    /**
     * Obtains a new {@link HumanReadableDuration} using {@link TimeUnit#MILLISECONDS}.
     *
     * @param count the number of milliseconds
     */
    public static HumanReadableDuration milliseconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.MILLISECONDS);
    }

    /**
     * Obtains a new {@link HumanReadableDuration} using {@link TimeUnit#SECONDS}.
     *
     * @param count the number of seconds
     */
    public static HumanReadableDuration seconds(long count) {
        return new HumanReadableDuration(count, TimeUnit.SECONDS);
    }

    /**
     * Obtains a new {@link HumanReadableDuration} using {@link TimeUnit#MINUTES}.
     *
     * @param count the number of minutes
     */
    public static HumanReadableDuration minutes(long count) {
        return new HumanReadableDuration(count, TimeUnit.MINUTES);
    }

    /**
     * Obtains a new {@link HumanReadableDuration} using {@link TimeUnit#HOURS}.
     *
     * @param count the number of hours
     */
    public static HumanReadableDuration hours(long count) {
        return new HumanReadableDuration(count, TimeUnit.HOURS);
    }

    /**
     * Obtains a new {@link HumanReadableDuration} using {@link TimeUnit#DAYS}.
     *
     * @param count the number of days
     */
    public static HumanReadableDuration days(long count) {
        return new HumanReadableDuration(count, TimeUnit.DAYS);
    }

    /**
     * Constructs a new {@link HumanReadableByteCount} from the provided string representation.
     *
     * @param duration the string HumanReadableDuration of this duration
     * @return the parsed {@link HumanReadableDuration}
     * @throws SafeIllegalArgumentException if the provided duration is invalid
     */
    @JsonCreator
    public static HumanReadableDuration valueOf(String duration) {
        final Matcher matcher = DURATION_PATTERN.matcher(duration);
        Preconditions.checkArgument(matcher.matches(), "Invalid duration", SafeArg.of("duration", duration));

        final long count = Long.parseLong(matcher.group(1));
        final TimeUnit unit = SUFFIXES.get(matcher.group(2));
        if (unit == null) {
            throw new SafeIllegalArgumentException(
                    "Invalid duration. Wrong time unit",
                    SafeArg.of("duration", duration));
        }

        return new HumanReadableDuration(count, unit);
    }

    private HumanReadableDuration(long count, TimeUnit unit) {
        this.count = count;
        this.unit = Preconditions.checkNotNull(unit, "unit must not be null");
    }

    /**
     * The quantity of this duration in {@link TimeUnit time units}.
     */
    public long getQuantity() {
        return count;
    }

    /**
     * The {@link TimeUnit time unit} of this duration.
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Converts this duration to the total length in nanoseconds expressed as a {@code long}.
     */
    public long toNanoseconds() {
        return TimeUnit.NANOSECONDS.convert(count, unit);
    }

    /**
     * Converts this duration to the total length in microseconds expressed as a {@code long}.
     */
    public long toMicroseconds() {
        return TimeUnit.MICROSECONDS.convert(count, unit);
    }

    /**
     * Converts this duration to the total length in milliseconds expressed as a {@code long}.
     */
    public long toMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(count, unit);
    }

    /**
     * Converts this duration to the total length in seconds expressed as a {@code long}.
     */
    public long toSeconds() {
        return TimeUnit.SECONDS.convert(count, unit);
    }

    /**
     * Converts this duration to the total length in minutes expressed as a {@code long}.
     */
    public long toMinutes() {
        return TimeUnit.MINUTES.convert(count, unit);
    }

    /**
     * Converts this duration to the total length in hours expressed as a {@code long}.
     */
    public long toHours() {
        return TimeUnit.HOURS.convert(count, unit);
    }

    /**
     * Converts this duration to the total length in days expressed as a {@code long}.
     */
    public long toDays() {
        return TimeUnit.DAYS.convert(count, unit);
    }

    /**
     * Converts this duration to an equivalent {@link Duration}.
     */
    public Duration toJavaDuration() {
        return Duration.of(count, chronoUnit(unit));
    }

    /**
     * Converts a {@code TimeUnit} to a {@code ChronoUnit}.
     * <p>
     * This handles the seven units declared in {@code TimeUnit}.
     *
     * @implNote This method can be removed in JDK9
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8141452">JDK-8141452</a>
     * @param unit the unit to convert, not null
     * @return the converted unit, not null
     */
    private static ChronoUnit chronoUnit(TimeUnit unit) {
        Preconditions.checkNotNull(unit, "unit");
        switch (unit) {
            case NANOSECONDS:
                return ChronoUnit.NANOS;
            case MICROSECONDS:
                return ChronoUnit.MICROS;
            case MILLISECONDS:
                return ChronoUnit.MILLIS;
            case SECONDS:
                return ChronoUnit.SECONDS;
            case MINUTES:
                return ChronoUnit.MINUTES;
            case HOURS:
                return ChronoUnit.HOURS;
            case DAYS:
                return ChronoUnit.DAYS;
        }
        throw new SafeIllegalArgumentException("Unknown TimeUnit constant");
    }

    /**
     * Compares this duration to the specified {@code HumanReadableDuration}.
     * <p>
     * The comparison is based on the total length of the durations.
     * It is "consistent with equals", as defined by {@link Comparable}.
     *
     * @param otherDuration  the other duration to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    @Override
    public int compareTo(HumanReadableDuration otherDuration) {
        if (unit == otherDuration.unit) {
            return Long.compare(count, otherDuration.count);
        }

        return Long.compare(toNanoseconds(), otherDuration.toNanoseconds());
    }

    /**
     * Checks if this duration is equal to the specified {@code HumanReadableDuration}.
     * <p>
     * The comparison is based on the total length of the durations.
     *
     * @param otherDuration  the other duration, null returns false
     * @return true if the other duration is equal to this one
     */
    @Override
    public boolean equals(Object otherDuration) {
        if (this == otherDuration) {
            return true;
        }
        if ((otherDuration == null) || (getClass() != otherDuration.getClass())) {
            return false;
        }
        final HumanReadableDuration duration = (HumanReadableDuration) otherDuration;
        if (unit == duration.unit) {
            return count == duration.count;
        }
        return toJavaDuration().equals(duration.toJavaDuration());

    }

    /**
     * A hash code for this duration.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return toJavaDuration().hashCode();
    }

    /**
     * A human-readable string representation of this duration.
     *
     * @return a human-readable string representation of this duration, not null
     */
    @Override
    @JsonValue
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (count == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }
}
