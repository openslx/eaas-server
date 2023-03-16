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


import com.openslx.eaas.common.config.util.MemoryUnitParser;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.Directive;
import io.minio.GetObjectArgs;
import io.minio.GetObjectTagsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectConditionalReadArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetObjectTagsArgs;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.http.Method;
import org.apache.tamaya.ConfigurationProvider;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/** Wrapper representing a blob in a bucket */
public class Blob extends TaskExecutor
{
	private final BlobStore storage;
	private final MinioClient minio;
	private final String bucket;
	private final String blob;

	/** Max. number of upload parts supported */
	private static final long UPLOAD_PART_COUNT_LIMIT = 10000;

	/** Default upload part-size */
	private static final long DEFAULT_UPLOAD_PART_SIZE;
	static {
		final var muparser = new MemoryUnitParser();
		final var psvalue = ConfigurationProvider.getConfiguration()
				.get("blobstore.uploader.partsize");

		DEFAULT_UPLOAD_PART_SIZE = muparser.parse(psvalue);
	}

	/** Blob's blobstore */
	public BlobStore storage()
	{
		return storage;
	}

	/** Blob's handle */
	public BlobHandle handle()
	{
		return new BlobHandle(bucket, blob);
	}

	/** Blob's bucket name */
	public String bucket()
	{
		return bucket;
	}

	/** Blob's name */
	public String name()
	{
		return blob;
	}


	// ===== Blob API ====================

	/** Builder for blob-downloading */
	public Downloader downloader()
	{
		return new Downloader();
	}

	/** Builder for blob-uploading */
	public Uploader uploader()
	{
		return new Uploader();
	}

	/** Builder for blob-copying */
	public Copier copier()
	{
		return new Copier();
	}

	/** Remove this blob from backing store */
	public void remove() throws BWFLAException
	{
		final RunnableTask op = () -> {
			final var args = RemoveObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.build();

			minio.removeObject(args);
		};

		this.execute(op, "Deleting blob failed!");
	}

	/** Return true if this blob exists, else false */
	public boolean exists()
	{
		try {
			return this.stat() != null;
		}
		catch (Exception error) {
			return false;
		}
	}

	/** Retrieve blob's description */
	public BlobDescription stat() throws BWFLAException
	{
		final SupplierTask<BlobDescription> op = () -> {
			final var args = StatObjectArgs.builder()
					.bucket(bucket)
					.object(blob)
					.build();

			return new BlobDescription(minio.statObject(args));
		};

		return this.execute(op, "Stating blob failed!");
	}

	/** Retrieve blob's tags */
	public Map<String, String> tags() throws BWFLAException
	{
		final SupplierTask<Map<String, String>> op = () -> {
			final var args = GetObjectTagsArgs.builder()
					.bucket(bucket)
					.object(blob)
					.build();

			return minio.getObjectTags(args)
					.get();
		};

		return this.execute(op, "Looking up blob tags failed!");
	}

	/** Set blob's tags */
	public Blob setTags(Map<String, String> tags) throws BWFLAException
	{
		final RunnableTask op = () -> {
			final var args = SetObjectTagsArgs.builder()
					.bucket(bucket)
					.object(blob)
					.tags(tags)
					.build();

			minio.setObjectTags(args);
		};

		this.execute(op, "Tagging blob failed!");
		return this;
	}

	/** Generate URL for pre-signed GET operation */
	public String newPreSignedGetUrl() throws BWFLAException
	{
		return this.newPreSignedGetUrl(7, TimeUnit.DAYS);
	}

	/** Generate URL for pre-signed GET operation */
	public String newPreSignedGetUrl(int expiry, TimeUnit unit) throws BWFLAException
	{
		return this.newPreSignedUrl(Method.GET, expiry, unit);
	}

	/** Generate URL for pre-signed PUT operation */
	public String newPreSignedPutUrl() throws BWFLAException
	{
		return this.newPreSignedPutUrl(7, TimeUnit.DAYS);
	}

	/** Generate URL for pre-signed PUT operation */
	public String newPreSignedPutUrl(int expiry, TimeUnit unit) throws BWFLAException
	{
		return this.newPreSignedUrl(Method.PUT, expiry, unit);
	}

	/** Generate URL for pre-signed operation */
	public String newPreSignedUrl(AccessMethod method) throws BWFLAException
	{
		return this.newPreSignedUrl(method, 7, TimeUnit.DAYS);
	}

	/** Generate URL for pre-signed operation */
	public String newPreSignedUrl(AccessMethod method, int expiry, TimeUnit unit) throws BWFLAException
	{
		return this.newPreSignedUrl(method.convert(), expiry, unit);
	}

