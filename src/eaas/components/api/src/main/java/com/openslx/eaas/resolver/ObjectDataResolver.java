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
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;


public class ObjectDataResolver extends ComponentDataResolver
{
	public ObjectDataResolver()
	{
		super("objects");
	}

	public String resolve(String component, FileCollectionEntry fce)
	{
		return this.resolve(component, fce.getArchive(), fce.getObjectId(), fce.getId());
	}

	public String resolve(FileCollectionEntry fce, UserContext userctx)
	{
		return this.resolve(fce.getArchive(), fce.getObjectId(), fce.getId(), userctx);
	}

	public String resolve(String archive, String object, String resource)
	{
		return this.resolve(archive, object, resource, null);
	}

	public String resolve(String archive, String object, String resource, UserContext userctx)
	{
		return this.resolve(userctx, kind, archive, object, resource);
	}
}
