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

package de.bwl.bwfla.eaas.cluster.provider.iaas.gce;

import com.google.api.client.http.HttpResponseException;
import com.google.api.services.compute.ComputeRequest;

import de.bwl.bwfla.common.exceptions.BWFLAException;


public class ComputeRequestException extends BWFLAException
{
	private static final long serialVersionUID = 2280948633909384512L;

	private final ComputeRequest<?> request;
	
	public ComputeRequestException(ComputeRequest<?> request)
	{
		super();
		this.request = request;
	}
	
	public ComputeRequestException(String message, ComputeRequest<?> request)
	{
		super(message);
		this.request = request;
	}
	
	public ComputeRequestException(Throwable throwable, ComputeRequest<?> request)
	{
		super(throwable);
		this.request = request;
	}
	
	public ComputeRequestException(String message, Throwable throwable, ComputeRequest<?> request)
	{
		super(message, throwable);
		this.request = request;
	}
	
	public HttpResponseException getComputeResponseException()
	{
		HttpResponseException exception = null;
	
		final Throwable cause = super.getCause();
		if (cause instanceof HttpResponseException)
			exception = (HttpResponseException) cause;
		
		return exception;
	}
	
	public ComputeRequest<?> getComputeRequest()
	{
		return request;
	}
}
