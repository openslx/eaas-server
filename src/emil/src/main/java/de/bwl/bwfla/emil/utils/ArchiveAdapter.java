package de.bwl.bwfla.emil.utils;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import de.bwl.bwfla.emil.datatypes.security.AuthenticatedUser;
import de.bwl.bwfla.emil.datatypes.security.UserContext;
import org.apache.tamaya.inject.api.Config;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.objectarchive.util.ObjectArchiveHelper;

@ApplicationScoped
public class ArchiveAdapter {
    protected final static Logger LOG = Logger.getLogger(ArchiveAdapter.class.getName());

    @Inject
    @Config(value = "ws.imagearchive")
    private String imageArchive;

    @Inject
    @AuthenticatedUser
    private UserContext authenticatedUser;


    static private EnvironmentsAdapter envHelper;

    @PostConstruct
    public void init()  {

        envHelper = new EnvironmentsAdapter(imageArchive);
    }

    public EnvironmentsAdapter environments()
    {
        return envHelper;
    }
}
