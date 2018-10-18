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

package de.bwl.bwfla.restutils;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;


public class ResponseUtils
{
	public static String getLocationUrl(Class<?> clazz, UriInfo uri, String subres, String id)
	{
		UriBuilder builder = uri.getBaseUriBuilder();
		builder.path(clazz);
		builder.path(subres);
		builder.path(id);
		return builder.build().toString();
	}
	
	public static Response createInternalErrorResponse(Throwable cause)
	{
		return ResponseUtils.createInternalErrorResponse(cause, true);
	}
	
	public static Response createInternalErrorResponse(Throwable cause, boolean printStackTrace)
	{
		if (printStackTrace)
			cause.printStackTrace();

		String message = "Server has encountered an internal error!";
		InternalErrorMessage error = new InternalErrorMessage(message, cause);
		return ResponseUtils.createResponse(Status.INTERNAL_SERVER_ERROR, error);
	}

	public static Response createMessageResponse(Status status, String message)
	{
		return ResponseUtils.createResponse(status, new Message(message));
	}

	public static Response createLocationResponse(Status status, String location, Object object)
	{
		ResponseBuilder builder = Response.status(status);
		builder.entity(object);
		builder.header("Access-Control-Allow-Origin", "*");
		builder.header("Location", location);
		return builder.build();
	}

	public static Response createResponse(Status status, Object object)
	{
		ResponseBuilder builder = Response.status(status);
		builder.entity(object);
		builder.header("Access-Control-Allow-Origin", "*");
		return builder.build();
	}
	
	
	/* =============== Internal Helpers =============== */
	
	@XmlRootElement
	public static class Message
	{
		private String message;
		
		public Message()
		{
			this(null);
		}
		
		public Message(String message)
		{
			this.message = message;
		}
		
		public String getMessage()
		{
			return message;
		}
		
		public void setMessage(String message)
		{
			this.message = message;
		}
	}
	
	
	@XmlRootElement
	public static class InternalErrorMessage extends Message
	{
		private String cause;
		
		public InternalErrorMessage()
		{
			super(null);
			this.cause = null;
		}
		
		public InternalErrorMessage(String message, Throwable throwable)
		{
			this(message, throwable.toString());
		}
		
		public InternalErrorMessage(String message, String cause)
		{
			super(message);
			this.cause = cause;
		}
		
		public String getCause()
		{
			return cause;
		}
		
		public void setCause(String cause)
		{
			this.cause = cause;
		}
	}
}
