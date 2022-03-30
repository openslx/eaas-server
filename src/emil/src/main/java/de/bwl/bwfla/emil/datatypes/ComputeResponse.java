package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ComputeResponse extends JaxbType {
    @XmlElement(required = true)
    private String id;

    @XmlElement
    private List<ComputeResult> result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ComputeResult> getResult() {
        return result;
    }

    public void setResult(List<ComputeResult> result) {
        this.result = result;
    }

    @XmlRootElement
    public static class ComputeResult {
        @XmlElement
        private String componentId;

        @XmlElement
        private String state;

        @XmlElement
        private String environmentId;

        @XmlElement
        private String resultBlob;

        public String getComponentId() {
            return componentId;
        }

        public void setComponentId(String componentId) {
            this.componentId = componentId;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getResultBlob() {
            return resultBlob;
        }

        public void setResultBlob(String resultBlob) {
            this.resultBlob = resultBlob;
        }

        public String getEnvironmentId() {
            return environmentId;
        }

        public void setEnvironmentId(String environmentId) {
            this.environmentId = environmentId;
        }
    }
}
