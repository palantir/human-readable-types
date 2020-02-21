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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public final class UrlStringTests {

    @Test
    public void canSerdeWithJackson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        UrlString url = mapper.readValue("\"http://foo\"", UrlString.class);
        assertThat(url).isEqualTo(UrlString.valueOf("http://foo"));
        assertThat(mapper.writeValueAsString(url)).isEqualTo("\"http://foo\"");
    }

    @Test
    public void cannotConstructIllegalUri() {
        assertThatThrownBy(() -> UrlString.valueOf(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> UrlString.valueOf("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UrlString.valueOf("foo")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UrlString.valueOf("http//foo")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UrlString.valueOf("://foo")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> UrlString.valueOf("news:12345667123%asdghfh@info.cern.ch"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void equalityIsBasedOnStringEquality() {
        UrlString uri1 = UrlString.valueOf("http://localhost");
        UrlString uri2 = UrlString.valueOf("http://127.0.0.1");
        assertThat(uri1).isNotEqualTo(uri2);
        assertThat(uri1.toUrl()).isEqualTo(uri2.toUrl());
    }

    @Test
    public void isNotEqualToRawString() {
        assertThat(UrlString.valueOf("http://localhost")).isNotEqualTo("http://localhost");
    }

    @Test
    public void hashCodeIsBasedOnString() {
        UrlString uri = UrlString.valueOf("http://foo");
        assertThat(uri.toUrl().hashCode()).isNotEqualTo("http://foo".hashCode());
        assertThat(uri.hashCode()).isEqualTo("http://foo".hashCode());
    }

    @Test
    public void toStringIsIdentity() {
        UrlString uri = UrlString.valueOf("http://foo");
        assertThat(uri.toString()).isEqualTo("http://foo");
    }
}
