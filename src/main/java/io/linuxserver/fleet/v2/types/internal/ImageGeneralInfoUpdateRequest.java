/*
 * Copyright (c)  2020 LinuxServer.io
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

package io.linuxserver.fleet.v2.types.internal;

import java.util.List;
import java.util.Map;

public class ImageGeneralInfoUpdateRequest extends AbstractParamRequest {

    private final ImageAppLogo imageAppLogo;

    public ImageGeneralInfoUpdateRequest(final Map<String, List<String>> params,
                                         final ImageAppLogo imageAppLogo) {
        super(params);
        this.imageAppLogo   = imageAppLogo;
    }

    public final ImageAppLogo getImageAppLogo() {
        return imageAppLogo;
    }

    public final String getBaseImage() {
        return getFirstOrNull("ImageBase");
    }

    public final String getCategory() {
        return getFirstOrNull("ImageCategory");
    }

    public final String getSupportUrl() {
        return getFirstOrNull("ImageSupportUrl");
    }

    public final String getApplicationUrl() {
        return getFirstOrNull("ImageApplicationUrl");
    }
}