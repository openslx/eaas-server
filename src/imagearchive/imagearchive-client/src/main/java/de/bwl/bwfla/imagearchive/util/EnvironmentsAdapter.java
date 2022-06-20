package de.bwl.bwfla.imagearchive.util;

import java.net.URL;
import java.util.List;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.*;


public class EnvironmentsAdapter extends ImageArchiveWSClient {

	public EnvironmentsAdapter(String wsHost) {
		super(wsHost);
	}

	// replace hardcoded EMULATOR_DEFAULT_ARCHIVE with something nicer
	private static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

	public List<ImageGeneralizationPatchDescription> getImageGeneralizationPatches() throws BWFLAException {
		connectArchive();
		return archive.getImageGeneralizationPatches();
	}
	
	@Deprecated
	public ImportImageHandle importImage(String backend, URL ref, ImageArchiveMetadata iaMd, boolean deleteIfExists) throws BWFLAException {
		connectArchive();
		if (ref == null)
			throw new BWFLAException("URL was null");

		String sessionId = archive.importImageFromUrl(backend, ref.toString(), iaMd);
		return new ImportImageHandle(archive, backend, iaMd.getType(), sessionId);
	}

	public String createPatchedImage(String imageId, ImageType type, String patchId) throws BWFLAException {
		return this.createPatchedImage(this.getDefaultBackendName(), imageId, type, patchId);
	}

	public String createPatchedImage(String backend, String imageId, ImageType type, String patchId) throws BWFLAException {
		connectArchive();
		return archive.createPatchedImage(backend, imageId, type, patchId);
	}

	public List<DefaultEntry> getDefaultEnvironments(String backend) throws BWFLAException {
		connectArchive();
		return archive.getDefaultEnvironments(backend).getMap();
	}

	public String getDefaultEnvironment(String osId) throws BWFLAException {
		return this.getDefaultEnvironment(this.getDefaultBackendName(), osId);
	}

	public String getDefaultEnvironment(String backend, String osId) throws BWFLAException {
		connectArchive();
		return archive.getDefaultEnvironment(backend, osId);
	}

	public void setDefaultEnvironment(String osId, String envId) throws BWFLAException {
		this.setDefaultEnvironment(this.getDefaultBackendName(), osId, envId);
	}

	public void setDefaultEnvironment(String backend, String osId, String envId) throws BWFLAException {
		connectArchive();
		archive.setDefaultEnvironment(backend, osId, envId);
	}

	@Deprecated
	public void deleteNameIndexesEntry(String id, String version) throws BWFLAException {
		this.deleteNameIndexesEntry(this.getDefaultBackendName(), id, version);
	}

	@Deprecated
	public void deleteNameIndexesEntry(String backend, String id, String version) throws BWFLAException {
		connectArchive();
		archive.deleteNameIndexesEntry(backend, id, version);
	}

	public class ImportImageHandle {
		private final String sessionId;
		private final ImageType type;
		private final ImageArchiveWS archive;
		private final String backend;

		ImportImageHandle(ImageArchiveWS archive, String backend, ImageType type, String sessionId) {
			this.sessionId = sessionId;
			this.type = type;
			this.archive = archive;
			this.backend = backend;
		}

		public ImageArchiveBinding getBinding() throws ImportNoFinishedException, BWFLAException {
			final ImageImportResult result = archive.getImageImportResult(backend, sessionId);
			if (result == null)
				throw new ImportNoFinishedException();

			return new ImageArchiveBinding(backend, result.getImageId(), type.value());
		}

		public ImageArchiveBinding getBinding(long timeout /* seconds */ ) throws BWFLAException {

			ImageArchiveBinding binding = null;

			while (binding == null) { // will throw a BWFLAException in case of an error
				try {
					if (timeout < 0)
						throw new BWFLAException("getBinding: timeout exceeded");
					binding = getBinding();
					timeout--;
				} catch (EnvironmentsAdapter.ImportNoFinishedException e) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						throw new BWFLAException(e1);
					}
				}
			}
			return binding;
		}
	}

	public EmulatorMetadata extractMetadata(String imageId) throws BWFLAException {
		connectArchive();
		return archive.extractMetadata(EMULATOR_DEFAULT_ARCHIVE, imageId);
	}

	@Deprecated
	public ImageNameIndex getNameIndexes() throws BWFLAException {
		connectArchive();
		return archive.getNameIndexes(getDefaultBackendName());
	}

	@Deprecated
	public ImageNameIndex getNameIndexes(String backend) throws BWFLAException {
		 connectArchive();
		 return archive.getNameIndexes(backend);
	}

	public String resolveEmulatorImage(String imgid) throws BWFLAException {
		return this.resolveImage(EMULATOR_DEFAULT_ARCHIVE, imgid);
	}

	public String resolveImage(String backend, String imgid) throws BWFLAException {
		connectArchive();
		return archive.resolveImage(backend, imgid);
	}

	public static class ImportNoFinishedException extends Exception {  }
}
