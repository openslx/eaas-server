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

package de.bwl.bwfla.emucomp.components;

import com.openslx.eaas.resolver.DataResolvers;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.ConfigurationProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class BindingsManager
{
	private final Logger log;

	private final Map<String, Binding> bindings;
	private final Map<String, String> paths;
	private final String objectArchiveAddress;
	private final ImageMounter imageMounter;

	public enum EntryType
	{
		IMAGE("image"),
		ALIAS("alias"),
		FS_MOUNT("fsmnt"),
		RAW_MOUNT("rawmnt");

		private final String value;

		EntryType(String value)
		{
			this.value = value;
		}
	}

	private static final String ID_SEPARATOR = "/_____";

	public BindingsManager()
	{
		this(Logger.getLogger(BindingsManager.class.getName()));
	}

	public BindingsManager(Logger log)
	{
		this.bindings = new HashMap<String, Binding>();
		this.paths = new LinkedHashMap<String, String>();
		this.log = log;
		this.objectArchiveAddress = ConfigurationProvider.getConfiguration().get("ws.objectarchive");
		this.imageMounter = new ImageMounter(log);
	}

	/** Returns all registered bindings: binding's ID -> binding object */
	public Map<String, Binding> entries()
	{
		return Collections.unmodifiableMap(bindings);
	}

	/** Returns all mountpoints: binding's ID -> mountpoint */
	public Map<String, String> mountpoints()
	{
		return Collections.unmodifiableMap(paths);
	}

	/** Returns all mountpoints: binding's ID -> binding's access path */
	public Stream<Map.Entry<String, String>> paths()
	{
		return paths.entrySet().stream()
				.filter((entry) -> !entry.getKey().contains(ID_SEPARATOR));
	}

	/** Returns a binding by its ID. */
	public Binding get(String id)
	{
		return bindings.get(id);
	}

	/** Registers a new binding. */
	public void register(AbstractDataResource resource) throws BWFLAException
	{
		final String id = resource.getId();
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException("Invalid resource's ID!");

		if (resource instanceof Binding) {
			this.put(id, (Binding) resource);
		}
		else if (resource instanceof ObjectArchiveBinding) {
			// If the resource is an ArchiveBinding, query the archive
			// and add all entries from the file collection
			final ObjectArchiveBinding object = (ObjectArchiveBinding) resource;
			final ObjectArchiveHelper helper = new ObjectArchiveHelper(this.objectArchiveAddress);
			final FileCollection fc = helper.getObjectReference(object.getArchive(), object.getId());
			if (fc == null || fc.id == null || fc.id.isEmpty())
				throw new BWFLAException("Retrieving object meta data failed!");

			for (FileCollectionEntry link : fc.files) {
				if (link.getId() == null || link.getUrl() == null)
					continue;

				this.put(id + "/" + link.getId(), link);
			}
		}
		else {
			final String clazz = resource.getClass().getName();
			throw new IllegalArgumentException("Unsupported resource type: " + clazz);
		}
	}

	/** Returns path to the binding's image, or null if not mounted or avalilable. */
	public String lookup(String binding)
	{
		final String prefix = "binding://";
		if (binding.startsWith(prefix))
			binding = binding.substring(prefix.length());

		return paths.get(binding);
	}

	/** Returns a collection of all binding-ids starting with prefix. */
	public Stream<String> find(String prefix)
	{
		return bindings.keySet().stream()
				.filter((id) -> id.startsWith(prefix));
	}

	private static void prepareResourceBinding(String componentId, Binding resource) throws IllegalArgumentException
	{
		/*
			ImageArchive Bindings contain no valid URLs, just image IDs
			We delegate the resolving image IDs to the proxy.
			Other resources may contain valid URLs.
		 */
		if (resource instanceof ImageArchiveBinding)
		{
			final String location;
			if ("emucon-rootfs".equals(resource.getId())) {
				location = DataResolvers.emulators()
						.resolve((ImageArchiveBinding) resource);
			}
			else {
				location = DataResolvers.images()
						.resolve(componentId, (ImageArchiveBinding) resource);
			}

			resource.setUrl(location);
		}

		// Resolve object-archive's binding URLs!
		if (resource instanceof FileCollectionEntry)
		{
			final var location = DataResolvers.objects()
					.resolve(componentId, (FileCollectionEntry) resource);

			resource.setUrl(location);
		}

		if (resource.getId() == null
				|| resource.getId().isEmpty()
				|| resource.getUrl() == null
				|| resource.getUrl().isEmpty())
			throw new IllegalArgumentException(
					"Given resource is null, has invalid id or empty url.");

		if (resource.getAccess() == null)
			resource.setAccess(Binding.AccessType.COW);
	}

	/**
	 * Resolves and mounts a binding location of either the form
	 * binding://<binding_id>> or <binding_id>.
	 *
	 * The <binding_id> is replaced with the actual filesystem location of the binding's mountpoint.
	 *
	 * @param binding  A binding location
	 * @return The resolved path or null, if the binding cannot be found
	 */
	public String mount(String componentId, String binding, Path outdir)
			throws BWFLAException, IOException, IllegalArgumentException
	{
		if (binding == null || binding.isEmpty())
			throw new IllegalArgumentException("Binding is null or empty!");

		if (binding.startsWith("rom://"))
			binding = "rom-" + binding.substring("rom://".length());

		if (binding.startsWith("binding://"))
			binding = binding.substring("binding://".length());

		// if (binding.contains("/"))
		// 	throw new BWFLAException("Subresource bindings are currently not supported!");

		// Let's see if we already have the resource mounted
		String resourcePath = this.lookup(binding);
		if (resourcePath != null)
			return resourcePath;

		log.info("Resolving resource '" + binding + "'...");

		final String realBindingId = binding;
		final Binding resource = bindings.get(realBindingId);
		if (resource == null)
			throw new BWFLAException("Could not find binding for resource " + binding);

		log.info("Mounting binding '" + binding + "'...");

		prepareResourceBinding(componentId, resource);

		// TODO: we need to resolve the full path here or earlier to
		// ensure that all access options use the same path:
		if(binding.startsWith("rom-")) // old rom bindings do not have COPY access by default
		{
			log.info("guessing resource is a ROM. force COPY access if not QCOW");

			// check the file type first.
			ImageInformation inf = new ImageInformation(resource.getUrl(), log);
			ImageInformation.QemuImageFormat fmt = inf.getFileFormat();
			if(fmt != ImageInformation.QemuImageFormat.QCOW2)
				resource.setAccess(Binding.AccessType.COPY);
		}

		final MountOptions mountOpts = new MountOptions();
		if (resource.getFileSize() > 0)
			mountOpts.setSize(resource.getFileSize());

		Path imgPath = null;
		ImageMounter.Mount mount = null;
		switch (resource.getAccess()) {
			case COW:
				imgPath = outdir.resolve(realBindingId + ".cow");

				QcowOptions qcowOptions = new QcowOptions();
				qcowOptions.setBackingFile(resource.getUrl());

				EmulatorUtils.createCowFile(imgPath, qcowOptions);

				Path rawImagePath = outdir.resolve(realBindingId + ".dd");
				mount = imageMounter.mount(imgPath, rawImagePath, mountOpts);

				this.put(realBindingId, EntryType.RAW_MOUNT, resourcePath);
				resourcePath = mount.getMountPoint().toAbsolutePath().toString();
				break;
			case COPY:
				imgPath = outdir.resolve(realBindingId + ".copy");
				EmulatorUtils.copyRemoteUrl(resource, imgPath, log);
				resourcePath =imgPath.toString();
				break;
		}
		this.put(realBindingId, EntryType.IMAGE, imgPath.toAbsolutePath().toString());

		// resourcePath is now the base path for the binding we want to find
		if (resourcePath == null || !(new File(resourcePath).canRead())) {
			final String message = "Binding target at location "
					+ resource.getUrl() + " cannot be accessed!";

			throw new BWFLAException(message);
		}

		// Is the raw file a block device with a filesystem?
		final String fsType = this.getImageFileSystem(resource);
		if (mount != null && fsType != null && !fsType.isEmpty()) {
			final Path mountpoint = outdir.resolve(realBindingId + "." + fsType.replace(',', '.') + ".fuse");
			imageMounter.mount(mount, mountpoint, FileSystemType.fromString(fsType));
			resourcePath = mountpoint.toString();
			this.put(realBindingId, EntryType.FS_MOUNT, resourcePath);
		}

		// Is local alias specified?
		final String alias = resource.getLocalAlias();
		if (alias != null) {
			final Path link = outdir.resolve(alias);
			Files.deleteIfExists(link);
			Files.createSymbolicLink(link, Paths.get(resourcePath));
			resourcePath = link.toString();

			this.put(realBindingId, EntryType.ALIAS, resourcePath);
		}

		this.add(realBindingId, resourcePath);
		return resourcePath;
	}

	/** Unmounts all registered bindings */
	public void cleanup()
	{
		log.info("Unmounting bindings...");
		{
			final Set<String> idsToRemove = new HashSet<String>();

			final String[] fuseIdSuffixes = new String[] {
					ID_SEPARATOR + EntryType.RAW_MOUNT.value,
					ID_SEPARATOR + EntryType.FS_MOUNT.value,
			};

			paths.forEach((id, path) -> {
				for (String suffix : fuseIdSuffixes) {
					if (!id.endsWith(suffix))
						continue;  // Not a FUSE-mount!

					final int end = id.length() - suffix.length();
					idsToRemove.add(id.substring(0, end));
				}
			});

			imageMounter.unmount();
			idsToRemove.forEach((id) -> this.remove(id));
			idsToRemove.clear();
		}
	}

	public static String toBindingId(String base, EntryType type)
	{
		return (base + ID_SEPARATOR + type.value);
	}


	/* =============== Internal Helpers =============== */

	private void put(String id, Binding binding)
	{
		bindings.put(id, binding);

		log.info("Added binding: " + id);
	}

	private void put(String base, EntryType type, String path)
	{
		final String id = BindingsManager.toBindingId(base, type);
		paths.put(id, path);

		log.info("Added binding's path entry: " + id + " -> " + path);
	}

	private void add(String id, String path)
	{
		paths.put(id, path);

		log.info("Added binding's access path: " + id + " -> " + path);
	}

	private void remove(String id)
	{
		bindings.remove(id);
		paths.remove(id);
		for (EntryType type : EntryType.values())
			paths.remove(BindingsManager.toBindingId(id, type));
	}

	private String getImageFileSystem(Binding resource)
	{
		String fstype = null;

		if (resource instanceof ImageArchiveBinding) {
			final ImageArchiveBinding image = (ImageArchiveBinding) resource;
			fstype = image.getFileSystemType();
		}
		else if (resource instanceof BlobStoreBinding) {
			final BlobStoreBinding image = (BlobStoreBinding) resource;
			if (image.getFileSystemType() != null && image.getMountFS())
				fstype = image.getFileSystemType().toString();
		}

		if (fstype != null)
			fstype = fstype.toLowerCase();

		return fstype;
	}
}
