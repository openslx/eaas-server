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

package de.bwl.bwfla.objectarchive.impl;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.openslx.eaas.common.concurrent.ParallelProcessors;
import com.openslx.eaas.common.util.AtomicMultiCounter;
import com.openslx.eaas.migration.MigrationUtils;
import com.openslx.eaas.migration.config.MigrationConfig;
import com.openslx.eaas.resolver.DataResolver;
import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.services.container.helpers.CdromIsoHelper;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.METS.MetsUtil;
import de.bwl.bwfla.objectarchive.conf.ObjectArchiveSingleton;
import de.bwl.bwfla.objectarchive.datatypes.*;

import gov.loc.mets.Mets;
import gov.loc.mets.MetsType;
import org.apache.commons.io.FileUtils;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.ConfigurationInjection;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Binding.ResourceType;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.utils.DefaultDriveMapper;

import static de.bwl.bwfla.common.utils.METS.MetsUtil.MetsEaasConstant.FILE_GROUP_OBJECTS;


// FIXME: this class should be implemented in a style of a "Builder" pattern
public class DigitalObjectFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	private String name;
	private String localPath;
	private boolean defaultArchive;
	private String exportUrlPrefix;

	protected ObjectFileFilter objectFileFilter = new ObjectFileFilter();

	private static final String METS_MD_FILENAME = "mets.xml";
	private static final String PACKED_FILES_ISO_FILENAME = "packed-files.iso";


	/**
	 * Simple ObjectArchive example. Files are organized as follows
	 * localPath/
	 *          ID/
	 *            floppy/
	 *            iso/
	 *               disk1.iso
	 *               disk2.iso
	 *               
	 * Allowed extensions:
	 *      iso : {.iso}
	 *      floppy : {.img, .ima, .adf, .D64, .x64, .dsk, .st }
	 * 
	 * @param name
	 * @param localPath
	 */
	public DigitalObjectFileArchive(String name, String localPath, boolean defaultArchive)
	{
		this.init(name, localPath, defaultArchive);
	}

	protected DigitalObjectFileArchive() {}

	protected void init(String name, String localPath, boolean defaultArchive)
	{
		var httpExport = ConfigurationProvider.getConfiguration()
				.get("objectarchive.httpexport");

		if (!httpExport.endsWith("/"))
			httpExport += "/";

		this.name = name;
		this.localPath = localPath;
		this.defaultArchive = defaultArchive;
		this.exportUrlPrefix = httpExport + URLEncoder.encode(name, StandardCharsets.UTF_8);
		ConfigurationInjection.getConfigurationInjector().configure(this);
	}

	private static String strSaveFilename(String filename)
	{
		return filename.replace(" ", "");
	}

	private Path resolveMetadatTarget(String id) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		Path targetDir = objectDir.toPath().resolve(id);
		if(!Files.exists(targetDir)) {
			try {
				Files.createDirectories(targetDir);
			} catch (IOException e) {
				throw new BWFLAException(e);
			}
		}
		return targetDir;
	}

	private Path resolveTarget(String id, ResourceType rt) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		Path targetDir = objectDir.toPath().resolve(id);
		targetDir = targetDir.resolve(rt.value());
		if(!targetDir.toFile().exists())
			if(!targetDir.toFile().mkdirs())
			{
				throw new BWFLAException("could not create directory: " + targetDir);
			}

		return targetDir;
	}

	private Path resolveTarget(String id, Drive.DriveType type) throws BWFLAException
	{
		if(type == null)
			return null;

		return this.resolveTarget(id, type.toResourceType());
	}

	public String getThumbnail(String id) throws BWFLAException {
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}
		Path targetDir = objectDir.toPath().resolve(id);
		Path target = targetDir.resolve("thumbnail.jpeg");
		if(!Files.exists(target))
			return null;

		return "thumbnail.jpeg";
	}

	public void importObjectThumbnail(FileCollectionEntry resource) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}
		Path targetDir = objectDir.toPath().resolve(resource.getId());
		if(!targetDir.toFile().exists()) {
			if (!targetDir.toFile().mkdirs()) {
				throw new BWFLAException("could not create directory: " + targetDir);
			}
		}

		Path target = targetDir.resolve("thumbnail.jpeg");
		if(Files.exists(target))
			return;
		EmulatorUtils.copyRemoteUrl(resource, target, log);
	}

	void importObjectFile(String objectId, FileCollectionEntry resource) throws BWFLAException
	{
		Path targetDir;

		if(resource.getType() != null)
			targetDir= resolveTarget(objectId, resource.getType());
		else if(resource.getResourceType() != null)
		{
			targetDir = resolveTarget(objectId, resource.getResourceType());
		}
		else throw new BWFLAException("could not determine drive/resource type of object");
		
		String fileName = resource.getLocalAlias();
		if(fileName == null || fileName.isEmpty())
			fileName = resource.getId();

		fileName = strSaveFilename(fileName);
		Path target = targetDir.resolve(fileName);

		EmulatorUtils.copyRemoteUrl(resource, target, log);
	}

	private void packFilesAsIso(String objectId) throws BWFLAException
	{
		final var iso = this.resolveTarget(objectId, ResourceType.ISO)
				.resolve(PACKED_FILES_ISO_FILENAME)
				.toFile();

		log.info("Packing files for object '" + objectId + "' as ISO...");
		try {
			final var srcdir = this.resolveTarget(objectId, ResourceType.FILE);
			final var files = Files.list(srcdir)
					.map(Path::toFile)
					.collect(Collectors.toList());

			if (!CdromIsoHelper.createIso(iso, files))
				throw new BWFLAException("Creating ISO from object files failed!");
		}
		catch (IOException error) {
			throw new BWFLAException("Listing object files failed!", error);
		}
	}

	@Override
	public void importObject(String metsdata) throws BWFLAException {
		MetsObject o = new MetsObject(metsdata);
		if(o.getId() == null || o.getId().isEmpty())
			throw new BWFLAException("invalid object id " + o.getId());

		FileCollection fc = o.getFileCollection(null);

		if(fc == null || fc.files == null)
			throw new BWFLAException("Invalid arguments");

		if(objectExits(o.getId()))
			return;

		for(FileCollectionEntry entry : fc.files)
			importObjectFile(o.getId(), entry);

		Mets m = fromFileCollection(o.getId(), fc);
		writeMetsFile(m);

		// NOTE: files can't usually be attached directly to emulators,
		//       hence we expect them to be packed in an ISO for now!
		if (fc.contains(ResourceType.FILE))
			this.packFilesAsIso(o.getId());
	}

	@Override
	public void updateLabel(String objectId, String newLabel) throws BWFLAException
	{
		log.info("Updating label for object " + objectId + " to '" + newLabel + "'");
		var mo = loadMetsData(objectId);
		mo.setLabel(newLabel);
		writeMetsFile(mo.getMets());
	}

	@Override
	public Stream<String> getObjectIds()
	{
		final Path basepath = this.getLocalPath();
		if (!Files.exists(basepath)) {
			log.warning("No object-archive exists at " + basepath.toString() + "!");
			return Stream.empty();
		}

		try {
			final Function<Path, String> mapper = (path) -> {
				return path.getFileName()
						.toString();
			};

			final DirectoryStream<Path> files = Files.newDirectoryStream(basepath);
			return StreamSupport.stream(files.spliterator(), false)
					.filter((path) -> Files.isDirectory(path))
					.map(mapper)
					.onClose(() -> {
						try {
							files.close();
						}
						catch (Exception error) {
							log.log(Level.WARNING, "Closing directory-stream failed!", error);
						}
					});
		}
		catch (Exception exception) {
			log.log(Level.SEVERE, "Reading object-archive's directory failed!", exception);
			return Stream.empty();
		}
	}

	private boolean objectExits(String objectId)
	{
		if(objectId == null)
			return false;

		log.info("looking for: " + objectId);
		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			log.warning("objectDir " + localPath + " does not exist");
			return false;
		}

		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			log.warning("objectDir " + objectDir + " does not exist");
			return false;
		}
		return true;
	}

	public void delete(String objectId) throws BWFLAException {
		if(objectId == null)
			throw new BWFLAException("object id was null");

		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + objectDir + " does not exist");
		}

		try {
			FileUtils.deleteDirectory(objectDir);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BWFLAException(e);
		}
	}

	private Mets fromFileCollection(String objectId, FileCollection fc) throws BWFLAException {
		if(fc == null || fc.files == null)
			throw new BWFLAException("Invalid arguments");

		String label = fc.getLabel() != null ? fc.getLabel() : objectId;

		Mets m = MetsUtil.createMets(fc.id, label);
		for(FileCollectionEntry entry : fc.files) {
			MetsUtil.FileTypeProperties properties = new MetsUtil.FileTypeProperties();
			Path targetDir = null;
			if(entry.getType() != null)
			 	targetDir = resolveTarget(objectId, entry.getType());
			else if(entry.getResourceType() != null)
				targetDir = resolveTarget(objectId, entry.getResourceType());
			else
				throw new BWFLAException("invalid file entry type");

			String url = this.resolveMetadatTarget(objectId)
					.relativize(targetDir)
					.toString();

			properties.fileFmt = entry.getResourceType() != null ? entry.getResourceType().toQID(): null;
			properties.deviceId = entry.getType() != null ? entry.getType().toQID() : null;

			String fileName = entry.getLocalAlias();
			if (fileName == null || fileName.isEmpty())
				fileName = entry.getId();
			url += "/" + fileName;

			log.warning(" local path url " + url);

			MetsUtil.addFile(m, entry.getId(), url, properties);
		}
		return m;
	}

	private void createMetsMetadata(String objectId) throws BWFLAException {
		FileCollection fc = this.describe(objectId);
		if (fc == null)
			throw new BWFLAException("Describing object '" + objectId + "' failed!");

		// HACK: let proper file-IDs be autogenerated during conversion to METS!
		for (final var fce : fc.files)
			fce.setId(null);

		Mets m = fromFileCollection(objectId, fc);
		writeMetsFile(m);
	}

	private void writeMetsFile(Mets m) throws BWFLAException {
		Path targetDir = resolveMetadatTarget(m.getID());
		Path metsPath = targetDir.resolve(METS_MD_FILENAME);
		try {
			Files.write(metsPath, m.toString().getBytes());
			log.info("Object metadata written to: " + metsPath);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BWFLAException(e);
		}
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

	private FileCollection describe(String objectId) {
		if(objectId == null)
			return null;

		log.info("Describing object: " + objectId);
		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			log.warning("objectDir " + localPath + " does not exist");
			return null;
		}		
		
		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			log.warning("objectDir " + objectDir + " does not exist");
			return null;
		}

		ObjectFileManifestation mf = null;
		try {
			mf = new ObjectFileManifestation(objectFileFilter, objectDir);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
		
		DefaultDriveMapper driveMapper = new DefaultDriveMapper();
		try {
			return driveMapper.map(null, mf);
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		
	}

	public Path getLocalPath()
	{
		return Paths.get(localPath);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	protected static class NullFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;

			if (file.getName().startsWith("."))
				return false;

			if (file.getName().endsWith(".properties"))
				return false;

			return true;
		}
	}
	
	protected static class IsoFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check file's extension...
			final String name = file.getName();
			final int length = name.length();
			return (name.regionMatches(true, length - 4, ".iso", 0, 4)  || name.regionMatches(true, length - 4, ".bin", 0, 4));
		}
	};

	protected static class FloppyFileFilter implements FileFilter
	{
		private final Set<String> formats = new HashSet<String>();
		
		public FloppyFileFilter()
		{
			// Add all valid formats
			formats.add(".img");
			formats.add(".ima");
			formats.add(".adf");
			formats.add(".d64");
			formats.add(".x64");
			formats.add(".dsk");
			formats.add(".st");
			formats.add(".tap");
		}
		
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check the file's extension...
			final String name = file.getName();
			final int extpos = name.lastIndexOf('.');
			if (extpos < 0)
				return false;  // No file extension found!
			
			final String ext = name.substring(extpos);
			return formats.contains(ext.toLowerCase());
		}
	}

	@Override
	public DigitalObjectMetadata getMetadata(String objectId) throws BWFLAException {

		// NOTE: METS file URLs need to stay absolute for now!
		final var metsExportPrefix = exportUrlPrefix + "/" + objectId + "/";

		MetsObject o = loadMetsData(objectId);
		Mets m = MetsUtil.export(o.getMets(), metsExportPrefix);
		DigitalObjectMetadata md = new DigitalObjectMetadata(m);

		String thumb = null;
		try {
			thumb = getThumbnail(objectId);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
		if (thumb != null)
			md.setThumbnail(thumb);

		return md;
	}

	private MetsObject loadMetsData(String objectId) throws BWFLAException {
		Path targetDir = resolveMetadatTarget(objectId);
		Path metsPath = targetDir.resolve(METS_MD_FILENAME);
		if (!Files.exists(metsPath))
			throw new BWFLAException("METS metadata for object '" + objectId + "' not found!");

		MetsObject mets = new MetsObject(metsPath.toFile());
		return mets;
	}

	@Override
	public Stream<DigitalObjectMetadata> getObjectMetadata() {

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
	public String resolveObjectResource(String objectId, String resourceId, String method) throws BWFLAException {
		final var url = DigitalObjectArchive.super.resolveObjectResource(objectId, resourceId, method);
		if (url == null || DataResolver.isAbsoluteUrl(url))
			return url;

		return exportUrlPrefix + "/" + objectId + "/" + url;
	}

	@Override
	public void sync() {	
	}

	@Override
	public TaskState sync(List<String> objectId) {
		return null;
	}


	@Override
	public boolean isDefaultArchive() {
		return defaultArchive;
	}

	@Override
	public int getNumObjectSeats(String id) {
		return -1;
	}

	public static class ObjectFileFilter
	{
		public FileFilter ISO_FILE_FILTER = new NullFileFilter();
		public FileFilter FLOPPY_FILE_FILTER = new NullFileFilter();
	}

	public enum UpdateCounts
	{
		PROCESSED,
		UPDATED,
		FAILED,
		__LAST;

		public static AtomicMultiCounter counter()
		{
			return new AtomicMultiCounter(__LAST.ordinal());
		}
	}

	public void createMetsFiles(MigrationConfig mc) throws Exception
	{
		final var counter = UpdateCounts.counter();

		final Predicate<String> filter = (objectId) -> {
			final var metsfile = Path.of(localPath, objectId, METS_MD_FILENAME);
			return !Files.exists(metsfile);
		};

		final Consumer<String> creator = (objectId) -> {
			try {
				this.createMetsMetadata(objectId);
				counter.increment(UpdateCounts.UPDATED);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Creating metadata for object '" + objectId + "' failed!", error);
				counter.increment(UpdateCounts.FAILED);
			}
		};

		log.info("Creating metadata for objects in archive '" + this.getName() + "'...");
		ParallelProcessors.consumer(filter, creator)
				.consume(this.getObjectIds(), ObjectArchiveSingleton.executor());

		final var numCreated = counter.get(UpdateCounts.UPDATED);
		final var numFailed = counter.get(UpdateCounts.FAILED);
		log.info("Created metadata for " + numCreated + " object(s), failed " + numFailed);
		if (!MigrationUtils.acceptable(numCreated + numFailed, numFailed, MigrationUtils.getFailureRate(mc)))
			throw new BWFLAException("Creating object-archive's metadata failed!");
	}

	private interface MetsFixer<T>
	{
		void apply(T value) throws Exception;
	}

	public void fixMetsFiles(MigrationConfig mc) throws Exception
	{
		final var digitalObjectsGroupName = FILE_GROUP_OBJECTS.toString();
		final var objectIdsToRemove = new ArrayList<String>();
		final var counter = UpdateCounts.counter();
		final var guard = new Object();


		final Predicate<String> filter = (objectId) -> {
			final var metsfile = Path.of(localPath, objectId, METS_MD_FILENAME);
			return Files.exists(metsfile);
		};

		final BiFunction<MetsType.FileSec, String, MetsType.FileSec.FileGrp> fgfinder = (fsec, fgname) -> {
			final var fgroups = (fsec != null) ? fsec.getFileGrp() : Collections.<MetsType.FileSec.FileGrp>emptyList();
			for (final var fgroup : fgroups) {
				if (fgname.equals(fgroup.getUSE()))
					return fgroup;
			}

			return null;
		};

		final Consumer<String> fixer = (objectId) -> {
			try {
				final var mets = this.loadMetsData(objectId)
						.getMets();

				var fsec = mets.getFileSec();
				if (fsec == null) {
					// add an empty file-section!
					fsec = new MetsType.FileSec();
					mets.setFileSec(fsec);
				}

				if (fgfinder.apply(fsec, digitalObjectsGroupName) == null) {
					// add an empty file-group!
					final var fgroup = new MetsType.FileSec.FileGrp();
					fgroup.setUSE(digitalObjectsGroupName);
					fsec.getFileGrp()
							.add(fgroup);
				}

				final var updatemsgs = new ArrayList<String>();

				final MetsFixer<MetsType.FileSec.FileGrp> digitalObjectsGroupFixer = (fgroup) -> {
					final var files = fgroup.getFile();
					for (final var fit = files.iterator(); fit.hasNext();) {
						final var flocations = fit.next().getFLocat();
						for (final var flit = flocations.iterator(); flit.hasNext();) {
							final var flocat = flit.next();
							final var oldurl = flocat.getHref();
							var newurl = oldurl;

							if (oldurl.endsWith("__import.iso")) {
								// CASE: legacy "__import.iso" is referenced directly
								updatemsgs.add("Removed legacy '__import.iso' reference!");
								flit.remove();
								continue;
							}

							if (oldurl.startsWith(objectId)) {
								// CASE: <object-id>/<subpath> -> <subpath>
								newurl = oldurl.substring(objectId.length() + 1);
							}

							var filePath = Path.of(localPath, objectId, newurl);

							// CASE: file not found via URL
							if (!Files.exists(filePath)){
								// NOTE: some legacy METS URLs can contain URL unsafe chars (e.g. " ").
								//       Fix metadata according to stored filename in such cases!
								updatemsgs.add("File '" + filePath + "' is specified in the METS file, but can't be found...");
								final List<String> replacements = Arrays.asList("", "-");
								boolean success = false;
								for (var replacement : replacements){
									updatemsgs.add("Trying '" + replacement + "' as replacement for ' ' (empty space)...");
									newurl = newurl.replace(" ", replacement);
									filePath = Path.of(localPath, objectId, newurl);
									if (Files.exists(filePath)){
										updatemsgs.add("File '" + filePath + "' found after replacement!");
										success = true;
										break;
									}
								}

								if (!success){
									throw new RuntimeException("File was not found after trying all replacements!");
								}
							}

							if (!newurl.equals(oldurl)){
								flocat.setHref(newurl);
								updatemsgs.add("FLocat-URL: " + oldurl + " -> " + flocat.getHref());
							}
						}

						if (flocations.isEmpty())
							fit.remove();
					}

					if (files.isEmpty()) {
						// no files referenced in metadata, check storage content
						final var fc = this.describe(objectId);
						for (final var fce : fc.files)
							fce.setId(null);

						// NOTE: returned METS here should correctly describe referenced files!
						final var newmets = this.fromFileCollection(objectId, fc);
						final var newfgroup = fgfinder.apply(newmets.getFileSec(), digitalObjectsGroupName);
						if (newfgroup == null) {
							log.warning("No file-entries found for object '" + objectId + "'!");
							objectIdsToRemove.add(objectId);
							return;
						}

						files.addAll(newfgroup.getFile());
						updatemsgs.add("Re-created file-section from storage!");
					}
				};

				final var fgfixers = new HashMap<String, MetsFixer<MetsType.FileSec.FileGrp>>();
				fgfixers.put(digitalObjectsGroupName, digitalObjectsGroupFixer);
				for (final var fgroup : fsec.getFileGrp()) {
					final var fgfixer = fgfixers.get(fgroup.getUSE());
					if (fgfixer != null)
						fgfixer.apply(fgroup);
				}

				if (objectId.contains(" ")) {
					// some legacy objects may contain spaces in IDs!
					final var newObjectId = objectId.replace(' ', '-');
					mets.setID(newObjectId);

					final var oldpath = Path.of(localPath, objectId);
					final var newpath = Path.of(localPath, newObjectId);
					Files.move(oldpath, newpath);

					updatemsgs.add("Object-ID: '" + objectId + "' -> " + newObjectId);
				}

				if (updatemsgs.isEmpty())
					return;

				synchronized (guard) {
					log.info("Updates for object '" + objectId + "':");
					for (final var msg : updatemsgs)
						log.info("  " + msg);
				}

				this.writeMetsFile(mets);
				counter.increment(UpdateCounts.UPDATED);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Fixing metadata for object '" + objectId + "' failed!", error);
				counter.increment(UpdateCounts.FAILED);
			}
		};

		log.info("Fixing metadata for objects in archive '" + this.getName() + "'...");
		ParallelProcessors.consumer(filter, fixer)
				.consume(this.getObjectIds(), ObjectArchiveSingleton.executor());

		if (!objectIdsToRemove.isEmpty()) {
			log.info("Removing empty objects in archive '" + this.getName() + "'...");
			final var deleter = new DeprecatedProcessRunner()
					.setLogger(log);

			objectIdsToRemove.forEach((objectId) -> {
				final var path = Path.of(localPath, objectId);
				final var removed = deleter.setCommand("rm")
						.addArguments("-r", "\"" + path + "\"")
						.execute();

				if (removed)
					log.info("Removed: " + path);
			});

			log.info("Removed " + objectIdsToRemove.size() + " empty object(s)");
		}

		final var numFixed = counter.get(UpdateCounts.UPDATED);
		final var numFailed = counter.get(UpdateCounts.FAILED);
		log.info("Fixed metadata for " + numFixed + " object(s), failed " + numFailed);
		if (!MigrationUtils.acceptable(numFixed + numFailed, numFailed, MigrationUtils.getFailureRate(mc)))
			throw new BWFLAException("Fixing object-archive's metadata failed!");
	}

	public void packFilesAsIso(MigrationConfig mc) throws Exception
	{
		final var counter = UpdateCounts.counter();

		final Predicate<String> filter = (objectId) -> {
			final var basedir = Path.of(localPath, objectId);
			final var files = basedir.resolve(ResourceType.FILE.value());
			final var iso = basedir.resolve(ResourceType.ISO.value())
					.resolve(PACKED_FILES_ISO_FILENAME);

			return Files.exists(files) && !Files.exists(iso);
		};

		final Consumer<String> packer = (objectId) -> {
			try {
				this.packFilesAsIso(objectId);
				counter.increment(UpdateCounts.UPDATED);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Packing files for object '" + objectId + "' failed!", error);
				counter.increment(UpdateCounts.FAILED);
			}
		};

		log.info("Packing object files in archive '" + this.getName() + "'...");
		ParallelProcessors.consumer(filter, packer)
				.consume(this.getObjectIds(), ObjectArchiveSingleton.executor());

		final var numPacked = counter.get(UpdateCounts.UPDATED);
		final var numFailed = counter.get(UpdateCounts.FAILED);
		log.info("Packed " + numPacked + " object(s), failed " + numFailed);
	}

	public void cleanupLegacyFiles(MigrationConfig mc) throws Exception
	{
		final var counter = UpdateCounts.counter();

		final Consumer<String> cleaner = (objectId) -> {
			final var files = new ArrayList<Path>();
			final var basedir = Path.of(localPath, objectId);
			files.add(basedir.resolve("file.zip"));
			files.add(basedir.resolve("iso").resolve("__import.iso"));

			for (final var file : files) {
				try {
					if (Files.deleteIfExists(file)) {
						log.info("Removed: " + file);
						counter.increment(UpdateCounts.UPDATED);
					}
				}
				catch (Exception error) {
					log.log(Level.WARNING, "Removing legacy files for object '" + objectId + "' failed!", error);
					counter.increment(UpdateCounts.FAILED);
				}
			}
		};

		log.info("Cleaning up object-archive '" + this.getName() + "'...");
		ParallelProcessors.consumer(cleaner)
				.consume(this.getObjectIds(), ObjectArchiveSingleton.executor());

		final var numRemoved = counter.get(UpdateCounts.UPDATED);
		final var numFailed = counter.get(UpdateCounts.FAILED);
		log.info("Removed " + numRemoved + " legacy file(s), failed " + numFailed);
	}
}
