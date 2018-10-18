package de.bwl.bwfla.common.datatypes.identification;

import javax.xml.bind.annotation.*;

// @XmlSeeAlso({ISO9660.class})
// @XmlDiscriminatorNode("type")
@XmlAccessorType(XmlAccessType.NONE)
public class Content {

    @XmlElement
    private String wikidata;

    @XmlElement
    private String type;

    public String getWikidata() {
        return wikidata;
    }

    public void setWikidata(String wikidata) {
        this.wikidata = wikidata;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

//    @XmlDiscriminatorValue("ISO9660")
//    public static class ISO9660 extends Content {
//
//        @XmlElement(name="properties")
//        private ISO9660Properties properties;
//
//        public ISO9660Properties getProperties() {
//            return properties;
//        }
//
//        public void setProperties(ISO9660Properties properties) {
//            this.properties = properties;
//        }
//
//        public static class ISO9660Properties {
//
//            private String volume_name;
//
//
//            public String getVolume_name() {
//                return volume_name;
//            }
//
//            public void setVolume_name(String volume_name) {
//                this.volume_name = volume_name;
//            }
//        }
//    }
}
