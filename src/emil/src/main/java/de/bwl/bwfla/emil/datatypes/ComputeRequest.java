package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ComputeRequest extends JaxbType {

    @XmlElement
    private List<ComponentSpec> components;

    @XmlElement(defaultValue = "120")
    private long timeout = 120;

    public List<ComponentSpec> getComponents() {
        return components;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setComponents(List<ComponentSpec> components)
    {
        this.components = components;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    @XmlRootElement
    public static class ComponentSpec {
        @XmlElement(required = true)
        private String componentId;

        @XmlElement
        private String environmentId;

        @XmlElement
        private boolean shouldSaveEnvironment;

        @XmlElement
        private String saveEnvironmentLabel;

        public String getComponentId() {
            return componentId;
        }

        public boolean shouldSaveEnvironment() {
            return shouldSaveEnvironment;
        }

        public String getSaveEnvironmentLabel() {
            return saveEnvironmentLabel;
        }

        public String getEnvironmentId() {
            return environmentId;
        }

        public void setComponentId(String componentId)
        {
            this.componentId = componentId;
        }

        public void setEnvironmentId(String environmentId)
        {
            this.environmentId = environmentId;
        }

        public void setShouldSaveEnvironment(boolean shouldSaveEnvironment)
        {
            this.shouldSaveEnvironment = shouldSaveEnvironment;
        }

        public void setSaveEnvironmentLabel(String saveEnvironmentLabel)
        {
            this.saveEnvironmentLabel = saveEnvironmentLabel;
        }
    }
}
