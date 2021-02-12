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

package com.openslx.eaas.imagearchive.indexing;

import com.openslx.eaas.imagearchive.ArchiveBackend;
import de.bwl.bwfla.common.database.document.DocumentCollection;
import de.bwl.bwfla.common.database.document.DocumentDatabaseConnector;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.apache.tamaya.ConfigurationProvider;

import java.util.logging.Logger;


public abstract class Index<T> implements AutoCloseable
{
	private final Logger logger;
	private final String name;
	private final Class<T> clazz;
	private final IPreparer<T> preparer;
	private final DocumentCollection<T> collection;


	public String name()
	{
		return name;
	}

	public DocumentCollection<T> collection()
	{
		return collection;
	}

	@Override
	public void close() throws Exception
	{
		// Empty!
	}


	// ===== Internal Helpers ==============================

	protected Index(String name, Class<T> clazz, IPreparer<T> preparer)
	{
		this.name = name;
		this.clazz = clazz;
		this.preparer = preparer;
		this.logger = ArchiveBackend.logger("index", this.name());
		this.collection = Index.construct(this.name(), clazz, logger);
	}

	protected void switchto(DocumentCollection<T> replacement) throws BWFLAException
	{
		// replace current collection with the given one by atomically dropping it
		// and making replacement collection available via current name!
		replacement.rename(this.name(), true);
	}

	protected Logger logger()
	{
		return logger;
	}

	protected Class<T> clazz()
	{
		return clazz;
	}

	protected IPreparer<T> preparer()
	{
		return preparer;
	}

	protected static <D> DocumentCollection<D> construct(String cname, Class<D> clazz, Logger logger)
	{
		final String dbname = ConfigurationProvider.getConfiguration()
				.get("commonconf.mongodb.dbname");

		logger.info("Initializing collection: " + cname + " (" + dbname + ")");

		return DocumentDatabaseConnector.instance()
				.database(dbname)
				.collection(cname, clazz);
	}
}
