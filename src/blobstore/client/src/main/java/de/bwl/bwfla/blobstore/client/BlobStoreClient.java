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

package de.bwl.bwfla.blobstore.client;

import java.net.URL;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.api.blobstore.BlobStoreService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.AbstractServiceClient;


@ApplicationScoped
public class BlobStoreClient extends AbstractServiceClient<BlobStoreService>
{
	private static final String WSDL_URL_TEMPLATE = "%s/blobstore/BlobStore?wsdl";

	public static BlobStoreClient get()
	{
		return CDI.current().select(BlobStoreClient.class).get();
	}

	@Override
	protected BlobStoreService createService(URL url)
	{
		return new BlobStoreService(url);
	}

	@Override
	protected String getWsdlUrl(String host)
	{
		return String.format(WSDL_URL_TEMPLATE, host);
	}

	public BlobStore getBlobStorePort(String host) throws BWFLAException
	{
		return this.getPort(host, BlobStore.class);
	}
}
