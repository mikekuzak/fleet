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

package io.linuxserver.fleet.v2.thread.schedule.cache;

import io.linuxserver.fleet.core.FleetAppController;
import io.linuxserver.fleet.v2.thread.schedule.AbstractAppSchedule;
import io.linuxserver.fleet.v2.thread.schedule.ScheduleSpec;

public final class RefreshCacheSchedule extends AbstractAppSchedule {

    public RefreshCacheSchedule(final ScheduleSpec spec,
                                final FleetAppController controller) {
        super(spec, controller);
    }

    @Override
    public void executeSchedule() {
        getController().getRepositoryService().reloadCache();
    }

    @Override
    protected boolean isAllowedToExecute() {
        return getController().getSynchronisationService().isSyncQueueEmpty();
    }
}
