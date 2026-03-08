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
import io.linuxserver.fleet.v2.client.docker.converter.AbstractDockerResponseConverter;
import io.linuxserver.fleet.v2.types.docker.DockerImage;
import io.linuxserver.fleet.v2.types.docker.DockerTag;

import java.time.LocalDateTime;

public class NexusImageConverter extends AbstractDockerResponseConverter<NexusV2TagList, DockerImage> {

    private final String pathPrefix;

    public NexusImageConverter() {
        this(null);
    }

    public NexusImageConverter(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    @Override
    protected DockerImage doPlainConvert(NexusV2TagList nexusTagList) {
        String fullName = nexusTagList.getName();
        String strippedName = stripPathPrefix(fullName);
        return new DockerImage(
            extractImageName(strippedName),
            extractRepositoryName(strippedName),
            null,
            0,
            0L,
            LocalDateTime.now()
        );
    }

    private String stripPathPrefix(String fullName) {
        if (pathPrefix == null || pathPrefix.isEmpty()) {
            return fullName;
        }
        if (fullName.startsWith(pathPrefix + "/")) {
            return fullName.substring(pathPrefix.length() + 1);
        }
        return fullName;
    }

    @Override
    public Class<NexusV2TagList> getConverterClass() {
        return NexusV2TagList.class;
    }

    private String extractImageName(String fullName) {
        if (fullName == null || !fullName.contains("/")) {
            return fullName;
        }
        return fullName.substring(fullName.lastIndexOf('/') + 1);
    }

    private String extractRepositoryName(String fullName) {
        if (fullName == null || !fullName.contains("/")) {
            return "";
        }
        int lastSlash = fullName.lastIndexOf('/');
        return lastSlash > 0 ? fullName.substring(0, lastSlash) : "";
    }
}
