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

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class DocumentCollection<T>
{
	private final MongoCollection<T> collection;

	DocumentCollection(MongoCollection<T> collection)
	{
		this.collection = collection;
	}

	/** Create an index on specified fields */
	public void index(String... fields) throws BWFLAException
	{
		try {
			collection.createIndex(Indexes.ascending(fields));
		}
		catch (MongoException error) {
			throw new BWFLAException("Creating index failed!", error);
		}
	}

	/** Look up a single document or return null if not found */
	public T lookup(Filter filter) throws BWFLAException
	{
		try {
			return this.documents(filter)
					.first();
		}
		catch (MongoException error) {
			throw new BWFLAException("Looking up document failed!", error);
		}
	}

	/** Find documents passing filter */
	public FindResult<T> find(Filter filter) throws BWFLAException
	{
		try {
			return new FindResult<>(this.documents(filter));
		}
		catch (MongoException error) {
			throw new BWFLAException("Finding documents failed!", error);
		}
	}

	/** List all documents */
	public FindResult<T> list() throws BWFLAException
	{
		return this.find(DocumentCollection.filter());
	}

	/** Insert a single document */
	public void insert(T document) throws BWFLAException
	{
		try {
			final InsertOneResult result = collection.insertOne(document);
			if (result.getInsertedId() == null)
				throw new BWFLAException("Inserting document failed!");
		}
		catch (MongoException error) {
			throw new BWFLAException("Inserting document failed!", error);
		}
	}

	/** Insert multiple documents */
	public void insert(Iterable<? extends T> documents) throws BWFLAException
	{
		this.insert(documents.iterator());
	}

	/** Insert multiple documents */
	public void insert(Iterator<? extends T> documents) throws BWFLAException
	{
		final int INSERT_BATCH_SIZE = 16;

		try {
			int numtotal = 0;
			int numdone = 0;

			final List<T> batch = new ArrayList<>(INSERT_BATCH_SIZE);

			// insert all docs in batches...
			while (documents.hasNext()) {
				batch.clear();

				// prepare next batch...
				while (documents.hasNext()) {
					batch.add(documents.next());
					if (batch.size() == INSERT_BATCH_SIZE)
						break;
				}

				numtotal += batch.size();

				// insert current batch...
				numdone += collection.insertMany(batch)
						.getInsertedIds()
						.size();
			}

			if (numdone != numtotal)
				throw new BWFLAException("Inserting documents failed!");
		}
		catch (MongoException error) {
			throw new BWFLAException("Inserting documents failed!", error);
		}
	}

	/** Replace or insert document matching filter */
	public void replace(Filter filter, T document) throws BWFLAException
	{
		this.replace(filter, document, true);
	}

	/** Replace or insert document matching filter */
	public void replace(Filter filter, T document, boolean upsert) throws BWFLAException
	{
		try {
			// NOTE: we use Mongo's replace-or-insert (upsert) operation here!
			final ReplaceOptions options = new ReplaceOptions()
					.upsert(upsert);

			final UpdateResult result = collection.replaceOne(filter.expression(), document, options);
			if (result.getModifiedCount() < 1 && result.getUpsertedId() == null)
				throw new BWFLAException("Upserting document failed!");
		}
		catch (MongoException error) {
			throw new BWFLAException("Replacing document failed!", error);
		}
	}

	/** Update (optionally many) document(s) matching filter */
	public long update(Filter filter, Update update, boolean many) throws BWFLAException
	{
		try {
			final BiFunction<Bson, Bson, UpdateResult> fn = (many) ? collection::updateMany : collection::updateOne;
			return fn.apply(filter.expression(), update.expression())
					.getModifiedCount();
		}
		catch (MongoException error) {
			throw new BWFLAException("Updating document(s) failed!", error);
		}
	}

	/** Update single document matching filter */
	public long update(Filter filter, Update update) throws BWFLAException
	{
		return this.update(filter, update, false);
	}

	/** Delete (optionally many) document(s) matching filter */
	public long delete(Filter filter, boolean many) throws BWFLAException
	{
		try {
			final Function<Bson, DeleteResult> fn = (many) ? collection::deleteMany : collection::deleteOne;
			return fn.apply(filter.expression())
					.getDeletedCount();
		}
		catch (MongoException error) {
			throw new BWFLAException("Deleting document(s) failed!", error);
		}
	}

	/** Delete single document matching filter */
	public boolean delete(Filter filter) throws BWFLAException
	{
		return this.delete(filter, false) > 0;
	}

	/** Count collection's documents */
	public long count()
	{
		return this.count(true);
	}

	/** Count (optionally estimate) collection's documents */
	public long count(boolean estimated)
	{
		return (estimated) ? collection.estimatedDocumentCount() : collection.countDocuments();
	}

	/** Count documents passing filter */
	public long count(Filter filter)
	{
		return collection.countDocuments(filter.expression());
	}

	/** Drop this collection */
	public void drop()
	{
		collection.drop();
	}

	/** Return a new document filter */
	public static Filter filter()
	{
		return new Filter();
	}

	/** Return a new document updater */
	public static Update updater()
	{
		return new Update();
	}


	/** Simple wrapper for a collection of documents returned from find() */
	public static class FindResult<T> implements Iterable<T>, AutoCloseable
	{
		private FindIterable<T> result;

		private FindResult(FindIterable<T> iterable)
		{
			this.result = iterable;
		}

		/** Number of documents to skip */
		public FindResult<T> skip(int skip)
		{
			result = result.skip(skip);
			return this;
		}

		/** Maximum number of documents to return */
		public FindResult<T> limit(int limit)
		{
			result = result.limit(limit);
			return this;
		}

		/** Return documents as stream */
		public Stream<T> stream()
		{
			return StreamSupport.stream(result.spliterator(), false);
		}

		@NotNull
		@Override
		public Iterator<T> iterator()
		{
			return result.iterator();
		}

		@Override
		public void close() throws Exception
		{
			result.cursor().close();
		}
	}


	/** Wrapper for a document-filter expression */
	public static class Filter
	{
		private Bson expression;

		private Filter(Bson expression)
		{
			this.expression = expression;
		}

		private Filter()
		{
			this(new Document());
		}

		public Filter withFromTime(String key, long value)
		{
			return this.withFromTime(key, Instant.ofEpochMilli(value));
		}

		public Filter withFromTime(String key, Instant value)
		{
			return this.gte(key, value.toString());
		}

		public Filter withUntilTime(String key, long value, boolean inclusive)
		{
			return this.withUntilTime(key, Instant.ofEpochMilli(value), inclusive);
		}

		public Filter withUntilTime(String key, Instant value, boolean inclusive)
		{
			final BinaryOperator<String> operator = (inclusive) ? Filters::lte : Filters::lt;
			return this.and(operator, key, value.toString());
		}

		public <V> Filter eq(String key, V value)
		{
			return this.and(Filters::eq, key, value);
		}

		public <V> Filter ne(String key, V value)
		{
			return this.and(Filters::ne, key, value);
		}

		public <V> Filter lt(String key, V value)
		{
			return this.and(Filters::lt, key, value);
		}

		public <V> Filter lte(String key, V value)
		{
			return this.and(Filters::lte, key, value);
		}

		public <V> Filter gt(String key, V value)
		{
			return this.and(Filters::gt, key, value);
		}

		public <V> Filter gte(String key, V value)
		{
			return this.and(Filters::gte, key, value);
		}

		public static Filter or(Filter... filters)
		{
			final ArrayList<Bson> expressions = new ArrayList<>(filters.length);
			for (Filter filter : filters)
				expressions.add(filter.expression());

			return new Filter(Filters.or(expressions));
		}

		public Bson expression()
		{
			return expression;
		}

		// ===== Internal Helpers ====================

		private <V> Filter and(BinaryOperator<V> operator, String key, V value)
		{
			final Bson other = operator.apply(key, value);
			expression = Filters.and(expression, other);
			return this;
		}
	}


	/** Wrapper for a document-update expression */
	public static class Update
	{
		private Bson expression;

		private Update(Bson expression)
		{
			this.expression = expression;
		}

		private Update()
		{
			this(new Document());
		}

		public <V> Update set(String key, V value)
		{
			return this.combine(Updates::set, key, value);
		}

		public Update unset(String key)
		{
			return this.combine(Updates.unset(key));
		}

		public Update inc(String key, long value)
		{
			return this.combine(Updates::inc, key, value);
		}

		public <V> Update min(String key, V value)
		{
			return this.combine(Updates::min, key, value);
		}

		public <V> Update max(String key, V value)
		{
			return this.combine(Updates::max, key, value);
		}

		public Update rename(String key, String newkey)
		{
			return this.combine(Updates::rename, key, newkey);
		}

		public Bson expression()
		{
			return expression;
		}

		// ===== Internal Helpers ====================

		private Update combine(Bson other)
		{
			expression = Updates.combine(expression, other);
			return this;
		}

		private <V> Update combine(BinaryOperator<V> operator, String key, V value)
		{
			return this.combine(operator.apply(key, value));
		}
	}


	// ===== Internal Helpers ====================

	private interface BinaryOperator<T> extends BiFunction<String, T, Bson>
	{
		// Empty!
	}

	private FindIterable<T> documents(Filter filter)
	{
		// NOTE: The mandatory field '_id' is removed from returned documents!
		return collection.find(filter.expression())
				.projection(Projections.excludeId());
	}
}
