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

package de.bwl.bwfla.metadata.repository.source;


public class MetaDataSource
{
	private SetSource sets;
	private ItemSource items;
	private ItemIdentifierSource ids;


	public MetaDataSource()
	{
		this.sets = null;
		this.items = null;
		this.ids = null;
	}

	public MetaDataSource set(SetSource source)
	{
		this.sets = source;
		return this;
	}

	public MetaDataSource set(ItemIdentifierSource source)
	{
		this.ids = source;
		return this;
	}

	public MetaDataSource set(ItemSource source)
	{
		this.items = source;
		return this;
	}

	public SetSource sets()
	{
		return sets;
	}

	public ItemIdentifierSource identifiers()
	{
		return ids;
	}

	public ItemSource items()
	{
		return items;
	}
}
