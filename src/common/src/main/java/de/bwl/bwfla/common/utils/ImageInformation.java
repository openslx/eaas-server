package de.bwl.bwfla.common.utils;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Created by klaus on 29.08.17.
 */
public class ImageInformation {

    private String backingFile = null;
    private QemuImageFormat fileFormat = null;

    private void parseOutput(String output) throws BWFLAException
    {
        String[] lines = output.trim().split("\n");
        for(String line : lines)
        {
            String[] tokens = line.split(": ");
            if(tokens.length < 2)
                continue;
            // System.out.println("value: " + tokens[0] + " " + tokens[1]);

            if(tokens[0].equals("backing file"))
                backingFile = tokens[1].trim();
            else if(tokens[0].equals("file format"))
            {
                for (QemuImageFormat fmt : QemuImageFormat.values()) {
                    if (tokens[1].startsWith(fmt.toString())) {
                        fileFormat = fmt;
                        break;
                    }
                }
            }
        }
    }

    static public String getBackingImageId(String bf) throws BWFLAException {
        if (bf.contains("exportname")) {
            return bf.substring(bf.lastIndexOf('=') + 1);
        }
        else if (bf.startsWith("http")) {
            return bf.substring(bf.lastIndexOf('/') + 1);
        }
        throw new BWFLAException("cannot determine image id. unsupported schema: " + bf);
    }

    public ImageInformation(String imageFile, Logger log) throws IOException, BWFLAException {
        DeprecatedProcessRunner process = new DeprecatedProcessRunner();
        process.setCommand("qemu-img");
        process.addArguments("info");
        process.addArgument(imageFile);
        process.setLogger(log);

        final DeprecatedProcessRunner.Result result = process.executeWithResult()
                .orElse(null);

        if (result == null || !result.successful())
            throw new BWFLAException("qemu-img info " + imageFile.toString() + " failed");

        this.parseOutput(result.stdout());
    }

    public boolean hasBackingFile() {
        return backingFile != null;
    }

    public String getBackingFile() {
        return backingFile;
    }

    public QemuImageFormat getFileFormat() {
        return fileFormat;
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
