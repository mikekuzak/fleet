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

import io.linuxserver.fleet.nexus.model.NexusV2Catalog;
import io.linuxserver.fleet.nexus.model.NexusV2TagList;
import io.linuxserver.fleet.v2.client.rest.RestClient;
import io.linuxserver.fleet.v2.client.rest.RestResponse;
import io.linuxserver.fleet.v2.types.docker.DockerImage;
import io.linuxserver.fleet.v2.types.docker.DockerTag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NexusApiClientTest {

    private RestClient restClient;
    private NexusApiClient nexusApiClient;

    private static final String NEXUS_URL = "https://nexus.example.com";
    private static final String DOCKER_REPO = "docker-hosted";
    private static final String PATH_PREFIX = null;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";

    @Before
    public void setUp() {
        restClient = mock(RestClient.class);
        nexusApiClient = new NexusApiClient(restClient, NEXUS_URL, DOCKER_REPO, PATH_PREFIX, USERNAME, PASSWORD);
    }

    @Test
    public void shouldFetchImageFromApi() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("library/alpine");
        tagList.setTags(Arrays.asList("latest", "3.14"));

        RestResponse<NexusV2TagList> mockedResponse = createMockResponse(tagList, 200);
        when(restClient.executeGet(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2TagList.class)
        )).thenReturn(mockedResponse);

        DockerImage image = nexusApiClient.fetchImage("library/alpine");

        assertThat(image, is(notNullValue()));
        assertThat(image.getName(), is(equalTo("alpine")));
        assertThat(image.getRepository(), is(equalTo("library")));
    }

    @Test
    public void shouldFetchAllImagesFromApi() {
        NexusV2Catalog catalog = new NexusV2Catalog();
        catalog.setRepositories(Arrays.asList("library/alpine", "library/nginx"));

        NexusV2TagList alpineTags = new NexusV2TagList();
        alpineTags.setName("library/alpine");
        alpineTags.setTags(Arrays.asList("latest"));

        NexusV2TagList nginxTags = new NexusV2TagList();
        nginxTags.setName("library/nginx");
        nginxTags.setTags(Arrays.asList("latest"));

        RestResponse<NexusV2Catalog> catalogResponse = createMockResponse(catalog, 200);
        RestResponse<NexusV2TagList> alpineResponse = createMockResponse(alpineTags, 200);
        RestResponse<NexusV2TagList> nginxResponse = createMockResponse(nginxTags, 200);

        when(restClient.executeGet(
            ArgumentMatchers.contains("_catalog"),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2Catalog.class)
        )).thenReturn(catalogResponse);

        when(restClient.executeGet(
            ArgumentMatchers.contains("library/alpine"),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2TagList.class)
        )).thenReturn(alpineResponse);

        when(restClient.executeGet(
            ArgumentMatchers.contains("library/nginx"),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2TagList.class)
        )).thenReturn(nginxResponse);

        List<DockerImage> images = nexusApiClient.fetchAllImages("library");

        assertThat(images, is(notNullValue()));
        assertThat(images.size(), is(equalTo(2)));
    }

    @Test
    public void shouldFetchImageTags() {
        NexusV2TagList tagList = new NexusV2TagList();
        tagList.setName("library/alpine");
        tagList.setTags(Arrays.asList("latest", "3.14", "3.15"));

        RestResponse<NexusV2TagList> mockedResponse = createMockResponse(tagList, 200);
        when(restClient.executeGet(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2TagList.class)
        )).thenReturn(mockedResponse);

        List<DockerTag> tags = nexusApiClient.fetchImageTags("library/alpine");

        assertThat(tags, is(notNullValue()));
        assertThat(tags.size(), is(equalTo(3)));
        assertThat(tags.stream().anyMatch(t -> t.getName().equals("latest")), is(true));
        assertThat(tags.stream().anyMatch(t -> t.getName().equals("3.14")), is(true));
        assertThat(tags.stream().anyMatch(t -> t.getName().equals("3.15")), is(true));
    }

    @Test
    public void shouldReturnNullWhenImageNotFound() {
        RestResponse<NexusV2TagList> mockedResponse = createMockResponse(null, 404);
        when(restClient.executeGet(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2TagList.class)
        )).thenReturn(mockedResponse);

        DockerImage image = nexusApiClient.fetchImage("nonexistent/image");

        assertThat(image, is(notNullValue()));
    }

    @Test
    public void shouldBuildCorrectBaseUrl() {
        String expectedUrl = NEXUS_URL + "/repository/" + DOCKER_REPO + "/v2/";
        
        assertThat(nexusApiClient.fetchImage("test/image"), is(notNullValue()));
    }

    @Test
    public void shouldValidateRepository() {
        NexusV2Catalog catalog = new NexusV2Catalog();
        catalog.setRepositories(Arrays.asList("library/alpine"));

        RestResponse<NexusV2Catalog> mockedResponse = createMockResponse(catalog, 200);
        when(restClient.executeGet(
            ArgumentMatchers.contains("_catalog"),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2Catalog.class)
        )).thenReturn(mockedResponse);

        boolean isValid = nexusApiClient.isRepositoryValid("library");

        assertThat(isValid, is(true));
    }

    @Test
    public void shouldReturnInvalidForEmptyRepository() {
        NexusV2Catalog catalog = new NexusV2Catalog();
        catalog.setRepositories(Arrays.asList());

        RestResponse<NexusV2Catalog> mockedResponse = createMockResponse(catalog, 200);
        when(restClient.executeGet(
            ArgumentMatchers.contains("_catalog"),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2Catalog.class)
        )).thenReturn(mockedResponse);

        boolean isValid = nexusApiClient.isRepositoryValid("empty");

        assertThat(isValid, is(false));
    }

    @SuppressWarnings("unchecked")
    private <T> RestResponse<T> createMockResponse(T payload, int statusCode) {
        RestResponse<T> response = mock(RestResponse.class);
        when(response.getPayload()).thenReturn(payload);
        when(response.getStatusCode()).thenReturn(statusCode);
        return response;
    }

    @Test
    public void shouldFilterImagesByPathPrefix() {
        NexusApiClient pathClient = new NexusApiClient(restClient, NEXUS_URL, DOCKER_REPO, "base-images", USERNAME, PASSWORD);
        
        NexusV2Catalog catalog = new NexusV2Catalog();
        catalog.setRepositories(Arrays.asList("base-images/alpine", "base-images/nginx", "other/image"));

        RestResponse<NexusV2Catalog> catalogResponse = createMockResponse(catalog, 200);
        when(restClient.executeGet(
            ArgumentMatchers.contains("_catalog"),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2Catalog.class)
        )).thenReturn(catalogResponse);

        NexusV2TagList alpineTags = new NexusV2TagList();
        alpineTags.setName("base-images/alpine");
        alpineTags.setTags(Arrays.asList("latest"));
        RestResponse<NexusV2TagList> alpineResponse = createMockResponse(alpineTags, 200);
        when(restClient.executeGet(
            ArgumentMatchers.contains("base-images/alpine"),
            ArgumentMatchers.nullable(Map.class),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.eq(NexusV2TagList.class)
        )).thenReturn(alpineResponse);

        List<DockerImage> images = pathClient.fetchAllImages("base-images");

        assertThat(images.size(), is(equalTo(1)));
        assertThat(images.get(0).getName(), is(equalTo("alpine")));
    }
}
