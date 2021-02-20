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

package com.openslx.eaas.imagearchive.api.v2;

import javax.ws.rs.Path;


public interface IArchiveV2
{
	@Path("/machines")
	IMachinesV2 machines();

	@Path("/templates")
	ITemplatesV2 templates();

	@Path("/images")
	IImagesV2 images();

	@Path("/storage")
	IStorageV2 storage();
}
