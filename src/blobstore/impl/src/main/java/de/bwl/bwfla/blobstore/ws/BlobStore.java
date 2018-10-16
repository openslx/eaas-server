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

package de.bwl.bwfla.blobstore.ws;

import de.bwl.bwfla.blobstore.BlobStoreBackend;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.blobstore.api.IBlobStore;
import de.bwl.bwfla.blobstore.api.Blob;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;
import java.util.logging.Logger;


@MTOM
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@WebService(targetNamespace = "http://bwfla.bwl.de/api/blobstore")
public class BlobStore implements IBlobStore
{
	protected static final Logger LOG = Logger.getLogger(BlobStore.class.getName());

	@Inject
	private BlobStoreBackend backend;


	/* =============== IBlobStore Implementation =============== */

	@Override
	public BlobHandle put(BlobDescription blob) throws BWFLAException
	{
		return backend.put(blob);
	}

	@Override
	public Blob get(BlobHandle handle) throws BWFLAException
	{
		return backend.get(handle);
	}

	@Override
	public void delete(BlobHandle handle) throws BWFLAException
	{
		backend.delete(handle);
	}
}
