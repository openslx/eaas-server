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

package de.bwl.bwfla.imagebuilder;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.logging.PrefixLogger;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.imagebuilder.api.ImageDescription;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;


public class MediumBuilderCD extends MediumBuilder
{
    @Override
    public ImageHandle execute(Path workdir, ImageDescription description) throws BWFLAException {

        final String outname = "image";
        final String outtype = ".iso";

        final Path outputISOFile = workdir.resolve(outname + outtype);
        final Path filesToCopy = workdir.resolve("files");

        final PrefixLogger log = new PrefixLogger(super.log.getName());
        log.getContext().add(workdir.getFileName().toString());
        log.info("Building ISO image...");
        try {
            Files.createDirectory(filesToCopy);
            MediumBuilderCD.copy(description.getContentEntries(), filesToCopy, workdir, log);
            this.createCDIso(filesToCopy, outputISOFile, log);
        }
        catch (Exception error) {
            if (error instanceof BWFLAException)
                throw (BWFLAException) error;

            throw new BWFLAException(error);
        }

        // The final image!
        return new ImageHandle(outputISOFile, outname, outtype);
    }


    /* ==================== Internal Helpers ==================== */

    private void createCDIso(Path input, Path output, Logger log) throws BWFLAException
    {
        final DeprecatedProcessRunner process = new DeprecatedProcessRunner();

        process.setLogger(log);
        process.setCommand("mkisofs");
        process.addArgument("-J"); // Generate Joliet directory records (Useful in Windows)
        process.addArgument("-l"); // Allow longer names
        process.addArgument("-R"); // Rock Ridge protocol to preserve ownership and permission records, enable support for symbolic links and device files
        process.addArguments("-o", output.getFileName().toString());
        process.addArgument(input.toString());
        process.setWorkingDirectory(output.getParent());

        if (!process.execute())
            throw new BWFLAException("Creating iso failed!");
    }
}
