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

import io.linuxserver.fleet.nexus.model.NexusV2TagList;
import io.linuxserver.fleet.v2.types.docker.DockerImage;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class NexusImageConverterTest {

    private NexusImageConverter converter;

    @Before
    public void setUp() {
        converter = new NexusImageConverter();
    }

    @Test
    public void shouldConvertFullImageName() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("library/alpine");
        tagList.setTags(Arrays.asList("latest"));

        DockerImage image = converter.convert(tagList);

        assertThat(image, is(notNullValue()));
        assertThat(image.getName(), is(equalTo("alpine")));
        assertThat(image.getRepository(), is(equalTo("library")));
    }

    @Test
    public void shouldConvertImageWithoutNamespace() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("alpine");
        tagList.setTags(Arrays.asList("latest"));

        DockerImage image = converter.convert(tagList);

        assertThat(image, is(notNullValue()));
        assertThat(image.getName(), is(equalTo("alpine")));
        assertThat(image.getRepository(), is(equalTo("")));
    }

    @Test
    public void shouldHandleNullInput() {
        DockerImage image = converter.convert(null);
        assertThat(image, is(nullValue()));
    }

    @Test
    public void shouldExtractCorrectImageName() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("myorg/myapp/v1.0");
        tagList.setTags(Arrays.asList("latest"));

        DockerImage image = converter.convert(tagList);

        assertThat(image.getName(), is(equalTo("v1.0")));
        assertThat(image.getRepository(), is(equalTo("myorg/myapp")));
    }

    @Test
    public void shouldReturnNullDescription() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("library/alpine");
        tagList.setTags(Arrays.asList("latest"));

        DockerImage image = converter.convert(tagList);

        assertThat(image.getDescription(), is(nullValue()));
    }

    @Test
    public void shouldReturnZeroStarAndPullCount() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("library/alpine");
        tagList.setTags(Arrays.asList("latest"));

        DockerImage image = converter.convert(tagList);

        assertThat(image.getStarCount(), is(equalTo(0)));
        assertThat(image.getPullCount(), is(equalTo(0L)));
    }

    @Test
    public void shouldReturnNonNullBuildDate() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("library/alpine");
        tagList.setTags(Arrays.asList("latest"));

        DockerImage image = converter.convert(tagList);

        assertThat(image.getBuildDate(), is(notNullValue()));
    }
}
