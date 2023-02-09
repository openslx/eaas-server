/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.objectarchive.impl;

import com.openslx.eaas.common.concurrent.ParallelProcessors;
import com.openslx.eaas.migration.MigrationUtils;
import com.openslx.eaas.migration.config.MigrationConfig;
import com.openslx.eaas.resolver.DataResolver;
import com.openslx.eaas.resolver.DataResolvers;
import de.bwl.bwfla.blobstore.Blob;
import de.bwl.bwfla.blobstore.BlobDescription;
import de.bwl.bwfla.blobstore.BlobStore;
import de.bwl.bwfla.blobstore.Bucket;
import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.CdromIsoHelper;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.METS.MetsUtil;
import de.bwl.bwfla.emucomp.api.Binding.ResourceType;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectArchive;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectFileMetadata;
import de.bwl.bwfla.objectarchive.datatypes.DigitalObjectS3ArchiveDescriptor;
import de.bwl.bwfla.objectarchive.datatypes.MetsObject;
import gov.loc.mets.Mets;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.bwl.bwfla.objectarchive.impl.DigitalObjectFileArchive.UpdateCounts;


public class DigitalObjectS3Archive implements Serializable, DigitalObjectArchive
{
	protected final Logger log = Logger.getLogger(this.getClass().getName());

	private DigitalObjectS3ArchiveDescriptor descriptor;
	private Bucket bucket;
	private String basename;
	private DriveMapper driveMapper;

	@Inject
	@Config(value="objectarchive.temp_directory")
	public String tempImportDirectory;

	private static final String METS_MD_FILENAME = "mets.xml";
	private static final String THUMBNAIL_FILENAME = "thumbnail.jpeg";
	private static final String PACKED_FILES_ISO_FILENAME = "packed-files.iso";


	/**
	 * Simple S3-based ObjectArchive. Files are organized as follows:
	 *
	 * BUCKET/ARCHIVE-NAME/
	 *          ID/
	 *            floppy/
	 *               floppy1.img
	 *            iso/
	 *               disk1.iso
	 *               disk2.iso
	 */
	public DigitalObjectS3Archive(DigitalObjectS3ArchiveDescriptor descriptor)
			throws BWFLAException
	{
		this.init(descriptor);
	}

	protected DigitalObjectS3Archive()
	{
		// Empty!
	}

	protected void init(DigitalObjectS3ArchiveDescriptor descriptor)
			throws BWFLAException
	{
		ConfigurationInjection.getConfigurationInjector()
				.configure(this);

		final var blobstore = BlobStore.builder()
				.endpoint(descriptor.getEndpoint())
				.credentials(descriptor.getAccessKey(), descriptor.getSecretKey())
				.logger(log)
				.build();

		this.descriptor = descriptor;
		this.bucket = blobstore.bucket(descriptor.getBucket());
		this.basename = DigitalObjectS3Archive.strSaveFilename(descriptor.getName());
		if (descriptor.getPath() != null) {
			this.basename = BlobStore.path(descriptor.getPath(), this.basename)
					.toString();
		}

		this.driveMapper = new DriveMapper();
	}

	private static String strSaveFilename(String filename)
	{
		return filename.replace(" ", "-");
	}

	private BlobStore.Path location(String id)
	{
		return BlobStore.path(basename, id);
	}

	private BlobStore.Path resolveTarget(String id, ResourceType rt)
	{
		return this.location(id)
				.resolve(rt.value());
	}

	private BlobStore.Path resolveTarget(String id, Drive.DriveType type) throws BWFLAException
	{
		if (type == null)
			return null;

		var targetDir = this.location(id);
		switch(type)
		{
			case CDROM:
				targetDir = targetDir.resolve("iso");
				break;
			case FLOPPY:
				targetDir = targetDir.resolve("floppy");
				break;
			case DISK:
				targetDir = targetDir.resolve("disk");
				break;
			case CART:
				targetDir = targetDir.resolve("cart");
				break;
			default:
				throw new BWFLAException("unsupported type " + type);
		}

		return targetDir;
	}

