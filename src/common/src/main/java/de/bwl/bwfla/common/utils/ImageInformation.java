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
                    if (tokens[1].contains(fmt.toString()))
                        fileFormat = fmt;
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
        try {
            if (!process.execute(false, false)) {
                throw new BWFLAException("qemu-img info " + imageFile.toString() + " failed");
            }

            String output = process.getStdOutString();

            parseOutput(output);

           //  throw new BWFLAException("qemu-img failed: " + process.getStdErrString() + " " + process.getStdOutString());
        } finally {
            process.cleanup();
        }
    }

    public String getBackingFile() {
        return backingFile;
    }

    public QemuImageFormat getFileFormat() {
        return fileFormat;
    }

    public enum QemuImageFormat{
        RAW("raw"),
        VDI("vdi"),
        VHD("vpc"),
        VMDK("vmdk"),
        EWF("ewf"),
        QCOW2("qcow2");

        private final String format;

        private QemuImageFormat(String s) {
            this.format = s;
        }
        public String toString() {
            return this.format;
        }
    }
}
