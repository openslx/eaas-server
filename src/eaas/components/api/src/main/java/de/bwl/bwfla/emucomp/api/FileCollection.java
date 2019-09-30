package de.bwl.bwfla.emucomp.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fileCollection", namespace="http://bwfla.bwl.de/common/datatypes")
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public class FileCollection extends JaxbType {
	@XmlElement(name="file", namespace="http://bwfla.bwl.de/common/datatypes")
	public List<FileCollectionEntry> files = new ArrayList<FileCollectionEntry>();

	@XmlElement(namespace="http://bwfla.bwl.de/common/datatypes")
	public String id;

	@XmlElement
	private String label;

	public FileCollection()
	{
		id = null;
	}
	
	public FileCollection(String id)
	{
		this.id = id;
	}
	
    public static FileCollection fromValue(String data) throws JAXBException {
        return JaxbType.fromValue(data, FileCollection.class);
    }

    public FileCollectionEntry getDefaultEntry()
	{
		for(FileCollectionEntry fc : files)
			if(fc.isDefault())
				return fc;

		return files.get(0);
	}

    public FileCollection copy()
    {
    	try {
    		return fromValue(this.value());
    	}
    	catch(JAXBException e) { 
    		// impossible 
    		return null;
    	}
    }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
