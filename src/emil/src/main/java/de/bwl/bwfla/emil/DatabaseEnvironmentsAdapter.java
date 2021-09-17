package de.bwl.bwfla.emil;
import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import org.apache.tamaya.ConfigurationProvider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;


@ApplicationScoped
public class DatabaseEnvironmentsAdapter {

    protected static final Logger LOG = Logger.getLogger(" de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter");
    EnvironmentsAdapter environmentsAdapter;

    @PostConstruct
    public void init() {
        final var imageArchive = ConfigurationProvider.getConfiguration()
                .get("ws.imagearchive");

        environmentsAdapter = new EnvironmentsAdapter(imageArchive);
    }

    public String createPatchedImage(String id, ImageType imageType, String patchId) throws BWFLAException {
        return environmentsAdapter.createPatchedImage(id, imageType, patchId);
    }

    public String createPatchedImage(String archive, String id, ImageType imageType, String patchId) throws BWFLAException {
        return environmentsAdapter.createPatchedImage(archive, id, imageType, patchId);
    }

    @Deprecated
    public EnvironmentsAdapter.ImportImageHandle importImage(String archive, URL url, ImageArchiveMetadata iaMd, boolean b) throws BWFLAException {
        return environmentsAdapter.importImage(archive, url, iaMd, b);
    }

    public String getDefaultEnvironment(String osId) throws BWFLAException {
        return environmentsAdapter.getDefaultEnvironment(osId);
    }

    public void setDefaultEnvironment(String osId, String envId) throws BWFLAException {
        environmentsAdapter.setDefaultEnvironment(osId, envId);
    }

    public static final String EMULATOR_DEFAULT_ARCHIVE = "emulators";

    @Deprecated
    public ImageNameIndex getNameIndexes() throws BWFLAException {
        return environmentsAdapter.getNameIndexes(EMULATOR_DEFAULT_ARCHIVE);
    }

    public List<ImageGeneralizationPatchDescription> getImageGeneralizationPatches() throws BWFLAException {
        return environmentsAdapter.getImageGeneralizationPatches();
    }

    @Deprecated
    public void addNameIndexesEntry(String backend, ImageMetadata entry, Alias alias) throws BWFLAException {
        environmentsAdapter.addNameIndexesEntry(backend, entry, alias);
    }

    @Deprecated
    public void deleteNameIndexesEntry(String id, String version) throws BWFLAException {
        environmentsAdapter.deleteNameIndexesEntry(id, version);
    }

    @Deprecated
    public void deleteNameIndexesEntry(String backend, String id, String version) throws BWFLAException {
        environmentsAdapter.deleteNameIndexesEntry(backend, id, version);
    }

    @Deprecated
    public void updateLatestEmulator(String backend, String emulator, String version) throws BWFLAException {
        environmentsAdapter.updateLatestEmulator(backend, emulator, version);
    }

    @Deprecated
    public ImageNameIndex getImagesIndex() throws BWFLAException
    {
        return environmentsAdapter.getNameIndexes();
    }

    public EmulatorMetadata extractMetadata(String imageId) throws BWFLAException {
        return environmentsAdapter.extractMetadata(imageId);
    }

    public List<DefaultEntry> getDefaultEnvironments() throws BWFLAException {
        return environmentsAdapter.getDefaultEnvironments("default");
    }
}
