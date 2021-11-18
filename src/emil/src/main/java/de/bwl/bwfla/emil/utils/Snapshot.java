package de.bwl.bwfla.emil.utils;

import com.openslx.eaas.imagearchive.ImageArchiveClient;
import com.openslx.eaas.imagearchive.api.v2.common.InsertOptionsV2;
import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.common.datatypes.EaasState;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.UserContext;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.EmilObjectData;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.rest.SnapshotResponse;
import de.bwl.bwfla.emil.datatypes.snapshot.*;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.emucomp.client.ComponentClient;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Snapshot
{
    protected static final Logger LOG = Logger.getLogger("Image Snapshot");

    private MachineConfiguration configuration;

    /** Binding's changed/written data */
    private List<BindingDataHandler> data;

    final private Machine machine;
    final private EmilEnvironmentRepository emilEnvironmentRepository;
    final private UserSessions sessions;
    final private EmilObjectData objects;
    final private ImageArchiveClient imagearchive;

    public Snapshot(Machine machine,
                    EmilEnvironmentRepository emilEnvironmentRepository,
                    EmilObjectData objects,
                    UserSessions sessions) throws Exception {
        this.machine = machine;
        this.emilEnvironmentRepository = emilEnvironmentRepository;
        this.sessions = sessions;
        this.imagearchive = emilEnvironmentRepository.getImageArchive();
        this.objects = objects;
    }

    private static void copyBinding(MachineConfiguration src, MachineConfiguration target, String id) {
        if (src == null || src.getAbstractDataResource() == null || target == null)
            return;

        for (AbstractDataResource r : src.getAbstractDataResource()) {
            if (!r.getId().equals(id))
                continue;
            if (target.getAbstractDataResource() == null)
                target.setAbstractDataResource(new ArrayList<AbstractDataResource>());
            target.getAbstractDataResource().add(r);

            String bindingId = r.getId();

            // need to register drive (ideally keep drive index)
            int index = 0;
            String dataValue = null;
            if (src.getDrive() == null)
                continue;

            for (Drive d : src.getDrive()) {
                if (d.getData().contains(bindingId)) {
                    dataValue = d.getData();
                    break;
                }
                index++;
            }
            if (dataValue == null)
                try {
                    LOG.warning("inconsistent metadata " + src.value(true));
                    return;
                } catch (JAXBException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                }
            List<Drive> drives = target.getDrive();
            drives.get(index).setData(dataValue);
        }
    }

    private static void addSoftwareId(MachineConfiguration env, String softwareId)
    {
        List<String> installedSoftwareList = env.getInstalledSoftwareIds();
        Set<String> installedSoftwareSet = new HashSet<String>(installedSoftwareList);
        if (!installedSoftwareSet.contains(softwareId)) {
            installedSoftwareList.add(softwareId);
        }
    }

    public EmilEnvironment createEnvironment(SaveDerivateRequest req,
                                             EmilEnvironment parentEnv,
                                             boolean checkpoint) throws BWFLAException {

        // if(dataHandler == null)
        //    throw new BWFLAException("empty snapshots not supported.");

        EmilEnvironment newEnv = null;
        MachineConfiguration machineConfiguration;
        if(!checkpoint) {
            machineConfiguration = EmulationEnvironmentHelper.clean(configuration, req.isCleanRemovableDrives());
            if (machineConfiguration.getDescription() != null && machineConfiguration.getDescription().getTitle() != null) {
                String title = machineConfiguration.getDescription().getTitle();

                int oldRev = title.indexOf("REV:");
                if (oldRev > 0)
                    title = title.substring(0, oldRev);

                title += "REV: " + String.format("%x", System.currentTimeMillis() / 1000);
                machineConfiguration.getDescription().setTitle(title);
            }
        }
        else {
            machineConfiguration = configuration.copy();
        }
        
        if (parentEnv instanceof EmilSessionEnvironment) {
            newEnv = new EmilSessionEnvironment((EmilSessionEnvironment)parentEnv);
        } else if (parentEnv instanceof EmilObjectEnvironment) {
            final var oldEnv = imagearchive.api()
                    .v2()
                    .machines()
                    .fetch(req.getEnvId());

            copyBinding(oldEnv, machineConfiguration, ((EmilObjectEnvironment) parentEnv).getObjectId());
            newEnv = new EmilObjectEnvironment((EmilObjectEnvironment) parentEnv);
        } else {
            newEnv = new EmilEnvironment(parentEnv);
        }

        if (req.getSoftwareId() != null)
            addSoftwareId(machineConfiguration, req.getSoftwareId());

        if(req instanceof SaveNewEnvironmentRequest)
        {
            SaveNewEnvironmentRequest newReq = (SaveNewEnvironmentRequest) req;
            machineConfiguration.getDescription().setTitle(newReq.getTitle());
            newEnv.setTitle(newReq.getTitle());

        }

        if(!checkpoint) {
            for (Iterator<BindingDataHandler> it = data.iterator(); it.hasNext(); ) {
                BindingDataHandler bdh = it.next();
                if (bdh.getId().equals("emucon-rootfs")) {
                    it.remove();
                }
            }
        }

        final var newId = imagearchive.api()
                .v2()
                .environments()
                .insert(machineConfiguration, data, checkpoint);

        if(newId == null)
            throw new BWFLAException("create revision: importMachineEnvironment failed");

        if(checkpoint)
            newEnv.setTitle("Snapshot " + parentEnv.getEnvId());

        newEnv.setParentEnvId(parentEnv.getEnvId());
        newEnv.setEnvId(newId);
        newEnv.setDescription(req.getMessage());
        newEnv.setArchive("default");
        return newEnv;
    }

    public String saveUserSession(SaveUserSessionRequest request) throws BWFLAException
    {

        if (data == null)
            throw new BWFLAException("empty snapshots not supported.");

        MachineConfiguration env = EmulationEnvironmentHelper.clean(configuration, true);
        env.getDescription().setTitle("user session: " + request.getUserId());

//        String objectId = request.getObjectId();
//        if(objectId != null)
//        {
//            String archiveName = request.getObjectArchiveId();
//            if (archiveName == null)
//                archiveName = "default";
//
//            ObjectArchiveBinding binding = new ObjectArchiveBinding(environmentsAdapter.toString(), archiveName, request.getObjectId());
//            FileCollection fc = objectArchiveHelper.getObjectReference(request.getObjectArchiveId(), request.getObjectId());
//
//            if (fc == null)
//                throw new BWFLAException("create object environment: object not found: " + request.getObjectId());
//
//            //**?
//            int driveId = EmulationEnvironmentHelper.addArchiveBinding(env, binding, fc);
//            try {
//                System.out.println("ready to import machine: " + env.value());
//            } catch (JAXBException e) {
//                e.printStackTrace();
//            }
//        }

        for (Iterator<BindingDataHandler> it = data.iterator(); it.hasNext();)
        {
            BindingDataHandler bdh = it.next();
            if(bdh.getId().equals("emucon-rootfs")) {
                it.remove();
            }
        }

        final var newId = imagearchive.api()
                .v2()
                .environments()
                .insert(env, data);

        if (newId == null)
            throw new BWFLAException("importMachineEnvironment failed");

        return newId;
    }

    private EmilObjectEnvironment createObjectEnvironment(SaveObjectEnvironmentRequest request) throws BWFLAException {

        if(request.getTitle() == null)
            throw new BWFLAException("invalid request: title is missing");

        if(request.getObjectId() == null)
            throw new BWFLAException("invalid request: invalid object data");

        MachineConfiguration env = EmulationEnvironmentHelper.clean(configuration, request.isCleanRemovableDrives());
        env.getDescription().setTitle(request.getTitle());

        if (data != null) {
            for (Iterator<BindingDataHandler> it = data.iterator(); it.hasNext();)
            {
                BindingDataHandler bdh = it.next();
                if(bdh.getId().equals("emucon-rootfs")) {
                    it.remove();
                }
            }
        }

        final var options = new InsertOptionsV2()
                .setLocation(request.getArchive());

        final var newId = imagearchive.api()
                .v2()
                .environments()
                .insert(env, data, options);

        if (newId == null)
            throw new BWFLAException("importMachineEnvironment failed");

        EmilObjectEnvironment ee = new EmilObjectEnvironment();
        ee.setParentEnvId(request.getEnvId());
        ee.setEnvId(newId);
        ee.setTitle(request.getTitle());
        ee.setDriveId(request.getDriveId());
        ee.setObjectId(request.getObjectId());
        ee.setObjectArchiveId(request.getObjectArchiveId());
        ee.setEmulator(env.getEmulator().getBean());
        ee.setOs(env.getOperatingSystemId());
        ee.setDescription(request.getMessage());
        ee.setArchive("default");

        return ee;
    }

    private synchronized void createSnapshot(String componentId, boolean checkpoint)
            throws BWFLAException, InterruptedException, JAXBException
    {
        // final Machine machine = componentClient.getMachinePort(eaasGw);
        final MachineConfiguration config = MachineConfiguration.fromValue(this.machine.getRuntimeConfiguration(componentId));
        String state = this.machine.getEmulatorState(componentId);
        if (checkpoint) {
            // Make a checkpoint + snapshot
            LOG.info("Preparing session " + componentId + " for checkpointing...");
            if (!state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {
                LOG.warning("Preparing session " + componentId + " for checkpointing failed! Invalid state: " + state);
                throw new BWFLAException("Preparing session " + componentId + " for checkpointing failed! Invalid state: " + state);
            }

            // Import checkpoint data into archive
            final DataHandler data = machine.checkpoint(componentId);
            final ImageArchiveBinding binding = new ImageArchiveBinding();
            binding.setId("checkpoint");
            binding.setLocalAlias("checkpoint.tar.gz");
            binding.setAccess(Binding.AccessType.COPY);

            LOG.info("Saving checkpointed environment in image-archive...");
            try (final var stream = data.getInputStream()) {
                final var imagearchive = emilEnvironmentRepository.getImageArchive();
                final var imageid = imagearchive.api()
                        .v2()
                        .checkpoints()
                        .insert(stream);

                binding.setImageId(imageid);
            }
            catch (IOException error) {
                throw new BWFLAException("Saving checkpoint failed!", error);
            }

            // Update machine's configuration
            config.setCheckpointBindingId("binding://" + binding.getId());
            config.getAbstractDataResource().add(binding);
        }
        else {
            // Make a snapshot only!
            if (state.equalsIgnoreCase(EaasState.SESSION_RUNNING.value())) {
                LOG.info("Preparing session " + componentId + " for snapshotting...");
                machine.stop(componentId);

                final String expState = EaasState.SESSION_STOPPED.value();
                for (int i = 0; i < 30; ++i) {
                    state = machine.getEmulatorState(componentId);
                    if (state.equalsIgnoreCase(expState))
                        break;

                    Thread.sleep(500);
                }
            }

            state = machine.getEmulatorState(componentId);
            if (!state.equalsIgnoreCase(EaasState.SESSION_STOPPED.value())) {
                LOG.warning("Preparing session " + componentId + " for snapshotting failed!");
                throw new BWFLAException("Preparing session " + componentId + " for snapshotting failed!");
            }
        }

        try {
            this.configuration = config;
            this.data = machine.snapshot(componentId);
        }
        catch (BWFLAException e)
        {
            e.printStackTrace();
            LOG.warning("failed to retrieve snapshot.");
            throw new BWFLAException("failed to retrieve snapshot");
        }
    }

    private synchronized String saveAsObjectEnvironment(SaveObjectEnvironmentRequest request, UserContext userContext) throws BWFLAException {

		String archiveName = request.getObjectArchiveId();
		if (archiveName == null) {
			if(!userContext.isAvailable())
				request.setObjectArchiveId("default");
			else
				request.setObjectArchiveId(userContext.getUserId());
		}

		if(request.getObjectArchiveId() == null)
			request.setObjectArchiveId(archiveName);

		EmilEnvironment parentEnv = emilEnvironmentRepository.getEmilEnvironmentById(request.getEnvId(), userContext);
		EmilObjectEnvironment ee = createObjectEnvironment(request);

		parentEnv.addBranchId(ee.getEnvId());
		emilEnvironmentRepository.save(parentEnv, false, userContext);
		emilEnvironmentRepository.save(ee, true, userContext);

		return ee.getEnvId();
	}

	private synchronized String saveAsRevision(SaveDerivateRequest req, boolean checkpoint, UserContext userContext) throws BWFLAException {

		EmilEnvironment env = emilEnvironmentRepository.getEmilEnvironmentById(req.getEnvId(), userContext);
		if (env == null) {
			if (req instanceof SaveCreatedEnvironmentRequest) {
				// no emil env -> new environment has been created and committed
				final Environment _env = imagearchive.api()
						.v2()
						.environments()
						.fetch(req.getEnvId());

				env = new EmilEnvironment();
				env.setTitle(_env.getDescription().getTitle());
				env.setEnvId(req.getEnvId());
				env.setDescription("empty hard disk");
			} else
				throw new BWFLAException("Environment with id " + req.getEnvId() + " not found");
		}

		EmilEnvironment newEnv = createEnvironment(req, env, checkpoint);
		if (req instanceof SaveCreatedEnvironmentRequest)
			newEnv.setTitle(((SaveCreatedEnvironmentRequest) req).getTitle());

		if(req instanceof SaveNewEnvironmentRequest)
		{
			env.addBranchId(newEnv.getEnvId());
		}
		else
			env.addChildEnvId(newEnv.getEnvId());

		emilEnvironmentRepository.save(env, false, userContext);
		emilEnvironmentRepository.save(newEnv, true, userContext);
		if (newEnv instanceof EmilSessionEnvironment) {
			EmilSessionEnvironment session = (EmilSessionEnvironment) newEnv;
			EmilSessionEnvironment oldEnv = sessions.get(session.getUserId(), session.getObjectId());
			LOG.info("update: " + session.getEnvId());
			if (oldEnv != null)
				LOG.info("update: found oldEnv: " + oldEnv.getEnvId());
			LOG.info("updates parent env was: " + session.getParentEnvId());

			if (oldEnv != null && session.getParentEnvId() != null && !oldEnv.getEnvId().equals(session.getParentEnvId())) {

				LOG.info("would like to delete " + oldEnv.getEnvId());
				// delete(oldEnv.getEnvId());
			}
			sessions.add(session);
		}

		return newEnv.getEnvId();
	}

	private synchronized String saveAsUserSession(SaveUserSessionRequest request, UserContext userContext) throws BWFLAException {
		String sessionEnvId = saveUserSession(request);

		EmilEnvironment parentEnv = emilEnvironmentRepository.getEmilEnvironmentById(request.getEnvId(), userContext);
		if (parentEnv == null)
			throw new BWFLAException("parent environment not found: " + request.getEnvId());

		EmilSessionEnvironment sessionEnv = new EmilSessionEnvironment(parentEnv);
		sessionEnv.setObjectId(request.getObjectId());
		sessionEnv.setCreationDate((new Date()).getTime());
		sessionEnv.setUserId(request.getUserId());
		sessionEnv.setParentEnvId(parentEnv.getEnvId());

		LOG.info("adding session for user: " + request.getUserId()
				+ " object: " + request.getObjectId() + " env " + sessionEnvId);

		sessionEnv.setEnvId(sessionEnvId);

		emilEnvironmentRepository.save(sessionEnv, false, userContext);
		EmilSessionEnvironment oldEnv = sessions.get(sessionEnv.getUserId(), sessionEnv.getObjectId());
		LOG.info("saving: " + sessionEnvId);
		if (oldEnv != null)
			LOG.info("found oldEnv: " + oldEnv.getEnvId());
		LOG.info("parent env was: " + parentEnv.getParentEnvId());

		if (oldEnv != null && parentEnv.getParentEnvId() != null && !oldEnv.getEnvId().equals(parentEnv.getEnvId())) {

			LOG.info("would like to delete " + oldEnv.getEnvId());
			// delete(oldEnv.getEnvId());
		}
		sessions.add(sessionEnv);

		parentEnv.addChildEnvId(sessionEnvId);
		emilEnvironmentRepository.save(parentEnv, false, userContext);
		return sessionEnv.getEnvId();
	}

    public SnapshotResponse handleSnapshotRequest(String componentId, SnapshotRequest request, boolean checkpoint, UserContext userContext)
    {
        try {
            createSnapshot(componentId, checkpoint);

            if (request instanceof SaveObjectEnvironmentRequest) {
                return new SnapshotResponse(saveAsObjectEnvironment((SaveObjectEnvironmentRequest) request, userContext));
            }
            else if (request instanceof SaveDerivateRequest) { // implies SaveCreatedEnvironmentRequest && newEnvironmentRequest
                return new SnapshotResponse(saveAsRevision((SaveDerivateRequest) request, checkpoint, userContext));
            }
            else if (request instanceof SaveUserSessionRequest) {
                return new SnapshotResponse(saveAsUserSession((SaveUserSessionRequest) request, userContext));
            }
            else {
                return new SnapshotResponse(new BWFLAException("Unknown request type!"));
            }
        }
        catch (Exception exception) {
            final BWFLAException error = (exception instanceof BWFLAException) ?
                    (BWFLAException) exception : new BWFLAException(exception);

            final String message = "Handling " + ((checkpoint) ? "checkpoint" : "snapshot") + " request failed!";
            LOG.log(Level.WARNING, message, error);
            return new SnapshotResponse(error);
        }
    }
}
