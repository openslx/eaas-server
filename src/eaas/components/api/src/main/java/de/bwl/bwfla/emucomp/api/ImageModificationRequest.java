package de.bwl.bwfla.emucomp.api;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ImageModificationRequest {

    @XmlElement(required = true)
    private ImageModificationAction action;

    @XmlElement
    private ImageModificationCondition condition;

    @XmlElement(required = true)
    private String dataUrl;

    @XmlElement(required = true)
    private String destination;

    public ImageModificationAction getAction() {
        return action;
    }

    public void setAction(ImageModificationAction action) {
        this.action = action;
    }

    public ImageModificationCondition getCondition() {
        return condition;
    }

    public void setCondition(ImageModificationCondition condition) {
        this.condition = condition;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @XmlEnum
    public enum ImageModificationAction {
        @XmlEnumValue("extract_tar")
        EXTRACT_TAR("extract_tar"),
        @XmlEnumValue("copy")
        COPY("copy");

        private final String value;

        ImageModificationAction (String str)
        {
            value = str;
        }

        public String value() {
            return name();
        }

        public static ImageModificationAction fromValue(String v) {
            return valueOf(v);
        }
    }
}


