package de.bwl.bwfla.common.utils;

import de.bwl.bwfla.common.datatypes.QemuImage;
import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Created by klaus on 29.08.17.
 */
public class ImageInformation {

    private String backingFile = null;
    private QemuImageFormat fileFormat = null;
    private QemuImage imageInfo;

    protected final Logger log = Logger.getLogger(this.getClass().getName());

    static public String getBackingImageId(String bf) throws BWFLAException {
        if (bf.contains("exportname")) {
            return bf.substring(bf.lastIndexOf('=') + 1);
        }
        else if (bf.startsWith("http")) {
            return bf.substring(bf.lastIndexOf('/') + 1);
        }
        else return bf;
    }

    public ImageInformation(String imageFile, Logger log) throws IOException, BWFLAException {
        DeprecatedProcessRunner process = new DeprecatedProcessRunner();
        process.setCommand("qemu-img");
        process.addArguments("info");
        process.addArguments("--output", "json");
        process.addArgument(imageFile);
        process.setLogger(log);

        final DeprecatedProcessRunner.Result result = process.executeWithResult()
                .orElse(null);

        if (result == null || !result.successful())
            throw new BWFLAException("qemu-img info '" + imageFile + "' failed!");

        imageInfo = QemuImage.fromJsonValueWithoutRoot(result.stdout(), QemuImage.class);
        process.cleanup();
    }

    public boolean hasBackingFile() {
        return imageInfo.getBackingFile() != null;
    }

    public String getBackingFile() {
        return imageInfo.getBackingFile();
    }

    public QemuImageFormat getFileFormat() {
        return QemuImageFormat.valueOf(imageInfo.getFormat().toUpperCase());
    }

    public enum QemuImageFormat{
        // OPTIMIZATION: formats should be declared in frequency-descending order
        //               (e.g. most common first, followed by less common ones)
        QCOW2("qcow2"),
        RAW("raw"),
        VDI("vdi"),
        VHD("vpc"),
        VMDK("vmdk"),
        EWF("ewf"),
        VHDX("vhdx");

        private final String format;

        private QemuImageFormat(String s) {
            this.format = s;
        }
        public String toString() {
            return this.format;
        }
    }
}
