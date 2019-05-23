package de.bwl.bwfla.emil;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.tamaya.inject.api.Config;
import org.jboss.resteasy.specimpl.ResponseBuilderImpl;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.api.emucomp.Component;
import de.bwl.bwfla.api.emucomp.ComponentService;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.utils.JsonBuilder;
import de.bwl.bwfla.eaas.client.EaasClient;
import de.bwl.bwfla.emil.classification.ArchiveAdapter;
import de.bwl.bwfla.emucomp.client.ComponentClient;

abstract class EmilRest {

	protected static final Logger LOG = Logger.getLogger("EMIL");
	
	@Inject
	protected EaasClient eaasClient;
	
	@Inject
	protected ComponentClient componentClient;
	
	@Inject
	@Config(value = "ws.objectarchive")
    protected String objectArchive;

	@Inject
	@Config(value = "ws.imagearchive")
	protected String imageArchive;

	@Inject
	@Config(value = "ws.apikey")
	protected String apiAuthenticationToken;

	@Inject
	@Config(value = "emil.exportpath")
	protected String exportPath;

	@Inject
	@Config(value = "ws.embedgw")
	protected String embedGw;
	
	@Inject
	@Config(value = "ws.softwarearchive")
	protected String softwareArchive;
	
	/** Security options response */
	protected static final Response WS_OPTIONS_RESPONSE = Response.ok().build();
	
	@Inject
	@Config(value = "ws.eaasgw")
    protected String eaasGw;
	
	/** Default buffer size for JSON responses (in chars). */
	protected static final int DEFAULT_RESPONSE_CAPACITY = 512;
	
	protected EaasWS getEaasWS()
    {
	    try {
            return eaasClient.getEaasWSPort(eaasGw);
        } catch (BWFLAException exception) {
            LOG.severe("Connecting to EaasWS failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
            return null;
        }
    }
	
	/**
	 * @deprecated use componentClient.getPort instead
	 */
	@Deprecated
    protected ComponentService getComponentService() {
        try {
            return new ComponentService(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"));
        } catch (Exception exception) {
            LOG.severe("Connecting to EaasWS failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
            return null;
        }
    }
    
    protected Component getComponentPort() {
        try {
            return componentClient.getPort(new URL(eaasGw + "/eaas/ComponentProxy?wsdl"), Component.class);
        } catch (Exception exception) {
            LOG.severe("Connecting to EaasWS failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
            return null;
        }
    }
	
	protected static Response internalErrorResponse(Throwable cause)
	{
		LOG.log(Level.SEVERE, cause.getMessage(), cause);
		
		return internalErrorResponse(cause.getMessage());
	}
	
	protected static Response internalErrorResponse(String message)
	{
		message = "Internal error: " + message;
		
		final String json = createJsonResponse("2", message);
		return createResponse(Status.INTERNAL_SERVER_ERROR, json);
	}

	protected static Response errorMessageResponse(String message)
	{
		final String json = createJsonResponse("1", message);
		return createResponse(Status.OK, json);
	}

	protected static Response successMessageResponse(String message)
	{
		final String json = createJsonResponse("0", message);
		return createResponse(Status.OK, json);
	}

	protected static Response createResponse(Status status, Object object)
	{
		ResponseBuilder builder = new ResponseBuilderImpl();
		builder.status(status);
		builder.entity(object);
		return builder.build();
	}
	
	protected static String createJsonResponse(String status, String message)
	{
		try {
			JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
			json.beginObject();
			json.add("status", status);
			json.add("message", message);
			json.endObject();
			json.finish();
			
			return json.toString();
		}
		catch (Exception exception) {
			LOG.warning("An error occured while composing a JSON message!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}
		
		return "{\"status\":\"" + status + "\"}";
	}
	
	protected static Response errorMessageResponse(Status status, String message)
	{
		return createMessageResponse(status, "1", message);
	}

	protected static Response successMessageResponse(Status status, String message)
	{
		return createMessageResponse(status, "0", message);
	}

	protected static Response createMessageResponse(Status respStatus, String jsonStatus, String message)
	{
		JsonBuilder json = new JsonBuilder();
		try {
			json.beginObject();
			json.add("status", jsonStatus);
			json.add("message", message);
			json.endObject();
		}
		catch (IOException exception) {
			LOG.warning("Constructing JSON response failed!");
			LOG.log(Level.SEVERE, exception.getMessage(), exception);
		}
		
		return createResponse(respStatus, json.toString());
	}
	
}
