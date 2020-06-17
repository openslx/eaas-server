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

package de.bwl.bwfla.imageclassifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.imageclassifier.client.Identification;
import de.bwl.bwfla.imageclassifier.client.IdentificationRequest;
import de.bwl.bwfla.imageclassifier.client.IdentificationResponse;
import de.bwl.bwfla.imageclassifier.impl.BaseTask;
import de.bwl.bwfla.imageclassifier.impl.ClassificationTask;
import de.bwl.bwfla.imageclassifier.impl.HistogramTask;
import de.bwl.bwfla.imageclassifier.impl.TaskManager;
import de.bwl.bwfla.imageclassifier.datatypes.UserData;


@Path("api/v1")
public class ImageClassifierAPI
{
	private static final Logger LOG = Logger.getLogger(ImageClassifierAPI.class.getName());

	@Context
	private UriInfo uri;

	@Inject
	private TaskManager taskmgr;


	/** Submit a new histogram task */
	@POST
	@Path("/histograms")
	@Consumes(IdentificationRequest.MEDIATYPE_AS_JSON)
	@Produces(IdentificationResponse.MEDIATYPE_AS_JSON)
	public Response postHistogramRequest(IdentificationRequest request)
	{
		return this.submitRequest(request, true);
	}
	
	/** Submit a new classification task */
	@POST
	@Path("/classifications")
	@Consumes(IdentificationRequest.MEDIATYPE_AS_JSON)
	@Produces(IdentificationResponse.MEDIATYPE_AS_JSON)
	public Response postClassificationRequest(IdentificationRequest request)
	{
		return this.submitRequest(request, false);
	}

	/** Poll an identification task for completion */
	@GET
	@Path("/waitqueue/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response poll(@PathParam("id") String id)
	{
		try {
			final TaskInfo<Object> info = taskmgr.getTaskInfo(id);
			if (info == null) {
				String message = "Passed ID is invalid: " + id;
				return ImageClassifierAPI.errorMessageResponse(Status.NOT_FOUND, message);
			}

			Status status = null;
			String location = null;

			final UserData userdata = info.userdata(UserData.class);
			if (info.result().isDone()) {
				// Result is available!
				status = Status.SEE_OTHER;
				location = userdata.getResultLocation();
			}
			else {
				// Result is not yet available!
				status = Status.OK;
				location = userdata.getWaitLocation();
			}

			return ImageClassifierAPI.createLocationResponse(status, location, null);
		}
		catch(Throwable t) {
			return ImageClassifierAPI.internalErrorResponse(t);
		}
	}

	/** Get a histogram resource */
	@GET
	@Path("/histograms/{id}")
	@Produces(Identification.MEDIATYPE_HISTOGRAM_AS_JSON)
	public Response getHistogram(@PathParam("id") String id)
	{
		return this.getResult(id);
	}
	
	/** Get a classification resource */
	@GET
	@Path("/classifications/{id}")
	@Produces(Identification.MEDIATYPE_CLASSIFICATION_AS_JSON)
	public Response getClassification(@PathParam("id") String id)
	{
		return this.getResult(id);
	}


	/* =============== Internal Helpers =============== */

	private Response submitRequest(IdentificationRequest request, boolean asHistogram)
	{
		try {
			// Check request...
			if (request == null)
				return ImageClassifierAPI.errorMessageResponse(Status.BAD_REQUEST, "Request parameter missing!");

			// Submit task
			final ExecutorService executor = taskmgr.executor();
			final BaseTask task = (asHistogram) ? new HistogramTask(request, executor) : new ClassificationTask(request, executor);
			final String taskid = taskmgr.submitTask(task);

			// Generate task's location URLs
			final String waitLocation = this.getLocationUrl("waitqueue", taskid);
			final String resultLocation = (asHistogram) ? this.getLocationUrl("histograms", taskid) : this.getLocationUrl("classifications", taskid);
			final TaskInfo<Object> info = taskmgr.getTaskInfo(taskid);
			info.setUserData(new UserData(waitLocation, resultLocation));
			
			// Info message
			IdentificationResponse response = new IdentificationResponse();
			response.setMessage("Identification task was submitted.");

			return ImageClassifierAPI.createLocationResponse(Status.ACCEPTED, waitLocation, response);
		}
		catch(RejectedExecutionException e) {
			LOG.warning("Submitted task was cancelled due to high thread-executor load!");
			String message = "Server temporarily lacks resources for request execution. Please try again later.";
			return ImageClassifierAPI.errorMessageResponse(Status.SERVICE_UNAVAILABLE, message);
		}
		catch (Throwable e) {
			return ImageClassifierAPI.internalErrorResponse(e);
		}
	}
	
	private Response getResult(String id)
	{
		try {
			if (id == null || id.isEmpty()) {
				String message = "ID was not specified or is invalid!";
				return ImageClassifierAPI.errorMessageResponse(Status.BAD_REQUEST, message);
			}

			final TaskInfo<Object> info = taskmgr.getTaskInfo(id);
			if (info == null || !info.result().isDone()) {
				String message = "Passed ID is invalid: " + id;
				return ImageClassifierAPI.errorMessageResponse(Status.NOT_FOUND, message);
			}

			try {
				// Result is available!
				final Future<Object> future = info.result();
				return ImageClassifierAPI.createResponse(Status.OK, future.get());
			}
			finally {
				taskmgr.removeTaskInfo(id);
			}
		}
		catch (Throwable throwable) {
			return ImageClassifierAPI.internalErrorResponse(throwable);
		}
	}
	
	private String getLocationUrl(String subres, String id)
	{
		UriBuilder builder = uri.getBaseUriBuilder();
		builder.path(this.getClass());
		builder.path(subres);
		builder.path(id);
		return builder.build().toString();
	}

	private static Response internalErrorResponse(Throwable cause)
	{
		cause.printStackTrace();

		final JsonObject json = Json.createObjectBuilder()
				.add("message", "Server has encountered an internal error!")
				.add("cause", cause.toString())
				.build();

		return ImageClassifierAPI.createResponse(Status.INTERNAL_SERVER_ERROR, json.toString());
	}

	private static Response errorMessageResponse(Status status, String message)
	{
		return ImageClassifierAPI.createMessageResponse(status, message);
	}

	private static Response createMessageResponse(Status status, String message)
	{
		final JsonObject json = Json.createObjectBuilder()
				.add("message", message)
				.build();

		return ImageClassifierAPI.createResponse(status, json.toString());
	}

	private static Response createLocationResponse(Status status, String location, Object object)
	{
		ResponseBuilder builder = Response.status(status);
		builder.entity(object);
		builder.header("Access-Control-Allow-Origin", "*");
		builder.header("Location", location);
		return builder.build();
	}

	private static Response createResponse(Status status, Object object)
	{
		ResponseBuilder builder = Response.status(status);
		builder.entity(object);
		builder.header("Access-Control-Allow-Origin", "*");
		return builder.build();
	}
}
