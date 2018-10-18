package de.bwl.bwfla.eaas.proxy;

import javax.jws.WebService;
import javax.servlet.annotation.WebServlet;

@WebServlet("/ComponentProxy")
@WebService(serviceName = "ComponentService", portName = "DontUseDummyPort", targetNamespace = "http://bwfla.bwl.de/api/emucomp", wsdlLocation = "WEB-INF/wsdl/Proxy.wsdl")
public class EaasProxy {

}
