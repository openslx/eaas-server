package de.bwl.bwfla.emil.database;

import com.mongodb.*;
import com.mongodb.util.JSON;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MongodbEaasConnector {

	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	@Inject
	@Config("emil.mongodbadress")
	private String mongodbHost;

	@Inject
	@Config("emil.mongodbport")
	private int port;

	@Inject
	@Config("emil.mongodbname")
	private String dbName;

	private MongoClient mongoClient = null;
	private DB databaseInstance = null;

	@PostConstruct
	public void init()
	{
		Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE);
		mongoClient = new MongoClient(mongodbHost, port);
		databaseInstance = mongoClient.getDB(dbName);
	}

	/**
	 * Load Json from database and cast it to given Jaxb object
	 * @param collectionName
	 * @param value
	 * @param key
	 * @param klass
	 * @param <T>
	 * @return
	 * @throws JAXBException
	 */
	public <T extends JaxbType> T getJaxbObject(String collectionName, String value, String key, Class<T> klass) throws JAXBException {

		DBCollection classificationCacheCollection = databaseInstance.getCollection(collectionName);
		BasicDBObject query = new BasicDBObject();
		query.put(key, value);
		// Remove internal _id field in json (internal unremovable key in database, next step might be to store envId as _id)
		BasicDBObject removeIdProjection = new BasicDBObject("_id", 0);

		DBObject queryResult = classificationCacheCollection.findOne(query, removeIdProjection);

		if (queryResult == null) {
			throw new NoSuchElementException();
		}

		return T.fromJsonValue(queryResult.toString(), klass);
	}

	/**
	 * Delete a document (json), which has particular key and value (e.g. "EmilEnvironment.envId" : "123456789")
	 * @param collectionName
	 * @param value
	 * @param key
	 */
	public void deleteDoc(String collectionName, String value, String key) {
		deleteDoc(collectionName, value, key , true);
	}

	/**
	 * Delete a document (json), which has particular key and value (e.g. "EmilEnvironment.envId" : "123456789")
	 * @param collectionName
	 * @param value
	 * @param key
	 */
	public void deleteDoc(String collectionName, String value, String key, boolean mustExist) {
		DBCollection classificationCacheCollection = databaseInstance.getCollection(collectionName);
		BasicDBObject query = new BasicDBObject();
		query.put(key, value);
		WriteResult result = classificationCacheCollection.remove(query);

		if (mustExist)
			if (result.getN() <= 0)
				throw new NoSuchElementException();
	}

	/**
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
	public <T extends JaxbType> ArrayList<T> getJaxbObjects(String collectionName, String key, Class<T> klass) throws JAXBException {


		DBCollection classificationCacheCollection = databaseInstance.getCollection(collectionName);

		BasicDBObject query = new BasicDBObject();
		query.put(key, new BasicDBObject("$exists", true));
		BasicDBObject removeIdProjection = new BasicDBObject("_id", 0);

		DBCursor queryResults = classificationCacheCollection.find(query, removeIdProjection);

		if (queryResults == null) {
			throw new NoSuchElementException();
		}
		ArrayList<T> list = new ArrayList<>();
		while (queryResults.hasNext()) {
			String s = queryResults.next().toString();
			list.add(T.fromJsonValue(s, klass));
		}
		return list;
	}

	/**
	 * Save json file to database.
	 * We find an element with specific key and value (e.g. "emilObjectEnvironment.envId" : "123456789") and update it with newJson
	 * If it does not exist, we create one.
	 *
	 * @param collectionName
	 * @param value
	 * @param key
	 * @param newJson
	 * @throws UnknownHostException
	 * @throws BWFLAException
	 */
	public void saveDoc(String collectionName, String value, String key, String newJson) throws BWFLAException {
		if (value.equals(""))
			throw new BWFLAException("Invalid ObjectId");

		DBObject dbObject = (DBObject) JSON.parse(newJson);
		DBCollection collection = databaseInstance.getCollection(collectionName);
		// update or create document
		collection.update(new BasicDBObject(key, value), dbObject, true, false);
	}
}