	/** Return builder for copy-sources */
	public static CopySourceBuilder newCopySource()
	{
		return new CopySourceBuilder();
	}

	/** Return default upload part-size */
	public static long getDefaultUploadPartSize()
	{
		return DEFAULT_UPLOAD_PART_SIZE;
	}


	public enum AccessMethod
	{
		HEAD,
		GET,
		PUT,
		POST,
		DELETE;

		private Method convert()
		{
			switch (this) {
				case HEAD:
					return Method.HEAD;
				case GET:
					return Method.GET;
				case PUT:
					return Method.PUT;
				case POST:
					return Method.POST;
				case DELETE:
					return Method.DELETE;
				default:
					throw new IllegalArgumentException();
			}
		}
	}

	public interface RangeBuilder<D extends RangeBuilder<D>>
	{
		D offset(long offset);
		D length(long length);

		default D range(long offset, long length)
		{
			return this.offset(offset)
					.length(length);
		}
	}

	public static class MetaDataBuilder<D extends MetaDataBuilder<D>>
	{
		protected Map<String, String> headers;
		protected Map<String, String> userdata;
		protected Map<String, String> tags;

		/** Add a new header */
		public D header(String name, String value)
		{
			if (headers == null)
				headers = new HashMap<>();

			headers.put(name, value);
			return (D) this;
		}

		/** Add a new user-data entry */
		public D userdata(String name, String value)
		{
			if (userdata == null)
				userdata = new HashMap<>();

			userdata.put(name, value);
			return (D) this;
		}

		/** Add a new tag entry */
		public D tag(String name, String value)
		{
			if (tags == null)
				tags = new HashMap<>();

			tags.put(name, value);
			return (D) this;
		}
	}

	public class Downloader implements RangeBuilder<Downloader>
	{
		private final GetObjectArgs.Builder args;

		private Downloader()
		{
			this.args = GetObjectArgs.builder();
		}

		@Override
		public Downloader offset(long offset)
		{
			args.offset(offset);
			return this;
		}

		@Override
		public Downloader length(long length)
		{
			args.length(length);
			return this;
		}

		/** Execute download operation, returning content as stream */
		public InputStream download() throws BWFLAException
		{
			final var self = Blob.this;

			final SupplierTask<InputStream> op = () -> {
				args.bucket(self.bucket());
				args.object(self.name());
				return minio.getObject(args.build());
			};

			return self.execute(op, "Downloading blob failed!");
		}
	}

	public class Uploader extends MetaDataBuilder<Uploader>
	{
		private Path filename;
		private InputStream stream;
		private long size;
		private long partsize;
		private String contentType;


		private Uploader()
		{
			this.partsize = DEFAULT_UPLOAD_PART_SIZE;
		}

		/** Specify file to be uploaded as content */
		public Uploader filename(Path filename)
		{
			this.filename = filename;
			this.stream = null;
			return this;
		}

		/** Specify stream to be uploaded as content */
		public Uploader stream(InputStream stream)
		{
			return this.stream(stream, -1L);
		}

		/** Specify stream to be uploaded as content */
		public Uploader stream(InputStream stream, long size)
		{
			this.stream = stream;
			this.size = size;
			this.filename = null;
			return this;
		}

		/** Override default part-size */
		public Uploader partsize(long size)
		{
			this.partsize = size;
			return this;
		}

		/** Specify content-type of uploaded data */
		public Uploader contentType(String type)
		{
			this.contentType = type;
			return this;
		}

		/** Execute upload operation */
		public void upload() throws BWFLAException
		{
			final var self = Blob.this;

			final RunnableTask op = () -> {
				if (filename != null) {
					final var args = UploadObjectArgs.builder()
							.filename(filename.toString())
							.bucket(self.bucket())
							.object(self.name())
							.contentType(contentType)
							.userMetadata(userdata)
							.headers(headers)
							.tags(tags)
							.build();

					minio.uploadObject(args);
				}
				else {
					if (size > 0L) {
						// let minio choose part-size if part-count limit is exceeded!
						final var partcount = 1L + size / partsize;
						if (partcount > UPLOAD_PART_COUNT_LIMIT)
							partsize = -1L;
					}

					final var args = PutObjectArgs.builder()
							.stream(stream, size, partsize)
							.bucket(self.bucket())
							.object(self.name())
							.contentType(contentType)
							.userMetadata(userdata)
							.headers(headers)
							.tags(tags)
							.build();

					minio.putObject(args);
				}
			};

			self.execute(op, "Uploading blob failed!");
		}
	}

