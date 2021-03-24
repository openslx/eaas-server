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

package com.openslx.eaas.imagearchive.api.v2.common;

import com.openslx.eaas.imagearchive.api.v2.databind.AccessMethodV2;

import javax.ws.rs.QueryParam;
import java.time.Duration;
import java.util.concurrent.TimeUnit;


public class ResolveOptionsV2 extends BasicOptionsV2<ResolveOptionsV2>
{
	@QueryParam("lifetime")
	private long lifetime;

	@QueryParam("method")
	private AccessMethodV2 method;


	public ResolveOptionsV2 setMethod(AccessMethodV2 method)
	{
		this.method = method;
		return this;
	}

	public AccessMethodV2 method()
	{
		return method;
	}

	public ResolveOptionsV2 setLifetime(long lifetime, TimeUnit unit)
	{
		return this.setLifetime(unit.toSeconds(lifetime));
	}

	public ResolveOptionsV2 setLifetime(Duration lifetime)
	{
		return this.setLifetime(lifetime.toSeconds());
	}

	public ResolveOptionsV2 setLifetime(long lifetime)
	{
		this.lifetime = lifetime;
		return this;
	}

	public Duration lifetime()
	{
		return (lifetime > 0) ? Duration.ofSeconds(lifetime) : null;
	}
}
