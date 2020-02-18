package de.bwl.bwfla.emil.session.rest;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;
import de.bwl.bwfla.emil.session.Session;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class RunningNetworkEnvironmentResponse  extends JaxbType {

    @XmlElement
    Session session;

    @XmlElement
    String networkEnvId;

    public RunningNetworkEnvironmentResponse(Session session, String networkEnvId) {
        this.session = session;
        this.networkEnvId = networkEnvId;
    }
}

