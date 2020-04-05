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

package de.bwl.bwfla.common.services.rest;

import de.bwl.bwfla.common.exceptions.BWFLAException;

import javax.ws.rs.core.Response;


public abstract class ResponseUtils
{
	public static Response newInternalError(String message, Throwable exception)
	{
		return ResponseUtils.newErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, message, exception);
	}

	public static Response newErrorResponse(Response.Status status, String message, Throwable exception)
	{
		// Find actual cause...
		while (exception.getCause() != null)
			exception = exception.getCause();

		// Add details to message...
		if (exception.getMessage() != null)
			message += " " + exception.getMessage();

		final BWFLAException error = new BWFLAException(message);
		final String prefix = status.getReasonPhrase()
				.replace(' ', '-')
				.toUpperCase();

		return Response.status(status)
				.entity(new ErrorInformation(prefix + ": " + error.getMessage()))
				.build();
	}
}
