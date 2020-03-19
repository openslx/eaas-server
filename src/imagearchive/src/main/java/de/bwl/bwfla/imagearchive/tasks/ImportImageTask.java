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
        DataUtil.writeData(inputStream, destImgFile);
        imageHandler.resolveLocalBackingFile(destImgFile);
        imageHandler.createOrUpdateHandle(importId);
        return importId;
    }

    private void downloadDependencies(String url) throws BWFLAException {
        final String imageid = url.substring(url.lastIndexOf("/") + 1);
        Binding b = new Binding();
        b.setUrl(url.toString());
        File dst = new File(target, imageid);

        if(dst.exists()) {
            log.warning("downloading dependencies: skip " + dst.getAbsolutePath());
        }
        else {
            try {
                EmulatorUtils.copyRemoteUrl(b, dst.toPath(), null);
            } catch (BWFLAException e) {
                String hdlUrl = imageHandler.getHandleUrl(imageid);
                if(hdlUrl != null) {
                    b.setUrl(hdlUrl);
                    log.severe("retrying handle... " + b.getUrl());
                    EmulatorUtils.copyRemoteUrl(b, dst.toPath(), null);
                }
            }
        }
        String result = imageHandler.resolveLocalBackingFile(dst);
        imageHandler.createOrUpdateHandle(imageid);

        if(result != null)
            downloadDependencies(result);
    }

    private String fromUrl() throws BWFLAException
    {
        try {
            Binding b = new Binding();
            b.setUrl(url.toString());

            // XmountOptions options = new XmountOptions();
            // EmulatorUtils.copyRemoteUrl(b, destImgFile.toPath(), options);

            if(!destImgFile.exists()) {
                EmulatorUtils.copyRemoteUrl(b, destImgFile.toPath(), null);
            }
            ImageInformation.QemuImageFormat fmt = EmulatorUtils.getImageFormat(destImgFile.toPath(), log);
            if (fmt == null) {
                throw new BWFLAException("could not determine file fmt");
            }
            switch (fmt) {
                case VMDK:
                case VHD:
                    final File origImgFile = new File(destImgFile.toString() + ".orig");
                    destImgFile.renameTo(origImgFile);
                    EmulatorUtils.convertImage(origImgFile.toPath(), destImgFile.toPath(), ImageInformation.QemuImageFormat.QCOW2, log);
                    origImgFile.delete();
                default:
                    String result = imageHandler.resolveLocalBackingFile(destImgFile);
                    imageHandler.createOrUpdateHandle(importId);

                    if(result != null)
                        downloadDependencies(result);

                    return importId;
            }
        } catch (Exception e1) {
            log.log(Level.WARNING, e1.getMessage(), e1);
            throw new BWFLAException("failed moving incoming image to " + destImgFile + " reason " + e1.getMessage());
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