	private BlobStore.Path resolveTarget(String id, FileCollectionEntry resource) throws BWFLAException
	{
		BlobStore.Path path = null;
		if (resource.getType() != null) {
			path = this.resolveTarget(id, resource.getType());
		}
		else if (resource.getResourceType() != null) {
			path = this.resolveTarget(id, resource.getResourceType());
		}
		else throw new BWFLAException("Could not determine drive/resource type of object!");

		String fileName = resource.getLocalAlias();
		if (fileName == null || fileName.isEmpty())
			fileName = resource.getId();

		fileName = DigitalObjectS3Archive.strSaveFilename(fileName);
		return path.resolve(fileName);
	}

	private void upload(Blob blob, FileCollectionEntry resource, String contentType, Path tmpdir)
			throws BWFLAException
	{
		var filename = resource.getLocalAlias();
		if (filename == null || filename.isEmpty())
			filename = resource.getId();

		final var tmpfile = tmpdir.resolve(filename);
		EmulatorUtils.copyRemoteUrl(resource, tmpfile, log);
		blob.uploader()
				.filename(tmpfile)
				.contentType(contentType)
				.upload();
	}

	public String getThumbnail(String id) throws BWFLAException
	{
		final var path = this.location(id)
				.resolve(THUMBNAIL_FILENAME);

		final var blob = bucket.blob(path.toString());
		if (!blob.exists())
			return null;

		return THUMBNAIL_FILENAME;
	}

	private void importObjectFile(String objectId, FileCollectionEntry resource, Path tmpdir)
			throws BWFLAException
	{
		final var path = this.resolveTarget(objectId, resource);
		final var blob = bucket.blob(path.toString());
		this.upload(blob, resource, "application/octet-stream", tmpdir);
	}

	private void packFilesAsIso(String objectId, Path srcdir) throws BWFLAException
	{
		log.info("Packing files for object '" + objectId + "' as ISO...");

		final List<File> files;
		try {
			files = Files.list(srcdir)
					.map(Path::toFile)
					.collect(Collectors.toList());
		}
		catch (IOException error) {
			throw new BWFLAException("Listing object files failed!", error);
		}

		final var iso = srcdir.resolve(PACKED_FILES_ISO_FILENAME);
		if (!CdromIsoHelper.createIso(iso.toFile(), files))
			throw new BWFLAException("Creating ISO from object files failed!");

		final var dstpath = this.resolveTarget(objectId, ResourceType.ISO)
				.resolve(PACKED_FILES_ISO_FILENAME);

		final var blob = bucket.blob(dstpath.toString());
		blob.uploader()
				.contentType("application/octet-stream")
				.filename(iso)
				.upload();
	}

	@Override
	public void importObject(String metsdata) throws BWFLAException
	{
		MetsObject o = new MetsObject(metsdata);
		if (o.getId() == null || o.getId().isEmpty())
			throw new BWFLAException("Invalid object ID: " + o.getId());

		FileCollection fc = o.getFileCollection(null);
		if (fc == null || fc.files == null)
			throw new BWFLAException("Invalid arguments");

		if (this.objectExists(o.getId()))
			return;

		final var tmpdir = Path.of(tempImportDirectory, o.getId());
		try {
			Files.createDirectories(tmpdir);
		}
		catch (Exception error) {
			throw new BWFLAException("Creating import-directory failed!", error);
		}

		try {
			for (FileCollectionEntry entry : fc.files)
				this.importObjectFile(o.getId(), entry, tmpdir);

			Mets m = this.fromFileCollection(o.getId(), fc);
			this.writeMetsFile(m);

			// NOTE: files can't usually be attached directly to emulators,
			//       hence we expect them to be packed in an ISO for now!
			if (fc.contains(ResourceType.FILE))
				this.packFilesAsIso(o.getId(), tmpdir);
		}
		finally {
			final var deleter = new DeprecatedProcessRunner("rm")
					.addArguments("-r", "--", tmpdir.toString())
					.setLogger(log);

			deleter.execute();
		}
	}

	@Override
	public Stream<String> getObjectIds()
	{
		final var prefix = basename + "/";

		try {
			return bucket.list(prefix)
					.map(new ObjectIdMapper(prefix))
					.filter(Objects::nonNull);
		}
		catch (Exception exception) {
			log.log(Level.SEVERE, "Listing objects failed!", exception);
			return Stream.empty();
		}
	}

	private boolean objectExists(String objectId)
	{
		if (objectId == null)
			return false;

		final var path = this.location(objectId)
				.resolve(METS_MD_FILENAME);

		final var blob = bucket.blob(path.toString());
		if (!blob.exists()) {
			log.warning("Object '" + objectId + "' not found!");
			return false;
		}

		return true;
	}

