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

package de.bwl.bwfla.emil.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import de.bwl.bwfla.emil.datatypes.ErrorInformation;

@Provider
public class ErrorResponseJsonFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext request,
            ContainerResponseContext response) throws IOException {
        /*
         * If we have an error response, always force application/json as
         * Content-type header, no matter what the handler says.
         */
        // TODO: it would be polite to parse the request's Accept header
        //       and serve as application/xml or text/plain if the client asked
        //       us to
        if (response.getEntity() instanceof ErrorInformation) {
            response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }
    }
}
