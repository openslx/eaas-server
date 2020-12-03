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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.UuidRepresentation;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.internal.MongoJackModule;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class DocumentDatabase
{
	private final MongoDatabase db;

	DocumentDatabase(MongoDatabase db)
	{
		this.db = db;
	}

	/** Get or create a document-collection */
	public <T> DocumentCollection<T> collection(String cname, Class<T> clazz)
	{
		// Configure object-mapper to use MongoJack module (with JAX-B support)
		final ObjectMapper mapper = MongoJackModule.configure(new ObjectMapper())
				.registerModule(new JaxbAnnotationModule());

		final MongoCollection<T> collection = JacksonMongoCollection.builder()
				.withObjectMapper(mapper)
				.build(db, cname, clazz, UuidRepresentation.STANDARD);

		return new DocumentCollection<T>(collection);
	}

	/** List all collections */
	public Stream<String> collections()
	{
		return StreamSupport.stream(db.listCollectionNames().spliterator(), false);
	}

	/** Drop this collection */
	public void drop()
	{
		db.drop();
	}
}