	@Override
	public void delete(String objectId) throws BWFLAException
	{
		if (objectId == null)
			throw new BWFLAException("object id was null");

		final Function<Blob, Integer> deleter = (blob) -> {
			try {
				blob.remove();
				return 1;
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Removing object's file failed!", error);
				return 0;
			}
		};

		final var prefix = this.location(objectId);
		try (final var blobs = bucket.blobs(prefix.toString())) {
			final var count = blobs.map(deleter)
					.reduce(0, Integer::sum);

			if (count > 0)
				log.info("Deleted object '" + objectId + "'");
		}
	}

	private Mets fromFileCollection(String objectId, FileCollection fc) throws BWFLAException
	{
		if(fc == null || fc.files == null)
			throw new BWFLAException("Invalid arguments");

		String label = fc.getLabel() != null ? fc.getLabel() : objectId;

		Mets m = MetsUtil.createMets(fc.id, label);
		for(FileCollectionEntry entry : fc.files) {
			MetsUtil.FileTypeProperties properties = new MetsUtil.FileTypeProperties();
			properties.fileFmt = entry.getResourceType() != null ? entry.getResourceType().toQID(): null;
			properties.deviceId = entry.getType() != null ? entry.getType().toQID() : null;

			var filename = entry.getLocalAlias();
			if (filename == null || filename.isEmpty())
				filename = entry.getId();
			filename = DigitalObjectS3Archive.strSaveFilename(filename);

			final ResourceType rt;
			if (entry.getResourceType() != null)
				rt = entry.getResourceType();
			else if (entry.getType() != null)
				rt = entry.getType().toResourceType();
			else throw new IllegalArgumentException();

			final var url = rt.value() + "/" + filename;
			MetsUtil.addFile(m, url, properties);
		}

		return m;
	}

	private void createMetsMetadata(String objectId) throws BWFLAException
	{
		FileCollection fc = this.describe(objectId);
		if (fc == null)
			throw new BWFLAException("Describing object '" + objectId + "' failed!");

		// HACK: let proper file-IDs be autogenerated during conversion to METS!
		for (final var fce : fc.files)
			fce.setId(null);

		Mets m = fromFileCollection(objectId, fc);
		writeMetsFile(m);
	}

	private void writeMetsFile(Mets mets) throws BWFLAException
	{
		final var metsdata = mets.toString();
		final var path = this.location(mets.getID())
				.resolve(METS_MD_FILENAME);

		final var blob = bucket.blob(path.toString());
		final var bytes = metsdata.getBytes(StandardCharsets.UTF_8);
		blob.uploader()
				.contentType("application/xml")
				.stream(new ByteArrayInputStream(bytes), bytes.length)
				.upload();

		log.info("Object metadata uploaded to: " + blob.name());
	}

	@Override
	public void updateLabel(String objectId, String newLabel) throws BWFLAException
	{
		log.info("Updating label for object " + objectId + " to '" + newLabel + "'.");
		var mo = loadMetsData(objectId);
		mo.setLabel(newLabel);
		writeMetsFile(mo.getMets());
	}

	@Override
	public FileCollection getObjectReference(String objectId)
	{
		try {
			final MetsObject mets = this.loadMetsData(objectId);
			final FileCollection fc = mets.getFileCollection(null);
			if (fc.contains(ResourceType.FILE)) {
				// NOTE: to stay compatible with existing clients, remove
				//       all files and replace them with a single ISO!
				fc.files = fc.files.stream()
						.filter((fce) -> fce.getResourceType() != ResourceType.FILE)
						.collect(Collectors.toList());

				final var url = "iso/" + PACKED_FILES_ISO_FILENAME;
				fc.files.add(new FileCollectionEntry(url, Drive.DriveType.CDROM, PACKED_FILES_ISO_FILENAME));
			}

			return fc;
		}
		catch (Exception error) {
			log.log(Level.WARNING, "Loading object description failed!", error);
			return null;
		}
	}

	private FileCollection describe(String objectId) throws BWFLAException
	{
		if (objectId == null)
			return null;

		log.info("Describing object: " + objectId);

		final var path = this.location(objectId);
		try (final var blobs = bucket.list(path.toString())) {
			final var manifestation = new ObjectManifestation(objectId)
					.construct(bucket, path.toString(), blobs, log);

			return driveMapper.map(manifestation);
		}
	}

