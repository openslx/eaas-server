package de.bwl.bwfla.imagearchive.ImageIndex;

import org.apache.tamaya.Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Alias
{
    @XmlElement
    private String name = "";
    @XmlElement
    private String version = "";
    @XmlElement
    private String alias = "";

    public Alias(Configuration values) {
        this.name = values.get("name");
        this.version = values.get("version");
        this.alias = values.get("alias");
        if (alias == null || name == null || version == null) {
            throw new IllegalStateException("Failed Alias:" + "\n" + name + "\n" + version + "\n" + alias);
        }
    }

    public Alias(String name, String version, String alias) {
        this.name = name;
        this.version = version;
        this.alias = alias;
    }

    public Alias() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
