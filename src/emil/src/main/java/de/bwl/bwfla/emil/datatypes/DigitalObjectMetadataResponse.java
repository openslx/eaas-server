package de.bwl.bwfla.emil.datatypes;

import de.bwl.bwfla.api.objectarchive.DigitalObjectMetadata;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.rest.EmilResponseType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DigitalObjectMetadataResponse extends EmilResponseType {

    @XmlElement
    private String description;
    @XmlElement
    private String summary;
    @XmlElement
    private String thumbnail;
    @XmlElement
    private String title;

    @XmlElement
    private String wikiDataId;

    @XmlElement
    private HashMap<String, String> customData;

    public DigitalObjectMetadataResponse(BWFLAException e)
    {
        super(e);
    }

    public DigitalObjectMetadataResponse() { }

    public DigitalObjectMetadataResponse(DigitalObjectMetadata md) throws BWFLAException {
        if(md == null)
                throw new BWFLAException("DigitalObjectMetadata is null");

        this.description = md.getDescription();
        this.summary = md.getSummary();
        this.thumbnail = md.getThumbnail();
        this.title = md.getTitle();
        this.wikiDataId = md.getWikiDataId();
        if(md.getCustomData() != null)
        {
            List<DigitalObjectMetadata.CustomData.Entry> entries = md.getCustomData().getEntry();
            this.customData = new HashMap<>();
            for(DigitalObjectMetadata.CustomData.Entry entry : entries)
            {
                customData.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HashMap<String, String> getCustomData() {
        return customData;
    }

    public void setCustomData(HashMap<String, String> customData) {
        this.customData = customData;
    }

}