	@Override
	public java.nio.file.Path getLocalPath()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName()
	{
		return descriptor.getName();
	}

	@Override
	public DigitalObjectMetadata getMetadata(String objectId) throws BWFLAException
	{
		// NOTE: METS file URLs need to stay absolute for now!
		final BiFunction<String, String, String> metsExportPrefixer = (resourceId, url) -> {
			return DataResolvers.objects()
					.resolve(basename, objectId, resourceId);
		};

		final var data = this.loadMetsData(objectId);
		final var mets = MetsUtil.export(data.getMets(), metsExportPrefixer);
		final var md = new DigitalObjectMetadata(mets);
		try {
			final var thumbnail = this.getThumbnail(objectId);
			if (thumbnail != null)
				md.setThumbnail(thumbnail);
		}
		catch (BWFLAException error) {
			log.log(Level.WARNING, "Loading thumbnail failed!", error);
		}

		return md;
	}

	private MetsObject loadMetsData(String objectId) throws BWFLAException
	{
		final var path = this.location(objectId)
				.resolve(METS_MD_FILENAME);

		final var blob = bucket.blob(path.toString());
		if (!blob.exists())
			throw new BWFLAException("METS metadata for object '" + objectId + "' not found!");

		try {
			final var stream = blob.downloader()
					.download();

			try (stream) {
				final var bytes = stream.readAllBytes();
				final var mets = new String(bytes, StandardCharsets.UTF_8);
				return new MetsObject(mets);
			}
		}
		catch (IOException error) {
			throw new BWFLAException("Downloading METS file failed!", error);
		}
	}

	@Override
	public Stream<DigitalObjectMetadata> getObjectMetadata()
	{
		final Function<String, DigitalObjectMetadata> mapper = (id) -> {
			try {
				return this.getMetadata(id);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Reading object's metadata failed!", error);
				return null;
			}
		};

		return this.getObjectIds()
				.map(mapper)
				.filter(Objects::nonNull);
	}

	@Override
	public String resolveObjectResource(String objectId, String resourceId, String method) throws BWFLAException
	{
		var url = DigitalObjectArchive.super.resolveObjectResource(objectId, resourceId, method);
		if (url == null || DataResolver.isAbsoluteUrl(url))
			return url;

		url = this.location(objectId) + "/" + url;
		return bucket.blob(url)
				.newPreSignedUrl(Blob.AccessMethod.valueOf(method));
	}

	@Override
	public void sync()
	{
		// Empty!
	}

	@Override
	public TaskState sync(List<String> objectId)
	{
		return null;
	}

	@Override
	public boolean isDefaultArchive()
	{
		return descriptor.isDefaultArchive();
	}

	@Override
	public int getNumObjectSeats(String id)
	{
		return -1;
	}

	public DigitalObjectS3ArchiveDescriptor getDescriptor()
	{
		return descriptor;
	}


	// ===== Internal Helpers ===============

	private static class ObjectIdMapper implements Function<BlobDescription, String>
	{
		private final String prefix;
		private String previd;

		public ObjectIdMapper(String prefix)
		{
			this.prefix = prefix;
			this.previd = null;
		}

		@Override
		public String apply(BlobDescription blob)
		{
			// NOTE: paths are expected to be of the form
			//       "<prefix>/<object-id>/<subpath>"

			// NOTE: usually multiple files are stored for each object,
			//       hence multiple paths per object-id will be listed!

			final var path = blob.name()
					.substring(prefix.length());

			final var endpos = path.indexOf("/");

			String curid = null;
			if (endpos > 0) {
				curid = path.substring(0, endpos);
				if (curid.equals(previd))
					return null;

				previd = curid;
			}

			return curid;
		}
	}

	private static class DefaultBlobFilter implements Predicate<String>
	{
		@Override
		public boolean test(String path)
		{
			final var name = BlobStore.path(path)
					.last()
					.toString();

			if (name.startsWith("."))
				return false;

			return true;
		}
	}

	/** A collection of known blob-filters for each object's resource type */
	private static final Map<ResourceType, Predicate<String>> OBJECT_BLOB_FILTERS = new HashMap<>();
	static {
		final var filter = new DefaultBlobFilter();
		OBJECT_BLOB_FILTERS.put(ResourceType.ISO, filter);
		OBJECT_BLOB_FILTERS.put(ResourceType.FLOPPY, filter);
	}

