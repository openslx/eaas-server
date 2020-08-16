




package de.bwl.bwfla.imageclassifier.impl;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.DeprecatedProcessRunner;
import de.bwl.bwfla.imageclassifier.datatypes.Classifier;
import de.bwl.bwfla.imageclassifier.datatypes.IdentificationOutputIndex;
import de.bwl.bwfla.imageclassifier.datatypes.Siegfried;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SiegfriedClassifier extends Classifier<Siegfried.File> {

    private Logger log = Logger.getLogger(SiegfriedClassifier.class.getName());
    private Siegfried runSiegfried(Path isopath)
    {
        log.info("running siegfried");
        try {
            DeprecatedProcessRunner process = new DeprecatedProcessRunner();
            process.setCommand("sf");
            process.addArgument("-json");
                process.addArgument(isopath.toAbsolutePath().toString());

            final DeprecatedProcessRunner.Result result = process.executeWithResult(false)
                    .orElse(null);

            if (result == null || !result.successful())
                throw new BWFLAException("Running siegfried failed!");

            final String res = result.stdout();
            // log.warning("{ \"siegfried\" : " + res + "}");
            return Siegfried.fromJsonValue("{ \"siegfried\" : " + res + "}", Siegfried.class);

        }
        catch(Exception exception) {
            log.log(Level.WARNING, exception.getMessage(), exception);
            return null;
        }
    }

    @Override
    public IdentificationOutputIndex<Siegfried.File> runIdentification(boolean verbose) {
        IdentificationOutputIndex<Siegfried.File> index = new SiegfriedOutputIndex();
        for(Path p : contentDirectories)
        {
            Siegfried sf = runSiegfried(p);
            if(sf == null || sf.getFiles() == null)
            {
                log.severe("running siegfried faild");
                continue;
            }
            index.add(sf.getFiles());
            index.addContentPath(p.toString());
        }
        return index;
    }
}
