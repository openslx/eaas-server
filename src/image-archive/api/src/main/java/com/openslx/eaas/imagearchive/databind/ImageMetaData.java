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
import com.fasterxml.jackson.annotation.JsonSetter;


public class ImageMetaData extends AbstractMetaData
{
	private String id;
	private String fstype;
	private String category;
	private String label;

	public ImageMetaData()
	{
		super(Kinds.V1);
	}

	@JsonSetter(Fields.ID)
	public ImageMetaData setId(String id)
	{
		this.id = id;
		return this;
	}

	@JsonGetter(Fields.ID)
	public String id()
	{
		return id;
	}

	@JsonSetter(Fields.FSTYPE)
	public ImageMetaData setFileSystemType(String fstype)
	{
		this.fstype = fstype;
		return this;
	}

	@JsonGetter(Fields.FSTYPE)
	public String fileSystemType()
	{
		return fstype;
	}

	@JsonSetter(Fields.CATEGORY)
	public ImageMetaData setCategory(String category)
	{
		this.category = category;
		return this;
	}

	@JsonGetter(Fields.CATEGORY)
	public String category()
	{
		return category;
	}

	@JsonSetter(Fields.LABEL)
	public ImageMetaData setLabel(String label)
	{
		this.label = label;
		return this;
	}

	@JsonGetter(Fields.LABEL)
	public String label()
	{
		return label;
	}


	public static final class Kinds
	{
		public static final String TYPE = "image";
		public static final String V1   = AbstractMetaData.kind(TYPE, "v1");
	}


	private static final class Fields
	{
		public static final String ID        = "id";
		public static final String FSTYPE    = "fstype";
		public static final String CATEGORY  = "category";
		public static final String LABEL     = "label";
	}
}
