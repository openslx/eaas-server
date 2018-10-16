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
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class NoContentFirefoxBugFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext request,
            ContainerResponseContext response) throws IOException {
        /* Send a Content-Type header that does not need processing
         * by the browser.
         * This is a workaround for https://bugzilla.mozilla.org/show_bug.cgi?id=884693 */
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            response.getHeaders().add("Content-Type", "application/octet-stream");
        }
    }
}
