package de.bwl.bwfla.emil.datatypes;


import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emil.datatypes.rest.EmilResponseType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MediaDescriptionResponse extends EmilResponseType {

    @XmlElement(required = true)
    private ArrayList<MediaDescriptionTypeList> medium = new ArrayList<>();


    public MediaDescriptionResponse ()
    {

    }

    public MediaDescriptionResponse(BWFLAException e)
    {
        super(e);
    }

    public ArrayList<MediaDescriptionTypeList> getMedium() {
        return medium;
    }

    public void setMedium(ArrayList<MediaDescriptionTypeList> medium) {
        this.medium = medium;
    }
}
