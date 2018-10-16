package de.bwl.bwfla.emil.datatypes.snapshot;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.*;

@XmlType(name = "saveImport")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaveImportRequest extends SnapshotRequest {

    @XmlElement(required = true)
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
