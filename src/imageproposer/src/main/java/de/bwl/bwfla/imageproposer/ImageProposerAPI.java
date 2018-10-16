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

package de.bwl.bwfla.imageproposer;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import de.bwl.bwfla.common.datatypes.identification.DiskType;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.imageproposer.client.Proposal;
import de.bwl.bwfla.imageproposer.client.ProposalRequest;
import de.bwl.bwfla.imageproposer.client.ProposalResponse;
import de.bwl.bwfla.imageproposer.impl.ImageIndexHandle;
import de.bwl.bwfla.imageproposer.impl.ImageSorter;
import de.bwl.bwfla.imageproposer.impl.ProposalTask;
import de.bwl.bwfla.imageproposer.impl.TaskManager;
import de.bwl.bwfla.imageproposer.impl.UserData;
import de.bwl.bwfla.restutils.ResponseUtils;


@Path("api/v1")
public class ImageProposerAPI
{
	private static final Logger LOG = Logger.getLogger(ImageProposerAPI.class.getName());
	
	
	@Inject
	private TaskManager taskmgr;
	
	@Inject
	private ImageIndexHandle imageIndex;

	@Inject
	private ImageSorter sorter;

	@Context
	private UriInfo uri;
	
	
	/** Submit a new proposal task */
	@POST
    @Path("/proposals")
    @Consumes(ProposalRequest.MEDIATYPE_AS_JSON)
    @Produces(ProposalResponse.MEDIATYPE_AS_JSON)
	public Response postProposal(ProposalRequest request)
	{
		try {
			// Check request...
			if (request == null)
				return ResponseUtils.createMessageResponse(Status.BAD_REQUEST, "Request parameter missing!");

			// ... and parameters
			final HashMap<String, List<ProposalRequest.Entry>> fileFormats = request.getFileFormats();
			if (fileFormats == null) {
				String message = "Incorrect fileformats parameter specified (null/empty list).";
				return ResponseUtils.createMessageResponse(Status.BAD_REQUEST, message);
			}

			final HashMap<String, DiskType> mediaFormats = request.getMediaFormats();
			if (mediaFormats == null) {
				String message = "Incorrect mediaformats parameter specified (null/empty list).";
				return ResponseUtils.createMessageResponse(Status.BAD_REQUEST, message);
			}

			// Submit task
			final String taskid = taskmgr.submitTask(new ProposalTask(request, this.imageIndex, this.sorter));

			// Generate task's location URLs
			final String waitLocation = this.getLocationUrl("waitqueue", taskid);
			final String resultLocation = this.getLocationUrl("proposals", taskid);
			final TaskInfo<Object> info = taskmgr.getTaskInfo(taskid);
			info.setUserData(new UserData(waitLocation, resultLocation));

			// Info message
			ProposalResponse response = new ProposalResponse();
			response.setMessage("Proposal task was submitted.");

			return ResponseUtils.createLocationResponse(Status.ACCEPTED, waitLocation, response);
		}
		catch (RejectedExecutionException exception) {
			LOG.warning("Submitted task was cancelled due to high thread-executor load!");
			String message = "Server temporarily lacks resources for request execution. Please try again later.";
			return ResponseUtils.createMessageResponse(Status.SERVICE_UNAVAILABLE, message);
		}
		catch (Throwable throwable) {
			return ResponseUtils.createInternalErrorResponse(throwable);
		}
	}
	
	/** Poll a proposal task for completion */
	@GET
	@Path("/waitqueue/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response poll(@PathParam("id") String id)
	{
		try {
			final TaskInfo<Object> info = taskmgr.getTaskInfo(id);
			if (info == null) {
				String message = "Passed ID is invalid: " + id;
				return ResponseUtils.createMessageResponse(Status.NOT_FOUND, message);
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

			return ResponseUtils.createLocationResponse(status, location, null);
		}
		catch (Throwable throwable) {
			return ResponseUtils.createInternalErrorResponse(throwable);
		}
	}
	
	/** Get a proposal resource */
	@GET
    @Path("/proposals/{id}")
    @Produces(Proposal.MEDIATYPE_AS_JSON)
	public Response getProposal(@PathParam("id") String id)
	{
		try {
			if (id == null || id.isEmpty()) {
				String message = "ID was not specified or is invalid!";
				return ResponseUtils.createMessageResponse(Status.BAD_REQUEST, message);
			}

			final TaskInfo<Object> info = taskmgr.getTaskInfo(id);
			if (info == null || !info.result().isDone()) {
				String message = "Passed ID is invalid: " + id;
				return ResponseUtils.createMessageResponse(Status.NOT_FOUND, message);
			}

			try {
				// Result is available!
				final Future<Object> future = info.result();
				return ResponseUtils.createResponse(Status.OK, future.get());
			}
			finally {
				taskmgr.removeTaskInfo(id);
			}
		}
		catch (Throwable throwable) {
			return ResponseUtils.createInternalErrorResponse(throwable);
		}
	}


	@POST
	@Path("/refreshIndex")
	@Produces(MediaType.APPLICATION_JSON)
	public Response refresh()
	{
		imageIndex.refresh();
		return ResponseUtils.createMessageResponse(Status.OK, "refresh initialized ");
	}
	
	/* =============== Internal Helpers =============== */
	
	private String getLocationUrl(String subres, String id)
	{
		return ResponseUtils.getLocationUrl(this.getClass(), uri, subres, id);
	}
}
