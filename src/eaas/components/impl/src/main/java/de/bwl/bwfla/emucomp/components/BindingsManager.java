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


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.emucomp.api.AbstractDataResource;
import de.bwl.bwfla.emucomp.api.Binding;
import de.bwl.bwfla.emucomp.api.BlobStoreBinding;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.ObjectArchiveBinding;
import de.bwl.bwfla.emucomp.api.VolatileResource;
import de.bwl.bwfla.emucomp.api.XmountOptions;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;
import org.apache.tamaya.ConfigurationProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class BindingsManager
{
	private final Logger log;

	private final Map<String, Binding> bindings;
	private final Map<String, String> paths;

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

	private final String imageProxy = ConfigurationProvider.getConfiguration().get("emucomp.image_proxy");
	private final String apiKey = ConfigurationProvider.getConfiguration().get("ws.apikey");

	public BindingsManager()
	{
		this(Logger.getLogger(BindingsManager.class.getName()));
	}

	public BindingsManager(Logger log)
	{
		this.bindings = new HashMap<String, Binding>();
		this.paths = new LinkedHashMap<String, String>();
		this.log = log;
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
			final ObjectArchiveHelper helper = new ObjectArchiveHelper(object.getArchiveHost());
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

	/**
	 * Resolves and mounts a binding location of either the form
	 * binding://<binding_id>> or <binding_id>.
	 *
	 * The <binding_id> is replaced with the actual filesystem location of the binding's mountpoint.
	 *
	 * @param binding  A binding location
	 * @return The resolved path or null, if the binding cannot be found
	 */
	public String mount(String binding, Path outdir, EmulatorUtils.XmountOutputFormat outformat)
			throws BWFLAException, IOException, IllegalArgumentException
	{
		if (binding == null || binding.isEmpty())
			throw new IllegalArgumentException("Binding is null or empty!");

		if (outformat == null)
			throw new IllegalArgumentException("Output format is null!");

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

		final XmountOptions xmountOpts = new XmountOptions(outformat);
		if (resource instanceof VolatileResource) {
			VolatileResource vResource = (VolatileResource) resource;
			// The resource should be written to in-place, ignoring the
			// value of getAccess(), as it is a temporary copy of user-data

			// (TODO) Currently only file: transport is allowed here
			if (!vResource.getUrl().startsWith("file:"))
				throw new IllegalArgumentException("Only 'file:' transport is allowed for injected objects/VolatileDrives.");

			resourcePath = EmulatorUtils.connectBinding(vResource, outdir, xmountOpts);
			vResource.setResourcePath(resourcePath);
			if (resourcePath == null || !(new File(resourcePath).canRead())) {
				final String message = "Binding target at location "
						+ vResource.getUrl() + " cannot be accessed!";

				throw new BWFLAException(message);
			}
		}
		else {
			if (resource.getFileSize() > 0)
				xmountOpts.setSize(resource.getFileSize());

			if(resource instanceof ImageArchiveBinding) {
				if (imageProxy != null)
					xmountOpts.setProxyUrl("http://jwt:" + apiKey + "@" +  imageProxy);
			}

			resourcePath = EmulatorUtils.connectBinding(resource, outdir, xmountOpts);

			// resourcePath is now the base path for the binding we want to find
			if (resourcePath == null || !(new File(resourcePath).canRead())) {
				final String message = "Binding target at location "
						+ resource.getUrl() + " cannot be accessed!";

				throw new BWFLAException(message);
			}
		}

		{
			// HACK: recreate the path to the image, as defined in
			//       EmulatorUtils.connectBinding()!
			String imgpath = outdir.resolve(realBindingId).toString();
			switch (resource.getAccess()) {
				case COW:
					this.put(realBindingId, EntryType.RAW_MOUNT, resourcePath);
					imgpath += ".cow";
					break;
				case COPY:
					imgpath += ".copy";
					break;
			}

			this.put(realBindingId, EntryType.IMAGE, imgpath);
		}

		// Is the raw file a block device with a filesystem?
		final String fsType = this.getImageFileSystem(resource);
		if (fsType != null && !fsType.isEmpty()) {
			final Path mountpoint = outdir.resolve(realBindingId + "." + fsType.replace(',', '.') + ".fuse");
			EmulatorUtils.lklMount(Paths.get(resourcePath), mountpoint, fsType, log, false);
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
		final String fuseSuffix = ".fuse";

		log.info("Unmounting bindings...");
		{
			// Finds the real FUSE-mountpoint in paths of the form:
			//     /some/path/binding-name.fuse/subresource...
			//     --> /some/path/binding-name.fuse
			final Function<String, String> toFuseMountpoint = (path) -> {
				final int index = path.indexOf(fuseSuffix);
				if (index < 0)
					return null;

				return path.substring(0, index + fuseSuffix.length());
			};

			final Set<String> idsToRemove = new HashSet<String>();
			final List<Pair<String, String>> entriesToUnmount = new ArrayList<Pair<String, String>>();

			final String[] fuseIdSuffixes = new String[] {
					ID_SEPARATOR + EntryType.RAW_MOUNT.value,
					ID_SEPARATOR + EntryType.FS_MOUNT.value,
			};

			paths.forEach((id, path) -> {
				for (String suffix : fuseIdSuffixes) {
					if (!id.endsWith(suffix))
						continue;  // Not a FUSE-mount!

					final String mountpoint = toFuseMountpoint.apply(path);
					if (mountpoint != null) {
						entriesToUnmount.add(new Pair<String, String>(id, mountpoint));
					}
					else {
						// Should never happen!
						log.warning("Expected a suffix '" + fuseSuffix + "' in path: " + path);
					}

					final int end = id.length() - suffix.length();
					idsToRemove.add(id.substring(0, end));

				}
			});

			// Unmount in reverse order
			final DeprecatedProcessRunner process = new DeprecatedProcessRunner();
			process.setLogger(log);
			for (int i = entriesToUnmount.size() - 1; i >= 0; --i) {
				final Pair<String, String> entry = entriesToUnmount.get(i);
				final String id = entry.getA();
				final String mountpoint = entry.getB();

				// Sync cached buffers first!
				process.setCommand("sync");
				process.execute();

				// Now it should be safe to unmount
				try {
					EmulatorUtils.unmountFuse(Paths.get(mountpoint), log);
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Unmounting binding failed: " + mountpoint, error);
				}
			}

			entriesToUnmount.clear();

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
