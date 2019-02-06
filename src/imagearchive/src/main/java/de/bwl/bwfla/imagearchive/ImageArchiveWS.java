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

package de.bwl.bwfla.imagearchive;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;

import de.bwl.bwfla.imagearchive.ImageIndex.Alias;
import de.bwl.bwfla.imagearchive.ImageIndex.Entry;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageNameIndex;
import de.bwl.bwfla.imagearchive.datatypes.ImageImportResult;
import org.jboss.ejb3.annotation.TransactionTimeout;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.guacplay.replay.IWDMetaData;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata;


@Stateless
@MTOM
@WebService(targetNamespace = "http://bwfla.bwl.de/api/imagearchive")
public class ImageArchiveWS
{
	@Inject
	private ImageArchiveRegistry backends = null;


	/* ========================= Public API ========================= */

	public void deleteTempEnvironments(String backend) throws BWFLAException
	{
		this.lookup(backend)
			.deleteTempEnvironments();
	}

	public void reload(String backend) throws BWFLAException
	{
		this.lookup(backend)
				.reload();
	}

	public String getExportPrefix(String backend) throws BWFLAException
	{
		return this.lookup(backend)
				.getExportPrefix();
	}

	public String getEnvironmentById(String backend, String id) throws BWFLAException
	{

		return this.lookup(backend)
				.getEnvironmentById(id);
	}

	public List<String> getEnvironments(String backend, String type) throws BWFLAException
	{
		return this.lookup(backend)
				.getEnvironments(type);
	}

	public boolean deleteImage(String backend, String imageId, String type) throws BWFLAException
	{
		return this.lookup(backend)
				.deleteImage(imageId, type);
	}
	
	public boolean deleteMetadata(String backend, String envId) throws BWFLAException
	{
		return this.lookup(backend)
				.deleteMetadata(envId);
	}

	public String importImageFromUrl(String backend, URL url, ImageArchiveMetadata request) throws BWFLAException
	{
		return this.lookup(backend)
				.importImageFromUrl(url, request);
	}

	@TransactionTimeout(value = 1, unit = TimeUnit.DAYS)
	public String importImageAsStream(String backend, @XmlMimeType("application/octet-stream") DataHandler image, ImageArchiveMetadata iaMd) throws BWFLAException
	{
		return this.lookup(backend)
				.importImageAsStream(image, iaMd);
	}

	public ImageImportResult getImageImportResult(String backend, String sessionId) throws BWFLAException
	{
		return this.lookup(backend)
				.getImageImportResult(sessionId);
	}

	public String generalizedImport(String backend, String imageId, ImageType type, String templateId) throws BWFLAException
	{
		return this.lookup(backend)
				.generalizedImport(imageId, type, templateId);
	}

	public String createImage(String backend, String size, String type) throws BWFLAException
	{
		return this.lookup(backend)
				.createImage(size, type);
	}

	public void updateConfiguration(String backend, String conf) throws BWFLAException
	{
		this.lookup(backend)
				.updateConfiguration(conf);
	}

	public void importConfiguration(String backend, String conf, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException
	{
		this.lookup(backend)
				.importConfiguration(conf, iaMd, deleteIfExists);
	}

    public void commitTempEnvironment(String backend, String id) throws BWFLAException
	{
		this.lookup(backend)
				.commitTempEnvironment(id);
	}

	public void commitTempEnvironmentWithCustomType(String backend, String id, String type) throws BWFLAException
	{
		this.lookup(backend)
				.commitTempEnvironmentWithCustomType(id, type);
	}

	public String getRecording(String backend, String envId, String traceId) throws BWFLAException
	{
		return this.lookup(backend)
				.getRecording(envId, traceId);
	}

	public List<IWDMetaData> getRecordings(String backend, String envId) throws BWFLAException
	{
		return this.lookup(backend)
				.getRecordings(envId);
	}

	public boolean addRecordingFile(String backend, String envId, String traceId, String data) throws BWFLAException
	{
		return this.lookup(backend)
				.addRecordingFile(envId, traceId, data);
	}

	public String getDefaultEnvironment(String backend, String osId) throws BWFLAException
	{
		return this.lookup(backend)
				.getDefaultEnvironment(osId);
	}

	public void setDefaultEnvironment(String backend, String osId, String envId) throws BWFLAException
	{
		this.lookup(backend)
				.setDefaultEnvironment(osId, envId);
	}

	public String getImageBinding(String backend, String name, String version) throws BWFLAException
	{
		return this.lookup(backend)
				.getImageBinding(name, version);
	}

	public List<String> replicateImages(String backend, List<String> images) throws BWFLAException
	{
		return this.lookup(backend)
				.replicateImages(images);
	}

	public ImageNameIndex getNameIndexes(String backend) throws BWFLAException {
		return this.lookup(backend)
				.getNameIndexes();
	}

	public void addNameIndexesEntry(String backend, Entry entry, Alias alias) throws BWFLAException {
		this.lookup(backend)
				.addNameIndexesEntry(entry, alias);
	}

	public String getDefaultBackendName()
	{
		return backends.getImageArchiveConfig().getDefaultBackendName();
	}

	public Collection<String> listBackendNames()
	{
			return backends.listBackendNames();
	}

	/* ========================= Internal Helpers ========================= */

	private ImageArchiveBackend lookup(String name) throws BWFLAException
	{
		final ImageArchiveBackend backend = backends.lookup(name);
		if (backend == null)
			throw new BWFLAException("ImageArchive's backend not found: " + name);

		return backend;
	}
}
