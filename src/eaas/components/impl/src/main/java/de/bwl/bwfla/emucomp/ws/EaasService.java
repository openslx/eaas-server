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

package de.bwl.bwfla.emucomp.ws;

import javax.jws.WebService;

/**
 * This is a webservice endpoint implementation whose sole purpose is to
 * publish the custom WSDL file (see wsdlLocation parameter).
 * 
 * Application servers have a feature to rewrite the URLs in webservice
 * endpoints to reflect the actual hostname/address the server is available
 * at. Custom-made WSDL files are not subject to this WSDL rewriting.
 * 
 * As the custom WSDL file is required in order to import other webservices
 * which is again required because JAX-WS won't automatically merge multiple
 * ports on the same webservice, this webservice endpoint class is required
 * to serve the WSDL file and have the application server rewrite the endpoints.
 */
@WebService(serviceName = "ComponentService", portName = "DontUseDummyPort", targetNamespace = "http://bwfla.bwl.de/api/emucomp", wsdlLocation = "WEB-INF/wsdl/Combine.wsdl")
public class EaasService {

}
