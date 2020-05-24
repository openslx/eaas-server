package de.bwl.bwfla.wikidata.reader.entities;


import java.util.ArrayList;

public class FileFormats {
    private ArrayList<String> readFormats;
    private ArrayList<String> writeFormats;

    public FileFormats(ArrayList<String> readFormats, ArrayList<String> writeFormats) {
        this.readFormats = readFormats;
        this.writeFormats = writeFormats;
    }

    public void setReadFormats(ArrayList<String> readFormats) {
        this.readFormats = readFormats;
    }

    public void setWriteFormats(ArrayList<String> writeFormats) {
        this.writeFormats = writeFormats;
    }

    public ArrayList<String> getReadFormats() {
        return readFormats;
    }

    public ArrayList<String> getWriteFormats() {
        return writeFormats;
    }
}
