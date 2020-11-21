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

package de.bwl.bwfla.common.database.document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@ApplicationScoped
public class DocumentDatabaseConnector
{
	private MongoClient mongo = null;

	@PostConstruct
	public void inititialize()
	{
		Logger logger = Logger.getLogger("org.mongodb.driver");
		logger.setLevel(Level.SEVERE);

		final Configuration config = ConfigurationProvider.getConfiguration();
		final String address = config.get("commonconf.mongodb.address");
		this.mongo = MongoClients.create(address);
	}

	@PreDestroy
	public void close()
	{
		mongo.close();
	}

	/** Return current connector-instance */
	public static DocumentDatabaseConnector instance()
	{
		return CDI.current()
				.select(DocumentDatabaseConnector.class)
				.get();
	}

	/** Get or create a document-database */
	public DocumentDatabase database(String name)
	{
		return new DocumentDatabase(mongo.getDatabase(name));
	}

	/** List all databases */
	public Stream<String> databases()
	{
		return StreamSupport.stream(mongo.listDatabaseNames().spliterator(), false);
	}
}
