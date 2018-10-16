package de.bwl.bwfla.common.datatypes.identification;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DiskType extends JaxbType{

    @XmlElement
    private String type;

    @XmlElement
    private String path;

    @XmlElement
    private String size;

    @XmlElement
    private String localAlias;

    @XmlElement
    private Collection<? extends Content> content;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Collection<? extends Content> getContent() {
        return content;
    }

    public void setContent(Collection<? extends Content> content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean hasContentType(String wikiId)
    {
        if(wikiId == null)
            return false;

        for(Content c : getContent())
        {
            String _wikiId = c.getWikidata();
            if(_wikiId != null && wikiId.equals(_wikiId)) {
                return true;
            }
        }
        return false;
    }

    public String getLocalAlias() {
        return localAlias;
    }

    public void setLocalAlias(String localAlias) {
        this.localAlias = localAlias;
    }
}
