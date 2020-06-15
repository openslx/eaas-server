package de.bwl.bwfla.imagearchive.tasks;

import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.QcowOptions;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageMetadata;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageDescription;
import de.bwl.bwfla.imagearchive.ImageIndex.ImageNameIndex;

import java.nio.file.Path;
import java.util.UUID;

public class CreateImageTask extends AbstractTask<String> {

    private final Path target;
    private final String size;

    private ImageMetadata md = null;
    private ImageNameIndex index = null;

    public CreateImageTask(Path target, String size)
    {
        this.target = target;
        this.size = size;
    }

    public void setMetadata(ImageNameIndex index, ImageMetadata md)
    {
        this.md = md;
        this.index = index;
    }

    @Override
    protected String execute() throws Exception {
        String id = UUID.randomUUID().toString();
        QcowOptions qcowOptions = new QcowOptions();
        qcowOptions.setSize(size);
        Path dir = target.resolve(id);
        EmulatorUtils.createCowFile(dir, qcowOptions);

        if (index != null && md != null)
        {
            md.getImage().setId(id);
            index.addNameIndexesEntry(md, null);
        }
        return id;
    }
}
