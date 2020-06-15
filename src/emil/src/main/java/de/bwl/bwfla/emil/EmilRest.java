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

package de.bwl.bwfla.emil;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.eaas.client.EaasClient;
import de.bwl.bwfla.emucomp.client.ComponentClient;


abstract class EmilRest
{
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
	@Config(value = "emil.exportpath")
	protected String exportPath;

	@Inject
	@Config(value = "ws.embedgw")
	protected String embedGw;

	@Inject
	@Config(value = "ws.softwarearchive")
	protected String softwareArchive;

	@Inject
	@Config(value = "ws.eaasgw")
	protected String eaasGw;

	/** Default buffer size for JSON responses (in chars). */
	protected static final int DEFAULT_RESPONSE_CAPACITY = 512;


	protected static Response internalErrorResponse(Throwable cause)
	{
		return EmilRest.internalErrorResponse(cause.getMessage());
	}

	protected static Response internalErrorResponse(String message)
	{
		message = "Internal error: " + message;

		final JsonObject json = EmilRest.newJsonObject("2", message);
		return EmilRest.createResponse(Status.INTERNAL_SERVER_ERROR, json);
	}

	protected static Response errorMessageResponse(String message)
	{
		final JsonObject json = EmilRest.newJsonObject("1", message);
		return EmilRest.createResponse(Status.OK, json);
	}

	protected static Response successMessageResponse(String message)
	{
		final JsonObject json = EmilRest.newJsonObject("0", message);
		return EmilRest.createResponse(Status.OK, json);
	}

	protected static Response createResponse(Status status, Object object)
	{
		return Response.status(status)
				.entity(object)
				.build();
	}

	protected static JsonObject newJsonObject(String status, String message)
	{
		return Json.createObjectBuilder()
				.add("status", status)
				.add("message", message)
				.build();
	}
}
