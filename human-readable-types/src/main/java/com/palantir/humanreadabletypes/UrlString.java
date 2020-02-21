/*
 * (c) Copyright 2020 Palantir Technologies Inc. All rights reserved.
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
import com.fasterxml.jackson.annotation.JsonValue;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An immutable {@link URL} whose {@link #equals}, {@link #toString}, and {@link #hashCode} are derived from the URL's
 * string representation (at instance construction time) rather than the {@link URL} class. For example, two {@link
 * UrlString}s are equal iff the strings from which they were {@link #valueOf constructed} are equal.
 * <p>
 * This class is intended to be used in configuration files where (1) URL syntax verification should be performed, (2)
 * host name resolution is not desired, and (3) equality/stability of the configuration file should be derived from the
 * configuration file syntax, rather than the dynamic behavior of the configuration values, cf. {@link URL#equals}.
 */
public final class UrlString {

    private final String stringUrl;
    private final URL url;

    private UrlString(String stringUrl) throws MalformedURLException {
        this.stringUrl = stringUrl;
        this.url = new URL(stringUrl);
    }

    /**
     * Constructs a new {@link UrlString} from the provided string representation, or throws an {@link
     * IllegalArgumentException} if the given string cannot be parsed as a {@link URL}.
     */
    @JsonCreator
    public static UrlString valueOf(String url) {
        try {
            return new UrlString(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Illegal URL", e);
        }
    }

    /**
     * Returns the {@link URL} derived from this {@link UrlString}.
     */
    public URL toUrl() {
        return url;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if ((other == null) || (getClass() != other.getClass())) {
            return false;
        }

        UrlString otherUri = (UrlString) other;
        return stringUrl.equals(otherUri.stringUrl);
    }

    @Override
    public int hashCode() {
        return stringUrl.hashCode();
    }

    @Override
    @JsonValue
    public String toString() {
        return stringUrl;
    }
}
