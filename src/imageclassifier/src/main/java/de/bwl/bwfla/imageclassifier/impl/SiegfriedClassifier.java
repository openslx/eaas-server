




package de.bwl.bwfla.imageclassifier.impl;

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

            process.execute(false, false);
            String res = process.getStdOutString();
            // log.warning("{ \"siegfried\" : " + res + "}");
            Siegfried sf = Siegfried.fromJsonValue("{ \"siegfried\" : " + res + "}", Siegfried.class);
            process.cleanup();
            return sf;

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
