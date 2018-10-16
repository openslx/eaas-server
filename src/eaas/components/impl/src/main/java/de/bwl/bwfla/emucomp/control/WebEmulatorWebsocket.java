package de.bwl.bwfla.emucomp.control;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import de.bwl.bwfla.emucomp.NodeManager;
import de.bwl.bwfla.emucomp.components.emulators.WebEmulatorBean;

@ServerEndpoint("/components/{componentId}/webemulator")
public class WebEmulatorWebsocket {
    @Inject
    protected NodeManager nodeManager;

    @OnOpen
    public void open(@PathParam("componentId") String componentId, Session session) {
        try {
            nodeManager.getComponentById(componentId, WebEmulatorBean.class).socket.attach(session);
        } catch (Exception e) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "componentId not found"));
            } catch (Exception e2) {
            }
        }
    }
}
