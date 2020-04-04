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
package de.bwl.bwfla.emucomp.api;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelper;
import de.bwl.bwfla.common.services.container.helpers.ContainerHelperFactory;
import de.bwl.bwfla.common.services.container.types.Container;
import de.bwl.bwfla.common.services.container.types.Container.Filesystem;
import de.bwl.bwfla.common.utils.BwflaFileInputStream;
import de.bwl.bwfla.common.utils.Pair;
import de.bwl.bwfla.emucomp.api.Binding.AccessType;
import de.bwl.bwfla.emucomp.api.Drive.DriveType;


public class EmulationEnvironmentHelper {
	protected final static Logger log = Logger.getLogger(EmulationEnvironmentHelper.class.getName());

	private static final String wsObjectArchive = ConfigurationProvider.getConfiguration().get("ws.objectarchive");

	/** List of beans, that support media-changing. */
	private static final Set<String> BEANS_WITH_MEDIACHANGE_SUPPORT = new HashSet<String>();
	static {
		// Add all supported bean names...
		Set<String> beans = BEANS_WITH_MEDIACHANGE_SUPPORT;
		beans.add("Kegs");
		beans.add("PceAtariSt");
		beans.add("PceIbmPc");
		beans.add("PceMacPlus");
		beans.add("Qemu");
		beans.add("VirtualBox");
	}

	/** List of beans, that support media-changing from internal UI. */
	private static final Map<String, String> BEANS_WITH_MEDIACHANGE_UI = new HashMap<String, String>();
	static {
		final String helpPrefix = "Media can be changed from emulator's internal UI.\n\n"
				+ "To open emulator's menu, please press ";

		final String beebemHelpMsg = helpPrefix + "the F11 key. Then select 'Discs' entry, "
				+ "followed by 'Change disc in drive 0/1'.";

		final String viceHelpMsg = helpPrefix + "the F12 key. Then select 'Drive' entry, "
				+ "followed by 'Attach disk image to drive 8-11'.";

		// Add all supported bean names...
		Map<String, String> beans = BEANS_WITH_MEDIACHANGE_UI;
		beans.put("Beebem", beebemHelpMsg);
		beans.put("ViceC64", viceHelpMsg);
	}

	public static Drive findEmptyDrive(MachineConfiguration env, Drive.DriveType type) {
		for (Drive d : env.getDrive())
			if (d.getType().equals(type) && (d.getData() == null || d.getData().isEmpty()))
				return d;

		log.info("can't find empty drive for type " + type);

		return null;
	}

	public static void setDrive(MachineConfiguration env, Drive d, int driveIndex) throws BWFLAException
	{
		try {
			Drive old = env.getDrive().get(driveIndex);
			old.setBoot(d.boot);
			old.setIface(d.iface);
			old.setBus(d.bus);
			old.setData(d.data);
			old.setFilesystem(d.filesystem);
			old.setType(d.type);
			old.setUnit(d.unit);
			old.setPlugged(d.plugged);
		}
		catch (IndexOutOfBoundsException e) {
			throw new BWFLAException(e);
		}
	}

	public static MachineConfiguration clean(final MachineConfiguration original, boolean cleanRemovableDrives) {
		MachineConfiguration env = original.copy();

		// remove all removable drives from the environment
		Map<String, Boolean> resourcesRemoveMap = new HashMap<>();

		for (Drive d : env.getDrive()) {
			String resourceUrl = d.getData();
			if(resourceUrl == null)
				continue;
			if (resourceUrl.startsWith("binding://")) {
				resourceUrl = resourceUrl.substring("binding://".length());
				resourceUrl = resourceUrl.substring(0,
						resourceUrl.indexOf("/") > 0 ? resourceUrl.indexOf("/") : resourceUrl.length());
			} else {
				resourceUrl = "";
			}

			if (d.getType() == Drive.DriveType.CDROM || d.getType() == Drive.DriveType.FLOPPY) {
				resourcesRemoveMap.put(resourceUrl, cleanRemovableDrives);
				if(cleanRemovableDrives)
					d.setData("");
			} else {
				resourcesRemoveMap.put(resourceUrl, false);
			}
		}

		// remove all spurious resources
		for (Iterator<AbstractDataResource> it = env.getAbstractDataResource().iterator(); it.hasNext();) {
			AbstractDataResource r = it.next();

			// resource was only used for a removable drive, so remove it
			Boolean remove = resourcesRemoveMap.get(r.getId());
			if (remove != null && remove.booleanValue()) {
				it.remove();
				continue;
			}

			if (r instanceof ImageArchiveBinding)
			{

				if(env.checkpointBindingId == null || env.checkpointBindingId.isEmpty()) {
					ImageArchiveBinding iab = (ImageArchiveBinding) r;
					if (iab.getId().equals("emucon-rootfs"))
						it.remove();
				}
			}

			// resource was volatile (but not in use by a drive), remove it, too
			for (AbstractDataResource origRes : original.getAbstractDataResource()) {

				if (origRes.getId().equals(r.getId()) && origRes instanceof VolatileResource) {
					it.remove();
					break;
				}
			}
		}
		// the copied environment should now be "clean" of any removable
		// drives and formerly volatile resources
		return env;
	}

