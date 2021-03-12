/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.imagearchive.databind;


public class ImportStatus
{
	private final int taskid;
	private ImportState state;
	private ImportTarget target;
	private ImportFailure failure;

	public ImportStatus(int taskid)
	{
		this.taskid = taskid;
	}

	public int taskid()
	{
		return taskid;
	}

	public ImportState state()
	{
		return state;
	}

	public ImportFailure failure()
	{
		return failure;
	}

	public ImportTarget target()
	{
		return target;
	}

	public static ImportStatus aborted(int taskid)
	{
		final var status = new ImportStatus(taskid);
		status.state = ImportState.ABORTED;
		return status;
	}

	public static ImportStatus from(ImportRecord record)
	{
		final var status = new ImportStatus(record.taskid());
		if (record.failed()) {
			status.state = ImportState.FAILED;
			status.failure = record.failure();
		}
		else if (record.finished()) {
			status.state = ImportState.FINISHED;
			status.target = record.task()
					.target();
		}
		else if (record.started())
			status.state = ImportState.RUNNING;
		else status.state = ImportState.PENDING;

		return status;
	}
}
