package de.bwl.bwfla.imagearchive.generalization;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.DiskDescription;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.emucomp.api.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ImageGeneralizer {

    protected static final Logger log = Logger.getLogger(ImageGeneralizer.class.getName());


    /**
     * Mount
     * @param imgFile
     * @param generalization
     * @param emulatorArchiveprefix
     * @throws BWFLAException
     * @throws IOException
     */
    public static void  applyScriptIfCompatible(File imgFile, GeneralizationPatch generalization, String emulatorArchiveprefix) throws BWFLAException, IOException {
        try (final ImageMounter mounter = new ImageMounter(log)) {
            final Path workdir = ImageMounter.createWorkingDirectory();
            mounter.addWorkingDirectory(workdir);

            ImageMounter.Mount rawmnt = mounter.mount(imgFile.toPath(), workdir.resolve("raw"));
            List<DiskDescription.Partition> partitions = findValidPartitions(rawmnt.getTargetImage(), generalization);
            String fs = generalization.getImageGeneralization().getPrecondition().getFileSystem();

            if (partitions == null) {
                throw new BWFLAException("Partion with label: " + generalization.getImageGeneralization().getPrecondition().getPartitionLabel() +
                        " and FileSystem " + fs + " was not found!");
            }

            for (DiskDescription.Partition partition : partitions) {
                final FileSystemType fstype = FileSystemType.fromString(partition.getFileSystemType());
                File patch = null;
                try {
                    rawmnt = mounter.remount(rawmnt, partition.getStartOffset(), partition.getSize());
                    ImageMounter.Mount fsmnt = mounter.mount(rawmnt, workdir.resolve("fs"), fstype);

                    patch  = new File("/tmp/patch-" + UUID.randomUUID());

                    InputStream is = EaasFileUtils.fromUrlToInputSteam(new URL(emulatorArchiveprefix + "/patch/" + generalization.getImageGeneralization().getModificationScript()), "GET", "metadata", "true");
                    FileUtils.copyInputStreamToFile(is, patch);

                    if (!patch.setExecutable(true)) {
                        throw new BWFLAException("failed to make patch executable!");
                    }
                    File[] fsList = fsmnt.getMountPoint().toFile().listFiles();
                    if (fsList == null)
                        throw new BWFLAException("mount failed: mounted dir is null");

                    if (isScriptCompatible(fsmnt.getMountPoint().toFile(), generalization)) {
                        patchPartition(fsmnt.getMountPoint().toFile(), patch.toString());
                        break;
                    } else {
                        fsmnt.unmount(false);
                    }
                    log.warning("Script is not compatible with partition: \n" + " Flags: " + partition.getFlags() + " PartitionName " + partition.getPartitionName());
                } finally {
                   if(patch != null && patch.exists()) patch.delete();
                }
            }
        }
    }



    /**
     * Check whether result of applied script would be successful
     * @return
     */
    private static boolean isScriptCompatible(File tempMountDir, GeneralizationPatch generalization) throws IOException {
        RequiredFiles requiredFiles = generalization.getImageGeneralization().getPrecondition().getRequiredFiles();
        /*
        if required files were specified in template xml file, we need to check if they're exist
         */
        if(requiredFiles.getFileNames() != null)
        for (int i = 0; i < requiredFiles.getFileNames().length; i++) {
            if(! new File(tempMountDir.getAbsolutePath() + requiredFiles.getFileNames()[i]).exists()) {
                log.warning("Required file not found! " + tempMountDir.getAbsolutePath() + requiredFiles.getFileNames()[i]);
                return false;
            }
        }
        return true;
    }

    /**
     * Run the patch (sh file)
     * @param mountedDir
     * @param patchPath
     */
    private static void patchPartition(File mountedDir, String patchPath) throws BWFLAException {
        DeprecatedProcessRunner runner = new DeprecatedProcessRunner();
        runner.setCommand(patchPath);
        runner.setLogger(log);
        runner.addArgument(mountedDir.getAbsolutePath());
        runner.redirectStdErrToStdOut(false);
        runner.start();
        int ret = runner.waitUntilFinished();
        String error, stdout;
        try {
            stdout = runner.getStdOutString();
            error = runner.getStdErrString();
        } catch (IOException e) {
            throw new BWFLAException("process runner: cant access output");
        }
        runner.cleanup();

        if(ret != 0)
            throw new BWFLAException(error);
        log.info(stdout);
    }

    /**
     * Function to find a necessary partition
     *
     * @param ddFile
     * @return
     * @throws BWFLAException
     * @throws IOException
     */
    private static List<DiskDescription.Partition> findValidPartitions(Path ddFile, GeneralizationPatch generalization) throws BWFLAException, IOException {
        String partitionLabel = generalization.getImageGeneralization().getPrecondition().getPartitionLabel();
        DiskDescription disk = DiskDescription.read(ddFile, log);
        List<DiskDescription.Partition> partitions = disk.getPartitions();
        List<DiskDescription.Partition> validPartitions = new ArrayList<>();
        for (DiskDescription.Partition p : partitions) {
            log.info("part: " + p.getIndex() + " start: " + p.getStartOffset() + " size: " + p.getSize() + " fs: "
                    + p.getFileSystemType() + " Flags: " + p.getFlags() + " PartitionName " + p.getPartitionName());
            if (partitionLabel.equals("") || partitionLabel == null) {
                return partitions;
            } else {
                if (ImageGeneralizationUtils.checkPartition(p, partitionLabel, generalization.getImageGeneralization().getPrecondition().getFileSystem()))
                    validPartitions.add(p);
            }
        }
        if (validPartitions.size() == 0)
            return null;
        else
            return validPartitions;
    }
}
