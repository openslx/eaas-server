package de.bwl.bwfla.emucomp.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.bwl.bwfla.common.utils.jaxb.JaxbType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "dataResourceType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ObjectArchiveBinding.class, name = "ObjectArchiveBinding"),
		@JsonSubTypes.Type(value = ImageArchiveBinding.class, name = "ImageArchiveBinding"),
		@JsonSubTypes.Type(value = BlobStoreBinding.class, name = "BlobStoreBinding"),
		@JsonSubTypes.Type(value = Binding.class, name = "Binding")
})
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

	@XmlAttribute(name = "dataResourceType")
	protected String dataResourceType;

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
