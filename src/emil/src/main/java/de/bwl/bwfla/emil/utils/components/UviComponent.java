/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.emil.utils.components;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emil.datatypes.rest.ComponentWithExternalFilesRequest;
import de.bwl.bwfla.emil.datatypes.rest.MachineComponentRequest;
import de.bwl.bwfla.emil.datatypes.rest.UviComponentRequest;

import de.bwl.bwfla.emil.utils.AutoRunScripts;
import de.bwl.bwfla.emucomp.api.Environment;
import de.bwl.bwfla.emucomp.api.FileSystemType;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.MediumType;
import de.bwl.bwfla.emucomp.api.PartitionTableType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


@ApplicationScoped
public class UviComponent
{
    @Inject
    private EmilEnvironmentRepository envrepo;

    @Inject
    private AutoRunScripts scripts = null;


    private byte[] createAutoRunScript(AutoRunScripts.Template template, String filename)
            throws BWFLAException
    {
        final Map<String, Object> context = new HashMap<>();
        context.put(AutoRunScripts.Variables.FILENAME, filename);

        try {
            return template.evaluate(context);
        }
        catch (IOException error) {
            throw new BWFLAException("Rendering autorun-script failed!", error);
        }
    }

    private String getOperatingSystemId(UviComponentRequest request) throws BWFLAException
    {
        final Environment env = envrepo.getImageArchive()
                .api()
                .v2()
                .environments()
                .fetch(request.getEnvironment());

        final String osid = ((MachineConfiguration) env).getOperatingSystemId();
        return (osid != null) ? osid : "UNKNOWN";
    }

    private void addAutoRunScript(ComponentWithExternalFilesRequest.InputMedium medium, UviComponentRequest request, Logger log)
            throws BWFLAException
    {
        final MediumType mtype = medium.getMediumType();
        final String osId = this.getOperatingSystemId(request);
        final AutoRunScripts.Template template = scripts.lookup(osId, mtype);
        if (template == null)
            throw new BWFLAException("No autorun-script template found for " + osId + "+" + mtype.name() + "!");

        log.info("Generating autorun-script for " + osId + "+" + mtype.name() + "...");
        final byte[] script = this.createAutoRunScript(template, request.getUviFilename());

        medium.getInlineFiles()
                .add(new ComponentWithExternalFilesRequest.FileData("copy", script, template.getFileName()));
    }

    private void addExternalFile(ComponentWithExternalFilesRequest.InputMedium medium, String url, String name)
    {
        medium.getExtFiles()
                .add(new ComponentWithExternalFilesRequest.FileURL("copy", url, name));
    }

    public MachineComponentRequest prepare(UviComponentRequest request, Logger log) throws BWFLAException
    {
        log.info("Preparing new medium for UVI's external file(s)...");

        final ComponentWithExternalFilesRequest.InputMedium medium = new ComponentWithExternalFilesRequest.InputMedium();
        medium.setMediumType((request.isUviWriteable()) ? MediumType.HDD : MediumType.CDROM);
        if (medium.getMediumType() == MediumType.HDD) {
            medium.setPartitionTableType(PartitionTableType.MBR);
            medium.setFileSystemType(FileSystemType.FAT32);
        }

        this.addAutoRunScript(medium, request, log);
        this.addExternalFile(medium, request.getUviUrl(), request.getUviFilename());
        for (UviComponentRequest.UviFile file : request.getAuxFiles())
            this.addExternalFile(medium, file.getUrl(), file.getFilename());

        log.info((1 + request.getAuxFiles().size()) + " UVI's external file(s) added");

        request.getInputMedia()
                .add(medium);

        return request;
    }
}
