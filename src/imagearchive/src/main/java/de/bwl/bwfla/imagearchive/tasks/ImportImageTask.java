package de.bwl.bwfla.imagearchive.tasks;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.common.utils.ImageInformation;
import de.bwl.bwfla.emucomp.api.Binding;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.imagearchive.DataUtil;
import de.bwl.bwfla.imagearchive.ImageHandler;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportImageTask extends AbstractTask<String> {


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
        imageHandler.resolveLocalBackingFile(destImgFile);
        imageHandler.createOrUpdateHandle(importId);
        return importId;
    }

    private void downloadDependencies(String depurl) throws BWFLAException
    {
        final String depid = depurl.substring(depurl.lastIndexOf("/") + 1);
        final Path depImgFile = target.toPath().resolve(depid);

        imageHandler.lock(depid);
        try {
            log.info("Downloading backing file " + depid + " from: " + depurl);

            if (Files.exists(depImgFile)) {
                log.info("Backing file exists locally, skipping...");
            }
            else {
                final Binding binding = new Binding();
                try {
                    binding.setUrl(depurl);
                    EmulatorUtils.copyRemoteUrl(binding, depImgFile, null, log);
                }
                catch (BWFLAException error) {
                    log.log(Level.WARNING, "Downloading backing file failed!", error);
                    final String handle = imageHandler.getHandleUrl(depid);
                    if (handle == null)
                        throw error;  // re-throw!

                    log.warning("Backing file handle found, retrying: " + handle);
                    binding.setUrl(handle);
                    EmulatorUtils.copyRemoteUrl(binding, depImgFile, null);
                }
            }
        }
        finally {
            imageHandler.unlock(depid);
        }

        final String nexturl = imageHandler.resolveLocalBackingFile(depImgFile);
        if (nexturl != null)
            this.downloadDependencies(nexturl);

        imageHandler.createOrUpdateHandle(depid);
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
                EmulatorUtils.copyRemoteUrl(b, destImgFile.toPath(), null, log);
            }

            log.info("Looking up image file format...");
            ImageInformation.QemuImageFormat fmt = EmulatorUtils.getImageFormat(destImgFile.toPath(), log);
            if (fmt == null) {
                destImgFile.delete();
                throw new BWFLAException("could not determine file fmt");
            }
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
                    String result = imageHandler.resolveLocalBackingFile(destImgFile);
                    if(result != null)
                        downloadDependencies(result);

                    imageHandler.createOrUpdateHandle(importId);
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
