package de.bwl.bwfla.emucomp.api;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.math.BigInteger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "file", propOrder = {"type", "isDefault", "order", "label", "handle"}, namespace="http://bwfla.bwl.de/common/datatypes")
public class FileCollectionEntry extends Binding implements Comparable<FileCollectionEntry>{
	
	@XmlElement(required = true, namespace="http://bwfla.bwl.de/common/datatypes")
	protected Drive.DriveType type;

	@XmlElement(required = false, namespace="http://bwfla.bwl.de/common/datatypes")
	protected BigInteger order;

	@XmlElement(required = false, namespace="http://bwfla.bwl.de/common/datatypes")
	protected String label;

	@XmlElement(required = false, namespace="http://bwfla.bwl.de/common/datatypes", defaultValue = "false")
	private boolean isDefault = false;

	/**
	 * DataHandler object for import / export of images.
	 */
	@XmlElement(required = false)
	private @XmlMimeType("application/octet-stream")DataHandler handle;

	public FileCollectionEntry()
	{
		this.url = null;
		this.type = null;
		this.id = null;
	}
	
	public FileCollectionEntry(String ref, Drive.DriveType type, String id) {
		this.url = ref;
		this.type = type;
		this.id = id;
	}

	public FileCollectionEntry(DataHandler handle, Drive.DriveType type, String id) {
		this.handle = handle;
		this.url = "handle://this is a bug";

		this.type = type;
		this.id = id;
	}

	public DataHandler getHandle() {
		return handle;
	}

	public Drive.DriveType getType() {
		return type;
	}

	public void setType(Drive.DriveType type) {
		this.type = type;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}

	@Override
	public int compareTo(FileCollectionEntry o) {
		return order.compareTo(o.order);
	}

	public BigInteger getOrder() {
		return order;
	}

	public void setOrder(BigInteger order) {
		this.order = order;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
