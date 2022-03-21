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

package com.openslx.eaas.imagearchive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.openslx.eaas.common.databind.DataUtils;
import com.openslx.eaas.imagearchive.databind.EmulatorMetaData;
import com.openslx.eaas.imagearchive.databind.ImageMetaData;

import java.util.function.Function;


public class ImageArchiveMappers
{
	/** Mapper from generic objects to parsed JSON */
	public static final Function<Object, JsonNode> OBJECT_TO_JSON_TREE = (object) -> {
		return DataUtils.json()
				.mapper()
				.valueToTree(object);
	};

	/** Mapper from parsed JSON to image-metadata */
	public static final FromJsonTree<ImageMetaData> JSON_TREE_TO_IMAGE_METADATA
			= new FromJsonTree<>(ImageMetaData.class);


	/** Mapper from parsed JSON to emulator-metadata */
	public static final FromJsonTree<EmulatorMetaData> JSON_TREE_TO_EMULATOR_METADATA
			= new FromJsonTree<>(EmulatorMetaData.class);


	/** Mapper from parsed JSON to generic objects */
	public static class FromJsonTree<T> implements Function<JsonNode, T>
	{
		private final ObjectReader reader;

		public FromJsonTree(Class<T> clazz)
		{
			this.reader = DataUtils.json()
					.reader()
					.forType(clazz);
		}

		@Override
		public T apply(JsonNode json)
		{
			try {
				return reader.readValue(json);
			}
			catch (Exception error) {
				throw new RuntimeException("Reading object from JSON tree failed!", error);
			}
		}
	}
}
