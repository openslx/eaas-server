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
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class BlobStoreClient
{
	private final Logger log;
	private final MinioClient minio;
	private final URL endpoint;


	public URL getEndpointUrl()
	{
		return endpoint;
	}

	public boolean exists(String bucket) throws BWFLAException
	{
		final SupplierTask<Boolean> op = () -> {
			final BucketExistsArgs args = BucketExistsArgs.builder()
					.bucket(bucket)
					.build();

			return minio.bucketExists(args);
		};

		return this.execute(op, "Checking bucket existence failed!");
	}

	public BlobStoreClient make(String bucket) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final MakeBucketArgs args = MakeBucketArgs.builder()
					.bucket(bucket)
					.build();

			minio.makeBucket(args);
		};

		return this.execute(op, "Creating bucket failed!");
	}

	public BlobStoreClient delete(String bucket) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final RemoveBucketArgs args = RemoveBucketArgs.builder()
					.bucket(bucket)
					.build();

			minio.removeBucket(args);
		};

		return this.execute(op, "Deleting bucket failed!");
	}

	public BlobStoreClient delete(String bucket, boolean recursive) throws BWFLAException
	{
		// Remove all blobs first?
		if (recursive) {
			final Consumer<String> deleter = (blob) -> {
				try {
					this.delete(bucket, blob);
				}
				catch (Exception error) {
					// Ignore it and proceed!
				}
			};

			this.blobs(bucket)
					.forEach(deleter);
		}

		// Now, delete the bucket
		return this.delete(bucket);
	}

	public BlobStoreClient delete(String bucket, String blob) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final RemoveObjectArgs args = RemoveObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.build();

			minio.removeObject(args);
		};

		return this.execute(op, "Deleting blob failed!");
	}

	public BlobStoreClient delete(BlobHandle handle) throws BWFLAException
	{
		return this.delete(handle.bucket(), handle.name());
	}

	public BlobStoreClient delete(String bucket, Iterable<String> blobs) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final Stream<DeleteObject> objects = StreamSupport.stream(blobs.spliterator(), false)
					.map(DeleteObject::new);

			final RemoveObjectsArgs args = RemoveObjectsArgs.builder()
					.bucket(bucket)
					.objects(objects::iterator)
					.build();

			minio.removeObjects(args);
		};

		return this.execute(op, "Deleting blob failed!");
	}

	public InputStream get(String bucket, String blob) throws BWFLAException
	{
		final SupplierTask<InputStream> op = () -> {
			final GetObjectArgs args = GetObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.build();

			return minio.getObject(args);
		};

		return this.execute(op, "Downloading blob failed!");
	}

	public InputStream get(BlobHandle handle) throws BWFLAException
	{
		return this.get(handle.bucket(), handle.name());
	}

	public InputStream get(String bucket, String blob, long offset) throws BWFLAException
	{
		final SupplierTask<InputStream> op = () -> {
			final GetObjectArgs args = GetObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.offset(offset)
					.build();

			return minio.getObject(args);
		};

		return this.execute(op, "Downloading blob failed!");
	}

	public InputStream get(BlobHandle handle, long offset) throws BWFLAException
	{
		return this.get(handle.bucket(), handle.name(), offset);
	}

	public InputStream get(String bucket, String blob, long offset, long length) throws BWFLAException
	{
		final SupplierTask<InputStream> op = () -> {
			final GetObjectArgs args = GetObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.offset(offset)
					.length(length)
					.build();

			return minio.getObject(args);
		};

		return this.execute(op, "Downloading blob failed!");
	}

	public InputStream get(BlobHandle handle, long offset, long length) throws BWFLAException
	{
		return this.get(handle.bucket(), handle.name(), offset, length);
	}

	public BlobStoreClient put(String bucket, String blob, Path filename) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final UploadObjectArgs args = UploadObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.filename(filename.toString())
					.build();

			minio.uploadObject(args);
		};

		return this.execute(op, "Uploading blob from file failed!");
	}

	public BlobStoreClient put(String bucket, String blob, Path filename, String contentType) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final UploadObjectArgs args = UploadObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.filename(filename.toString())
					.contentType(contentType)
					.build();

			minio.uploadObject(args);
		};

		return this.execute(op, "Uploading blob from file failed!");
	}

	public BlobStoreClient put(String bucket, String blob, InputStream data, String contentType) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final PutObjectArgs args = PutObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.stream(data, -1L, 10L*1024L*1024L)
					.contentType(contentType)
					.build();

			minio.putObject(args);
		};

		return this.execute(op, "Uploading blob from stream failed!");
	}

	public BlobStoreClient put(String bucket, String blob, InputStream data, long size, String contentType) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final PutObjectArgs args = PutObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.stream(data, size, -1L)
					.contentType(contentType)
					.build();

			minio.putObject(args);
		};

		return this.execute(op, "Uploading blob from stream failed!");
	}

	public BlobInfo stat(String bucket, String blob) throws BWFLAException
	{
		final SupplierTask<BlobInfo> op = () -> {
			final StatObjectArgs args = StatObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.build();

			return new BlobInfo(minio.statObject(args));
		};

		return this.execute(op, "Stating blob failed!");
	}

	public BlobInfo stat(BlobHandle handle) throws BWFLAException
	{
		return this.stat(handle.bucket(), handle.name());
	}

	public Stream<String> buckets() throws BWFLAException
	{
		final SupplierTask<Stream<String>> op = () -> minio.listBuckets()
					.stream()
					.map(Bucket::name);

		return this.execute(op, "Listing buckets failed!");
	}

	public Stream<String> blobs(String bucket) throws BWFLAException
	{
		return this.blobs(bucket, null);
	}

	public Stream<String> blobs(String bucket, String prefix) throws BWFLAException
	{
		final SupplierTask<Stream<String>> op = () -> {
			final Spliterator<Result<Item>> spliterator = minio.listObjects(bucket, prefix)
					.spliterator();

			final Function<Result<Item>, String> converter = (result) -> {
				try {
					return result.get().objectName();
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Accessing blob description failed!", error);
					return null;
				}
			};

			return StreamSupport.stream(spliterator, false)
					.map(converter)
					.filter(Objects::nonNull);
		};

		return this.execute(op, "Listing blobs failed!");
	}

	public String getBlobUrl(String bucket, String blob) throws BWFLAException
	{
		final SupplierTask<String> op = () -> minio.getObjectUrl(bucket, blob);
		return this.execute(op, "Generating URL for downloading failed!");
	}

	public String getBlobUrl(BlobHandle handle) throws BWFLAException
	{
		return this.getBlobUrl(handle.bucket(), handle.name());
	}

	public String newPreSignedGet(String bucket, String blob) throws BWFLAException
	{
		return this.newPreSignedGet(bucket, blob, 7, TimeUnit.DAYS);
	}

	public String newPreSignedGet(BlobHandle handle) throws BWFLAException
	{
		return this.newPreSignedGet(handle.bucket(), handle.name());
	}

	public String newPreSignedGet(String bucket, String blob, int expiry, TimeUnit unit) throws BWFLAException
	{
		final SupplierTask<String> op = () -> {
			final GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
					.method(Method.GET)
					.bucket(bucket)
					.object(blob)
					.expiry(expiry, unit)
					.build();

			return minio.getPresignedObjectUrl(args);
		};

		return this.execute(op, "Generating pre-signed URL for downloading failed!");
	}

	public String newPreSignedGet(BlobHandle handle, int expiry, TimeUnit unit) throws BWFLAException
	{
		return this.newPreSignedGet(handle.bucket(), handle.name(), expiry, unit);
	}

	public String newPreSignedPut(String bucket, String blob) throws BWFLAException
	{
		return this.newPreSignedPut(bucket, blob, 7, TimeUnit.DAYS);
	}

	public String newPreSignedPut(BlobHandle handle) throws BWFLAException
	{
		return this.newPreSignedPut(handle.bucket(), handle.name());
	}

	public String newPreSignedPut(String bucket, String blob, int expiry, TimeUnit unit) throws BWFLAException
	{
		final SupplierTask<String> op = () -> {
			final GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
					.method(Method.PUT)
					.bucket(bucket)
					.object(blob)
					.expiry(expiry, unit)
					.build();

			return minio.getPresignedObjectUrl(args);
		};

		return this.execute(op, "Generating pre-signed URL for uploading failed!");
	}

	public String newPreSignedPut(BlobHandle handle, int expiry, TimeUnit unit) throws BWFLAException
	{
		return this.newPreSignedPut(handle.bucket(), handle.name(), expiry, unit);
	}


	public static BlobStoreClient create(Logger log) throws BWFLAException
	{
		final Configuration config = ConfigurationProvider.getConfiguration();
		final String endpoint = config.get("blobstore.endpoint");
		final String accesskey = config.get("blobstore.access_key");
		final String secretkey = config.get("blobstore.secret_key");

		return BlobStoreClient.create(endpoint, accesskey, secretkey, log);
	}

	public static BlobStoreClient create(String endpoint, String accessKey, String secretKey, Logger log) throws BWFLAException
	{
		return BlobStoreClient.create(endpoint, accessKey, secretKey, null, log);
	}

	public static BlobStoreClient create(String endpoint, String accessKey, String secretKey, String region, Logger log) throws BWFLAException
	{
		return new BlobStoreClient(endpoint, accessKey, secretKey, region, log);
	}

	public static String getDefaultBucket()
	{
		return ConfigurationProvider.getConfiguration()
				.get("blobstore.bucket");
	}


	// ========== Internal Helpers ====================

	@FunctionalInterface
	private interface RunnableTask
	{
		void run() throws Exception;
	}

	@FunctionalInterface
	private interface SupplierTask<T>
	{
		T get() throws Exception;
	}

	private BlobStoreClient(String endpoint, String accessKey, String secretKey, String region, Logger log) throws BWFLAException
	{
		this.log = log;
		try {
			this.endpoint = new URL(endpoint);
			this.minio = MinioClient.builder()
					.credentials(accessKey, secretKey)
					.endpoint(endpoint)
					.region(region)
					.build();

		}
		catch (Exception error) {
			throw new BWFLAException("Initializing Minio client failed!", error);
		}
	}

	private BlobStoreClient execute(RunnableTask task, String errmsg) throws BWFLAException
	{
		try {
			task.run();
		}
		catch (Exception error) {
			throw new BWFLAException(errmsg, error);
		}

		return this;
	}

	private <T> T execute(SupplierTask<T> task, String errmsg) throws BWFLAException
	{
		try {
			return task.get();
		}
		catch (Exception error) {
			throw new BWFLAException(errmsg, error);
		}
	}
}
