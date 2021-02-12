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

package com.openslx.eaas.imagearchive.config;

import com.openslx.eaas.imagearchive.BlobKind;
import de.bwl.bwfla.blobstore.BlobStore;
import de.bwl.bwfla.common.utils.ConfigHelpers;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.inject.api.Config;

import java.util.logging.Logger;


public class StorageLocationConfig extends BaseConfig<StorageLocationConfig>
{
	private String name;
	private String endpoint;
	private String bucket;

	private final BlobStore.Path[] paths = new BlobStore.Path[BlobKind.values().length];


	@Config("name")
	public void setName(String name)
	{
		ConfigHelpers.check(name, "Name is invalid!");
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Config("endpoint")
	public void setEndpoint(String endpoint)
	{
		ConfigHelpers.check(endpoint, "Endpoint name is invalid!");
		this.endpoint = endpoint;
	}

	public String getEndpoint()
	{
		return endpoint;
	}

	@Config("bucket")
	public void setBucket(String bucket)
	{
		ConfigHelpers.check(bucket, "Bucket name is invalid!");
		this.bucket = bucket;
	}

	public String getBucket()
	{
		return bucket;
	}

	public void setPathPrefix(BlobKind kind, String prefix)
	{
		ConfigHelpers.check(prefix, "Path prefix is invalid!");
		this.paths[kind.ordinal()] = BlobStore.path(prefix);
	}

	public void setPathPrefix(String kind, String prefix)
	{
		this.setPathPrefix(BlobKind.from(kind), prefix);
	}

	public BlobStore.Path getPathPrefix(BlobKind kind)
	{
		return paths[kind.ordinal()];
	}

	@Override
	protected StorageLocationConfig load(Configuration config, Logger log)
	{
		super.load(config, log);

		// load path prefixes
		ConfigHelpers.filter(config, "paths.")
				.getProperties()
				.forEach(this::setPathPrefix);

		return this;
	}
}
