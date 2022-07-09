package de.bwl.bwfla.objectarchive.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.apache.tamaya.ConfigurationProvider;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


public class DigitalObjectUserFileArchive extends DigitalObjectFileArchive {

    @Inject
    @Config(value="objectarchive.userarchive")
    public String userArchiveBase;


    public DigitalObjectUserFileArchive(String name) throws BWFLAException {
        ConfigurationInjection.getConfigurationInjector().configure(this);
        File userPath = new File(userArchiveBase, name);
        if(!userPath.exists())
        {
            if(!userPath.mkdirs())
                throw new BWFLAException("can not create user object dir:" + userPath.getAbsolutePath());
        }
        init(name, userPath.getAbsolutePath(), false);
    }

    public static Collection<String> listArchiveNames() throws IOException
    {
        final var basedir = ConfigurationProvider.getConfiguration()
                .get("objectarchive.userarchive");

        final var path = Path.of(basedir);
        if (!Files.isDirectory(path))
            return Collections.emptyList();

        return Files.list(path)
                .filter(Files::isDirectory)
                .map((p) -> p.getFileName().toString())
                .collect(Collectors.toList());
    }
}
