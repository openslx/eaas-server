package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.EmilEnvironment;
import de.bwl.bwfla.emucomp.api.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExportEnvironmentTask extends AbstractTask<Object> {

    private static final Logger LOG = Logger.getLogger(ExportEnvironmentTask.class.getName());

//    private final String host;
//    private final List<String> envIds;
//    private boolean embedded;
//    private final String remoteObjectArchiveHost;

    private ExportEnvironmentRequest request;

    public ExportEnvironmentTask(ExportEnvironmentRequest request)
    {
        this.request = request;
    }

//    private void exportObjectAsImage(MachineConfiguration conf, EnvironmentsAdapter remoteAdapter)
//            throws BWFLAException {
//        List<AbstractDataResource> resources = conf.getAbstractDataResource();
//        for(AbstractDataResource r : resources)
//        {
//            if(!(r instanceof ObjectArchiveBinding))
//                continue;
//
//            ObjectArchiveBinding oab = (ObjectArchiveBinding)r;
//
//            String host = oab.getArchiveHost();
//            String objectId = oab.getObjectId();
//            String archive = oab.getArchive();
//
//            ObjectArchiveHelper helper = new ObjectArchiveHelper(host);
//            ObjectArchiveHelper remoteHelper = new ObjectArchiveHelper(remoteObjectArchiveHost);
//            ObjectFileCollection object = helper.getObjectHandle(archive, objectId);
////			if(object.getFiles() != null) {
////				for (ObjectFileCollectionHandle entry : object.getFiles()) {
////					LOG.info("entry: " + entry.getFilename());
////				}
////			}
//            remoteHelper.importObject(archive, object);
//            oab.setArchiveHost(remoteObjectArchiveHost);
//        }
//    }

//    private void exportObjectEmbedded(MachineConfiguration conf, EnvironmentsAdapter remoteAdapter) throws BWFLAException {
//        LOG.info("export image embedded");
//        List<ImageArchiveBinding> importedObjects = new ArrayList<>();
//        for(Iterator<AbstractDataResource> iter = conf.getAbstractDataResource().iterator(); iter.hasNext();)
//        {
//            AbstractDataResource r = iter.next();
//            if(!(r instanceof ObjectArchiveBinding))
//                continue;
//
//            ObjectArchiveBinding oab = (ObjectArchiveBinding)r;
//            String host = oab.getArchiveHost();
//            String objectId = oab.getObjectId();
//            String archive = oab.getArchive();
//
//            ObjectArchiveHelper helper = new ObjectArchiveHelper(host);
//            ObjectFileCollection object = helper.getObjectHandle(archive, objectId);
//
//            if(object.getFiles() == null || object.getFiles().size() == 0)
//                continue;
//
//            if(object.getFiles().size() > 1) {
//                LOG.warning("objects with multiple files are not supported");
//                continue;
//
//            }
//
//            ImageArchiveMetadata iaMD = new ImageArchiveMetadata();
//            iaMD.setType(ImageType.OBJECT);
//            ObjectFileCollectionHandle objHandle = object.getFiles().get(0);
//            EnvironmentsAdapter.ImportImageHandle imageHandle= remoteAdapter.importImage(objHandle.getHandle(), iaMD);
//
//            iter.remove();
//            ImageArchiveBinding imageArchiveBinding = imageHandle.getBinding(60*60*24);
//            imageArchiveBinding.setId(objectId);
//            importedObjects.add(imageArchiveBinding);
//        }
//
//        for(ImageArchiveBinding b : importedObjects)
//        {
//            String bindingUrl = "binding://" + b.getId();
//            for(Drive d : conf.getDrive())
//            {
//                if(!d.getData().startsWith(bindingUrl))
//                    continue;
//                d.setData(bindingUrl);
//            }
//            conf.getAbstractDataResource().add(b);
//        }
//    }

    @Override
    protected Object execute() throws Exception {
        try {
            exportToPath();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /*
        /exportDir
           /image-data
             /image
             /meta-data
           /object-data
             /...
           /ui-data
     */

    private static final String IMAGE_DATA_DIR = "image-data";
    private static final String OBJECT_DATA_DIR = "object-data";
    private static final String ENV_DATA_DIR = "environment-data";

    private static Path createExportFolders(String base, String envId) throws IOException, BWFLAException {

        Path exportPath = Paths.get(base);
        if(!Files.exists(exportPath))
        {
            LOG.info("Creating export directory: " + exportPath);
            Files.createDirectories(exportPath);
        }

        Path exportEnvPath = exportPath.resolve(envId);
        // if(Files.exists(exportEnvPath))
        //    throw new BWFLAException("Environment already exported: " + exportEnvPath);

        Files.createDirectories(exportEnvPath);
        Files.createDirectories(exportEnvPath.resolve(IMAGE_DATA_DIR));
        Files.createDirectories(exportEnvPath.resolve(OBJECT_DATA_DIR));
        Files.createDirectories(exportEnvPath.resolve(ENV_DATA_DIR));

        return exportEnvPath;
    }

    private void exportImage(Path exportEnvPath, Environment environment) throws IOException, BWFLAException {
        BufferedWriter writer = null;

        Path mdPath = exportEnvPath.resolve(IMAGE_DATA_DIR).resolve("meta-data");
        if(!Files.exists(mdPath))
            Files.createDirectories(mdPath);

        Path imagePath = exportEnvPath.resolve(IMAGE_DATA_DIR).resolve("image");
        if(!Files.exists(imagePath))
            Files.createDirectories(imagePath);

        if(environment instanceof MachineConfiguration) {
            for (AbstractDataResource ab : ((MachineConfiguration)environment).getAbstractDataResource()) {

                if (ab instanceof ImageArchiveBinding) {
                    ImageArchiveBinding iab = (ImageArchiveBinding) ab;
                    exportFullImageStack(iab, imagePath, iab.getImageId());
                }
            }
        }

        try {
            writer = new BufferedWriter(new FileWriter(mdPath + "/" + environment.getId() + ".xml"));
            writer.write(environment.toString());
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void exportToPath() throws IOException, BWFLAException {
        final DatabaseEnvironmentsAdapter environmentHelper = request.envHelper;
        final EmilEnvironmentRepository environmentRepository = request.environmentRepository;

        Path exportFolder = createExportFolders(request.exportFilePath, request.envId);

        EmilEnvironment emilEnvironment = environmentRepository.getEmilEnvironmentById(request.envId, request.userCtx);
        if(emilEnvironment == null)
            throw new BWFLAException("Unknown ID: EmilEnvironment not found: " + request.envId + " " + request.userCtx);

        while(true) {

            Environment abstractEnv = environmentHelper.getEnvironmentById(request.archive, request.envId);
            if (abstractEnv == null)
                throw new BWFLAException("could not find environment: " + request.envId);

            Path outfile = exportFolder.resolve(ENV_DATA_DIR).resolve(emilEnvironment.getEnvId());
            Files.write(outfile, emilEnvironment.jsonValueWithoutRoot(true).getBytes());
            exportImage(exportFolder, abstractEnv);

            if(emilEnvironment.getParentEnvId() == null) {
                break;
            }

            emilEnvironment = environmentRepository.getEmilEnvironmentById(emilEnvironment.getParentEnvId(), request.userCtx);
            if(emilEnvironment == null)
                throw new BWFLAException("Unknown ParentID: EmilEnvironment not found: " + request.envId + " " + request.userCtx);
        }


        /*
        exportEnvironmentMedia((MachineConfiguration) abstractEnv,
                (MachineConfiguration) localChosenEnv, imageDir, objectDir);

        // fix archive binding for USB
        for (AbstractDataResource ab : ((MachineConfiguration) abstractEnv).getAbstractDataResource()) {
            if (ab instanceof ObjectArchiveBinding) {
                ObjectArchiveBinding binding = (ObjectArchiveBinding) ab;
                binding.setArchive("objects");
                binding.setArchiveHost("localhost:8080");
            }
        }
         */

    }

    private void exportObject(File objectIdDir, FileCollectionEntry fce) throws BWFLAException
    {
        String typeName = fce.getType().name().toLowerCase();
        if(typeName.equals("cdrom"))
            typeName = "iso";

        File typeDir = new File(objectIdDir, typeName);
        if(!typeDir.exists())
            typeDir.mkdir();

        File destImage;
        if(fce.getLocalAlias() == null || fce.getLocalAlias().isEmpty())
            destImage = new File(typeDir, fce.getId());
        else
            destImage = new File(typeDir, fce.getLocalAlias());

        System.out.println(fce.getUrl() + " to: " + destImage);
        EmulatorUtils.copyRemoteUrl(fce, destImage.toPath(), null);
    }

    private static void exportFullImageStack(Binding ref, Path imageDir, String fileName) throws BWFLAException, IOException {
        Path dest = imageDir.resolve(fileName);
        EmulatorUtils.copyRemoteUrl(ref, dest, null);
        ImageInformation info = new ImageInformation(dest.toString(), LOG);

//        String backingfile = info.getBackingFile();
//        while(backingfile != null) {
//            LOG.severe(backingfile);
//            dest = imageDir.resolve(ImageInformation.getBackingImageId(backingfile));
//            Binding b = new Binding();
//            b.setUrl(backingfile);
//            EmulatorUtils.copyRemoteUrl(b, dest, null);
//            info = new ImageInformation(dest.toString(), LOG);
//            backingfile = info.getBackingFile();
//        }
    }

    private static void exportCowFile(String ref, File imageDir) throws IOException, BWFLAException
    {
        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);

        File tempDir = Files.createTempDirectory("", PosixFilePermissions.asFileAttribute(permissions)).toFile();

        java.nio.file.Path cowPath = tempDir.toPath().resolve("export.cow");
        QcowOptions options = new QcowOptions();
        options.setBackingFile(ref);
        EmulatorUtils.createCowFile(cowPath, options);
        java.nio.file.Path fuseMountpoint = cowPath
                .resolveSibling(cowPath.getFileName() + ".fuse");

        File exportFile = EmulatorUtils.mountCowFile(cowPath, fuseMountpoint).toFile();

        File dest = new File(imageDir, ImageInformation.getBackingImageId(ref));
        // java.nio.file.Files.copy(exportFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        EmulatorUtils.convertImage(exportFile.toPath(), dest.toPath(), ImageInformation.QemuImageFormat.QCOW2, LOG);
        tempDir.delete();
    }

//    private void sync()
//    {
//        EnvironmentsAdapter remoteAdapter = new EnvironmentsAdapter(host);
//        try {
//            for (String envId : envIds) {
//                LOG.info("syncing " + envId);
//                Environment env = envHelper.getEnvironmentById(envId);
//
//                ImageExport dependencies = envHelper.getImageDependecies(envId);
//                List<ImageFileInfo> infos = dependencies.getImageFiles();
//                for (ImageFileInfo info : infos) {
//                    LOG.info("ExportImageTask: upload dependency " + info.getId());
//                    ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
//                    iaMd.setType(info.getType());
//                    iaMd.setImageId(info.getId());
//                    iaMd.setDeleteIfExists(true);
//                    EnvironmentsAdapter.ImportImageHandle handle = remoteAdapter.importImage(info.getFileHandle(), iaMd);
//                    if(handle.getBinding(60*60*24*7)== null)
//                        return new BWFLAException("import failed: timeout");
//                }
//
////                MachineConfiguration configuration = (MachineConfiguration)env;
////                if(!embedded)
////                    exportObjectAsImage(configuration, remoteAdapter);
////                else
////                    exportObjectEmbedded(configuration, remoteAdapter);
////
////                try {
////                    ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
////                    iaMd.setType(ImageType.OBJECT);
////                    remoteAdapter.importMetadata(env.value(), iaMd, true);
////                } catch (JAXBException e) {
////                    LOG.log(Level.WARNING, e.getMessage(), e);
////                    return new BWFLAException("metadata import failed");
////                }
//            }
//        } catch (BWFLAException e) {
//            e.printStackTrace();
//            return e;
//        }
//
//        return null;
//    }

    private AbstractDataResource getResourceById(MachineConfiguration env, String id)
    {
        for (AbstractDataResource ab : env.getAbstractDataResource()) {
            if (ab.getId().equals(id))
                return ab;
        }
        return null;

    }

    public static class ExportEnvironmentRequest {
        enum ExportTarget {
            FILEPATH,
        }

        public String exportFilePath;
        public DatabaseEnvironmentsAdapter envHelper;
        public String envId;
        public String archive;
        public EmilEnvironmentRepository environmentRepository;
        public String userCtx;
    }
}
