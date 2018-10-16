package de.bwl.bwfla.emucomp.control.xpratunnel;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.AbstractEaasComponent;
import de.bwl.bwfla.emucomp.control.connectors.IConnector;
import de.bwl.bwfla.emucomp.control.connectors.XpraConnector;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = XpraTunnelServlet.SERVLET_NAME)
public class XpraTunnelServlet extends HttpServlet {


    private final Logger log = Logger.getLogger("XpraTunnelServlet");

    public static final String SERVLET_NAME = "XpraServlet";

    @Inject
    protected NodeManager nodeManager;

    private String getComponentId(HttpServletRequest request)
            throws ServletException {
        // Parse the request's path, that should contain the session's ID
        String path = request.getPathInfo();


        if (path == null || !path.contains("/" + XpraConnector.PROTOCOL)) {
            throw new NotFoundException(
                    "No tunnel ID specified in request.");
        }
        // remove leading /components/
        String componentId = path.substring("/components/".length());
        // remove /tunnel suffix
        componentId = componentId.substring(0, componentId.indexOf("/"));

        if (componentId.isEmpty()) {
            throw new NotFoundException(
                    "No tunnel ID specified in request.");
        }
        return componentId;
    }

    private int getPortNumber(HttpServletRequest request) throws ServletException, BWFLAException {
        String componentId = getComponentId(request);
        log.warning("componentId " + componentId);
        AbstractEaasComponent component = nodeManager.getComponentById(componentId, AbstractEaasComponent.class);
        IConnector connector = component.getControlConnector(XpraConnector.PROTOCOL);
        int port = ((XpraConnector) connector).getPort();
        log.warning("port  " + port);
        return port;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.warning("ServletConfig " + config);
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        forwardRequest("GET", req, resp);
    }

    /**
     * Method, which is useful only for debug
     * @param req
     * @return
     */
    private boolean printHeaders(HttpServletRequest req) {
        log.warning("method: " + req.getMethod());
        Enumeration<String> reqHeaders = req.getHeaderNames();
        while (reqHeaders.hasMoreElements()) {
            String headName = reqHeaders.nextElement();
            log.warning("header name: " + headName);
            log.warning("Header: " + req.getHeader(headName));
            if (req.getHeader(headName).equals("WebSocket")) {
                log.warning("got webscoket header");
                return true;
            }
        }
        return false;
    }


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        forwardRequest("POST", req, resp);
    }

    private void forwardRequest(String method, HttpServletRequest req, HttpServletResponse resp) {
        final boolean hasoutbody = (method.equals("POST"));
        try {

            int portNumber = getPortNumber(req);
            String URLFileName = req.getRequestURI().substring(req.getRequestURI().indexOf("xpra") + 4);
            if(req.getQueryString() != null)
                URLFileName+= "?" + req.getQueryString();

            /*
            Create URL to the localhost:*PORT*, proxy following communication
             */
            final URL url = new URL("http://localhost:" + portNumber + URLFileName // no trailing slash
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);

            final Enumeration<String> headers = req.getHeaderNames();
            while (headers.hasMoreElements()) {
                final String header = headers.nextElement();
                final Enumeration<String> values = req.getHeaders(header);
                if(header.equals("Accept-Encoding")) {
                    conn.addRequestProperty(header, "identity");
                }else
                while (values.hasMoreElements()) {
                    final String value = values.nextElement();

                    conn.addRequestProperty(header, value);
                }
            }


            conn.setUseCaches(false);
            final byte[] buffer = new byte[8];
            conn.setDoInput(true);
            conn.setDoOutput(hasoutbody);
            conn.connect();


            while (hasoutbody) {
                final int read = req.getInputStream().read(buffer);
                if (read <= 0) break;
                conn.getOutputStream().write(buffer, 0, read);
            }
            resp.setStatus(conn.getResponseCode());
            for (int i = 0; ; ++i) {
                final String header = conn.getHeaderFieldKey(i);
                if (header == null) break;
                final String value = conn.getHeaderField(i);
                resp.setHeader(header, value);
            }
            OutputStream os = resp.getOutputStream();

            /**
             * if we still want to change uri in Client.js, this should be uncommented
             */
//            if (URLFileName.equals("/js/Client.js")) {
//                StringWriter strWriter = new StringWriter();
//                IOUtils.copy(conn.getInputStream(), strWriter, "UTF-8");
//                String clientJs = strWriter.toString();
//                // changing the client.js with an appropriate URL, which will send websocket handshake
//                clientJs = new StringBuilder(clientJs).insert(clientJs.indexOf("uri += \":\" + this.port;") + 24, createStrToInsert(req)).toString();
//                os.write(clientJs.getBytes(Charset.forName("UTF-8")));
//
//            } else {
                while (true) {
                    final int read = conn.getInputStream().read(buffer);
                    if (read <= 0) break;
                    os.write(buffer, 0, read);
                }
//            }
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            // pass
        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         super.service(req, resp);
    }

    private String createStrToInsert(HttpServletRequest req) throws ServletException {
        return "\n uri +=\"/emucomp/components/\";\n" +
                "uri += \"" + getComponentId(req) + "\";\n" +
                "uri += \"/xpra/\";\n";
    }


}



