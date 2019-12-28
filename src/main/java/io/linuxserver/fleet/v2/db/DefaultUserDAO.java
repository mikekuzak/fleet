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

package io.linuxserver.fleet.v2.db;

import io.linuxserver.fleet.core.db.DatabaseProvider;
import io.linuxserver.fleet.v2.key.UserKey;
import io.linuxserver.fleet.v2.types.User;

public class DefaultUserDAO extends AbstractDAO implements UserDAO {

    public DefaultUserDAO(DatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    @Override
    public User fetchUser(final UserKey userKey) {
        return null;
    }

    @Override
    public User lookUpUser(final String username) {
        return null;
    }
}
