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

package de.bwl.bwfla.envproposer;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
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

import de.bwl.bwfla.common.services.security.SecuredAPI;
import de.bwl.bwfla.common.taskmanager.TaskInfo;
import de.bwl.bwfla.envproposer.api.ProposalRequest;
import de.bwl.bwfla.envproposer.api.ProposalResponse;
import de.bwl.bwfla.envproposer.impl.ProposalTask;
import de.bwl.bwfla.envproposer.impl.TaskManager;
import de.bwl.bwfla.envproposer.impl.UserData;
import de.bwl.bwfla.restutils.ResponseUtils;


@Path("api/v1")
public class EnvironmentProposerAPI
{
	private static final Logger LOG = Logger.getLogger("ENVIRONMENT-PROPOSER");
	
	@Inject
	private TaskManager taskmgr;

	@Context
	private UriInfo uri;
	
	
	/**
	 * Submit a new proposal task
	 *
	 * @returnWrapped de.bwl.bwfla.envproposer.api.ProposalResponse
	 *
	 * @ResponseHeader Location The location URL for polling task completion.
	 *
	 * @HTTP 202 If task was submitted successfully.
	 * @HTTP 503 If server is out of resources. Request should be retried later.
	 * @HTTP 500 If other internal errors occure.
	 */
	@POST
    @Path("/proposals")
	@SecuredAPI
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public Response postProposal(ProposalRequest request)
	{
		try {
			// Submit task
			final String taskid = taskmgr.submitTask(new ProposalTask(request));

			// Generate task's location URLs
			final String waitLocation = this.getLocationUrl("waitqueue", taskid);
			final String resultLocation = this.getLocationUrl("proposals", taskid);
			final TaskInfo<Object> info = taskmgr.getTaskInfo(taskid);
			info.setUserData(new UserData(waitLocation, resultLocation));

			// Info message
			final ProposalResponse response = new ProposalResponse()
					.setMessage("Proposal task was submitted.")
					.setId(taskid);

			return ResponseUtils.createLocationResponse(Status.ACCEPTED, waitLocation, response);
		}
		catch (RejectedExecutionException exception) {
			LOG.warning("Submitted task was cancelled due to high thread-executor load!");
			String message = "Server temporarily lacks resources for request execution. Please try again later.";
			return ResponseUtils.createMessageResponse(Status.SERVICE_UNAVAILABLE, message);
		}
		catch (Throwable throwable) {
			LOG.log(Level.WARNING, "Submitting proposal failed!", throwable);
			return ResponseUtils.createInternalErrorResponse(throwable);
		}
	}
	
	/**
	 * Poll a proposal task for completion
	 *
	 * @ResponseHeader Location URL for next polling request or task result.
	 *
	 * @HTTP 200 Task not completed, retry later.
	 * @HTTP 303 If task completed. Location header will contain the URL for fetching result.
	 */
	@GET
	@SecuredAPI
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
	
	/**
	 * Get and remove a proposal resource
	 *
	 * @returnWrapped de.bwl.bwfla.envproposer.api.Proposal
	 */
	@GET
	@SecuredAPI
    @Path("/proposals/{id}")
    @Produces(MediaType.APPLICATION_JSON)
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


	// ========== Internal Helpers ====================
	
	private String getLocationUrl(String subres, String id)
	{
		return ResponseUtils.getLocationUrl(this.getClass(), uri, subres, id);
	}
}
