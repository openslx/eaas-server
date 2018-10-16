
package de.bwl.bwfla.wikidata.reader.entities;

import java.util.ArrayList;

public class SoftwareQIDs {
	private ArrayList<String> readQIDs;
	private ArrayList<String> writeQIDs;

	public SoftwareQIDs(ArrayList<String> readQIDs, ArrayList<String> writeQIDs) {
		this.readQIDs = readQIDs;
		this.writeQIDs = writeQIDs;
	}

	public ArrayList<String> getReadQIDs() {
		return this.readQIDs;
	}

	public ArrayList<String> getWriteQIDs() {
		return this.writeQIDs;
	}
}
