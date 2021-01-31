package de.bwl.bwfla.imagearchive.tasks;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.emucomp.api.Binding;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.imagearchive.DataUtil;
import de.bwl.bwfla.imagearchive.ImageHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportImageTask extends BlockingTask<String>
{


    private File destImgFile;
    private URL url;
    private File target;
    private String importId;
    private ImportType type;
    private InputStream inputStream;

    private final ImageHandler imageHandler;
    private final Logger log;

    private enum ImportType {
        URL,
        STREAM
    };

    public ImportImageTask(InputStream inputStream, File target, String importId, ImageHandler imageHandler, Logger log)  {

        this.imageHandler = imageHandler;
        this.inputStream = inputStream;
        this.log = log;

        this.importId = importId;
        this.target = target;
        destImgFile = new File(target, importId);

        type = ImportType.STREAM;
    }

    public ImportImageTask(URL url, File target, String importId, ImageHandler imageHandler, Logger log)
    {
        type = ImportType.URL;

        this.url = url;
        this.imageHandler = imageHandler;
        this.log = log;
        this.target = target;
        this.importId = importId;
        destImgFile = new File(target, importId);
    }

    private String fromStream() throws BWFLAException
    {
        log.info("Importing image " + importId + " from local stream...");
        DataUtil.writeData(inputStream, destImgFile);
        imageHandler.updateBackingFileUrl(destImgFile);
        imageHandler.createOrUpdateHandle(importId);
        return importId;
    }

    private void downloadBackingFiles(Path image, String curid) throws BWFLAException, IOException
    {
        log.info("Looking up image-info for: " + image.toString());

        final ImageInformation imginfo = new ImageInformation(image.toString(), log);
        if (!imginfo.hasBackingFile()) {
            log.info("No backing files left, stopping...");
            return;
        }

        final String nexturl = imginfo.getBackingFile();
        final String nextid = nexturl.substring(nexturl.lastIndexOf("/") + 1);
        final Path layer = target.toPath().resolve(nextid);

        imageHandler.lock(nextid);
        try {
            log.info("Downloading backing file " + nextid + " from: " + nexturl);

            if (Files.exists(layer)) {
                log.info("Backing file exists locally, skip downloading...");
            }
            else {
                final Binding binding = new Binding();
                try {
                    binding.setUrl(nexturl);
                    EmulatorUtils.copyRemoteUrl(binding, layer, log);
                }
                catch (BWFLAException error) {
                    log.log(Level.WARNING, "Downloading backing file failed!", error);
                    final String handle = imageHandler.getHandleUrl(nextid);
                    if (handle == null)
                        throw error;  // re-throw!

                    log.warning("Backing file handle found, retrying: " + handle);
                    binding.setUrl(handle);
                    EmulatorUtils.copyRemoteUrl(binding, layer, null);
                }
            }
        }
        finally {
            imageHandler.unlock(nextid);
        }

        // Update backing file URL for current image
        // to point to the newly downloaded layer...
        imageHandler.lock(curid);
        try {
            imageHandler.updateBackingFileUrl(image, imginfo);
        }
        finally {
            imageHandler.unlock(curid);
        }

        // Continue with lower layers...
        this.downloadBackingFiles(layer, nextid);
        imageHandler.createOrUpdateHandle(nextid);
    }

    private String fromUrl() throws BWFLAException
    {
        log.info("Downloading image " + importId + " from: " + url.toString());
        try {
            Binding b = new Binding();
            b.setUrl(url.toString());

            // XmountOptions options = new XmountOptions();
            // EmulatorUtils.copyRemoteUrl(b, destImgFile.toPath(), options);

            if(!destImgFile.exists()) {
                EmulatorUtils.copyRemoteUrl(b, destImgFile.toPath(), log);
            }

            log.info("Looking up image file format...");
            ImageInformation info = new ImageInformation(destImgFile.toPath().toString(), log);
            ImageInformation.QemuImageFormat fmt = info.getFileFormat();
            if (fmt == null) {
                destImgFile.delete();
                throw new BWFLAException("could not determine file fmt");
            }

            log.info("Image file format: " + fmt.toString());

            switch (fmt) {
                case VMDK:
                case VHD:
                    final File origImgFile = new File(destImgFile.toString() + ".orig");
                    destImgFile.renameTo(origImgFile);
                    try {
                        EmulatorUtils.convertImage(origImgFile.toPath(), destImgFile.toPath(), ImageInformation.QemuImageFormat.QCOW2, log);
                    }
                    finally {
                        origImgFile.delete();
                    }
                default:
                    log.info("Downloading image's backing files...");
                    this.downloadBackingFiles(destImgFile.toPath(), importId);
                    imageHandler.createOrUpdateHandle(importId);
                    log.info("Downloading image " + importId + " finished successfully");
                    return importId;
            }
        } catch (Exception error) {
            log.log(Level.WARNING, "Downloading image failed!", error);
            throw new BWFLAException("Downloading image '" + destImgFile + "' failed!", error);
        }
    }

    @Override
    protected String execute() throws Exception {
        switch(type) {
            case URL:
                return fromUrl();
            case STREAM:
                return fromStream();
            default:
                throw new BWFLAException("unknown import type");
        }
    }
}
