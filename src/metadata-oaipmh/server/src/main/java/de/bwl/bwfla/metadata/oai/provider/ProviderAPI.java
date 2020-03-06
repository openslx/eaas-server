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

package de.bwl.bwfla.metadata.oai.provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import de.bwl.bwfla.common.services.security.SecuredAPI;
import org.dspace.xoai.dataprovider.DataProvider;
import org.dspace.xoai.dataprovider.builder.OAIRequestParametersBuilder;
import org.dspace.xoai.dataprovider.parameters.OAIRequest;
import org.dspace.xoai.model.oaipmh.OAIPMH;
import org.dspace.xoai.xml.XmlWriter;


/**
 * HTML-Interface implementation of OAI-PMH v2.0 according to
 * <a href="http://www.openarchives.org/OAI/2.0/openarchivesprotocol.htm">specification</a>.
 */

@ApplicationScoped
@Path("/providers")
public class ProviderAPI
{
	private static final Logger LOG = Logger.getLogger(ProviderAPI.class.getName());

	@Resource(lookup = "java:jboss/ee/concurrency/executor/io")
	private Executor executor = null;

	@Inject
	private ProviderRegistry providers = null;

	// ========== Admin API ==============================
	@GET
	@SecuredAPI
	@Produces(MediaType.APPLICATION_JSON)
	public Response listProviders()
	{
		final Collection<String> ids = providers.list();
		return Response.ok(ids, MediaType.APPLICATION_JSON_TYPE)
				.build();
	}


	// ========== OAI-PMH API ==============================

	@GET
	@SecuredAPI
	@Path("/{name}")
	public CompletionStage<Response> get(@PathParam("name") String name, @Context HttpServletRequest request)
	{
		return this.handle(name, request);
	}

	@POST
	@Path("/{name}")
	@SecuredAPI
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public CompletionStage<Response> post(@PathParam("name") String name, @Context HttpServletRequest request)
	{
		return this.handle(name, request);
	}


	// ========== Internal Helpers ==============================

	private CompletableFuture<Response> handle(String name, HttpServletRequest request)
	{
		final DataProvider provider = providers.lookup(name);
		if (provider == null)
			throw new NotFoundException("Provider not found: " + name);

		final OAIRequest oaireq = ProviderAPI.buildOaiRequest(request);

		final Supplier<Response> responder = () -> {
			try {
				// Compute OAI result
				final OAIPMH oaires = provider.handle(oaireq);

				// Streaming response writer
				final StreamingOutput streamer = (output) -> {
					try {
						final XmlWriter writer = new XmlWriter(output);
						oaires.write(writer);
						writer.flush();
						writer.close();
					}
					catch (Exception error) {
						throw new IOException("Serializing OAIPMH failed!", error);
					}

					output.flush();
				};

				return Response.ok(streamer, MediaType.TEXT_XML)
						.encoding(StandardCharsets.UTF_8.toString())
						.build();
			}
			catch (Exception error) {
				final String message = "Building response failed!";
				throw new InternalServerErrorException(message, error);
			}
		};

		return CompletableFuture.supplyAsync(responder, executor);
	}

	private static OAIRequest buildOaiRequest(HttpServletRequest request)
	{
		final OAIRequestParametersBuilder parameters = new OAIRequestParametersBuilder();
		request.getParameterMap().forEach(parameters::with);
		return parameters.build();
	}
}
