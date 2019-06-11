/*
 * Copyright (c) 2019 LinuxServer.io
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

package io.linuxserver.fleet.model.internal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * <p>
 * Representation of a stored image in the Fleet database. Each image contains
 * specific information regarding its build status and pull count.
 * </p>
 */
public class Image extends PersistableItem<Image> {

    private final int       repositoryId;
    private final String    name;

    private Tag             tag;
    private long            pullCount;
    private String          versionMask;
    private boolean         unstable;
    private boolean         hidden;

    private boolean         deprecated;
    private String          deprecationReason;

    public Image(Integer id, int repositoryId, String name, Tag latestVersion) {

        super(id);

        this.name           = name;
        this.repositoryId   = repositoryId;
        this.tag            = new Tag(latestVersion.getVersion(), latestVersion.getMaskedVersion(), latestVersion.getBuildDate());
    }

    public Image(int repositoryId, String name) {
        this(null, repositoryId, name, Tag.NONE);
    }

    public static Image copyOf(Image image) {

        Image cloned                = new Image(image.getId(), image.repositoryId, image.name, image.tag);
        cloned.pullCount            = image.pullCount;
        cloned.versionMask          = image.versionMask;
        cloned.unstable             = image.unstable;
        cloned.hidden               = image.hidden;
        cloned.deprecated           = image.deprecated;
        cloned.deprecationReason    = image.deprecationReason;

        return cloned;
    }

    public Image withPullCount(long pullCount) {

        this.pullCount = pullCount;
        return this;
    }

    public Image withVersionMask(String versionMask) {

        this.versionMask = versionMask;
        return this;
    }

    public Image withHidden(boolean hidden) {

        this.hidden = hidden;
        return this;
    }

    public Image withUnstable(boolean unstable) {

        this.unstable = unstable;
        return this;
    }

    public Image withDeprecated(boolean deprecated) {

        this.deprecated = deprecated;
        return this;
    }

    public Image withDeprecationReason(String deprecationReason) {

        this.deprecationReason = deprecationReason;
        return this;
    }

    public void updateTag(Tag maskedVersion) {
        this.tag = new Tag(maskedVersion.getVersion(), maskedVersion.getMaskedVersion(), maskedVersion.getBuildDate());
    }

    public int getRepositoryId() {
        return repositoryId;
    }

    public String getName() {
        return name;
    }

    public long getPullCount() {
        return pullCount;
    }

    public String getMaskedVersion() {
        return tag.getMaskedVersion();
    }

    public String getRawVersion() {
        return tag.getVersion();
    }

    public LocalDateTime getBuildDate() {
        return tag.getBuildDate();
    }

    public String getBuildDateAsString() {

        if (getBuildDate() != null) {
            return getBuildDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss"));
        }

        return null;
    }

    public String getVersionMask() {
        return versionMask;
    }

    public boolean isUnstable() {
        return unstable;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public String getDeprecationReason() {
        return deprecationReason;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return repositoryId == image.repositoryId && Objects.equals(name, image.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryId, name);
    }
}