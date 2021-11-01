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

package com.openslx.eaas.migration;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;


public class MigrationResult
{
	private final String migration;
	private final String stime;
	private String ftime;
	private MigrationState state;


	public MigrationResult(String migration)
	{
		this.migration = migration;
		this.stime = Instant.now()
				.toString();
	}

	@JsonGetter("migration")
	public String migration()
	{
		return migration;
	}

	@JsonGetter("started_at")
	public String stime()
	{
		return stime;
	}

	@JsonGetter("finished_at")
	public String ftime()
	{
		return ftime;
	}

	@JsonGetter("state")
	public MigrationState state()
	{
		return state;
	}

	@JsonIgnore
	public MigrationResult finish(MigrationState state)
	{
		this.state = state;
		this.ftime = Instant.now()
				.toString();

		return this;
	}
}
