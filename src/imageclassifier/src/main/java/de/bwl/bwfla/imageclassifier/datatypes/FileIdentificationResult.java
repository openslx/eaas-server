package de.bwl.bwfla.imageclassifier.datatypes;

public class FileIdentificationResult<T> {

    private final String url;
    private final String fileName;
    private final IdentificationData<?> data;

    public FileIdentificationResult(String url, String name, IdentificationData<?> data)
    {
        this.url = url;
        this.fileName = name;
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public IdentificationData<?> getData() {
        return data;
    }
}
