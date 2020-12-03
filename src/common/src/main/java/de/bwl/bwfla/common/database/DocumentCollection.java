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

package de.bwl.bwfla.common.database;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import java.util.NoSuchElementException;


@Deprecated
public class DocumentCollection<T extends JaxbType>
{
	private final String cname;
	private final Class<T> clazz;
	private final MongodbEaasConnector.DatabaseInstance db;

	public DocumentCollection(String cname, Class<T> clazz, MongodbEaasConnector.DatabaseInstance db)
	{
		this.cname = cname;
		this.clazz = clazz;
		this.db = db;
	}

	public void index(String... fields) throws BWFLAException
	{
		db.createIndex(cname, fields);
	}

	public T lookup(String... filter) throws BWFLAException, NoSuchElementException
	{
		return db.lookup(cname, filter, clazz);
	}

	public void save(T document, String... filter) throws BWFLAException
	{
		db.save(cname, filter, document.jsonValueWithoutRoot(false));
	}

	public void delete(String... filter) throws NoSuchElementException
	{
		db.delete(cname, filter);
	}
}
