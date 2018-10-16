package de.bwl.bwfla.common.interfaces;

import java.io.File;

public interface NBDFileProvider {
    public File resolveRequest(String reqStr);
}
