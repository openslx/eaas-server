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

package de.bwl.bwfla.digpubsharing.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;


/** Tenant's site settings  */
public class Settings
{
	private String sitename;
	private String sitelogo;
	private String headerTextColor;
	private String headerBackgroundColor;
	private String extIdLabel;

	public Settings()
	{
		this.extIdLabel = "ID";
	}

	@JsonSetter(Fields.SITE_NAME)
	public Settings setSiteName(String name)
	{
		this.sitename = name;
		return this;
	}

	/** Organization name */
	@JsonGetter(Fields.SITE_NAME)
	public String getSiteName()
	{
		return sitename;
	}

	@JsonSetter(Fields.SITE_LOGO)
	public Settings setSiteLogo(String logo)
	{
		this.sitelogo = logo;
		return this;
	}

	/** Organization's logo encoded as base64-string */
	@JsonGetter(Fields.SITE_LOGO)
	public String getSiteLogo()
	{
		return sitelogo;
	}

	@JsonSetter(Fields.HEADER_TEXT_COLOR)
	public Settings setHeaderTextColor(String color)
	{
		this.headerTextColor = color;
		return this;
	}

	/** Header's text color */
	@JsonGetter(Fields.HEADER_TEXT_COLOR)
	public String getHeaderTextColor()
	{
		return headerTextColor;
	}

	@JsonSetter(Fields.HEADER_BACKGROUND_COLOR)
	public Settings setHeaderBackgroundColor(String color)
	{
		this.headerBackgroundColor = color;
		return this;
	}

	/** Header's background color */
	@JsonGetter(Fields.HEADER_BACKGROUND_COLOR)
	public String getHeaderBackgroundColor()
	{
		return headerBackgroundColor;
	}

	@JsonSetter(Fields.EXTERNAL_ID_LABEL)
	public Settings setExternalIdLabel(String label)
	{
		this.extIdLabel = label;
		return this;
	}

	/** Label for external IDs */
	@JsonGetter(Fields.EXTERNAL_ID_LABEL)
	public String getExternalIdLabel()
	{
		return extIdLabel;
	}

	public static final class Fields
	{
		public static final String SITE_NAME = "site_name";
		public static final String SITE_LOGO = "site_logo";
		public static final String HEADER_TEXT_COLOR = "header_text_color";
		public static final String HEADER_BACKGROUND_COLOR = "header_bg_color";
		public static final String EXTERNAL_ID_LABEL = "ext_id_label";
	}
}
