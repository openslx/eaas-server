package de.bwl.bwfla.eaas.cluster.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CORSResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request,
                       ContainerResponseContext response) throws IOException {
        response.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Headers", "Cache-Control, x-admin-access-token");
    }

}