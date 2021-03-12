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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.openslx.eaas.imagearchive.ArchiveBackend;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.exceptions.BWFLAException;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportRecord
{
	private int taskid;
	private ImportTask task;
	private ImportFailure failure;
	private long ctime = -1L;
	private long stime = -1L;
	private long ftime = -1L;

	@JsonSetter(Fields.TASK_ID)
	public void setTaskId(int id)
	{
		this.taskid = id;
	}

	@JsonGetter(Fields.TASK_ID)
	public int taskid()
	{
		return taskid;
	}

	@JsonSetter(Fields.TASK_CONFIG)
	public void setTask(ImportTask task)
	{
		task.validate();
		this.task = task;
	}

	@JsonGetter(Fields.TASK_CONFIG)
	public ImportTask task()
	{
		return task;
	}

	@JsonSetter(Fields.FAILURE)
	public void setFailure(ImportFailure failure)
	{
		this.failure = failure;
	}

	@JsonGetter(Fields.FAILURE)
	public ImportFailure failure()
	{
		return failure;
	}

	@JsonIgnore
	public boolean failed()
	{
		return failure != null;
	}

	@JsonSetter(Fields.CREATED_AT_TIME)
	public void setCreatedAtTime(long timestamp)
	{
		this.ctime = timestamp;
	}

	@JsonGetter(Fields.CREATED_AT_TIME)
	public long createdAtTime()
	{
		if (ctime < 0L)
			ctime = ArchiveBackend.now();

		return ctime;
	}

	@JsonSetter(Fields.STARTED_AT_TIME)
	public void setStartedAtTime(long timestamp)
	{
		this.stime = timestamp;
	}

	@JsonGetter(Fields.STARTED_AT_TIME)
	public long startedAtTime()
	{
		return stime;
	}

	@JsonIgnore
	public boolean started()
	{
		return stime > 0L;
	}

	@JsonSetter(Fields.FINISHED_AT_TIME)
	public void setFinishedAtTime(long timestamp)
	{
		this.ftime = timestamp;
	}

	@JsonGetter(Fields.FINISHED_AT_TIME)
	public long finishedAtTime()
	{
		return ftime;
	}

	@JsonIgnore
	public boolean finished()
	{
		return ftime > 0L;
	}

	public static ImportRecord create(int taskid, ImportTask task)
	{
		final var record = new ImportRecord();
		record.setTaskId(taskid);
		record.setTask(task);
		return record;
	}

	public static DocumentCollection.Filter filter(int taskid)
	{
		return DocumentCollection.filter()
				.eq(Fields.TASK_ID, taskid);
	}

	public static DocumentCollection.Filter expired(long timestamp)
	{
		return DocumentCollection.filter()
				.gt(Fields.FINISHED_AT_TIME, 0L)
				.lt(Fields.FINISHED_AT_TIME, timestamp);
	}

	public static DocumentCollection.Filter pending()
	{
		return DocumentCollection.filter()
				.lt(Fields.STARTED_AT_TIME, 0L);
	}

	public static void index(DocumentCollection<ImportRecord> records) throws BWFLAException
	{
		records.index(Fields.TASK_ID);
	}


	private static final class Fields
	{
		public static final String TASK_ID          = "tid";
		public static final String TASK_CONFIG      = "cfg";
		public static final String FAILURE          = "fai";
		public static final String CREATED_AT_TIME  = "cts";
		public static final String STARTED_AT_TIME  = "sts";
		public static final String FINISHED_AT_TIME = "fts";
	}
}
