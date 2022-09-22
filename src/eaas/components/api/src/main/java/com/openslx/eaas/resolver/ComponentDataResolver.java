/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.openslx.eaas.resolver;

import de.bwl.bwfla.common.services.security.UserContext;


public class ComponentDataResolver extends DataResolver
{
	protected final String kind;

	public ComponentDataResolver(String kind)
	{
		this(kind, DataResolver.getDefaultEndpoint());
	}

	public ComponentDataResolver(String kind, String endpoint)
	{
		super(endpoint);

		if (kind == null || kind.isEmpty())
			throw new IllegalArgumentException();

		this.kind = kind;
	}

	public String resolve(String component, String... resource)
	{
		final var subpaths = new String[3 + resource.length];
		subpaths[0] = "components";
		subpaths[1] = component;
		subpaths[2] = kind;
		System.arraycopy(resource, 0, subpaths, 3, resource.length);
		return this.resolve((UserContext) null, subpaths);
	}
}
