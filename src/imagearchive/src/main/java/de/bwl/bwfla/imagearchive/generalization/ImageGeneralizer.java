package de.bwl.bwfla.imagearchive.generalization;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.common.utils.DiskPartitionDescription;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.emucomp.api.*;
import de.bwl.bwfla.imagearchive.ImageArchiveBackend;
import de.bwl.bwfla.imagearchive.ImageHandler;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class ImageGeneralizer {

    protected static final Logger log = Logger.getLogger(ImageGeneralizer.class.getName());


    /**
     * Mount
     *
     * @param imgFile
     * @param templateEnv
     * @throws BWFLAException
     * @throws IOException
     */
    public static void  applyScriptIfCompatible(File imgFile, MachineConfigurationTemplate templateEnv, String emulatorArchiveprefix, String apiKey, String imageProxy) throws BWFLAException, IOException {
        ImageMounter image = null;
        try {
            image = new ImageMounter(imgFile.toPath());
            image.setXmountProxy("http://jwt:" + apiKey + "@" + imageProxy);
            image.mountDD();

            List<DiskPartitionDescription.DiskPartition> partitions = findValidPartitions(image.getDdFile().toFile(), templateEnv);
            String fs = templateEnv.getImageGeneralization().getPrecondition().getFileSystem();

            if (partitions == null) {
                throw new BWFLAException("Partion with label: " + templateEnv.getImageGeneralization().getPrecondition().getPartitionLabel() +
                        " and FileSystem " + fs + " was not found!");
            }

            for (DiskPartitionDescription.DiskPartition partition : partitions) {
                File patch = null;
                try {
                    image.remountDDWithOffset(partition.getBegin(), partition.getSize());
                    image.mountFileSystem(FileSystemType.fromString(partition.getFsType()));

                    patch  = new File("/tmp/patch-" + UUID.randomUUID());

                    InputStream is = EaasFileUtils.fromUrlToInputSteam(new URL(emulatorArchiveprefix + "/patch/" + templateEnv.getImageGeneralization().getModificationScript()), "GET", "metadata", "true", apiKey, imageProxy);
                    FileUtils.copyInputStreamToFile(is, patch);

                    if (!patch.setExecutable(true)) {
                        throw new BWFLAException("failed to make patch executable!");
                    }
                    File[] fsList = image.getFsDir().toFile().listFiles();
                    if (fsList == null)
                        throw new BWFLAException("mount failed: mounted dir is null");

                    if (isScriptCompatible(templateEnv, image.getFsDir().toFile())) {
                        patchPartition(image.getFsDir().toFile(), patch.toString());
                        break;
                    } else {
                        image.completeUnmount();
                    }
                    log.warning("Script is not compatible with partition: \n" + " Flags: " + partition.getFlags() + " PartitionName " + partition.getPartitionName());
                } finally {
                   if(patch != null && patch.exists()) patch.delete();
                }
            }
            image.completeUnmount();
            image = null;
        } finally {
            if (image != null)
                image.completeUnmount();
        }
    }



    /**
     * Check whether result of applied script would be successful
     * @return
     */
    private static boolean isScriptCompatible(MachineConfigurationTemplate templateEnv, File tempMountDir) throws IOException {
        RequiredFiles requiredFiles = templateEnv.getImageGeneralization().getPrecondition().getRequiredFiles();
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
    private static List<DiskPartitionDescription.DiskPartition> findValidPartitions(File ddFile, MachineConfigurationTemplate templateEnv) throws BWFLAException, IOException {
        String partitionLabel = templateEnv.getImageGeneralization().getPrecondition().getPartitionLabel();
        DiskPartitionDescription parted = new DiskPartitionDescription(ddFile);
        List<DiskPartitionDescription.DiskPartition> partitions = parted.getPartitionTable();
        List<DiskPartitionDescription.DiskPartition> validPartitions = new ArrayList<>();
        for (DiskPartitionDescription.DiskPartition p : partitions) {
            log.info("part: " + p.getIndex() + " start: " + p.getBegin() + " size: " + p.getSize() + " fs: "
                    + p.getFsType() + " Flags: " + p.getFlags() + " PartitionName " + p.getPartitionName());
            if (partitionLabel.equals("") || partitionLabel == null) {
                return partitions;
            } else {
                if (ImageGeneralizationUtils.checkPartition(p, partitionLabel, templateEnv.getImageGeneralization().getPrecondition().getFileSystem()))
                    validPartitions.add(p);
            }
        }
        if (validPartitions.size() == 0)
            return null;
        else
            return validPartitions;
    }

    private static List<DiskPartitionDescription.DiskPartition> getPartitions (File ddFile) throws BWFLAException, IOException {
        DiskPartitionDescription parted = new DiskPartitionDescription(ddFile);
        return parted.getPartitionTable();
    }
}