	public class Copier extends MetaDataBuilder<Copier>
	{
		private final List<CopySourceBuilder> sources;
		private boolean multipart;

		private Copier()
		{
			this.sources = new ArrayList<>();
			this.multipart = false;
		}

		/**
		 * Optionally enable multipart mode
		 * <br><br>
		 * <b>NOTE:</b> Multipart mode must be explicitly enabled if copy size exceeds 5GB!
		 */
		public Copier multipart(boolean enabled)
		{
			this.multipart = enabled;
			return this;
		}

		/** Specify source to copy from */
		public Copier source(String bucket, String blob)
		{
			final var source = new CopySourceBuilder()
					.bucket(bucket)
					.blob(blob);

			return this.source(source);
		}

		/** Specify source to copy from */
		public Copier source(Blob blob)
		{
			return this.source(blob.bucket(), blob.name());
		}

		/** Specify source to copy from */
		public Copier source(CopySourceBuilder source)
		{
			sources.add(source);
			return this;
		}

		/** Execute server-side copy operation */
		public void copy() throws BWFLAException
		{
			if (multipart || sources.size() > 1)
				this.copyMultiPart();
			else this.copySinglePart();
		}

		private void copySinglePart() throws BWFLAException
		{
			final var self = Blob.this;

			final RunnableTask op = () -> {
				final var source = sources.get(0)
						.toCopySource();

				final var args = CopyObjectArgs.builder()
						.bucket(self.bucket())
						.object(self.name())
						.source(source);

				if (userdata != null) {
					args.metadataDirective(Directive.REPLACE)
							.userMetadata(userdata);
				}

				if (headers != null) {
					args.taggingDirective(Directive.REPLACE)
							.headers(headers);
				}

				if (tags != null) {
					args.taggingDirective(Directive.REPLACE)
							.tags(tags);
				}

				minio.copyObject(args.build());
			};

			self.execute(op, "Copying blob failed!");
		}

		private void copyMultiPart() throws BWFLAException
		{
			final var self = Blob.this;

			final RunnableTask op = () -> {
				final var blobs = sources.stream()
						.map(CopySourceBuilder::toComposeSource)
						.collect(Collectors.toList());

				final var args = ComposeObjectArgs.builder()
						.bucket(self.bucket())
						.object(self.name())
						.sources(blobs);

				if (userdata != null)
					args.userMetadata(userdata);

				if (headers != null)
					args.headers(headers);

				if (tags != null)
					args.tags(tags);

				minio.composeObject(args.build());
			};

			self.execute(op, "Copying multipart blob failed!");
		}
	}

	public static class CopySourceBuilder implements RangeBuilder<CopySourceBuilder>
	{
		private String bucket;
		private String blob;
		private long offset;
		private long length;

		private CopySourceBuilder()
		{
			this.bucket = null;
			this.blob = null;
			this.offset = -1L;
			this.length = -1L;
		}

		public CopySourceBuilder bucket(String name)
		{
			this.bucket = name;
			return this;
		}

		public CopySourceBuilder blob(String name)
		{
			this.blob = name;
			return this;
		}

		public CopySourceBuilder blob(Blob source)
		{
			return this.bucket(source.bucket())
					.blob(source.name());
		}

		@Override
		public CopySourceBuilder offset(long offset)
		{
			this.offset = offset;
			return this;
		}

		@Override
		public CopySourceBuilder length(long length)
		{
			this.length = length;
			return this;
		}

		private CopySource toCopySource()
		{
			final var source = CopySource.builder();
			this.setup(source);
			return source.build();
		}

		private ComposeSource toComposeSource()
		{
			final var source = ComposeSource.builder();
			this.setup(source);
			return source.build();
		}

		private void setup(ObjectConditionalReadArgs.Builder<?,?> source)
		{
			source.bucket(bucket);
			source.object(blob);

			if (offset >= 0L)
				source.offset(offset);

			if (length > 0L)
				source.length(length);
		}
	}


	// ===== Internal Helpers ====================

	Blob(BlobStore storage, String bucket, String blob)
	{
		this.storage = storage;
		this.minio = storage.minio();
		this.bucket = bucket;
		this.blob = blob;
	}

	private String newPreSignedUrl(Method method, int expiry, TimeUnit unit) throws BWFLAException
	{
		final SupplierTask<String> op = () -> {
			final var args = GetPresignedObjectUrlArgs.builder()
					.method(method)
					.bucket(bucket)
					.object(blob)
					.expiry(expiry, unit)
					.build();

			return minio.getPresignedObjectUrl(args);
		};

		return this.execute(op, "Generating pre-signed URL for " + method.name() + " failed!");
	}
}
