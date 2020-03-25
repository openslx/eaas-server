package de.bwl.bwfla.objectarchive.datatypes;


import de.bwl.bwfla.common.exceptions.BWFLAException;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Properties;

public class DigitalObjectFileMetadata implements Comparable<DigitalObjectFileMetadata>{

    String label = null;
    String id = null;
    String order = null;

    enum PropKeys
    {
        LABEL,
        ID,
        ORDER
    }

    public static DigitalObjectFileMetadata fromPropertiesFile(Path path) throws BWFLAException {
        File propFile = path.toFile();
        if(!propFile.exists())
            return null;

        Properties objectProperties = new Properties();
        InputStream input;
        try {
            input = new FileInputStream(propFile);
            objectProperties.load(input);
        } catch (IOException e) {
            throw new BWFLAException(e);
        }
        return new DigitalObjectFileMetadata(objectProperties);
    }

    public DigitalObjectFileMetadata(Properties props)
    {
        this.label = props.getProperty(PropKeys.LABEL.name());
        this.id = props.getProperty(PropKeys.ID.name());

        String orderVal = props.getProperty(PropKeys.ORDER.name());
        if(orderVal != null)
          this.order = orderVal;
    }

    public DigitalObjectFileMetadata(String id, String label, String order) {
        this.label = label;
        this.id = id;
        this.order = order;
    }

    public String getLabel() {
        return label;
    }

    public String getOrder() {
        return order;
    }

    public String toString(){
        return "id: " + id + " label " + label + " order " + order + "\n";
    }

    @Override
    public int compareTo(DigitalObjectFileMetadata o) {
        return order.compareTo(o.order);
    }

    public void writeProperties(Path dir) throws IOException {
        Properties props = new Properties();
        if(label != null)
            props.setProperty(PropKeys.LABEL.name(), label);
        if(id != null)
            props.setProperty(PropKeys.ID.name(), id);
        if(order != null)
            props.setProperty(PropKeys.ORDER.name(), order.toString());

        OutputStream out = new FileOutputStream(dir.toFile());
        props.store(out, "metadata");
    }
}