	private static String _registerDataSource(String conf, String ref, String type) {
		try {
			MachineConfiguration env = MachineConfiguration.fromValue(conf);

			DriveType t = DriveType.valueOf(type);
			Drive d = findEmptyDrive(env, t);
			if (d != null) {
				Binding r = new Binding();
				String id = UUID.randomUUID().toString();
				r.setId(id);
				r.setUrl(ref);
				r.setAccess(AccessType.COW);
				env.getAbstractDataResource().add(r);
				d.setData("binding://" + id);
			}
			return env.toString();
		} catch (JAXBException e) {
			log.severe("invalid config format, got " + conf);
			return null;
		}
	}

//	/**
//	 * Drives capable to accept ready made images.
//	 * 
//	 * @return
//	 */
//	public static List<String> getImageDrives(MachineConfiguration env) {
//		List<String> emptyDrives = new ArrayList<>();
//		Iterator<Drive> iterator = env.getDrive().iterator();
//
//		while (iterator.hasNext()) {
//			Drive d = iterator.next();
//			if (d.getData() == null || d.getData().isEmpty()) {
//				Drive.DriveType type = d.getType();
//				emptyDrives.add(type.name());
//			}
//		}
//
//		return emptyDrives;
//	}

	/**
	 * Drives require a file system helper (FS annotation is required)
	 * 
	 * @return
	 */
	public static List<Pair<String, String>> getHelperDrives(MachineConfiguration env) {
		ArrayList<Pair<String, String>> emptyDrives = new ArrayList<Pair<String, String>>();
		Iterator<Drive> iterator = env.getDrive().iterator();

		while (iterator.hasNext()) {
			Drive d = iterator.next();

			if ((d.getData() == null || d.getData().isEmpty())
					&& (d.getFilesystem() != null && !d.getFilesystem().isEmpty())) {
				Pair<String, String> p = new Pair<String, String>(d.getType().name().toUpperCase(), d.getFilesystem());
				emptyDrives.add(p);
			}
		}

		return emptyDrives;
	}

	public static Environment registerDataSource(Environment env, String ref, Drive.DriveType type)
			throws BWFLAException, IllegalArgumentException {
		if (type == null)
			type = Drive.DriveType.CDROM;
		if (env == null || ref == null || type == null)
			throw new IllegalArgumentException();

		String xml = env.toString();
		xml = _registerDataSource(xml, ref, type.name());
		if (xml == null)
			return null;
		try {
			return Environment.fromValue(xml);
		} catch (JAXBException e) {
			throw new BWFLAException("register data source failed: " + e.getMessage(), e);
		}
	}

	public static String addBinding(MachineConfiguration env, String url) {
		return addBinding(env, url, AccessType.COW);
	}

	public static String addBinding(MachineConfiguration env, String url, AccessType accessType) {
		if (env == null) {
			log.info("given environment is null");
			return null;
		}
		if (url == null || url.isEmpty()) {
			log.info("given binding url is null or empty");
			return null;
		}
		String uuid = UUID.randomUUID().toString();
		Binding res = new Binding();
		res.setId(uuid);
		res.setUrl(url);
		res.setAccess(accessType);
		env.getAbstractDataResource().add(res);

		return uuid;
	}

	public static void removeNbdRefs(MachineConfiguration env) {
		if (env == null)
			return;

		for (AbstractDataResource ab : env.getAbstractDataResource()) {
			if (ab instanceof Binding) {
				Binding b = (Binding) ab;
				String url = b.getUrl();
				if (url.contains("exportname")) {
					b.setUrl("imagearchive:" + url.substring(url.lastIndexOf('=') + 1));
				}
			}
		}
	}

	public static String getMainHddRef(MachineConfiguration env) {
		if (env == null)
			return null;
		for (AbstractDataResource ab : env.getAbstractDataResource()) {
			if (ab.getId().equals("main_hdd")) {
				Binding b = (Binding) ab;
				return b.getUrl();
			}
		}
		return null;
	}