	private static class ObjectManifestation
	{
		private final String id;
		private final Map<ResourceType, List<BlobDescription>> entries;
		private final Map<String, DigitalObjectFileMetadata> metadata;

		public ObjectManifestation(String id)
		{
			this.id = id;
			this.entries = new HashMap<>();
			this.metadata = new HashMap<>();
		}

		public String id()
		{
			return id;
		}

		public Map<ResourceType, List<BlobDescription>> entries()
		{
			return entries;
		}

		public List<BlobDescription> blobs(ResourceType rt)
		{
			return entries.get(rt);
		}

		public DigitalObjectFileMetadata metadata(String name)
		{
			return metadata.get(name);
		}

		private void add(ResourceType rt, BlobDescription blob)
		{
			entries.computeIfAbsent(rt, (unused) -> new ArrayList<>())
					.add(blob);
		}

		private void add(String name, DigitalObjectFileMetadata md)
		{
			metadata.put(name, md);
		}

		private ObjectManifestation construct(Bucket bucket, String prefix, Stream<BlobDescription> blobs, Logger log)
		{
			// NOTE: paths are expected to be of the form
			//       "<prefix>/<resource-type>/<filename>"

			final var separator = "/";
			if (!prefix.endsWith(separator))
				prefix += separator;

			final var NAME_PREFIX = prefix;
			final Consumer<BlobDescription> processor = (blob) -> {
				final var endpos = blob.name()
						.indexOf(separator, NAME_PREFIX.length());

				if (endpos < 0)
					return;  // does not look like a valid resource path!

				final var typename = blob.name()
						.substring(NAME_PREFIX.length(), endpos);

				try {
					final var rt = ResourceType.fromValue(typename);
					if (!ObjectManifestation.filter(rt, blob))
						return;  // skip current blob!

					// process custom properties...
					final var blobname = blob.name();
					if (blobname.endsWith(".properties")) {
						final var source = bucket.blob(blobname);
						this.add(blobname, ObjectManifestation.loadmd(source));
					}
					else this.add(rt, blob);
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Processing object's blob at '" + blob.name() + "' failed!", error);
				}
			};

			blobs.forEach(processor);
			return this;
		}

		private static boolean filter(ResourceType rt, BlobDescription blob)
		{
			final var filter = OBJECT_BLOB_FILTERS.get(rt);
			if (filter == null)
				return false;

			return filter.test(blob.name());
		}

		private static DigitalObjectFileMetadata loadmd(Blob blob) throws Exception
		{
			final var istream = blob.downloader()
					.download();

			try (istream) {
				final var properties = new Properties();
				properties.load(istream);
				return new DigitalObjectFileMetadata(properties);
			}
		}
	}

	/** A map for object's resource types to matching drive types */
	private static final Map<ResourceType, Drive.DriveType> RESOURCE_DRIVE_TYPES = new HashMap<>();
	static {
		RESOURCE_DRIVE_TYPES.put(ResourceType.ISO, Drive.DriveType.CDROM);
		RESOURCE_DRIVE_TYPES.put(ResourceType.DISK, Drive.DriveType.DISK);
		RESOURCE_DRIVE_TYPES.put(ResourceType.FLOPPY, Drive.DriveType.FLOPPY);
		RESOURCE_DRIVE_TYPES.put(ResourceType.CART, Drive.DriveType.CART);
	}

	public static class DriveMapper
	{
		private final String extUrlPrefix;

		public DriveMapper()
		{
			this.extUrlPrefix = null;
		}

		public DriveMapper(String extUrlPrefix)
		{
			this.extUrlPrefix = extUrlPrefix;
		}

		public FileCollection map(ObjectManifestation object) throws BWFLAException
		{
			final var types = object.entries()
					.keySet();

			if (types.contains(ResourceType.FILE)) {
				// NOTE: files can't usually be attached directly to emulators,
				//       hence we expect them to be packed in an ISO for now!
				final var isos = object.blobs(ResourceType.ISO);
				final var found = (isos != null) && isos.stream()
						.anyMatch((blob) -> blob.name().endsWith(PACKED_FILES_ISO_FILENAME));

				if (!found)
					throw new BWFLAException("ISO for multi-file object '" + object.id() + "' is missing!");

				types.remove(ResourceType.FILE);
			}

			final var fc = new FileCollection(object.id());
			types.forEach((rt) -> this.map(object, rt, fc.files));
			return fc;
		}

