package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.XmlSeeAlso;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlSeeAlso({VdeSlirpConfiguration.class,
             Environment.class,
             NetworkSwitchConfiguration.class,
             VdeSocksConfiguration.class,
             NodeTcpConfiguration.class})
public abstract class ComponentConfiguration extends JaxbType {

}
