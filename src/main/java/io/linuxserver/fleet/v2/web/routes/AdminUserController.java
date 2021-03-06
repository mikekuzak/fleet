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

package io.linuxserver.fleet.v2.web.routes;

import io.javalin.http.Context;
import io.linuxserver.fleet.core.FleetAppController;
import io.linuxserver.fleet.v2.key.UserKey;
import io.linuxserver.fleet.v2.service.UserService;
import io.linuxserver.fleet.v2.types.User;
import io.linuxserver.fleet.v2.types.internal.UserOutlineRequest;
import io.linuxserver.fleet.v2.web.AppRole;
import io.linuxserver.fleet.v2.web.PageModelSpec;

public class AdminUserController extends AbstractPageHandler {

    private UserService userService;

    public AdminUserController(final FleetAppController controller) {
        super(controller);
        userService = controller.getUserService();
    }

    @Override
    protected PageModelSpec handlePageLoad(final Context ctx) {

        final PageModelSpec modelSpec = new PageModelSpec("views/pages/admin/users.ftl");
        modelSpec.addModelAttribute("users", userService.fetchAllUsers());
        return modelSpec;
    }

    @Override
    protected PageModelSpec handleFormSubmission(final Context ctx) {

        final String action = ctx.queryParam("action", String.class).get();
        if ("delete".equalsIgnoreCase(action)) {

            final UserKey userKey = ctx.formParam("UserPendingDeletion", UserKey.class).get();
            final User    user    = userService.fetchUser(userKey);

            userService.removeUser(user);

        } else if("update".equalsIgnoreCase(action)) {

            final UserKey userKey  = ctx.formParam("UserPendingPasswordChange", UserKey.class).get();
            final String  password = ctx.formParam("UserPassword", String.class).get();
            final User    user     = userService.fetchUser(userKey);

            if (null == user) {
                throw new IllegalArgumentException("No user found with key " + userKey);
            }

            userService.updateUserPassword(user, password);

        } else if ("create".equalsIgnoreCase(action)) {

            final String username = ctx.formParam("NewUserName", String.class).get();
            final String password = ctx.formParam("NewUserPassword", String.class).get();

            final UserOutlineRequest request = new UserOutlineRequest(username, password, AppRole.Admin);
            userService.createUserAndHashPassword(request);
        }

        return new PageModelSpec("redirect:/admin/users");
    }
}
