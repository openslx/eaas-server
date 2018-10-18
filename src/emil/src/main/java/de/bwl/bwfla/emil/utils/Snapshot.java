package de.bwl.bwfla.emil.utils;

import de.bwl.bwfla.api.imagearchive.ImageArchiveMetadata;
import de.bwl.bwfla.api.imagearchive.ImageType;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.*;
import de.bwl.bwfla.emil.datatypes.snapshot.SaveDerivateRequest;
import de.bwl.bwfla.emil.datatypes.snapshot.SaveNewEnvironmentRequest;
import de.bwl.bwfla.emil.datatypes.snapshot.SaveObjectEnvironmentRequest;
import de.bwl.bwfla.emil.datatypes.snapshot.SaveUserSessionRequest;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Snapshot
{
    protected static final Logger LOG = Logger.getLogger("Image Snapshot");

    private final MachineConfiguration configuration;

    /** Binding's changed/written data */
    private final List<BindingDataHandler> data;


    public Snapshot(MachineConfiguration config)
    {
        // No disks, but we create an empty snapshot to handle floppy objs env
        this(config, null);
    }

    public Snapshot(MachineConfiguration config, List<BindingDataHandler> data) {
        this.configuration = config;
        this.data = data;
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

    public EmilEnvironment createEnvironment(EnvironmentsAdapter environmentsAdapter,
                                             SaveDerivateRequest req,
                                             EmilEnvironment parentEnv) throws BWFLAException {

        // if(dataHandler == null)
        //    throw new BWFLAException("empty snapshots not supported.");

        EmilEnvironment newEnv = null;
        MachineConfiguration machineConfiguration = EmulationEnvironmentHelper.clean(configuration);

        ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
        if (parentEnv instanceof EmilSessionEnvironment) {
            iaMd.setType(ImageType.SESSIONS);
            newEnv = new EmilSessionEnvironment((EmilSessionEnvironment)parentEnv);
        } else if (parentEnv instanceof EmilObjectEnvironment) {
            iaMd.setType(ImageType.OBJECT);
            MachineConfiguration oldEnv = (MachineConfiguration) environmentsAdapter.getEnvironmentById(req.getEnvId());
            copyBinding(oldEnv, machineConfiguration, ((EmilObjectEnvironment) parentEnv).getObjectId());
            newEnv = new EmilObjectEnvironment((EmilObjectEnvironment) parentEnv);
        } else {
            iaMd.setType(ImageType.DERIVATE);
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

        String newId = environmentsAdapter.importMachineEnvironment(machineConfiguration, data, iaMd);
        if(newId == null)
            throw new BWFLAException("create revision: importMachineEnvironment failed");

        newEnv.setVisible(true);
        newEnv.setParentEnvId(parentEnv.getEnvId());
        newEnv.setEnvId(newId);
        newEnv.setDescription(req.getMessage());
        return newEnv;
    }

    public String saveUserSession(EnvironmentsAdapter environmentsAdapter,
                                  ObjectArchiveHelper objectArchiveHelper,
                                  SaveUserSessionRequest request) throws BWFLAException
    {

        if (data == null)
            throw new BWFLAException("empty snapshots not supported.");

        MachineConfiguration env = EmulationEnvironmentHelper.clean(configuration);
        env.getDescription().setTitle("user session: " + request.getUserId());

        ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
        iaMd.setType(ImageType.SESSIONS);
        iaMd.setUserId(request.getUserId());

        String objectId = request.getObjectId();
//        if(objectId != null)
//        {
//            String archiveName = request.getArchiveId();
//            if (archiveName == null)
//                archiveName = "default";
//
//            ObjectArchiveBinding binding = new ObjectArchiveBinding(environmentsAdapter.toString(), archiveName, request.getObjectId());
//            FileCollection fc = objectArchiveHelper.getObjectReference(request.getArchiveId(), request.getObjectId());
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
        String newId = environmentsAdapter.importMachineEnvironment(env, data, iaMd);
        if (newId == null)
            throw new BWFLAException("importMachineEnvironment failed");

        return newId;
    }

    public EmilObjectEnvironment createObjectEnvironment(EnvironmentsAdapter environmentsAdapter,
                                                         ObjectArchiveHelper objectArchiveHelper,
                                                         SaveObjectEnvironmentRequest request) throws BWFLAException {

        if(request.getTitle() == null)
            throw new BWFLAException("invalid request: title is missing");

        if(request.getObjectId() == null)
            throw new BWFLAException("invalid request: invalid object data");

        MachineConfiguration env = EmulationEnvironmentHelper.clean(configuration);
        env.getDescription().setTitle(request.getTitle());

        ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
        iaMd.setType(ImageType.OBJECT);

        int driveId = request.getDriveId();

        if(request.isEmbeddedObject()) {
            String archiveName = request.getArchiveId();
            if (archiveName == null)
                archiveName = "default";

            ObjectArchiveBinding binding = new ObjectArchiveBinding(environmentsAdapter.toString(), archiveName, request.getObjectId());
            FileCollection fc = objectArchiveHelper.getObjectReference(request.getArchiveId(), request.getObjectId());

            if (fc == null)
                throw new BWFLAException("create object environment: object not found: " + request.getObjectId());

            driveId = EmulationEnvironmentHelper.addArchiveBinding(env, binding, fc);
            try {
                System.out.println("ready to import machine: " + env.value());
            } catch (JAXBException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }

        String newId = null;
        if (data != null)
            newId = environmentsAdapter.importMachineEnvironment(env, data, iaMd);
        else
            newId = environmentsAdapter.importMetadata(env.toString(), iaMd, false);

        if (newId == null)
            throw new BWFLAException("importMachineEnvironment failed");

        EmilObjectEnvironment ee = new EmilObjectEnvironment();
        ee.setEnvId(newId);
        ee.setVisible(true);
        ee.setTitle(request.getTitle());
        ee.setDriveId(driveId);
        ee.setObjectId(request.getObjectId());
        ee.setArchiveId(request.getArchiveId());
        ee.setEmulator(env.getEmulator().getBean());
        ee.setOs(env.getOperatingSystemId());
        ee.setDescription(request.getMessage());

        return ee;
    }
}
