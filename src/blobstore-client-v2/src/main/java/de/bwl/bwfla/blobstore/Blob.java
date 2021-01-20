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
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.http.Method;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Blob extends TaskExecutor
{
	private final BlobStore storage;
	private final MinioClient minio;
	private final String bucket;
	private final String blob;


	public BlobStore storage()
	{
		return storage;
	}

	public BlobHandle handle()
	{
		return new BlobHandle(bucket, blob);
	}

	public String bucket()
	{
		return bucket;
	}

	public String name()
	{
		return blob;
	}


	// ===== Blob API ====================

	public Downloader downloader()
	{
		return new Downloader();
	}

	public Uploader uploader()
	{
		return new Uploader();
	}

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

	public String newPreSignedGetUrl() throws BWFLAException
	{
		return this.newPreSignedGetUrl(7, TimeUnit.DAYS);
	}

	public String newPreSignedGetUrl(int expiry, TimeUnit unit) throws BWFLAException
	{
		return this.newPreSignedUrl(Method.GET, expiry, unit, "downloading");
	}

	public String newPreSignedPutUrl() throws BWFLAException
	{
		return this.newPreSignedPutUrl(7, TimeUnit.DAYS);
	}

	public String newPreSignedPutUrl(int expiry, TimeUnit unit) throws BWFLAException
	{
		return this.newPreSignedUrl(Method.PUT, expiry, unit, "uploading");
	}


	public class Downloader
	{
		private final GetObjectArgs.Builder args;

		private Downloader()
		{
			this.args = GetObjectArgs.builder();
		}

		public Downloader offset(long offset)
		{
			args.offset(offset);
			return this;
		}

		public Downloader length(long length)
		{
			args.length(length);
			return this;
		}

		public Downloader range(long offset, long length)
		{
			return this.offset(offset)
					.length(length);
		}

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

	public class Uploader
	{
		private Path filename;
		private InputStream stream;
		private long size;
		private Map<String, String> userdata;
		private Map<String, String> tags;
		private String contentType;


		private Uploader()
		{
			// Empty!
		}

		public Uploader filename(Path filename)
		{
			this.filename = filename;
			this.stream = null;
			return this;
		}

		public Uploader stream(InputStream stream)
		{
			return this.stream(stream, -1L);
		}

		public Uploader stream(InputStream stream, long size)
		{
			this.stream = stream;
			this.size = size;
			this.filename = null;
			return this;
		}

		public Uploader contentType(String type)
		{
			this.contentType = type;
			return this;
		}

		public Uploader userdata(String name, String value)
		{
			if (userdata == null)
				userdata = new HashMap<>();

			userdata.put(name, value);
			return this;
		}

		public Uploader tag(String name, String value)
		{
			if (tags == null)
				tags = new HashMap<>();

			tags.put(name, value);
			return this;
		}

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
							.tags(tags)
							.build();

					minio.uploadObject(args);
				}
				else {
					final var args = PutObjectArgs.builder()
							.stream(stream, size, 128L*1024L*1024L)
							.bucket(self.bucket())
							.object(self.name())
							.contentType(contentType)
							.userMetadata(userdata)
							.tags(tags)
							.build();

					minio.putObject(args);
				}
			};

			self.execute(op, "Uploading blob failed!");
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

	private String newPreSignedUrl(Method method, int expiry, TimeUnit unit, String action) throws BWFLAException
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

		return this.execute(op, "Generating pre-signed URL for " + action + " failed!");
	}
}