		private void map(ObjectManifestation object, ResourceType rt, Collection<FileCollectionEntry> entries)
		{
			final var blobs = object.blobs(rt);
			if (blobs == null || blobs.isEmpty())
				return;

			final var dt = RESOURCE_DRIVE_TYPES.get(rt);
			if (dt == null)
				return;

			boolean isFirst = true;

			String urlPrefix = rt.value();
			if (extUrlPrefix != null)
				urlPrefix = extUrlPrefix + "/" + object.id() + "/" + urlPrefix;

			for (final var blob : blobs) {
				final var fce = this.map(blob, dt, urlPrefix);
				final var md = object.metadata(blob.name());
				if (md != null) {
					fce.setOrder(md.getOrder());
					fce.setLabel(md.getLabel());
				}

				if (isFirst) {
					fce.setDefault(true);
					isFirst = false;
				}

				entries.add(fce);
			}
		}

		private FileCollectionEntry map(BlobDescription blob, Drive.DriveType driveType, String urlPrefix)
		{
			final var name = BlobStore.path(blob.name())
					.last()
					.toString();

			final var fce = new FileCollectionEntry(urlPrefix + "/" + name, driveType, name);
			fce.setFileSize(blob.size());
			fce.setLocalAlias(name);
			return fce;
		}
	}


	public void importLegacyArchive(MigrationConfig mc, Path basedir) throws Exception
	{
		final var fcounter = UpdateCounts.counter();
		final var ocounter = UpdateCounts.counter();

		final Function<Path, Integer> fuploader = (path) -> {
			final var name = basename + "/" + basedir.relativize(path);
			final var contentType = (name.endsWith(METS_MD_FILENAME)) ?
					MediaType.APPLICATION_XML : MediaType.APPLICATION_OCTET_STREAM;

			try {
				bucket.blob(name)
						.uploader()
						.contentType(contentType)
						.filename(path)
						.upload();

				log.info("  Uploaded: " + path);
				fcounter.increment(UpdateCounts.UPDATED);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Uploading legacy object failed!", error);
				fcounter.increment(UpdateCounts.FAILED);
				return 0;
			}

			try {
				Files.delete(path);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Deleting file failed!", error);
			}

			return 1;
		};

		final Function<Path, Boolean> ouploader = (opath) -> {
			// upload each object's file...
			try (final var paths = Files.walk(opath)) {
				final var uploaded = paths.filter(Files::isRegularFile)
						.map(fuploader)
						.allMatch((v) -> v > 0);

				if (!uploaded) {
					ocounter.increment(UpdateCounts.FAILED);
					return false;
				}

				ocounter.increment(UpdateCounts.UPDATED);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Uploading object failed!", error);
				return false;
			}

			final var deleter = new DeprecatedProcessRunner("rm")
					.addArguments("-r", opath.toString())
					.setLogger(log);

			if (deleter.execute())
				log.info("Removed directory: " + opath);

			return true;
		};

		log.info("Importing legacy objects into '" + this.getName() + "' archive...");
		try (final var paths = Files.list(basedir)) {
			final var uploaded = ParallelProcessors.reducer(Files::isDirectory, ouploader, Boolean::logicalAnd)
					.reduce(true, paths, ObjectArchiveSingleton.executor());

			if (uploaded) {
				final var deleter = new DeprecatedProcessRunner("rm")
						.addArguments("-r", "--", basedir.toString())
						.setLogger(log);

				if (deleter.execute())
					log.info("Removed directory: " + basedir);
			}
		}

		final var numFilesUploaded = fcounter.get(UpdateCounts.UPDATED);
		final var numFilesFailed = fcounter.get(UpdateCounts.FAILED);
		log.info("Uploaded " + numFilesUploaded + " file(s), failed " + numFilesFailed);

		final var numImported = ocounter.get(UpdateCounts.UPDATED);
		final var numFailed = ocounter.get(UpdateCounts.FAILED);
		log.info("Imported " + numImported + " legacy object(s), failed " + numFailed);
		if (!MigrationUtils.acceptable(numImported + numFailed, numFailed, MigrationUtils.getFailureRate(mc)))
			throw new BWFLAException("Importing legacy objects failed!");
	}
}
