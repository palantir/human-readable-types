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

public final class StringBasedUrlTests {

    @Test
    public void canSerdeWithJackson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        StringBasedUrl url = mapper.readValue("\"http://foo\"", StringBasedUrl.class);
        assertThat(url).isEqualTo(StringBasedUrl.valueOf("http://foo"));
        assertThat(mapper.writeValueAsString(url)).isEqualTo("\"http://foo\"");
    }

    @Test
    public void cannotConstructIllegalUri() {
        assertThatThrownBy(() -> StringBasedUrl.valueOf(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StringBasedUrl.valueOf("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StringBasedUrl.valueOf("foo")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StringBasedUrl.valueOf("http//foo")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StringBasedUrl.valueOf("://foo")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StringBasedUrl.valueOf("news:12345667123%asdghfh@info.cern.ch"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void equalityIsBasedOnStringEquality() {
        StringBasedUrl uri1 = StringBasedUrl.valueOf("http://localhost");
        StringBasedUrl uri2 = StringBasedUrl.valueOf("http://127.0.0.1");
        assertThat(uri1).isNotEqualTo(uri2);
        assertThat(uri1.toUrl()).isEqualTo(uri2.toUrl());
    }

    @Test
    public void isNotEqualToRawString() {
        assertThat(StringBasedUrl.valueOf("http://localhost")).isNotEqualTo("http://localhost");
    }

    @Test
    public void hashCodeIsBasedOnString() {
        StringBasedUrl uri = StringBasedUrl.valueOf("http://foo");
        assertThat(uri.toUrl().hashCode()).isNotEqualTo("http://foo".hashCode());
        assertThat(uri.hashCode()).isEqualTo("http://foo".hashCode());
    }

    @Test
    public void toStringIsIdentity() {
        StringBasedUrl uri = StringBasedUrl.valueOf("http://foo");
        assertThat(uri.toString()).isEqualTo("http://foo");
    }
}
