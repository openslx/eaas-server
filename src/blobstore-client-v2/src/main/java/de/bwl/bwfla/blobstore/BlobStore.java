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
import java.util.logging.Logger;
import java.util.stream.Stream;


public class BlobStore extends TaskExecutor
{
	private final Logger log;
	private final MinioClient minio;


	public Stream<String> list() throws BWFLAException
	{
		final SupplierTask<Stream<String>> op = () -> {
			return minio.listBuckets()
					.stream()
					.map((entry) -> entry.name());
		};

		return this.execute(op, "Listing buckets failed!");
	}

	public Stream<Bucket> buckets() throws BWFLAException
	{
		return this.list()
				.map(this::bucket);
	}

	public Bucket bucket(String name)
	{
		return new Bucket(this, name);
	}

	public Blob blob(String bucket, String name)
	{
		return new Blob(this, bucket, name);
	}

	public Blob blob(BlobHandle handle)
	{
		return this.blob(handle.bucket(), handle.name());
	}

	public static Builder builder()
	{
		return new Builder();
	}

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

	public static String getDefaultBucket()
	{
		return ConfigurationProvider.getConfiguration()
				.get("blobstore.bucket");
	}


	public static class Builder
	{
		private final MinioClient.Builder mcb;
		private Logger logger;

		public Builder()
		{
			this.mcb = MinioClient.builder();
		}

		public Builder credentials(String accessKey, String secretKey)
		{
			mcb.credentials(accessKey, secretKey);
			return this;
		}

		public Builder region(String region)
		{
			mcb.region(region);
			return this;
		}

		public Builder endpoint(String endpoint)
		{
			mcb.endpoint(endpoint);
			return this;
		}

		public Builder endpoint(URL endpoint)
		{
			mcb.endpoint(endpoint);
			return this;
		}

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
