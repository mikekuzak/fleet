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
import io.linuxserver.fleet.nexus.model.NexusV2Manifest;
import io.linuxserver.fleet.nexus.model.NexusV2TagList;
import io.linuxserver.fleet.v2.Utils;
import io.linuxserver.fleet.v2.client.docker.AbstractDockerApiClient;
import io.linuxserver.fleet.v2.client.rest.HttpException;
import io.linuxserver.fleet.v2.client.rest.RestClient;
import io.linuxserver.fleet.v2.client.rest.RestResponse;
import io.linuxserver.fleet.v2.types.docker.DockerImage;
import io.linuxserver.fleet.v2.types.docker.DockerTag;
import io.linuxserver.fleet.v2.types.docker.DockerTagManifestDigest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NexusApiClient extends AbstractDockerApiClient<NexusV2TagList, String, NexusImageConverter, NexusTagConverter> {

    private final RestClient restClient;
    private final String baseUrl;
    private final String username;
    private final String password;
    private final NexusTagConverter tagConverter;
    private final String pathPrefix;

    public NexusApiClient(final RestClient restClient,
                          final String nexusUrl,
                          final String dockerRepository,
                          final String pathPrefix,
                          final String username,
                          final String password) {
        super(new NexusImageConverter(pathPrefix), new NexusTagConverter());
        this.restClient = Utils.ensureNotNull(restClient);
        this.baseUrl = buildBaseUrl(nexusUrl, dockerRepository);
        this.pathPrefix = normalizePathPrefix(pathPrefix);
        this.username = username;
        this.password = password;
        this.tagConverter = new NexusTagConverter();
    }

    private String buildBaseUrl(String nexusUrl, String dockerRepository) {
        String url = nexusUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += "repository/" + dockerRepository + "/v2/";
        return url;
    }

    private String normalizePathPrefix(String pathPrefix) {
        if (pathPrefix == null || pathPrefix.trim().isEmpty()) {
            return null;
        }
        String normalized = pathPrefix.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean matchesPathPrefix(String imageName) {
        if (pathPrefix == null) {
            return true;
        }
        return imageName.startsWith(pathPrefix + "/") || imageName.equals(pathPrefix);
    }

    private String stripPathPrefix(String imageName) {
        if (pathPrefix == null) {
            return imageName;
        }
        if (imageName.startsWith(pathPrefix + "/")) {
            return imageName.substring(pathPrefix.length() + 1);
        }
        return imageName;
    }

    @Override
    public boolean isRepositoryValid(String repositoryName) {
        try {
            return !fetchAllImages(repositoryName).isEmpty();
        } catch (HttpException e) {
            throw new NexusException("Unable to verify repository " + repositoryName, e);
        }
    }

    @Override
    protected NexusV2TagList fetchImageFromApi(String imageName) {
        try {
            String fullImageName = pathPrefix != null ? pathPrefix + "/" + imageName : imageName;
            String url = baseUrl + fullImageName + "/tags/list";
            RestResponse<NexusV2TagList> response = doGet(url, NexusV2TagList.class);
            if (isResponseOK(response)) {
                return response.getPayload();
            }
            return null;
        } catch (HttpException e) {
            throw new NexusException("Unable to get image " + imageName, e);
        }
    }

    @Override
    protected List<NexusV2TagList> fetchAllImagesFromApi(String repositoryName) {
        try {
            List<NexusV2TagList> images = new ArrayList<>();
            String url = baseUrl + "_catalog";
            RestResponse<NexusV2Catalog> response = doGet(url, NexusV2Catalog.class);
            if (isResponseOK(response) && response.getPayload() != null) {
                List<String> repositories = response.getPayload().getRepositories();
                if (repositories != null) {
                    for (String repo : repositories) {
                        if (!matchesPathPrefix(repo)) {
                            continue;
                        }
                        String imageUrl = baseUrl + repo + "/tags/list";
                        RestResponse<NexusV2TagList> imageResponse = doGet(imageUrl, NexusV2TagList.class);
                        if (isResponseOK(imageResponse) && imageResponse.getPayload() != null) {
                            images.add(imageResponse.getPayload());
                        }
                    }
                }
            }
            return images;
        } catch (HttpException e) {
            throw new NexusException("Unable to get images for " + repositoryName, e);
        }
    }

    @Override
    protected List<String> fetchTagsFromApi(String imageName) {
        try {
            List<String> tags = new ArrayList<>();
            String fullImageName = pathPrefix != null ? pathPrefix + "/" + imageName : imageName;
            String url = baseUrl + fullImageName + "/tags/list";
            RestResponse<NexusV2TagList> response = doGet(url, NexusV2TagList.class);
            if (isResponseOK(response) && response.getPayload() != null) {
                List<String> tagList = response.getPayload().getTags();
                if (tagList != null) {
                    tags.addAll(tagList);
                }
            }
            return tags;
        } catch (HttpException e) {
            throw new NexusException("Unable to get tags for " + imageName, e);
        }
    }

    public DockerImage fetchImageWithTags(String imageName) {
        String fullImageName = pathPrefix != null ? pathPrefix + "/" + imageName : imageName;
        NexusV2TagList tagList = fetchImageFromApi(imageName);
        if (tagList == null) {
            return null;
        }

        DockerImage image = new NexusImageConverter().convert(tagList);
        
        List<String> tagNames = tagList.getTags();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                DockerTag tag = fetchTagWithManifest(fullImageName, tagName);
                if (tag != null) {
                    image.addTag(tag);
                }
            }
        }
        
        return image;
    }

    private DockerTag fetchTagWithManifest(String fullImageName, String tagName) {
        try {
            String manifestUrl = baseUrl + fullImageName + "/manifests/" + tagName;
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/vnd.docker.distribution.manifest.v2+json");
            
            RestResponse<NexusV2Manifest> response = restClient.executeGet(
                manifestUrl, 
                null, 
                headers, 
                NexusV2Manifest.class
            );
            
            if (isResponseOK(response) && response.getPayload() != null) {
                NexusV2Manifest manifest = response.getPayload();
                long totalSize = 0;
                String digest = null;
                String architecture = "amd64";
                
                if (manifest.getConfig() != null) {
                    totalSize = manifest.getConfig().getSize();
                    digest = manifest.getConfig().getDigest();
                }
                
                if (manifest.getLayers() != null && !manifest.getLayers().isEmpty()) {
                    NexusV2Manifest.NexusManifestLayer layer = manifest.getLayers().get(0);
                    totalSize += layer.getSize();
                }
                
                return tagConverter.convertWithDigest(tagName, totalSize, digest, architecture);
            }
            
            return tagConverter.convert(tagName);
        } catch (HttpException e) {
            return tagConverter.convert(tagName);
        }
    }

    private <T> RestResponse<T> doGet(String url, Class<T> responseType) {
        return restClient.executeGet(url, null, buildAuthHeaders(), responseType);
    }

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (username != null && password != null && !username.isEmpty()) {
            String credentials = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            headers.put("Authorization", "Basic " + encoded);
        }
        return headers;
    }

    private boolean isResponseOK(RestResponse<?> response) {
        return response.getStatusCode() == 200;
    }
}
