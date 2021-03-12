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

package com.openslx.eaas.imagearchive.api.v2.databind;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImportStatusV2
{
	private String taskid;
	private ImportStateV2 state;
	private ImportTargetV2 target;
	private ImportFailureV2 failure;

	@JsonSetter(Fields.TASK_ID)
	public ImportStatusV2 setTaskId(String id)
	{
		this.taskid = id;
		return this;
	}

	@JsonGetter(Fields.TASK_ID)
	public String taskid()
	{
		return taskid;
	}

	@JsonSetter(Fields.STATE)
	public ImportStatusV2 setState(ImportStateV2 state)
	{
		this.state = state;
		return this;
	}

	@JsonGetter(Fields.STATE)
	public ImportStateV2 state()
	{
		return state;
	}

	@JsonSetter(Fields.TARGET)
	public ImportStatusV2 setTarget(ImportTargetV2 target)
	{
		this.target = target;
		return this;
	}

	@JsonGetter(Fields.TARGET)
	public ImportTargetV2 target()
	{
		return target;
	}

	@JsonSetter(Fields.FAILURE)
	public ImportStatusV2 setFailure(ImportFailureV2 failure)
	{
		this.failure = failure;
		return this;
	}

	@JsonGetter(Fields.FAILURE)
	public ImportFailureV2 failure()
	{
		return failure;
	}


	private static final class Fields
	{
		public static final String TASK_ID = "task_id";
		public static final String STATE   = "state";
		public static final String TARGET  = "target";
		public static final String FAILURE = "failure";
	}
}
