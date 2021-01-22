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

package de.bwl.bwfla.blobstore;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import io.minio.BucketExistsArgs;
import io.minio.DeleteBucketPolicyArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.GetBucketTagsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.SetBucketPolicyArgs;
import io.minio.SetBucketTagsArgs;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Bucket extends TaskExecutor
{
	private final BlobStore storage;
	private final MinioClient minio;
	private final String bucket;


	public BlobStore storage()
	{
		return storage;
	}

	public String name()
	{
		return bucket;
	}


	// ===== Bucket API ====================

	public boolean exists() throws BWFLAException
	{
		final SupplierTask<Boolean> op = () -> {
			final var args = BucketExistsArgs.builder()
					.bucket(bucket)
					.build();

			return minio.bucketExists(args);
		};

		return this.execute(op, "Checking bucket existence failed!");
	}

	public void create() throws BWFLAException
	{
		final RunnableTask op = () -> {
			final var args = MakeBucketArgs.builder()
					.bucket(bucket)
					.build();

			minio.makeBucket(args);
		};

		this.execute(op, "Creating bucket failed!");
	}

	public void remove() throws BWFLAException
	{
		final RunnableTask op = () -> {
			final var args = RemoveBucketArgs.builder()
					.bucket(bucket)
					.build();

			minio.removeBucket(args);
		};

		this.execute(op, "Removing bucket failed!");
	}

	public void remove(boolean recursive) throws BWFLAException
	{
		// Remove all blobs first?
		if (recursive) {
			final Consumer<Blob> deleter = (blob) -> {
				try {
					blob.remove();
				}
				catch (Exception error) {
					storage.logger()
							.log(Level.WARNING, "Removing object failed!", error);
				}
			};

			this.blobs()
					.forEach(deleter);
		}

		// Now, delete the bucket
		this.remove();
	}

	public void remove(Stream<String> blobs) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final var objects = blobs.map(DeleteObject::new);
			final var args = RemoveObjectsArgs.builder()
					.bucket(bucket)
					.objects(objects::iterator)
					.build();

			final var consumer = new DeleteErrorConsumer(storage.logger());
			minio.removeObjects(args)
					.forEach(consumer);

			if (consumer.getNumErrors() > 0)
				throw new BWFLAException();
		};

		this.execute(op, "Removing blob(s) failed!");
	}

	public void remove(Iterator<String> blobs) throws BWFLAException
	{
		final var spliterator = Spliterators.spliteratorUnknownSize(blobs, 0);
		this.remove(StreamSupport.stream(spliterator, false));
	}

	public void remove(Iterable<String> blobs) throws BWFLAException
	{
		this.remove(StreamSupport.stream(blobs.spliterator(), false));
	}

	public Stream<BlobDescription> list() throws BWFLAException
	{
		return this.list(null);
	}

	public Stream<BlobDescription> list(String prefix) throws BWFLAException
	{
		return this.items(prefix)
				.map((item) -> new BlobDescription(bucket, item));
	}

	public Stream<Blob> blobs() throws BWFLAException
	{
		return this.blobs(null);
	}

	public Stream<Blob> blobs(String prefix) throws BWFLAException
	{
		return this.items(prefix)
				.map((item) -> this.blob(item.objectName()));
	}

	public Blob blob(String blob)
	{
		return new Blob(storage, bucket, blob);
	}

	public Map<String, String> tags() throws BWFLAException
	{
		final SupplierTask<Map<String, String>> op = () -> {
			final var args = GetBucketTagsArgs.builder()
					.bucket(bucket)
					.build();

			return minio.getBucketTags(args)
					.get();
		};

		return this.execute(op, "Looking up bucket tags failed!");
	}

	public Bucket setTags(Map<String, String> tags) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final var args = SetBucketTagsArgs.builder()
					.bucket(bucket)
					.tags(tags)
					.build();

			minio.setBucketTags(args);
		};

		this.execute(op, "Tagging bucket failed!");
		return this;
	}

	public String policy() throws BWFLAException
	{
		final SupplierTask<String> op = () -> {
			final var args = GetBucketPolicyArgs.builder()
					.bucket(bucket)
					.build();

			return minio.getBucketPolicy(args);
		};

		return this.execute(op, "Looking up bucket-policy failed!");
	}

	public Bucket setPolicy(String policy) throws BWFLAException
	{
		final RunnableTask op = () -> {
			if (policy != null) {
				final var args = SetBucketPolicyArgs.builder()
						.bucket(bucket)
						.config(policy)
						.build();

				minio.setBucketPolicy(args);
			}
			else {
				final var args = DeleteBucketPolicyArgs.builder()
						.bucket(bucket)
						.build();

				minio.deleteBucketPolicy(args);
			}
		};

		this.execute(op, "Updating bucket-policy failed!");
		return this;
	}


	// ===== Internal Helpers ====================

	Bucket(BlobStore storage, String bucket)
	{
		this.storage = storage;
		this.minio = storage.minio();
		this.bucket = bucket;
	}

	private Stream<Item> items(String prefix) throws BWFLAException
	{
		final SupplierTask<Stream<Item>> op = () -> {
			final var args = ListObjectsArgs.builder()
					.bucket(bucket)
					.prefix(prefix)
					.recursive(true)
					.includeUserMetadata(true)
					.build();

			final Spliterator<Result<Item>> spliterator = minio.listObjects(args)
					.spliterator();

			final Function<Result<Item>, Item> unwrapper = (result) -> {
				try {
					return result.get();
				}
				catch (Exception error) {
					storage.logger()
							.log(Level.WARNING, "Listing blob failed!", error);

					return null;
				}
			};

			return StreamSupport.stream(spliterator, false)
					.map(unwrapper)
					.filter(Objects::nonNull);
		};

		return this.execute(op, "Listing blobs failed!");
	}


	private static class DeleteErrorConsumer implements Consumer<Result<DeleteError>>
	{
		private final Logger log;
		private int numerrors;

		public DeleteErrorConsumer(Logger log)
		{
			this.log = log;
		}

		@Override
		public void accept(Result<DeleteError> result)
		{
			try {
				final var error = result.get();
				log.warning("Removing blob '" + error.objectName() + "' failed! " + error.message());
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Removing blob failed!", error);
			}

			++numerrors;
		}

		public int getNumErrors()
		{
			return numerrors;
		}
	}
}
