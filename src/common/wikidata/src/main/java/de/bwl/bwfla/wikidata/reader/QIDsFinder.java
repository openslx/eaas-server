package de.bwl.bwfla.wikidata.reader;

import com.hp.hpl.jena.rdf.model.RDFNode;
import de.bwl.bwfla.wikidata.reader.config.Config;
import de.bwl.bwfla.wikidata.reader.entities.FileFormats;
import de.bwl.bwfla.wikidata.reader.entities.RelatedQIDS;
import de.bwl.bwfla.wikidata.reader.entities.SoftwareQIDs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class QIDsFinder {
	private static final Logger log = Logger.getLogger(QIDsFinder.class.getName());

	public static ArrayList<ArrayList<String>> findQID(String format) {
		ArrayList dataList = new ArrayList();

		try {
			dataList = WikiRead.readWithPronomId(format, Config.getLANGUAGE());
		} catch (Exception var3) {
			log.log(Level.SEVERE, var3.getMessage(), var3);
		}

		return dataList;
	}

	public static List<String> extendSupportedFormats(String qID, List<String> supportedFormats) {
		FileFormats fileFormats = WikiRead.getFileFormats(qID);
		supportedFormats.addAll(fileFormats.getReadFormats());
		supportedFormats.addAll(fileFormats.getWriteFormats());
		return supportedFormats.stream().distinct().collect(Collectors.toList());
	}

	public static SoftwareQIDs findQIDs(String pronomId) {
		ArrayList<ArrayList<String>> dataList = findQID(pronomId);
		//TODO rework student code and change the way we return QIDs
		ArrayList<String> readSoftware = (ArrayList)dataList.get(0);
		ArrayList<String> writeSoftware = (ArrayList)dataList.get(1);
		return new SoftwareQIDs(readSoftware, writeSoftware);
	}

	private static void showQIDs(ArrayList<RDFNode> quids, boolean isWrite) {
		if(isWrite) {
			log.warning("Write software" + quids);
		} else {
			log.warning("Read software" + quids);
		}

	}

	public static RelatedQIDS findFollowingAndFollowedQIDS(String qID){
		return WikiRead.findFollowingAndFollowedQIDS(qID);
	}
}