	/** Replaces current binding in machine-config with specified binding */
	public static void replace(MachineConfiguration env, ImageArchiveBinding replacement, boolean keepBindingId)
			throws BWFLAException
	{
		ImageArchiveBinding current = null;
		for (AbstractDataResource entry : env.getAbstractDataResource()) {
			if (!entry.getId().equals(replacement.getId()))
				continue;

			if (!(entry instanceof ImageArchiveBinding)) {
				// Should never happen! If yes, then something is broken!
				throw new BWFLAException("Binding's class mismatch! Current: " + entry.getClass());
			}

			current = (ImageArchiveBinding) entry;
			if (!keepBindingId) {
				int driveId = getDriveId(env, replacement.getId());
				Drive d = getDrive(env, driveId);
				if (d != null) {
					d.setData("binding://" + replacement.getImageId());
				} else {
					log.severe("XXX: replace(): drive not found");
				}
				current.setId(replacement.getImageId());
			}
			current.update(replacement);
			break;
		}

		if (current == null) {
			// env did not contain a binding
			replacement.setId(replacement.getImageId());
			env.getAbstractDataResource()
					.add(replacement);

			return;
		}
	}

	public static boolean beanSupportsMediaChange(String bean, DriveType type) {
		return BEANS_WITH_MEDIACHANGE_SUPPORT.contains(bean);
	}

	public static boolean beanSupportsMediaChangeUi(String bean, DriveType type) {
		return BEANS_WITH_MEDIACHANGE_UI.containsKey(bean);
	}

	public static String getMediaChangeHelp(String bean) {
		return BEANS_WITH_MEDIACHANGE_UI.get(bean);
	}

	public static String isObjectEnvironment(MachineConfiguration env)
	{
		if(env == null || env.getAbstractDataResource() == null)
			return null;

		for(AbstractDataResource dataResource : env.getAbstractDataResource())
		{
			if((dataResource instanceof ObjectArchiveBinding))
				return ((ObjectArchiveBinding)dataResource).getObjectId();
		}
		return null;
	}

	public static List<ObjectArchiveBinding> getObjects(MachineConfiguration env)
	{
		List<ObjectArchiveBinding> objectList = new ArrayList<>();
		if(env == null || env.getAbstractDataResource() == null)
			return objectList;

		for(AbstractDataResource dataResource : env.getAbstractDataResource())
		{
			if(!(dataResource instanceof ObjectArchiveBinding))
				continue;

			ObjectArchiveBinding oab = (ObjectArchiveBinding)dataResource;
			objectList.add(oab);
		}
		return objectList;
	}

	public static boolean hasObjectBinding(MachineConfiguration env, String objectId)
	{
		if(env == null || env.getAbstractDataResource() == null)
			return false;

		for(AbstractDataResource dataResource : env.getAbstractDataResource())
		{
			if(!(dataResource instanceof ObjectArchiveBinding))
				continue;

			ObjectArchiveBinding oab = (ObjectArchiveBinding)dataResource;
			if(oab.getObjectId().equals(objectId))
				return true;
		}
		return false;
	}

	public static int addObjectArchiveBinding(MachineConfiguration env, ObjectArchiveBinding binding, FileCollection fc, int index) throws BWFLAException {
		env.getAbstractDataResource().add(binding);
		FileCollectionEntry fce = fc.getDefaultEntry();
		return EmulationEnvironmentHelper.registerDrive(env, binding.getId(), fce.getId(), index);
	}

	public static int addArchiveBinding(MachineConfiguration env, ObjectArchiveBinding binding, FileCollection fc) throws BWFLAException {

		// FIXME
		Drive.DriveType type = fc.files.get(0).getType();
		env.getAbstractDataResource().add(binding);

		final String bean = env.getEmulator().getBean();
		if (EmulationEnvironmentHelper.beanSupportsMediaChange(bean, type)) {
			FileCollectionEntry fce = fc.getDefaultEntry();
			return EmulationEnvironmentHelper.registerDrive(env, binding.getId(), fce.getId(), type);
		} else if (EmulationEnvironmentHelper.beanSupportsMediaChangeUi(bean, type)) {
			FileCollectionEntry fce = fc.getDefaultEntry();
			EmulationEnvironmentHelper.registerDrive(env, binding.getId(), fce.getId(), type);
			return -1;
		} else // greedy allocation
		{
			for (FileCollectionEntry fce : fc.files) {
				log.info("adding fce to drive: " + fce.getId());
				EmulationEnvironmentHelper.registerDrive(env, binding.getId(), fce.getId(), type);
			}
			return -2;
		}
	}

	public static int getDriveId(MachineConfiguration env, String objectId)
	{
		int driveId = -1;
		for (Drive drive : env.getDrive()) {
			++driveId; // hack: fix me
			if(drive.getData() != null && drive.getData().contains(objectId))
				return driveId;
		}
		return -1;
	}

	public static Drive getDrive(MachineConfiguration env, int driveIndex) {
		if(driveIndex < 0 || env.getDrive().size() <= driveIndex)
			return null;

		return env.getDrive().get(driveIndex);
	}

