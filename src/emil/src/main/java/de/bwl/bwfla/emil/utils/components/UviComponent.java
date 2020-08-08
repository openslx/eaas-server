package de.bwl.bwfla.emil.utils.components;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.datatypes.rest.ComponentWithExternalFilesRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.UviComponentRequest;

import de.bwl.bwfla.emil.utils.AutoRunScripts;
import de.bwl.bwfla.emucomp.api.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


@ApplicationScoped
public class UviComponent
{
    @Inject
    private DatabaseEnvironmentsAdapter envHelper;

    @Inject
    private AutoRunScripts scripts = null;


    private String createAutoRunScript(AutoRunScripts.Template template, String filename)
            throws BWFLAException
    {
        final Map<String, Object> context = new HashMap<>();
        context.put(AutoRunScripts.Variables.FILENAME, filename);

        final Writer writer = new StringWriter();
        try {
            template.evaluate(writer, context);

        }
        catch (IOException error) {
            throw new BWFLAException("Rendering autorun-script failed!", error);
        }

        return writer.toString();
    }

    public MachineComponentRequest createUVIComponent(UviComponentRequest request) throws BWFLAException {
        Environment chosenEnv = envHelper.getEnvironmentById(request.getArchive(), request.getEnvironment());
        MachineConfiguration config = (MachineConfiguration)chosenEnv;

        String osId = config.getOperatingSystemId();
        if(osId == null)
            osId = "UNKNOWN";

        final MediumType mtype = (request.isUviWriteable()) ? MediumType.HDD : MediumType.CDROM;

        final AutoRunScripts.Template template = scripts.lookup(osId, mtype);
        if (template == null)
            throw new BWFLAException("No autorun-script template found for " + osId + "+" + mtype.name() + "!");

        final String script = this.createAutoRunScript(template, request.getUviFilename());

        ComponentWithExternalFilesRequest.InputMedium medium = new ComponentWithExternalFilesRequest.InputMedium();
        medium.setMediumType(mtype);
        if (mtype == MediumType.HDD) {
            medium.setPartitionTableType(PartitionTableType.MBR);
            medium.setFileSystemType(FileSystemType.FAT32);
        }

        medium.getInlineFiles()
                .add(new ComponentWithExternalFilesRequest.FileData("copy", script.getBytes(), template.getFileName()));

        ComponentWithExternalFilesRequest.FileURL inputFile =
                new ComponentWithExternalFilesRequest.FileURL("copy", request.getUviUrl(), request.getUviFilename());

        medium.getExtFiles()
                .add(inputFile);

        for(UviComponentRequest.UviFile auxFile : request.getAuxFiles())
        {
            ComponentWithExternalFilesRequest.FileURL _inputFile =
                    new ComponentWithExternalFilesRequest.FileURL("copy",
                            auxFile.getUrl(), auxFile.getFilename());

            medium.getExtFiles()
                    .add(_inputFile);
        }

        request.getInputMedia()
                .add(medium);

        return request;
    }
}
