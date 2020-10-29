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

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import com.mongodb.Function;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.JAXBException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@ApplicationScoped
public class MongodbEaasConnector {

	private final Logger log = Logger.getLogger(this.getClass().getName());
	private final Map<String, DatabaseInstance> instances = new ConcurrentHashMap<>();
	private MongoClient mongoClient = null;

	@PostConstruct
	public void init() {
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);

		final Configuration config = ConfigurationProvider.getConfiguration();
		final String dbConnectionString = config.get("commonconf.mongodb.address");
		this.mongoClient = MongoClients.create(dbConnectionString);
	}

	public DatabaseInstance getInstance(String name)
	{
		return instances.computeIfAbsent(name, (unused) -> new DatabaseInstance(name));
	}


	public class DatabaseInstance {

		private final MongoDatabase db;

		protected DatabaseInstance(String dbname) {
			this.db = mongoClient.getDatabase(dbname);
		}

		public <T extends JaxbType> DocumentCollection<T> getCollection(String cname, Class<T> clazz)
		{
			return new DocumentCollection<>(cname, clazz, this);
		}

		/**
		 * Load Json from database and cast it to given Jaxb object
		 *
		 * @param collectionName
		 * @param fvalue
		 * @param fkey
		 * @param klass
		 * @param <T>
		 * @return
		 * @throws JAXBException
		 */
		@Deprecated
		public <T extends JaxbType> T getJaxbObject(String collectionName, String fvalue, String fkey, Class<T> klass) throws BWFLAException {
			final String[] filter = { fkey, fvalue };
			return this.lookup(collectionName, filter, klass);
		}

		/** Look up document using specified filter-kv */
		public <T extends JaxbType> T lookup(String cname, String[] filter, Class<T> klass)
				throws BWFLAException, NoSuchElementException, IllegalArgumentException
		{
			final MongoCollection<Document> collection = db.getCollection(cname);

			// Remove internal _id field in json (internal unremovable key in database, next step might be to store envId as _id)
			final Document result = collection.find(MongodbEaasConnector.toFilter(filter))
					.projection(Projections.excludeId())
					.first();

			if (result == null)
				throw new NoSuchElementException();

			return T.fromJsonValueWithoutRoot(result.toJson(), klass);
		}

		/**
		 * Load Json from database and cast it to given Jaxb object
		 *
		 * @param collectionName
		 * @param fvalue
		 * @param fkey
		 * @param klass
		 * @param <T>
		 * @return
		 * @throws JAXBException
		 * @deprecated
		 */
		public <T extends JaxbType> T getJaxbRootbasedObject(String collectionName, String fvalue, String fkey, Class<T> klass) throws BWFLAException {

			final MongoCollection<Document> collection = db.getCollection(collectionName);
			final Bson filter = Filters.eq(fkey, fvalue);

			// Remove internal _id field in json (internal unremovable key in database, next step might be to store envId as _id)
			final Document result = collection.find(filter)
					.projection(Projections.excludeId())
					.first();

			if (result == null)
				throw new NoSuchElementException();

			try {
				return T.fromJsonValue(result.toJson(), klass);
			} catch (JAXBException e) {
				e.printStackTrace();
				throw new BWFLAException(e);
			}
		}

		/**
		 * Load Json from database
		 *
		 * @param collectionName
		 * @param fvalue
		 * @param fkey
		 * @return
		 * @throws JAXBException
		 */
		public <T extends JaxbType> T getObjectWithClassFromDatabaseKey(String collectionName, String classNameKey, String fvalue, String fkey) throws BWFLAException {

			final MongoCollection<Document> collection = db.getCollection(collectionName);
			final Bson filter = Filters.eq(fkey, fvalue);

			// Remove internal _id field in json (internal unremovable key in database, next step might be to store envId as _id)
			final Document result = collection.find(filter)
					.projection(Projections.excludeId())
					.first();

			if (result == null)
				throw new NoSuchElementException();

			String classname = (String) result.get(classNameKey);
			Class<T> klass = null;
			try {
				klass = (Class<T>) Class.forName(classname);
				return T.fromJsonValueWithoutRoot(result.toJson(), klass);
			}
			catch (ClassNotFoundException e) {
				classname = "de.bwl.bwfla.emil.datatypes." + classname;
				try {
					klass = (Class<T>) Class.forName(classname);
					return T.fromJsonValueWithoutRoot(result.toJson(), klass);
				} catch (ClassNotFoundException e1) {
					throw new BWFLAException("failed to create object from JSON");
				}
			} catch (BWFLAException e) {
				return checkForDeprecatedData(result, klass, e);
			}
		}

		/**
		 * Get an array of Jaxb objects, which contain specific tuple, from database
		 * Example (key: value)
		 * "type: EmilObjectEnvironment", "type: EmilContainerEnvironment";
		 * "archiveEnvironmentType: base", "archiveEnvironmentType: tmp"
		 *
		 * @param collectionName
		 * @param <T>
		 * @return
		 * @throws UnknownHostException
		 * @throws JAXBException
		 */
		public <T extends JaxbType> ArrayList<T> getObjectsWithClassFromDatabaseKey(String collectionName, String classNameDBKey) throws BWFLAException {

			// TODO: the results can be pretty big, so here we should return a streaming result!

			final MongoCollection<Document> collection = db.getCollection(collectionName);

			// Remove internal _id field in json (internal unremovable key in database, next step might be to store envId as _id)
			final FindIterable<Document> results = collection.find()
					.projection(Projections.excludeId());

			final ArrayList<T> objects = new ArrayList<>();
			for (Document result : results) {
				String classname = (String) result.get(classNameDBKey);
				Class<T> klass = null;
				try {
					klass = (Class<T>) Class.forName(classname);
					objects.add(T.fromJsonValueWithoutRoot(result.toJson(), klass));
				}
				catch (ClassNotFoundException e) {
					classname = "de.bwl.bwfla.emil.datatypes." + classname;
					try {
						klass = (Class<T>) Class.forName(classname);
						objects.add(T.fromJsonValueWithoutRoot(result.toJson(), klass));
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
						continue;
					}
				}
				catch (BWFLAException e1){
					objects.add(checkForDeprecatedData(result, klass, e1));
				}
			}
			return objects;
		}


		/**
		 * Delete a document (json), which has particular key and value (e.g. "envId" : "123456789")
		 *
		 * @param collectionName
		 * @param fvalue
		 * @param fkey
		 */
		@Deprecated
		public void deleteDoc(String collectionName, String fvalue, String fkey) {
			deleteDoc(collectionName, fvalue, fkey, true);
		}

		/**
		 * Delete a document (json), which has particular key and value (e.g. "envId" : "123456789")
		 *
		 * @param collectionName
		 * @param fvalue
		 * @param fkey
		 */
		@Deprecated
		public void deleteDoc(String collectionName, String fvalue, String fkey, boolean mustExist) {
			final String[] filter = { fkey, fvalue };
			this.delete(collectionName, filter, mustExist);
		}

		/** Delete single document matching specified filter */
		public void delete(String cname, String[] filter) throws NoSuchElementException
		{
			this.delete(cname, filter, true);
		}

		/** Delete single document matching specified filter */
		public void delete(String cname, String[] filter, boolean mustExist) throws NoSuchElementException
		{
			final MongoCollection<Document> collection = db.getCollection(cname);
			final DeleteResult result = collection.deleteOne(MongodbEaasConnector.toFilter(filter));
			if (mustExist && result.getDeletedCount() <= 0)
				throw new NoSuchElementException();
		}


		/**
		 * Deprecated method, which is only used for importing deprecated database elements.
		 * Get an array of Jaxb objects directly from database. The example of key "emilObjectEnvironment", "classificationResult"
		 *
		 * @param collectionName
		 * @param key
		 * @param klass
		 * @param <T>
		 * @return
		 * @throws UnknownHostException
		 * @throws JAXBException
		 */
		@Deprecated
		public <T extends JaxbType> ArrayList<T> getJaxbObjects(String collectionName, String key, Class<T> klass) throws JAXBException {

			// TODO: the results can be pretty big, so here we should return a streaming result!

			final MongoCollection<Document> collection = db.getCollection(collectionName);
			final Bson filter = Filters.exists(key);

			final FindIterable<Document> results = collection.find(filter)
					.projection(Projections.excludeId());

			final ArrayList<T> objects = new ArrayList<>();
			for (Document result : results)
				objects.add(T.fromJsonValue(result.toJson(), klass));

			return objects;
		}


		/**
		 * Get an array of Jaxb objects without root element. Each object contains specific tuple in database
		 * Example of values: "EmilObjectEnvironment", "EmilContainerEnvironment"
		 *
		 * @param collectionName
		 * @param <T>
		 * @return
		 * @throws UnknownHostException
		 * @throws JAXBException
		 */
		public <T extends JaxbType> ArrayList<T> getRootlessJaxbObjects(String collectionName, String value, String classNameDBKey) throws BWFLAException {
			return getRootlessJaxbObjects(collectionName, value, "type", classNameDBKey);
		}

		/**
		 * Get an array of Jaxb objects, which contain specific tuple, from database
		 * Example (key: value)
		 * "type: EmilObjectEnvironment", "type: EmilContainerEnvironment";
		 * "archiveEnvironmentType: base", "archiveEnvironmentType: tmp"
		 *
		 * @param collectionName
		 * @param fkey
		 * @param <T>
		 * @return
		 * @throws UnknownHostException
		 * @throws JAXBException
		 */
		public <T extends JaxbType> ArrayList<T> getRootlessJaxbObjects(String collectionName, String fvalue, String fkey, String classNameDBKey) throws BWFLAException {

			// TODO: the results can be pretty big, so here we should return a streaming result!

			final MongoCollection<Document> collection = db.getCollection(collectionName);
			final Bson filter = Filters.eq(fkey, fvalue);

			final FindIterable<Document> results = collection.find(filter)
					.projection(Projections.excludeId());

			final ArrayList<T> objects = new ArrayList<>();
			for (Document result : results) {
				String classname = (String) result.get(classNameDBKey);
				Class<T> klass = null;
				try {
					klass = (Class<T>) Class.forName(classname);
					objects.add(T.fromJsonValueWithoutRoot(result.toJson(), klass));
				} catch (ClassNotFoundException e) {
					classname = "de.bwl.bwfla.emil.datatypes." + classname;
					try {
						klass = (Class<T>) Class.forName(classname);
						objects.add(T.fromJsonValueWithoutRoot(result.toJson(), klass));
					}
					catch (ClassNotFoundException e1)
					{
						e1.printStackTrace();
						continue;
					}
				}
				catch (BWFLAException e1){
					objects.add(checkForDeprecatedData(result, klass, e1));
				}
			}
			return objects;
		}

		public void createIndex(String cname, String... fields) throws BWFLAException
		{
			try {
				db.getCollection(cname)
						.createIndex(Indexes.ascending(fields));
			}
			catch (Exception error) {
				throw new BWFLAException("Creating index failed!", error);
			}
		}

		/**
		 * Save json file to database.
		 * We find an element with specific key and value (e.g. "emilObjectEnvironment.envId" : "123456789") and update it with newJson
		 * If it does not exist, we create one.
		 *
		 * @param collectionName
		 * @param fvalue
		 * @param fkey
		 * @param newJson
		 * @throws UnknownHostException
		 * @throws BWFLAException
		 */
		@Deprecated
		public void saveDoc(String collectionName, String fvalue, String fkey, String newJson) throws BWFLAException {
			if (fvalue.equals(""))
				throw new BWFLAException("Invalid ObjectId");

			final String[] filter = { fkey, fvalue };
			this.save(collectionName, filter, newJson);
		}

		public void save(String cname, String[] filter, String json) throws BWFLAException
		{
			final MongoCollection<Document> collection = db.getCollection(cname);
			final Document replacement = Document.parse(json);
			final ReplaceOptions options = new ReplaceOptions()
					.upsert(true);

			final Document fd = MongodbEaasConnector.toFilter(filter);
			final UpdateResult result = collection.replaceOne(fd, replacement, options);
			if (!result.isModifiedCountAvailable() && result.getUpsertedId() == null)
				throw new BWFLAException("Upsert failed!");
		}

		public void drop(String collectionName)
		{
			final MongoCollection<Document> collection = db.getCollection(collectionName);
			collection.drop();
		}

		public List<String> getCollections()
		{
			List<String> result = new ArrayList<>();
			MongoCursor<String> cursor = db.listCollectionNames().iterator();
			while(cursor.hasNext()) {
				String collectionName = cursor.next();
				if(collectionName.equals("system.indexes"))
					continue;
				result.add(collectionName);
			}

			return result;
		}

		public <T extends JaxbType> Stream<T> find(String colname, FilterBuilder filter, String clazzkey)
		{
			final Function<Document, T> mapper = (document) -> {

				Class<T> clazz = null;
				try {
					String className = document.getString(clazzkey);
					if(className == null)
						throw new ClassNotFoundException();
					clazz = (Class<T>) Class.forName(className);
				} catch (ClassNotFoundException e) {
					try {
						String klass = document.getString(clazzkey);
						if(klass.contains("EmilNetworkEnvironment"))
							klass = "NetworkEnvironment";

						clazz = (Class<T>) Class.forName("de.bwl.bwfla.emil.datatypes." + klass);
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
						throw new MongoException("Deserializing document failed!", e1);
					}
				}

				try {
					return T.fromJsonValueWithoutRoot(document.toJson(), clazz);
				} catch (BWFLAException e1) {
					return checkForDeprecatedData(document, clazz, e1);
				}
			};

			// NOTE: The mandatory field '_id' is removed from returned documents
			final MongoCollection<Document> collection = db.getCollection(colname);
			final Spliterator<T> spliterator = collection.find(filter.build())
					.projection(Projections.excludeId())
					.map(mapper)
					.spliterator();

			return StreamSupport.stream(spliterator, false);
		}

		public <T extends JaxbType> Stream<T> find(String colname, int offset, int maxcount, FilterBuilder filter, String clazzkey)
		{
			final Function<Document, T> mapper = (document) -> {

				Class<T> clazz = null;
				try {
					clazz = (Class<T>) Class.forName(document.getString(clazzkey));
				} catch (ClassNotFoundException e) {
					try {
						clazz = (Class<T>) Class.forName("de.bwl.bwfla.emil.datatypes." + document.getString(clazzkey));
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
						throw new MongoException("Deserializing document failed!", e1);
					}
				}

				try {
					return T.fromJsonValueWithoutRoot(document.toJson(), clazz);
				} catch (BWFLAException e1) {
					return checkForDeprecatedData(document, clazz, e1);
				}
			};

			// NOTE: The mandatory field '_id' is removed from returned documents
			final MongoCollection<Document> collection = db.getCollection(colname);
			final Spliterator<T> spliterator = collection.find(filter.build())
					.projection(Projections.excludeId())
					.skip(offset)
					.limit(maxcount)
					.map(mapper)
					.spliterator();

			return StreamSupport.stream(spliterator, false);
		}

		public long count(String colname)
		{
			return db.getCollection(colname)
					.countDocuments();
		}

		public long count(String colname, FilterBuilder filter) {
			return db.getCollection(colname)
					.countDocuments(filter.build());
		}

		public void ensureTimestamp(String collectionName) {

			final MongoCollection<Document> collection = db.getCollection(collectionName);
			final Bson filter = Filters.eq("timestamp", null);
			final Bson query = new Document("$set", new Document("timestamp", Instant.now().toString()));
			UpdateResult result = collection.updateMany(filter, query);

			log.info("ensure timestamp: " + collectionName + " modified items: " + result.getModifiedCount());
		}

		/**
		 * Helper method to ensure DB content compatibility. Ideally, all entries in oldStyleDB will be re-saved in new format
		 * @param document
		 * @param clazz
		 * @param e1
		 * @param <T>
		 * @return
		 */
		private  <T extends JaxbType> T checkForDeprecatedData(Document document, Class<T> clazz, Exception e1){
			if (e1.getCause() instanceof UnrecognizedPropertyException) {
				// check if content of database has deprecated networking entries
				String[] networkingValues = {"gwPrivateMask", "gwPrivateIp", "serverIp", "serverPort", "connectEnvs", "enableSocks", "localServerMode", "serverMode", "enableInternet", "helpText"};
				Document subDocument = new Document();
				for (String networkValue : networkingValues) {
					subDocument.put(networkValue, document.get(networkValue));
					document.remove(networkValue);
				}
				document.put("networking", subDocument);
				try {
					return T.fromJsonValueWithoutRoot(document.toJson(), clazz);
				} catch (BWFLAException e) {
					throw new MongoException("Deserializing document failed!", e);
				}
			} else
				throw new MongoException("Deserializing document failed!", e1);
		}
	}

	private static Document toFilter(String[] kvs)
	{
		if (kvs.length % 2 != 0)
			throw new IllegalArgumentException("Filter's KV-pairs are invalid!");

		final Document filter = new Document();
		for (int i = 0; i < kvs.length; i += 2)
			filter.append(kvs[i], kvs[i+1]);

		return filter;
	}

	public static class FilterBuilder
	{
		private final List<Bson> filters;


		public FilterBuilder()
		{
			this.filters = new ArrayList<>(4);
		}

		public FilterBuilder withFromTime(String key, long value)
		{
			return this.withFromTime(key, Instant.ofEpochMilli(value));
		}

		public FilterBuilder withFromTime(String key, Instant value)
		{
			filters.add(Filters.gte(key, value.toString()));
			return this;
		}

		public FilterBuilder withUntilTime(String key, long value, boolean inclusive)
		{
			return this.withUntilTime(key, Instant.ofEpochMilli(value), inclusive);
		}

		public FilterBuilder withUntilTime(String key, Instant value, boolean inclusive)
		{
			filters.add((inclusive) ? Filters.lte(key, value.toString()) : Filters.lt(key, value.toString()));
			return this;
		}

		public FilterBuilder eq(String key, String value)
		{
			filters.add(Filters.eq(key, value));
			return this;
		}

		public Bson build()
		{
			return (filters.isEmpty()) ? new Document() : Filters.and(filters);
		}
	}
}
