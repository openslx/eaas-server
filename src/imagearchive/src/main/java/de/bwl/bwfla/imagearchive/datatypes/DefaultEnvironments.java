package de.bwl.bwfla.imagearchive.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DefaultEnvironments {

    @XmlElement
    private List<DefaultEntry> map;


    public List<DefaultEntry> getMap() {
        return map;
    }

    public void setMap(List<DefaultEntry> map) {
        this.map = map;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class DefaultEntry {
        @XmlElement
        private String key;

        @XmlElement
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
