package de.bwl.bwfla.emucomp.api;

import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractDataResource", namespace = "http://bwfla.bwl.de/common/datatypes")
@XmlSeeAlso({
		ObjectArchiveBinding.class,
		ImageArchiveBinding.class,
		BlobStoreBinding.class,
		Binding.class
})
@XmlRootElement(namespace = "http://bwfla.bwl.de/common/datatypes")
public abstract class AbstractDataResource extends JaxbType
{
	@XmlAttribute(name = "id")
	protected String id;

	/**
	 * Gets the value of the id property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link String }
	 *     
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link String }
	 *     
	 */
	public void setId(String value) {
		this.id = value;
	}
}
