package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class ContainerMetadata extends JaxbType {

    @XmlElement
    private boolean dhcp;

    @XmlElement
    private boolean telnet;

    @XmlElement
    private String process;

    @XmlElement
    private List<ContainerRootfsInput> inputs;

    @XmlElement
    private List<String> args;

    public boolean isDhcp() {
        return dhcp;
    }

    public void setDhcp(boolean dhcp) {
        this.dhcp = dhcp;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public boolean isTelnet() {
        return telnet;
    }

    public void setTelnet(boolean telnet) {
        this.telnet = telnet;
    }

    public List<ContainerRootfsInput> getInputs() {
        if(inputs == null)
            inputs = new ArrayList<>();
        return inputs;
    }

    @XmlRootElement
    public static class ContainerRootfsInput {
        @XmlElement
        private String method;

        @XmlElement
        private String archive;

        @XmlElement
        private String src;

        @XmlElement
        private String dst;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getArchive() {
            return archive;
        }

        public void setArchive(String archive) {
            this.archive = archive;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getDst() {
            return dst;
        }

        public void setDst(String dst) {
            this.dst = dst;
        }
    }
}
