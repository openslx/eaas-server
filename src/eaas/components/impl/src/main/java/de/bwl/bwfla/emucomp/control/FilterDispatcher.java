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

package de.bwl.bwfla.emucomp.control;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import de.bwl.bwfla.emucomp.control.connectors.GuacamoleConnector;
import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;

/*
 * This class dispatches URL requests to the correct servlet.
 * This sort of manual dispatching is necessary because we want the URL
 *    /components/{componentId}/state
 * to be served by the JAX-RS resource with the corresponding @Path annotation
 * but we want
 *    /components/{componentId}/tunnel
 * to be served by the guacamole servlet.
 * It would be possible to do achieve the same effect directly in a @Path for
 * the tunnel resource and forward the request to the guacamole servlet in
 * @GET and @POST, but using a filter like this
 *   - circumvents JAX-RS/resteasy logic which improves performance a little bit
 *   - makes the JAX-RS resource classes more concise
 *   - is overall more flexible and extensible
 */

@WebFilter("/components/*")
public class FilterDispatcher implements Filter
{
    private final Logger log = Logger.getLogger("FilterDispatcher");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // Empty
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Don't filter websocket requests. Even if their uri template overlaps
        // with another JAX-RS endpoint, they are easily distinguished by their
        // Upgrade: header and will end up at the right place
        final String upgradeHeader = httpRequest.getHeader("upgrade");
        if (upgradeHeader != null && upgradeHeader.equalsIgnoreCase("websocket")) {
            log.info("Upgrading connection to websocket...");
            chain.doFilter(request, response);
            return;
        }

        if (httpRequest.getPathInfo() != null) {
            final Path path = Paths.get(httpRequest.getPathInfo());
            if (path.getName(0).toString().equals("components")) {
                String servletName = null;
                switch (path.getName(2).toString())
                {
                    case GuacamoleConnector.PROTOCOL: {
                        // use the guacamole tunnel servlet for tunnel requests
                        servletName = BWFLAGuacamoleTunnelServlet.SERVLET_NAME;
                        break;
                    }

                    case XpraConnector.PROTOCOL: {
                        // use the custom signalling servlet for incoming requests
                        servletName = WebRtcSignallingServlet.SERVLET_NAME;
                        break;
                    }
                }

                if (servletName != null) {
                    final ServletContext context = httpRequest.getServletContext();
                    context.getNamedDispatcher(servletName)
                            .forward(httpRequest, response);

                    return;
                }
            }

            // all other resources are handled using the default servlet chain
            // especially the SOAP servlets are not mentioned explicitly
            // here because they have an explicit URL pattern, thus being
            // always preferred over any default servlet or the JAX-RS servlet
            // mapped at /*
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
        // Empty
    }
}
