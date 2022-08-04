package de.bwl.bwfla.emil.utils;

import com.openslx.eaas.imagearchive.api.v2.databind.ImportRequestV2;
import com.openslx.eaas.imagearchive.api.v2.databind.ImportTargetV2;
import com.openslx.eaas.imagearchive.client.endpoint.ImageArchive;
import com.openslx.eaas.imagearchive.client.endpoint.v2.util.EmulatorMetaHelperV2;
import com.openslx.eaas.imagearchive.databind.EmulatorMetaData;
import com.openslx.eaas.resolver.DataResolvers;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.datatypes.rest.CreateContainerImageResult;
import de.bwl.bwfla.emil.datatypes.rest.ImportEmulatorRequest;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.ImageMounter;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.MachineConfigurationTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ImportEmulatorUtil {

    public static void execute(ImportEmulatorRequest emulatorRequest, EmulatorMetaHelperV2 emuMetaHelper,
                               ImageArchive imagearchive, Logger log)
            throws BWFLAException
    {
        final var imageid = ImportEmulatorUtil.importImage(emulatorRequest.getImageUrl(), imagearchive);
        ImportEmulatorUtil.importMetadata(imageid, imagearchive, log);

        final var newEmulatorMetaData = new EmulatorMetaData();
        newEmulatorMetaData.image()
                .setId(imageid)
                .setCategory("base")
                .setFileSystemType(FileSystemType.EXT4.toString());

        if (emulatorRequest.getMetadata() != null) {
            CreateContainerImageResult.ContainerImageMetadata cmd = emulatorRequest.getMetadata();
            newEmulatorMetaData.provenance()
                    .setUrl(cmd.getContainerSourceUrl())
                    .setTag(cmd.getTag());

            if( cmd.getEmulatorType() == null || cmd.getEmulatorVersion() == null)
                throw new BWFLAException("not a emulator container or unsupported metadata format");

            newEmulatorMetaData.setName(cmd.getEmulatorType())
                    .setVersion(cmd.getEmulatorVersion())
                    .setDigest(cmd.getContainerDigest());
        }
        else {
            throw new BWFLAException("not a emulator container or unsupported metadata format");
        }

        emuMetaHelper.insert(newEmulatorMetaData);
    }

    private static String importImage(String srcurl, ImageArchive archive) throws BWFLAException
    {
        if (srcurl == null || srcurl.isEmpty())
            throw new IllegalArgumentException("Invalid data source URL!");

        final var request = new ImportRequestV2();
        request.source()
                .setUrl(srcurl);

        request.target()
                .setKind(ImportTargetV2.Kind.EMULATOR);

        try {
            return archive.v2()
                    .imports()
                    .await(request, 1, TimeUnit.HOURS);
        }
        catch (Exception error) {
            if (error instanceof BWFLAException)
                throw (BWFLAException) error;
            else throw new BWFLAException(error);
        }
    }

    private static void importMetadata(String imageid, ImageArchive archive, Logger log)
            throws BWFLAException
    {
        final var imageurl = DataResolvers.emulators()
                .resolve(imageid);

        log.info("Importing metadata from emulator image '" + imageid + "'...");
        try (final ImageMounter mounter = new ImageMounter(log)) {
            final Path workdir = ImageMounter.createWorkingDirectory();
            mounter.addWorkingDirectory(workdir);

            final var rawmnt = mounter.mount(imageurl, workdir.resolve("raw"));
            final var fsmnt = mounter.mount(rawmnt, workdir.resolve("fs"), FileSystemType.EXT4, true);
            if (!Files.exists(fsmnt.getMountPoint())) {
                throw new BWFLAException("Can't find filesystem mount, looks like an invalid emulator image!");
            }

            final var metadata = fsmnt.getMountPoint()
                    .resolve("metadata");

            if (!Files.exists(metadata)) {
                log.warning("No emulator metadata to import. Skipping!");
                return;
            }

            try {
                ImportEmulatorUtil.copyEnvironments(metadata.resolve("environments"), archive, log);
                ImportEmulatorUtil.copyTemplates(metadata.resolve("templates"), archive, log);
            }
            catch (Exception error) {
                log.log(Level.WARNING, "Importing emulator's metadata failed!", error);
            }
        }
    }

    private static void copyEnvironments(Path srcdir, ImageArchive archive, Logger log) throws IOException
    {
        if (!Files.exists(srcdir))
            return;  // nothing to copy!

        final var machines = archive.v2()
                .machines();

        final Consumer<Path> uploader = (path) -> {
            if (Files.isDirectory(path))
                return;

            try (final var input = Files.newInputStream(path)) {
                final var machine = JaxbType.from(input, MachineConfiguration.class);
                machines.replace(machine.getId(), machine);
                log.info("Imported machine '" + machine.getId() + "'");
            }
            catch (Exception error) {
                log.log(Level.WARNING, "Importing machine failed!", error);
            }
        };

        try (final var files = Files.list(srcdir)) {
            files.forEach(uploader);
        }
    }

    private static void copyTemplates(Path srcdir, ImageArchive archive, Logger log) throws IOException
    {
        if (!Files.exists(srcdir))
            return;  // nothing to copy!

        final var templates = archive.v2()
                .templates();

        final Consumer<Path> uploader = (path) -> {
            if (Files.isDirectory(path))
                return;

            try (final var input = Files.newInputStream(path)) {
                final var template = JaxbType.from(input, MachineConfigurationTemplate.class);
                templates.replace(template.getId(), template);
                log.info("Imported machine-template '" + template.getId() + "'");
            }
            catch (Exception error) {
                log.log(Level.WARNING, "Importing machine-template failed!", error);
            }
        };

        try (final var files = Files.list(srcdir)) {
            files.forEach(uploader);
        }
    }
}
