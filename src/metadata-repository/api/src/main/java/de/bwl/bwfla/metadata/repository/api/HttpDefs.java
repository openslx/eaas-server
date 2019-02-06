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

package de.bwl.bwfla.metadata.repository.api;

import javax.ws.rs.core.MediaType;


public final class HttpDefs
{
	public static final class Paths
	{
		public static final String IDENTIFIERS = "item-identifiers";
		public static final String ITEMS       = "items";
		public static final String SETS        = "sets";
	}

	public static final class MediaTypes
	{
		public static final String IDENTIFIERS = MediaType.APPLICATION_JSON;
		public static final String ITEMS       = MediaType.APPLICATION_JSON;
		public static final String SETS        = MediaType.APPLICATION_JSON;
	}

	public static final class QueryParams
	{
		public static final String OFFSET  = "offset";
		public static final String COUNT   = "count";
		public static final String FROM    = "from";
		public static final String UNTIL   = "until";
		public static final String SETSPEC = "setspec";
	}
}
