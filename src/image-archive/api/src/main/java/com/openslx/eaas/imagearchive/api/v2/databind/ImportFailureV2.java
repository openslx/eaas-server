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
public class ImportFailureV2
{
	private String reason;
	private String detail;

	@JsonSetter(Fields.REASON)
	public ImportFailureV2 setReason(String reason)
	{
		this.reason = reason;
		return this;
	}

	@JsonGetter(Fields.REASON)
	public String reason()
	{
		return reason;
	}

	@JsonSetter(Fields.DETAIL)
	public ImportFailureV2 setDetail(String detail)
	{
		this.detail = detail;
		return this;
	}

	@JsonGetter(Fields.DETAIL)
	public String detail()
	{
		return detail;
	}


	private static final class Fields
	{
		public static final String REASON = "reason";
		public static final String DETAIL = "detail";
	}
}
