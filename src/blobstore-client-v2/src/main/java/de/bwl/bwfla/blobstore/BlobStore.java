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
import io.minio.MinioClient;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.Stream;


/** Client for S3-compatible BlobStore */
public class BlobStore extends TaskExecutor
{
	private final Logger log;
	private final MinioClient minio;


	/** List all available bucket names */
	public Stream<String> list() throws BWFLAException
	{
		final SupplierTask<Stream<String>> op = () -> {
			return minio.listBuckets()
					.stream()
					.map((entry) -> entry.name());
		};

		return this.execute(op, "Listing buckets failed!");
	}

	/** List all available buckets */
	public Stream<Bucket> buckets() throws BWFLAException
	{
		return this.list()
				.map(this::bucket);
	}

	/** Create wrapper for named bucket */
	public Bucket bucket(String name)
	{
		return new Bucket(this, name);
	}

	/** Create wrapper for named blob */
	public Blob blob(String bucket, String name)
	{
		return new Blob(this, bucket, name);
	}

	/** Create wrapper from blob-handle */
	public Blob blob(BlobHandle handle)
	{
		return this.blob(handle.bucket(), handle.name());
	}

	/** Create new path builder */
	public static Path path(String first, String... more)
	{
		return Path.wrap(first, more);
	}

	/** Create new client builder */
	public static Builder builder()
	{
		return new Builder();
	}

	/** Create client with default configuration */
	public static BlobStore create(Logger log) throws BWFLAException
	{
		final Configuration config = ConfigurationProvider.getConfiguration();
		final String endpoint = config.get("blobstore.endpoint");
		final String accesskey = config.get("blobstore.access_key");
		final String secretkey = config.get("blobstore.secret_key");

		return BlobStore.builder()
				.credentials(accesskey, secretkey)
				.endpoint(endpoint)
				.logger(log)
				.build();
	}

	/** Return configured default bucket name */
	public static String getDefaultBucket()
	{
		return ConfigurationProvider.getConfiguration()
				.get("blobstore.bucket");
	}


	/** Client builder for S3-compatible BlobStore */
	public static class Builder
	{
		private final MinioClient.Builder mcb;
		private Logger logger;

		public Builder()
		{
			this.mcb = MinioClient.builder();
		}

		/** Access credentials for backing storage service */
		public Builder credentials(String accessKey, String secretKey)
		{
			mcb.credentials(accessKey, secretKey);
			return this;
		}

		/** Storage service's region */
		public Builder region(String region)
		{
			mcb.region(region);
			return this;
		}

		/** Endpoint for backing storage service */
		public Builder endpoint(String endpoint)
		{
			mcb.endpoint(endpoint);
			return this;
		}

		/** Endpoint for backing storage service */
		public Builder endpoint(URL endpoint)
		{
			mcb.endpoint(endpoint);
			return this;
		}

		/** Logger to use internally */
		public Builder logger(Logger logger)
		{
			this.logger = logger;
			return this;
		}

		public BlobStore build() throws BWFLAException
		{
			if (logger == null)
				logger = Logger.getLogger("BLOB-STORE");

			try {
				return new BlobStore(mcb.build(), logger);
			}
			catch (Exception error) {
				throw new BWFLAException("Initializing MinIO client failed!", error);
			}
		}
	}

	/** Helper class for paths */
	public static class Path implements Iterable<Path>
	{
		// Reuse standard filesystem-based implementation
		private final java.nio.file.Path value;

		/** Return true if this path begins with given path, else false */
		public boolean startswith(String other)
		{
			other = Path.fixroot(other);
			return value.startsWith(other);
		}

		/** Return true if this path begins with given path, else false */
		public boolean startswith(Path other)
		{
			return value.startsWith(other.value);
		}

		/** Return true if this path ends with given path, else false */
		public boolean endswith(String other)
		{
			other = Path.fixroot(other);
			return value.endsWith(other);
		}

		/** Return true if this path ends with given path, else false */
		public boolean endswith(Path other)
		{
			return value.endsWith(other.value);
		}

		/** Resolve given path against this path */
		public Path resolve(String other)
		{
			other = Path.fixroot(other);
			return Path.wrap(value.resolve(other));
		}

		/** Resolve given path against this path */
		public Path resolve(Path other)
		{
			return Path.wrap(value.resolve(other.value));
		}

		/** Eliminate redundant elements */
		public Path normalize()
		{
			return Path.wrap(value.normalize());
		}

		/** Return path's first element */
		public Path first()
		{
			return this.element(0);
		}

		/** Return path's last element */
		public Path last()
		{
			return Path.wrap(value.getFileName());
		}

		/** Return path's parent if it has one, else null */
		public Path parent()
		{
			return Path.wrap(value.getParent());
		}

		/** Return i-th element of this path */
		public Path element(int i)
		{
			return Path.wrap(value.getName(i));
		}

		/** Return subpath of this path */
		public Path subpath(int begin, int end)
		{
			return Path.wrap(value.subpath(begin, end));
		}

		/** Return number of path elements */
		public int count()
		{
			return value.getNameCount();
		}

		/** Return iterator over path elements */
		@Override
		public Iterator<Path> iterator()
		{
			final var iter = value.iterator();
			return new Iterator<>() {
				@Override
				public boolean hasNext()
				{
					return iter.hasNext();
				}

				@Override
				public Path next()
				{
					return Path.wrap(iter.next());
				}
			};
		}

		@Override
		public boolean equals(Object other)
		{
			if (other instanceof Path)
				return value.equals(((Path) other).value);

			return false;
		}

		@Override
		public String toString()
		{
			return value.toString();
		}


		// ===== Internal Helpers ====================

		private Path(java.nio.file.Path path)
		{
			if (path.isAbsolute())
				throw new IllegalArgumentException("Absolute paths are not allowed!");

			this.value = path;
		}

		private static Path wrap(java.nio.file.Path other)
		{
			return (other != null) ? new Path(other) : null;
		}

		private static Path wrap(String first, String... more)
		{
			first = Path.fixroot(first);
			return Path.wrap(java.nio.file.Path.of(first, more));
		}

		private static String fixroot(String path)
		{
			// Absolute paths are not allowed!
			return (path.startsWith("/")) ? path.substring(1) : path;
		}
	}


	// ===== Internal Helpers ====================

	private BlobStore(MinioClient minio, Logger log)
	{
		this.log = log;
		this.minio = minio;
	}

	MinioClient minio()
	{
		return minio;
	}

	Logger logger()
	{
		return log;
	}
}
