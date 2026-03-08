/*
 * Copyright (c)  2019 LinuxServer.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.linuxserver.fleet.v2.client.docker.nexus;

import io.linuxserver.fleet.v2.types.docker.DockerTag;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class NexusTagConverterTest {

    private NexusTagConverter converter;

    @Before
    public void setUp() {
        converter = new NexusTagConverter();
    }

    @Test
    public void shouldConvertTagName() {
        DockerTag tag = converter.convert("latest");

        assertThat(tag, is(notNullValue()));
        assertThat(tag.getName(), is(equalTo("latest")));
    }

    @Test
    public void shouldReturnZeroSizeForSimpleConversion() {
        DockerTag tag = converter.convert("v1.0.0");

        assertThat(tag.getSize(), is(equalTo(0L)));
    }

    @Test
    public void shouldReturnNonNullBuildDate() {
        DockerTag tag = converter.convert("latest");

        assertThat(tag.getBuildDate(), is(notNullValue()));
    }

    @Test
    public void shouldConvertWithDigest() {
        String digest = "sha256:abc123";
        long size = 1024L;
        String architecture = "amd64";

        DockerTag tag = converter.convertWithDigest("latest", size, digest, architecture);

        assertThat(tag, is(notNullValue()));
        assertThat(tag.getName(), is(equalTo("latest")));
        assertThat(tag.getSize(), is(equalTo(size)));
        assertThat(tag.getDigests(), is(notNullValue()));
        assertThat(tag.getDigests().size(), is(equalTo(1)));
        assertThat(tag.getDigests().get(0).getDigest(), is(equalTo(digest)));
        assertThat(tag.getDigests().get(0).getArchitecture(), is(equalTo(architecture)));
    }

    @Test
    public void shouldHandleNullDigest() {
        DockerTag tag = converter.convertWithDigest("latest", 1024L, null, "arm64");

        assertThat(tag.getDigests().size(), is(equalTo(1)));
        assertThat(tag.getDigests().get(0).getDigest(), is(equalTo(null)));
    }

    @Test
    public void shouldHandleEmptyDigest() {
        DockerTag tag = converter.convertWithDigest("latest", 1024L, "", "amd64");

        assertThat(tag.getDigests().size(), is(equalTo(1)));
        assertThat(tag.getDigests().get(0).getDigest(), is(equalTo("")));
    }

    @Test
    public void shouldReturnConverterClass() {
        assertThat(converter.getConverterClass(), is(equalTo(String.class)));
    }
}
