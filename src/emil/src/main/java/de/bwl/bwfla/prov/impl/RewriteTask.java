package de.bwl.bwfla.prov.impl;

import de.bwl.bwfla.api.blobstore.BlobStore;
import de.bwl.bwfla.blobstore.api.BlobDescription;
import de.bwl.bwfla.blobstore.api.BlobHandle;
import de.bwl.bwfla.blobstore.client.BlobStoreClient;
import de.bwl.bwfla.common.taskmanager.BlockingTask;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.emil.datatypes.rest.ProcessResultUrl;
import de.bwl.bwfla.prov.api.RewriteRequest;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class RewriteTask extends BlockingTask<Object> {

    private final String repoCWLURL;


    private static final Logger LOG = Logger.getLogger("");

    public RewriteTask(RewriteRequest request) {
        repoCWLURL = request.getRewriteURL();
    }


    @Override
    protected ProcessResultUrl execute() throws Exception {

        LOG.severe("-------- Starting Rewriter subprocess -------");
        DeprecatedProcessRunner rewriteRunner = new DeprecatedProcessRunner("sudo");
        rewriteRunner.setWorkingDirectory(Path.of("/libexec/CWL-Rewriter"));
        rewriteRunner.addArgument("python3");
        rewriteRunner.addArgument("/libexec/CWL-Rewriter/rewriter.py");
        rewriteRunner.addArgument(repoCWLURL);
        rewriteRunner.addArgument("--upload");
        rewriteRunner.execute(true);
        rewriteRunner.cleanup();
        LOG.severe("Rewriter is done!");

        String path = "/libexec/CWL-Rewriter/rewritten.tgz";

        final Configuration config = ConfigurationProvider.getConfiguration();

        final BlobStore blobstore = BlobStoreClient.get()
                .getBlobStorePort(config.get("ws.blobstore"));
        final String blobStoreAddress = config.get("rest.blobstore");

        final BlobDescription blob = new BlobDescription()
                .setDescription("Rewritten CWL Workflow")
                .setNamespace("Rewriter")
                .setDataFromFile(Paths.get(path))
                .setType(".tgz")
                .setName("rewritten_workflow");

        BlobHandle handle = blobstore.put(blob);

        ProcessResultUrl returnResult = new ProcessResultUrl();
        returnResult.setUrl(handle.toRestUrl(blobStoreAddress));

        return returnResult;
    }
}
