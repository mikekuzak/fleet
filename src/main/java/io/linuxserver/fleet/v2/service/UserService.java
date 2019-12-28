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

package io.linuxserver.fleet.v2.service;

import io.linuxserver.fleet.core.FleetAppController;
import io.linuxserver.fleet.v2.db.UserDAO;
import io.linuxserver.fleet.v2.types.User;

public class UserService extends AbstractAppService {

    private final UserDAO userDAO;

    public UserService(final FleetAppController controller,
                       final UserDAO userDAO) {
        super(controller);
        this.userDAO = userDAO;
    }

    public final User lookUpUser(final String username) {
        return userDAO.lookUpUser(username);
    }
}
