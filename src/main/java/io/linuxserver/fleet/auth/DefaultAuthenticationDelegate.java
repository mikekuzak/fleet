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

package io.linuxserver.fleet.auth;

import io.linuxserver.fleet.auth.authenticator.UserAuthenticator;
import io.linuxserver.fleet.auth.security.PasswordEncoder;

public class DefaultAuthenticationDelegate implements AuthenticationDelegate {

    private final UserAuthenticator authenticator;

    public DefaultAuthenticationDelegate(final UserAuthenticator authenticator) {
        this.authenticator   = authenticator;
    }

    @Override
    public AuthenticationResult authenticate(final String username, final String password) {
        return authenticator.authenticate(new UserCredentials(username, password));
    }

    @Override
    public String encodePassword(final String rawPassword) {
        return authenticator.getPasswordEncoder().encode(rawPassword);
    }
}
