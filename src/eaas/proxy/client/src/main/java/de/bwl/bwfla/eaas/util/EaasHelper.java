package de.bwl.bwfla.eaas.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import de.bwl.bwfla.api.eaas.EaasWS;
import de.bwl.bwfla.api.eaas.EaasWSService;
import de.bwl.bwfla.api.emucomp.Machine;
import de.bwl.bwfla.common.services.container.types.Container;

public class EaasHelper {
    /**
     * @deprecated inject an EaasClient instead, it has proxy instance caching.
     */
    @Deprecated
    public static EaasWS getEaas(String host) throws MalformedURLException {
        if (host == null)
            return null;

        EaasWSService service = new EaasWSService(new URL(host + "/eaas/EaasWS?wsdl"));
        EaasWS comp = service.getEaasWSPort();

        BindingProvider bp = (BindingProvider) comp;
        SOAPBinding binding = (SOAPBinding) bp.getBinding();
        bp.getRequestContext().put("javax.xml.ws.client.receiveTimeout", "0");
        bp.getRequestContext()
        .put("javax.xml.ws.client.connectionTimeout", "0");
        binding.setMTOMEnabled(true);
        bp.getRequestContext()
        .put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size",
                8192);
        return comp;
    }
    
//    public static int attachContainer(EaasWS _eaas, String _sessionId, String _dev, Container container)
//    {
//        try
//        {
//            DataSource ds = new FileDataSource(container.getFile());
//            DataHandler dh = new DataHandler(ds);
//            if(_sessionId == null)
//                return -1;
//            
//            return _eaas.attachMedium(_sessionId, dh, _dev.toUpperCase());
//        }
//        catch(Throwable t)
//        {
//            t.printStackTrace();
//            return -1;
//        }
//        finally
//        {
//            if(container != null)
//            {
//                File fl = container.getFile();
//                if(fl != null && fl.isFile())
//                    fl.delete();
//            }
//        }
//    }
//    
    public static int attachContainer(Machine _eaas, String _sessionId, String _dev, Container container)
    {
        try
        {
            DataSource ds = new FileDataSource(container.getFile());
            DataHandler dh = new DataHandler(ds);
            if(_sessionId == null)
                return -1;
            
            return _eaas.attachMedium(_sessionId, dh, _dev.toUpperCase());
        }
        catch(Throwable t)
        {
            Logger.getLogger(EaasHelper.class.getName()).log(Level.SEVERE, t.getMessage(), t);
            return -1;
        }
        finally
        {
            if(container != null)
            {
                File fl = container.getFile();
                if(fl != null && fl.isFile())
                    fl.delete();
            }
        }
    }
}
