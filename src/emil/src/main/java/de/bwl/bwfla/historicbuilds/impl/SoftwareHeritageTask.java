package de.bwl.bwfla.historicbuilds.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.envproposer.api.ProposalResponse;
import de.bwl.bwfla.historicbuilds.api.HistoricResponse;
import de.bwl.bwfla.historicbuilds.api.SoftwareHeritageRequest;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;

public class SoftwareHeritageTask extends BlockingTask<Object> {

    private final String revisionId;
    private final String directoryId;
    private final Boolean shouldExtract;

    public SoftwareHeritageTask(SoftwareHeritageRequest request) {
        this.revisionId = request.getRevisionId();
        this.directoryId = request.getDirectoryId();
        this.shouldExtract = request.isExtract(); //TODO change getter for extract

    }

    //FIXME Use other class or make response class common
    @Override
    protected ProposalResponse execute() throws Exception {
        try {

            ArrayList<String> arguments = new ArrayList<String>();
            arguments.add("/home/ubuntu/swh-downloader/venv/bin/python3 "); //TODO make configurable?
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

            ProcessBuilder processBuilder = new ProcessBuilder(arguments.toArray(String[]::new));
            Process process = processBuilder.start();

            process.waitFor();
            if (process.exitValue() == 0) { //TODO create temp work dir for this

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
}
