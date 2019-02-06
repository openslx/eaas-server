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

package de.bwl.bwfla.imagebuilder;

import de.bwl.bwfla.imagebuilder.api.metadata.ImageBuilderMetadata;
import de.bwl.bwfla.imagebuilder.ws.ImageBuilder;

import java.nio.file.Path;


public class ImageHandle
{
	private final Path path;
	private final String name;
	private final String type;
	private ImageBuilderMetadata metadata;

	public ImageHandle(Path path, String name, String type)
	{
		this.path = path;
		this.name = name;
		this.type = type;
	}

	public Path getPath()
	{
		return path;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public ImageBuilderMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ImageBuilderMetadata metadata) {
		this.metadata = metadata;
	}
}
