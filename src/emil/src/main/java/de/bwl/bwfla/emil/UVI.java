package de.bwl.bwfla.emil;

import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("/uvi")
@ApplicationScoped
public class UVI {

    @Inject
    private Components components;

    public Response createUVIComponent()
    {
        MachineConfiguration comp = null;

    }
}
