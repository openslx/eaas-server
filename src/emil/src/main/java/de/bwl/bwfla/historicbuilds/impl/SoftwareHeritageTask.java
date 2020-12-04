package de.bwl.bwfla.historicbuilds.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.EaasFileUtils;
import de.bwl.bwfla.envproposer.api.ProposalResponse;
import de.bwl.bwfla.historicbuilds.api.HistoricResponse;
import de.bwl.bwfla.historicbuilds.api.SoftwareHeritageRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoftwareHeritageTask extends BlockingTask<Object> {

    private final String revisionId;
    private final String directoryId;
    private final Boolean shouldExtract;
    private final String scriptLocation;
    private final Path workingDir;

    private static final Logger LOG = Logger.getLogger("SWH-TASK");

    public SoftwareHeritageTask(SoftwareHeritageRequest request) throws BWFLAException {
        this.revisionId = request.getRevisionId();
        this.directoryId = request.getDirectoryId();
        this.shouldExtract = request.isExtract(); //TODO change getter for extract
        this.scriptLocation = request.getScriptLocation();
        this.workingDir = createWorkingDir();

    }

    //FIXME Use other class or make response class common
    @Override
    protected ProposalResponse execute() throws Exception {
        try {

            ArrayList<String> arguments = new ArrayList<String>();
            arguments.add("/usr/bin/python3");
            arguments.add(scriptLocation); //TODO check if this needs to be configurable or hardcoded location
            String idToBeUsed;

            if (revisionId == null && directoryId == null) {
                throw new BWFLAException("Can't download without id (no revisionId and not directoryId given!");
            } else if (revisionId != null) {
                arguments.add(revisionId);
                idToBeUsed = revisionId;
            } else {
                arguments.add(directoryId);
                arguments.add("--dir");
                idToBeUsed = directoryId;
            }

            if (shouldExtract) {
                arguments.add("--extract");
            }

            final Path outputFolder = workingDir.resolve("pythonOutput");
            Files.createDirectory(outputFolder);

            arguments.add("-o " + outputFolder.toString());

            String[] pythonCmds = arguments.toArray(String[]::new);

            LOG.info("Starting python script.");
            ProcessBuilder processBuilder = new ProcessBuilder(pythonCmds);
            Process process = processBuilder.start();

            process.waitFor();

            if (process.exitValue() == 0) {

                return new ProposalResponse()
                        .setMessage("Download was successful! File can be found at:" + Paths.get("").toAbsolutePath().normalize().toString() + "/" + idToBeUsed + ".tar.gz")
                        .setId("NO ID HERE, this class is just being used for testing purposes right now!");

            } else { //TODO give better information (access python script output?)
                throw new BWFLAException("Could not download from SWH, exitValue was not 0.");
            }

        } catch (Exception error) {
            log.log(Level.WARNING, "Downloading Software from SWH failed!", error);
            throw error;
        }
    }

    private static Path createWorkingDir() throws BWFLAException
    {
        try {
            return EaasFileUtils.createTempDirectory(Paths.get("/tmp-storage"), "historic-");
        }
        catch (Exception error) {
            throw new BWFLAException("Creating working directory failed!", error);
        }
    }
}
