package de.bwl.bwfla.common.utils;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public class InputStreamDataSource implements DataSource {
    private InputStream inputStream;

    public InputStreamDataSource(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public String getName() {
        return "InputStreamDataSource";
    }
}
