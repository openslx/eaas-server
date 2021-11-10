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

package de.bwl.bwfla.imagearchive.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import de.bwl.bwfla.api.imagearchive.ImageArchiveWS;
import de.bwl.bwfla.api.imagearchive.ImageArchiveWSService;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.services.security.MachineTokenProvider;
import de.bwl.bwfla.common.services.security.SOAPClientAuthenticationHandlerResolver;

abstract class ImageArchiveWSClient implements Serializable
{		
	private static final long serialVersionUID = 1L;
	protected final Logger	log	= Logger.getLogger(this.getClass().getName());
	
	protected ImageArchiveWS archive = null;
	protected String wsHost;

	private String defaultBackendName = null;
	private String defaultExportPrefix = null;
	
	public String toString()
	{
		return wsHost;
	}
	
	protected ImageArchiveWSClient(String wsHost)
	{
		this.wsHost = wsHost;
	}
	
	protected void connectArchive() throws BWFLAException
	{
		if(archive != null)
			return;

		for (int retries = 60; retries > 0; --retries) {
			archive = ImageArchiveWSClient.getImageArchiveCon(wsHost, retries == 1);
			if (archive != null)
				return;

			try {
				Thread.sleep(1000L);
			}
			catch (Exception error) {
				// Ignore it!
			}

			// retry again!
		}

		throw new BWFLAException("Connecting to legacy image-archive failed!");
	}

	public String getDefaultBackendName() throws BWFLAException
	{
		if (defaultBackendName != null)
			return defaultBackendName;

		this.connectArchive();

		this.defaultBackendName = archive.getDefaultBackendName();
		return defaultBackendName;
	}

	protected String getExportPrefix() throws BWFLAException
	{
		if (defaultExportPrefix != null)
			return defaultExportPrefix;

		this.connectArchive();

		this.defaultExportPrefix = archive.getExportPrefix(this.getDefaultBackendName());
		return defaultExportPrefix;
	}

	protected String getExportPrefix(String imageArchiveName) throws BWFLAException
	{
		this.connectArchive();
		return archive.getExportPrefix(imageArchiveName);
	}
	
	private static ImageArchiveWS getImageArchiveCon(String host, boolean verbose)
	{
		URL wsdl = null;
		ImageArchiveWS archive;
		if(host == null)
			return null;

		final var log = Logger.getLogger(ImageArchiveWSClient.class.getName());
		log.info("Connecting to legacy image-archive...");
		try { 
			wsdl = new URL(host + "/imagearchive/ImageArchiveWS?wsdl");
		}
		catch(MalformedURLException e)
		{
			// try adding http
			try {
				wsdl = new URL("http://" + host + "/imagearchive/ImageArchiveWS?wsdl");
			}
			catch(MalformedURLException e2)
			{
				log.info("Initializing WSDL from '" + host + "/imagearchive/ImageArchiveWS?wsdl' failed!");
			}
		}
		
		try 
		{
			ImageArchiveWSService service = new ImageArchiveWSService(wsdl);
			SOAPClientAuthenticationHandlerResolver resolver = MachineTokenProvider.getSoapAuthenticationResolver();
			if(resolver != null)
				service.setHandlerResolver(resolver);
			archive = service.getImageArchiveWSPort();
		} 
		catch (Throwable error)
		{
			if (verbose) {
				log.log(Level.SEVERE, "Connecting to legacy image-archive failed!\n", error);
			}
			else {
				Throwable cause = error;
				while (cause.getCause() != null)
					cause = cause.getCause();

				log.warning("Connecting to legacy image-archive failed! " + cause);
			}

			return null;
		}

		log.info("Connected to legacy image-archive at '" + wsdl.toString() + "'");

		BindingProvider bp = (BindingProvider)archive;
		SOAPBinding binding = (SOAPBinding) bp.getBinding();
		binding.setMTOMEnabled(true);
		bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
		bp.getRequestContext().put("javax.xml.ws.client.connectionTimeout", "0");
		bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);

		return archive;
	}
}