	public static int registerEmptyDrive(MachineConfiguration env, int index) {
		// construct URL
		int driveId = -1;
		try {
			Drive d = env.getDrive().get(index);
			d.setData(null);
			return index;
		}
		catch(IndexOutOfBoundsException e)
		{
			return -1;
		}
	}

	public static int registerDrive(MachineConfiguration env, String binding, String path, int index) {
		// construct URL
		String subres = "";
		if (path != null)
			subres += "/" + path;

		String dataUrl = "binding://" + binding + subres;
		int driveId = -1;
		try {
			Drive d = env.getDrive().get(index);
			d.setData(dataUrl);
			return index;
		}
		catch(IndexOutOfBoundsException e)
		{
			return -1;
		}
	}

	public static int registerDrive(MachineConfiguration env, String binding, String path, Drive.DriveType driveType) {
		// construct URL
		String subres = "";
		if (path != null)
			subres += "/" + path.toString();

		String dataUrl = "binding://" + binding + subres;
		int driveId = -1;
		for (Drive drive : env.getDrive()) {
			++driveId; // hack: fix me

			if(drive.getType() == null)
			{
				log.warning("invalid drive data: drive type empty");
				continue;
			}

			if (drive.getType().equals(driveType) && (drive.getData() == null || drive.getData().isEmpty())) {
				drive.setData(dataUrl);
				break;
			}
		}
		return driveId;
	}

	public static Container createFilesContainer(MachineConfiguration _environment, String _dev, List<File> files) {
		Container container = null;
		ContainerHelper helper = null;

		List<Pair<String, String>> devices = EmulationEnvironmentHelper
				.getHelperDrives((MachineConfiguration) _environment);

		Filesystem fs = null;
		for (Pair<String, String> device : devices)
			if (device.getA().equalsIgnoreCase(_dev))
				fs = Filesystem.valueOf(device.getB().toUpperCase());

		if (fs == null) {
			log.severe("could not determine filesystem to uploaded attach files for the device (skipping): " + _dev);
			return null;
		}

		helper = ContainerHelperFactory.getContainerHelper(_dev, fs);

		if (helper == null) {
			log.severe(
					"container helper is null, make sure to check whether helper factory supports this device/filesystem combination");
			return null;
		}

		container = helper.createEmptyContainer();
		if (container == null) {
			log.severe("container is null, make sure to check whether corresponding helper is properly configured: "
					+ helper.getClass().getSimpleName());
			return null;
		}

		if (!helper.insertIntoContainer(container, files)) {
			log.warning("data attachment failed for the following container: " + container.getClass().getSimpleName());
			return null;
		}

		return container;

	}

	public static void setKbdConfig(MachineConfiguration env, String clientLang, String clientLayout) {
		if (clientLang == null || clientLayout == null) {
			log.warning("setKbdConfig: parameter null");
			return;
		}
		if (env.getUiOptions() == null)
			env.setUiOptions(new UiOptions());

		InputOptions options = env.getUiOptions().getInput();
		if (options == null)
			options = new InputOptions();
		options.setClientKbdLayout(clientLang);
		options.setClientKbdModel(clientLayout);
		env.getUiOptions().setInput(options);
	}

	public static void setTimeContext(MachineConfiguration env, String timeContext)
	{
		if(env == null || timeContext == null)
		{
			log.warning("setTimeContext: invalid data");
			return;
		}

		if (env.getUiOptions() == null)
			env.setUiOptions(new UiOptions());

		TimeOptions tOpts = new TimeOptions();
		tOpts.setEpoch(timeContext);

		env.getUiOptions().setTime(tOpts);
	}

    public static void enableRelativeMouse(MachineConfiguration chosenEnv) {
		if(chosenEnv == null)
			return;

		if (chosenEnv.getUiOptions() == null)
			chosenEnv.setUiOptions(new UiOptions());

		if(chosenEnv.getUiOptions().getInput() == null)
			chosenEnv.getUiOptions().setInput(new InputOptions());

		Html5Options html = new Html5Options();
		html.setPointerLock(true);
		chosenEnv.getUiOptions().setHtml5(html);
    }





	/*
	 * public static List<Resource> getIndirectBindings(EmulationEnvironment
	 * env) { ArrayList<Resource> l = new ArrayList<>();
	 * 
	 * for (Resource r : env.getBinding()) { try { URI uri = new
	 * URI(r.getUrl()); if (uri.isOpaque() &&
	 * uri.getScheme().equalsIgnoreCase("objectarchive")) { l.add(r); } }
	 * catch(Exception e) { log.warning("uri creation faild. skipping: " +
	 * r.getUrl()); continue; } } return l; }
	 */
}
