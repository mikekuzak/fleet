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

import io.linuxserver.fleet.v2.client.docker.converter.AbstractDockerResponseConverter;
import io.linuxserver.fleet.v2.types.docker.DockerTag;
import io.linuxserver.fleet.v2.types.docker.DockerTagManifestDigest;

import java.time.LocalDateTime;

public class NexusTagConverter extends AbstractDockerResponseConverter<String, DockerTag> {

    @Override
    protected DockerTag doPlainConvert(String tagName) {
        return new DockerTag(tagName, 0L, LocalDateTime.now());
    }

    public DockerTag convertWithDigest(String tagName, long size, String digest, String architecture) {
        DockerTag tag = new DockerTag(tagName, size, LocalDateTime.now());
        if (digest != null && !digest.isEmpty()) {
            tag.addDigest(new DockerTagManifestDigest(size, digest, architecture, null));
        }
        return tag;
    }

    @Override
    public Class<String> getConverterClass() {
        return String.class;
    }
}
